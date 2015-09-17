(ns mbeanz.handler
  (:gen-class)
  (:require [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer :all]
            [mbeanz.core :refer :all]
            [mbeanz.common :refer :all]
            [environ.core :refer [env]]
            [clojure.java.jmx :as jmx])
  (:use [org.httpkit.server :only [run-server]]
        [clj-stacktrace.core :only [parse-exception]])
  (:import java.lang.management.ManagementFactory))

(def object-pattern (delay (or (env :mbeanz-object-pattern) "*:*")))

(def jmx-remote-host (delay (or (env :mbeanz-jmx-remote-host) "localhost")))

(def jmx-remote-port (delay (or (Integer/parseInt (env :mbeanz-jmx-remote-port)) 11080)))

(defn- identifier-string [identifiers]
  (map #(str (:bean %) " " (stringify (:operation %))) identifiers))

(defn- handle-describe [operation]
  (fn [request]
    (jmx/with-connection {:host @jmx-remote-host :port @jmx-remote-port}
      (let [mbean (get-in request [:params :bean])
            op (keyword operation)]
        {:body (doall (describe mbean op))}))))

(defn- try-invoke [mbean operation args types]
  (try
    (if (string? args)
      (invoke mbean (keyword operation) (list types args))
      (apply invoke mbean (keyword operation) (map list types args)))
    (catch Exception exception
      (let [{:keys [message class]} (parse-exception exception)]
        {:error {:class (str class) :message message}}))))

(defn- handle-invoke [operation]
  (fn [request]
    (jmx/with-connection {:host @jmx-remote-host :port @jmx-remote-port}
      (let [mbean (get-in request [:params :bean])
            args (get-in request [:params :args])
            types (get-in request [:params :types])]
        {:body {:result (try-invoke mbean operation args types)}}))))

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

(defn -main [& args]
  (run-server app {:port 7999}))
