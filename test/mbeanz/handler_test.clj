(ns mbeanz.handler-test
  (:require [clojure.test :refer :all]
            [mbeanz.handler :refer :all]
            [mbeanz.config :refer [config get-connection-map]]
            [ring.mock.request :as mock]
            [clojure.edn :as edn]
            [clojure.data.json :as json]))

(def jmx-port 7777)

(use-fixtures :each (fn [do-tests]
                      (reset! config {:default {:object-pattern "java.lang:*"
                                                :jmx-remote-host "localhost"
                                                :jmx-remote-port jmx-port}})
                      (do-tests)))

(defn- request [url params cb]
  (let [mock-request (mock/query-string (mock/request :get url) params)
        response (app mock-request)]
    (cb response)))

(deftest config-test
  (testing "get-connection-map"
    (is (= (get-connection-map :default) {:host "localhost" :port jmx-port}))))

(deftest list-route
  (testing "list route"
    (request "/default/list" {}
             #(is (= (json/read-str (:body %))
                     (edn/read-string (slurp "test/mbeanz/fixtures/list-route.edn")))))))

(deftest describe-route
  (testing "operation with single signature"
    (request "/default/describe/gc" {"bean" "java.lang:type=Memory"}
             #(is (= (json/read-str (:body %))
                     [{"name" "gc", "description" "gc", "signature" []}]))))

  (testing "operation with multiple signatures"
    (request "/default/describe/getThreadCpuTime" {"bean" "java.lang:type=Threading"}
             #(is (= (json/read-str (:body %))
                     [{"name" "getThreadCpuTime"
                       "description" "getThreadCpuTime"
                       "signature" [{"description" "p0", "name" "p0", "type" "[J"}]}
                      {"name" "getThreadCpuTime"
                       "description" "getThreadCpuTime"
                       "signature" [{"description" "p0", "name" "p0", "type" "long"}]}]))))

  (testing "inexistent operation"
    ;TODO: Make api return error (404):
    (request "/default/describe/inexistent" {"bean" "java.lang:type=Threading"}
             #(is (= (json/read-str (:body %)) [])))))

(deftest invoke-route
  (testing "operation without arguments"
    (request "/default/invoke/gc" {"bean" "java.lang:type=Memory"}
             #(is (= (json/read-str (:body %)) {"result" nil}))))

  (testing "operation with arguments invoked with wrong signature"
    (request "/default/invoke/getThreadInfo" {"bean" "java.lang:type=Threading"}
             #(is (= (json/read-str (:body %))
                     {"error" {"class" "class javax.management.ReflectionException"
                               "message" "Operation getThreadInfo exists but not with this signature: ()"}}))))

  (testing "operation with single argument invoked with correct signature"
    ;TODO: Setup mock mbeans
    (request "/default/invoke/getThreadCpuTime"
             {"bean" "java.lang:type=Threading", "args" "99999999999", "types" "long"}
             #(is (= (json/read-str (:body %)) {"result" -1})))))

(deftest multiple-configs
  (testing "config for key not found"
    (request "/noooonexistant/list"
             {}
             #(is (= (json/read-str (:body %))
                     {"error" {"class" "class java.lang.RuntimeException"
                               "message" "no config entry for :noooonexistant"}}))))
  (testing "different output for different configs"
    (reset! config {:lang {:object-pattern "java.lang:type=Threading"
                           :jmx-remote-host "localhost"
                           :jmx-remote-port jmx-port}
                    :logging {:object-pattern "java.util.logging:*"
                              :jmx-remote-host "localhost"
                              :jmx-remote-port jmx-port}})
    (request "/lang/list" {}
             #(is (= (json/read-str (:body %))
                     (edn/read-string (slurp "test/mbeanz/fixtures/multiple-configs.edn")))))
    (request "/logging/list" {}
             #(is (= (json/read-str (:body %))
                     [{"bean" "java.util.logging:type=Logging", "operation" "getLoggerLevel"}
                      {"bean" "java.util.logging:type=Logging", "operation" "getParentLoggerName"}
                      {"bean" "java.util.logging:type=Logging", "operation" "setLoggerLevel"}
                      {"bean" "java.util.logging:type=Logging", "attribute" "LoggerNames"}
                      {"bean" "java.util.logging:type=Logging", "attribute" "ObjectName"}])))))

(deftest read-attribute-route
  (testing "read attribute"
    (request "/default/read/Verbose" {"bean" "java.lang:type=Memory"}
             #(is (= (json/read-str (:body %)) {"result" false}))))
  (testing "read attribute failure (inexistent attribute)"
    (request "/default/read/inexistent" {"bean" "java.lang:type=ClassLoading"}
             #(is (= (json/read-str (:body %))
                     {"error" {"class" "class javax.management.AttributeNotFoundException"
                               "message" "No such attribute: inexistent"}})))))

(deftest write-attribute-route
  (testing "set the value of an attribute"
    (request "/default/write/Verbose" {"bean" "java.lang:type=ClassLoading"
                                       "value" "true"
                                       "type" "boolean"}
             #(is (= (json/read-str (:body %)) {"result" nil})))
    (request "/default/read/Verbose" {"bean" "java.lang:type=ClassLoading"}
             #(is (= (json/read-str (:body %)) {"result" true}))))
  (testing "fail setting value because of incompatible type"
    (request "/default/write/Verbose" {"bean" "java.lang:type=ClassLoading"
                                       "value" "true"
                                       "type" "java.lang.String"}
             #(is (= (json/read-str (:body %))
                     {"error" {"class" "class javax.management.InvalidAttributeValueException"
                               "message" "Invalid value for attribute Verbose: true"}})))))
