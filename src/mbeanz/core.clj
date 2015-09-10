(ns mbeanz.core
  (:gen-class)
  (:require [clojure.java.jmx :as jmx]
            [clojure.tools.cli :as cli]
            [clojure.core.match :refer [match]]
            [clojure.string :as string])
  (:import (java.lang System)))

(def object-name-pattern "Adscale:*")

(def cli-options [["-m" "--mode list" "mode"
                   :id :mode
                   :parse-fn #(keyword %)]
                  ["-b" "--bean some-mbean" "mbean name"
                   :id :bean
                   :parse-fn #(str %)]
                  ["-o" "--operation setMaintenance" "operation name"
                   :id :operation
                   :parse-fn #(keyword %)]])

(defn get-strings [[bean-name & bean-ops]]
  (->> bean-ops
       (flatten)
       (map (partial str (.toString bean-name) " "))))

(defn list-beans []
  (let [bean-strings (->> (jmx/mbean-names object-name-pattern)
                          (sort)
                          (map #(list % (jmx/operation-names %)))
                          (map get-strings)
                          (flatten))]
    (doseq [mbean (flatten bean-strings)] (println mbean))
    (System/exit 0)))

(defn- get-operation-info [bean-name operation]
  (first (filter #(= (-> % .getName keyword) operation) (jmx/operations bean-name))))

(defn get-params [bean-name operation]
  (let [parameter-maps (->> (get-operation-info bean-name operation)
                            (.getSignature)
                            (map #(hash-map :name (.getName %)
                                            :descr (.getDescription %)
                                            :type (.getType %))))]
    (doseq [parameter parameter-maps] (println parameter)))
  (System/exit 0))

(defn- cast-type [[arg type-name]]
  (match [type-name]
         [:int] (Integer. arg)
         [:long] (Long. arg)
         [:boolean] (Boolean. arg)
         [:java.lang.String] arg))

(defn invoke [bean-name operation]
  (let [signature (.getSignature (get-operation-info bean-name operation))]
    (if (> (count signature) 0)
      (let [args (string/split (read-line) #" ")
            typed-args (->> signature
                            (map #(keyword (.getType %)))
                            (map list args)
                            (map cast-type))]
        (println (apply jmx/invoke bean-name operation typed-args)))
      (println (jmx/invoke bean-name operation)))
    (System/exit 0)))

(defn describe [bean-name operation]
  (println (.getDescription (get-operation-info bean-name operation))))

(defn -main [& args]
  (jmx/with-connection {:host "localhost" :port 11080}
    (let [{:keys (options summary)} (cli/parse-opts args cli-options)]
      (match [(:mode options)]
             [:list] (list-beans)
             [:params] (get-params (:bean options) (:operation options))
             [:invoke] (invoke (:bean options) (:operation options))
             [:describe] (describe (:bean options) (:operation options))
             [:else] (print summary)))))
