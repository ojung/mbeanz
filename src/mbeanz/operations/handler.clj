(ns mbeanz.operations.handler
  (:gen-class)
  (:require [mbeanz.operations.core :refer :all]
            [mbeanz.config :refer [get-connection-map get-object-pattern]]
            [clojure.java.jmx :as jmx]))

(defn handle-describe [config-name operation]
  (fn [request]
    (jmx/with-connection (get-connection-map config-name)
      (let [mbean (get-in request [:params :bean])
            op (keyword operation)]
        (doall (describe mbean op))))))

(defn handle-invoke [config-name operation]
  (fn [request]
    (jmx/with-connection (get-connection-map config-name)
      (let [mbean (get-in request [:params :bean])
            args (get-in request [:params :args])
            types (get-in request [:params :types])]
        {:body {:result (if (string? args)
                          (invoke mbean (keyword operation) (list types args))
                          (apply invoke mbean (keyword operation) (map list types args)))}}))))
