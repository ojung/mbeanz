(defproject mbeanz "0.1.0-SNAPSHOT"
  :description "helper to find and use mbeans"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :main ^:skip-aot mbeanz.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
