(ns mbeanz.attributes.core
  (:gen-class)
  (:require [mbeanz.common :refer [cast-type]]
            [clojure.java.jmx :as jmx]))

(defn get-identifiers [[bean-name bean-attrs]]
  (->> bean-attrs
       (sort)
       (map (partial hash-map :bean (str bean-name) :attribute))))

(defn list-attributes [object-name-pattern]
  (->> (jmx/mbean-names object-name-pattern)
       (sort)
       (mapcat (comp get-identifiers
                     #(list % (jmx/attribute-names %))))))

(defn read-attribute [bean-name attribute]
  (jmx/read bean-name attribute))

(defn write-attribute [bean-name attribute value type-name]
  (let [typed-value (cast-type [type-name value])]
    (jmx/write! bean-name attribute typed-value)))
