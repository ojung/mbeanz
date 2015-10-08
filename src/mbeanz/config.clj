(ns mbeanz.config
  (:gen-class))

(defonce config (atom nil))

(defn get-object-pattern [config-name]
  (:object-pattern (config-name @config)))

(defn get-connection-map [config-name]
  (if-let [{:keys [jmx-remote-host jmx-remote-port]} (config-name @config)]
    {:host jmx-remote-host :port jmx-remote-port}
    (throw (RuntimeException. (str "no config entry for " config-name)))))
