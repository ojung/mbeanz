(ns mbeanz.attributes.handler
  (:gen-class)
  (:require [mbeanz.attributes.core :refer :all]
            [mbeanz.config :refer [get-connection-map]]
            [clojure.java.jmx :as jmx]))

(defn handle-read [config-name attribute]
  (fn [request]
    (jmx/with-connection (get-connection-map config-name)
      (let [mbean (get-in request [:params :bean])]
        {:body {:result (read-attribute mbean attribute)}}))))

(defn handle-write [config-name attribute]
  (fn [request]
    (jmx/with-connection (get-connection-map config-name)
      (let [mbean (get-in request [:params :bean])
            value (get-in request [:params :value])
            type-name (get-in request [:params :type])]
        {:body {:result (write-attribute mbean attribute value type-name)}}))))
