(ns mbeanz.handler
  (:gen-class)
  (:require [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer :all]
            [mbeanz.operations.handler :refer :all]
            [mbeanz.config :refer [config]]
            [clojure.edn :as edn])
  (:use [org.httpkit.server :only [run-server]]
        [clj-stacktrace.core :only [parse-exception]]
        [clojure.java.io :only [writer]]))

(defonce server (atom nil))

(defroutes app-routes
  (GET "/:config/list" [config] (handle-list-beans (keyword config)))
  (GET "/:config/describe/:operation"
       [config operation]
       (handle-describe (keyword config) operation))
  (GET "/:config/invoke/:operation" [config operation] (handle-invoke (keyword config) operation))
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
