(ns mbeanz.handler
  (:require [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [compojure.core :refer :all]
            [mbeanz.core :refer :all]
            [mbeanz.common :refer :all]))

(defn- identifier-string [identifiers]
  (map #(str (:bean %) " " (stringify (:operation %))) identifiers))

(defn- with-bean-and-operation [operation function]
  (fn [request]
    (let [mbean (get-in request [:params :bean])]
      (function mbean (keyword operation)))))

(defn- handle-invoke [operation]
  (fn [request]
    (let [mbean (get-in request [:params :bean])]
      (if-let [args (get-in request [:params :args])]
        (apply invoke mbean (keyword operation) args)
        (invoke mbean (keyword operation))))))

(defroutes app-routes
  (GET "/list" [] (identifier-string (list-beans "java.lang:*")))
  (GET "/describe/:operation" [operation] (with-bean-and-operation operation describe))
  (GET "/parameters/:operation" [operation] (with-bean-and-operation operation get-params))
  (GET "/invoke/:operation" [operation] {:body (handle-invoke operation)})
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-with-logger)
      (wrap-json-response)
      (wrap-defaults api-defaults)))
