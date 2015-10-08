(ns mbeanz.operations.core
  (:gen-class)
  (:require [clojure.java.jmx :as jmx]
            [clojure.core.match :refer [match]])
  (:import [java.lang.IllegalArgumentException]))

(defn get-identifiers [[bean-name bean-ops]]
  (->> bean-ops
       (sort)
       (map (partial hash-map :bean (str bean-name) :operation))))

(defn list-operations [object-name-pattern]
  (->> (jmx/mbean-names object-name-pattern)
       (sort)
       (mapcat (comp get-identifiers
                     #(list % (map first (partition-by identity (jmx/operation-names %))))))))

(defn- get-operation-info [bean-name operation]
  (filter #(= (-> % .getName keyword) operation) (jmx/operations bean-name)))

(defn cast-type [[type-name arg]]
  (match [type-name]
         ["int"] (int (Integer/parseInt arg))
         ["java.lang.Integer"] (Integer/parseInt arg)
         ["long"] (long (Long/parseLong arg))
         ["java.lang.Long"] (Long/parseLong arg)
         ["boolean"] (boolean (Boolean/parseBoolean arg))
         ["java.lang.Boolean"] (Boolean/parseBoolean arg)
         ["double"] (double (Double/parseDouble arg))
         ["java.lang.Double"] (Double/parseDouble arg)
         ["float"] (float (Float/parseFloat arg))
         ["java.lang.Float"] (Float/parseFloat arg)
         ["java.lang.String"] arg
         :else (throw (IllegalArgumentException. (str "Unsupported argument type " type-name)))))

(defn invoke [bean-name operation & typed-args]
  (if (seq? typed-args)
    (let [types (map first typed-args)
          values (map cast-type typed-args)]
      (apply jmx/invoke-signature bean-name operation types values))
    (jmx/invoke bean-name operation)))

(defn- get-signature-descriptions [operation]
  {:name (.getName operation)
   :description (.getDescription operation)
   :signature (map #(hash-map :name (.getName %)
                              :description (.getDescription %)
                              :type (.getType %))
                   (.getSignature operation))})

(defn describe [bean-name operation]
  (map get-signature-descriptions (get-operation-info bean-name operation)))
