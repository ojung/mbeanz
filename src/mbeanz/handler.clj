(ns mbeanz.handler
  (:gen-class)
  (:require [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer :all]
            [mbeanz.core :refer :all]
            [clojure.java.jmx :as jmx]
            [clojure.edn :as edn])
  (:use [org.httpkit.server :only [run-server]]
        [clj-stacktrace.core :only [parse-exception]]
        [clojure.java.io :only [writer]])
  (:import java.lang.management.ManagementFactory))

(defonce server (atom nil))

(defonce config (atom nil))

(defn- get-error-response [exception]
  (let [{:keys [message class]} (parse-exception exception)]
    {:error {:class (str class) :message message}}))

(defn get-connection-map [config-name]
  (if-let [{:keys [jmx-remote-host jmx-remote-port]} (config-name @config)]
    {:host jmx-remote-host :port jmx-remote-port}
    (throw (RuntimeException. (str "no config entry for " config-name)))))

(defn get-object-pattern [config-name]
  (:object-pattern (config-name @config)))

(defn- handle-describe [config-name operation]
  (fn [request]
    (jmx/with-connection (get-connection-map config-name)
      (let [mbean (get-in request [:params :bean])
            op (keyword operation)]
        (doall (describe mbean op))))))

(defn- try-invoke [mbean operation args types]
  (if (string? args)
    {:result (invoke mbean (keyword operation) (list types args))}
    {:result (apply invoke mbean (keyword operation) (map list types args))}))

(defn- handle-invoke [config-name operation]
  (fn [request]
    (jmx/with-connection (get-connection-map config-name)
      (let [mbean (get-in request [:params :bean])
            args (get-in request [:params :args])
            types (get-in request [:params :types])]
        {:body (try-invoke mbean operation args types)}))))

(defn- handle-list-beans [config-name]
  (fn [request]
    (jmx/with-connection (get-connection-map config-name)
      (doall (list-beans (get-object-pattern config-name))))))

(defroutes app-routes
  (GET "/:config/list" [config] (handle-list-beans (keyword config)))
  (GET "/:config/describe/:operation"
       [config operation]
       (handle-describe (keyword config) operation))
  (GET "/:config/invoke/:operation" [config operation] (handle-invoke (keyword config) operation))
  (route/not-found "Not Found"))

(defn wrap-exception-handling [next-handler]
  (fn [request]
    (try
      (next-handler request)
      (catch Exception exception {:body (get-error-response exception)}))))

(def app
  (-> app-routes
      (wrap-reload)
      (wrap-exception-handling)
      (wrap-json-response)
      (wrap-defaults api-defaults)))

(defn -main [& args]
  (when-let [config-path (first args)]
    (reset! config (merge @config (edn/read-string (slurp config-path)))))
  (reset! server (run-server app {:port 0}))
  (with-open [my-writer (writer "/var/tmp/mbeanz.port")]
    (.write my-writer (str (:local-port (meta @server))))))
