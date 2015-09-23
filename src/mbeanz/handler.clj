(ns mbeanz.handler
  (:gen-class)
  (:require [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer :all]
            [mbeanz.core :refer :all]
            [mbeanz.common :refer :all]
            [clojure.java.jmx :as jmx]
            [clojure.edn :as edn])
  (:use [org.httpkit.server :only [run-server]]
        [clj-stacktrace.core :only [parse-exception]]
        [clojure.java.io :only [writer]])
  (:import java.lang.management.ManagementFactory))

(defonce server (atom nil))

(defonce object-pattern (atom "java.lang:*"))

(defonce jmx-remote-host (atom "localhost"))

(defonce jmx-remote-port (atom 1080))

(defn- identifier-string [identifiers]
  (map #(str (:bean %) " " (stringify (:operation %))) identifiers))

(defn- handle-describe [operation]
  (fn [request]
    (jmx/with-connection {:host @jmx-remote-host :port @jmx-remote-port}
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
    (jmx/with-connection {:host @jmx-remote-host :port @jmx-remote-port}
      (let [mbean (get-in request [:params :bean])
            args (get-in request [:params :args])
            types (get-in request [:params :types])]
        {:body (try-invoke mbean operation args types)}))))

(defn- handle-list-beans []
  (fn [request]
    (jmx/with-connection {:host @jmx-remote-host :port @jmx-remote-port}
      (identifier-string (doall (list-beans @object-pattern))))))

(defroutes app-routes
  (GET "/list" [] (handle-list-beans))
  (GET "/describe/:operation" [operation] (handle-describe operation))
  (GET "/invoke/:operation" [operation] (handle-invoke operation))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-json-response)
      (wrap-defaults api-defaults)))

(defn- reset-if-set [config key atom]
  (when-let [value (key config)]
    (reset! atom value)))

(defn -main [& args]
  (when-let [config-file-path (first args)]
    (let [config (edn/read-string (slurp config-file-path))]
      (reset-if-set config :object-pattern object-pattern)
      (reset-if-set config :jmx-remote-host jmx-remote-host)
      (reset-if-set config :jmx-remote-port jmx-remote-port)))
  (reset! server (run-server app {:port 0}))
  (with-open [my-writer (writer "/var/tmp/mbeanz.port")]
    (.write my-writer (str (:local-port (meta @server))))))
