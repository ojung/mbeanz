(ns mbeanz.handler
  (:gen-class)
  (:require [mbeanz.config :refer [get-connection-map get-object-pattern]]
            [mbeanz.operations.handler :refer :all]
            [mbeanz.operations.core :refer [list-operations]]
            [mbeanz.attributes.core :refer [list-attributes]]
            [mbeanz.attributes.handler :refer :all]
            [mbeanz.config :refer [config]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer :all]
            [clojure.edn :as edn]
            [clojure.java.jmx :as jmx])
  (:use [org.httpkit.server :only [run-server]]
        [clj-stacktrace.core :only [parse-exception]]
        [clojure.java.io :only [writer]]))

(defonce server (atom nil))

(defn handle-list [config-name]
  (fn [request]
    (jmx/with-connection (get-connection-map config-name)
      (let [pattern (get-object-pattern config-name)]
        (concat (doall (list-operations pattern))
                (doall (list-attributes pattern)))))))

(defroutes app-routes
  (GET "/:config/list" [config] (handle-list (keyword config)))
  (GET "/:config/describe/:operation"
       [config operation]
       (handle-describe (keyword config) operation))
  (GET "/:config/invoke/:operation" [config operation] (handle-invoke (keyword config) operation))
  (GET "/:config/read/:attribute" [config attribute] (handle-read (keyword config) attribute))
  (GET "/:config/write/:attribute" [config attribute] (handle-write (keyword config) attribute))
  (route/not-found "Not Found"))

(defn- get-error-response [exception]
  (let [{:keys [message class]} (parse-exception exception)]
    {:error {:class (str class) :message message}}))

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
