(ns mbeanz.handler
  (:gen-class)
  (:require [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [compojure.core :refer :all]
            [mbeanz.core :refer :all]
            [mbeanz.common :refer :all]
            [environ.core :refer [env]]
            [clojure.java.jmx :as jmx]
            [ring.adapter.jetty :refer [run-jetty]]))

(def object-pattern (env :mbeanz-object-pattern))

(def jmx-remote-host (env :mbeanz-jmx-remote-host))

(def jmx-remote-port (Integer/parseInt (env :mbeanz-jmx-remote-port)))

(defn- identifier-string [identifiers]
  (map #(str (:bean %) " " (stringify (:operation %))) identifiers))

(defn- handle-describe [operation]
  (fn [request]
    (jmx/with-connection {:host jmx-remote-host :port jmx-remote-port}
      (let [mbean (get-in request [:params :bean])
            op (keyword operation)]
        {:body (hash-map :description (doall (describe mbean op))
                         :parameters (doall (get-params mbean op)))}))))

(defn- handle-invoke [operation]
  (fn [request]
    (jmx/with-connection {:host jmx-remote-host :port jmx-remote-port}
      (let [mbean (get-in request [:params :bean])
            args (get-in request [:params :args])]
        (if (string? args)
          {:body {:result (invoke mbean (keyword operation) args)}}
          {:body {:result (apply invoke mbean (keyword operation) args)}})))))

(defn- handle-list-beans []
  (fn [request]
    (jmx/with-connection {:host jmx-remote-host :port jmx-remote-port}
      (identifier-string (doall (list-beans object-pattern))))))

(defroutes app-routes
  (GET "/list" [] (handle-list-beans))
  (GET "/describe/:operation" [operation] (handle-describe operation))
  (GET "/invoke/:operation" [operation] (handle-invoke operation))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-with-logger)
      (wrap-json-response)
      (wrap-defaults api-defaults)))

(defn -main [& args]
  (run-jetty app {:port 7999}))
