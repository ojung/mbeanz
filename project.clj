(defproject mbeanz "0.1.0-SNAPSHOT"
  :description "helper to find and use mbeans"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.jmx "0.3.1"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [compojure "1.3.4"]
                 [ring.middleware.logger "0.5.0"]
                 [http-kit "2.0.0"]
                 [ring/ring-mock "0.3.0"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-stacktrace "0.2.8"]
                 [environ "1.0.1"]]
  :plugins [[lein-ring "0.8.13"]
            [jonase/eastwood "0.2.1"]
            [lein-kibit "0.1.2"]
            [lein-cloverage "1.0.6"]
            [lein-environ "1.0.1"]]
  :ring {:handler mbeanz.handler/app}
  :main mbeanz.handler
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
