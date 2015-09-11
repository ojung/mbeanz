(ns mbeanz.common
  (:gen-class))

(defn stringify [keyword]
  (subs (str keyword) 1))
