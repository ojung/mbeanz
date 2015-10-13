(ns mbeanz.common
  (:gen-class)
  (:require [clojure.core.match :refer [match]]))

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
