(ns mbeanz.core
  (:gen-class)
  (:import (java.lang System)
           (javax.management ObjectName
                             MBeanOperationInfo)
           (javax.management.remote JMXServiceURL
                                    JMXConnectorFactory)))

(def jmx-url (JMXServiceURL. "service:jmx:rmi:///jndi/rmi://localhost:61342/jmxrmi"))

(def object-name-pattern (ObjectName. "*:*"))

(defn -main
  [& args]
  (println "Hello, World!"))

(defn get-connection []
  (.getMBeanServerConnection (JMXConnectorFactory/connect jmx-url)))

(defn list-mbeans []
  (let [pairs (->> (.queryNames (get-connection) object-name-pattern nil)
                   (map #(->> (.getMBeanInfo connection %)
                              (.getOperations)
                              (map .getName)
                              (map (partial str (.getCanoncialName %) " "))))
                   (flatten))]
    (doseq [line pairs] (println line))
    (System/exit 0)))

