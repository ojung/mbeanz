(ns mbeanz.core
  (:gen-class)
  (:require [clojure.java.jmx :as jmx]
            [clojure.core.match :refer [match]]
            [mbeanz.common :refer :all]
            [environ.core :refer [env]])
  (:import [java.lang.IllegalArgumentException]))

(defn get-identifiers [[bean-name & bean-ops]]
  (->> bean-ops
       (flatten)
       (map (partial hash-map :bean (str bean-name) :operation))))

(defn list-beans [object-name-pattern]
  (->> (jmx/mbean-names object-name-pattern)
       (sort)
       (map (comp get-identifiers #(list % (jmx/operation-names %))))
       (flatten)))

(defn- get-operation-info [bean-name operation]
  (->> (jmx/operations bean-name)
       (filter #(= (-> % .getName keyword) operation))
       (first)))

(defn get-params [bean-name operation]
  (->> (get-operation-info bean-name operation)
       (.getSignature)
       (map #(hash-map :name (.getName %) :description (.getDescription %) :type (.getType %)))))

(defn cast-type [[type-name arg]]
  (match [type-name]
         [:int] (int (Integer/parseInt arg))
         [:long] (long (Long/parseLong arg))
         [:boolean] (boolean (Boolean/parseBoolean arg))
         [:java.lang.String] arg
         :else (throw (IllegalArgumentException. (str "Unsupported argument type " type-name)))))

(defn get-typed-args [bean-name operation & args]
  (let [types (map (comp keyword :type) (get-params bean-name operation))]
    (map list types args)))

(defn invoke [bean-name operation & args]
  (if (seq? args)
    (let [typed-args (apply get-typed-args bean-name operation args)
          types (map (comp stringify first) typed-args)
          values (map cast-type typed-args)]
      (apply jmx/invoke-signature bean-name operation types values))
    (jmx/invoke bean-name operation)))

(defn describe [bean-name operation]
  (.getDescription (get-operation-info bean-name operation)))
