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

(defonce config (atom {:object-pattern "java.lang:*"
                       :jmx-remote-host "localhost"
                       :jmx-remote-port 11080}))

(defn- get-connection-map []
  {:host (:jmx-remote-host @config) :port (:jmx-remote-port @config)})

(defn- handle-describe [operation]
  (fn [request]
    (jmx/with-connection (get-connection-map)
      (let [mbean (get-in request [:params :bean])
            op (keyword operation)]
        (doall (describe mbean op))))))

(defn- try-invoke [mbean operation args types]
  (try
    (if (string? args)
      {:result (invoke mbean (keyword operation) (list types args))}
      {:result (apply invoke mbean (keyword operation) (map list types args))})
    (catch Exception exception
      (let [{:keys [message class]} (parse-exception exception)]
        {:error {:class (str class) :message message}}))))

(defn- handle-invoke [operation]
  (fn [request]
    (jmx/with-connection (get-connection-map)
      (let [mbean (get-in request [:params :bean])
            args (get-in request [:params :args])
            types (get-in request [:params :types])]
        {:body (try-invoke mbean operation args types)}))))

(defn- handle-list-beans []
  (fn [request]
    (jmx/with-connection (get-connection-map)
      (doall (list-beans (:object-pattern @config))))))

(defroutes app-routes
  (GET "/list" [] (handle-list-beans))
  (GET "/describe/:operation" [operation] (handle-describe operation))
  (GET "/invoke/:operation" [operation] (handle-invoke operation))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-reload)
      (wrap-json-response)
      (wrap-defaults api-defaults)))

(defn -main [& args]
  (when-let [config-path (first args)]
    (reset! config (edn/read-string (slurp config-path))))
  (reset! server (run-server app {:port 0}))
  (with-open [my-writer (writer "/var/tmp/mbeanz.port")]
    (.write my-writer (str (:local-port (meta @server))))))
