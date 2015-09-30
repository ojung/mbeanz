(ns mbeanz.handler-test
  (:require [clojure.test :refer :all]
            [mbeanz.handler :refer :all]
            [ring.mock.request :as mock]
            [clojure.data.json :as json]))

(use-fixtures :once (fn [do-tests] (reset! object-pattern "java.lang:*") (do-tests)))

(defn- request [url params cb]
  (let [mock-request (mock/query-string (mock/request :get url) params)
        response (app mock-request)]
    (cb response)))

(deftest list-route
  (testing "list route"
    (request "/list" {}
             #(is (= (json/read-str (:body %))
                     [{"bean" "java.lang:type=Memory", "operation" "gc"}
                      {"bean" "java.lang:type=MemoryPool,name=Code Cache", "operation" "resetPeakUsage"}
                      {"bean" "java.lang:type=MemoryPool,name=Compressed Class Space"
                       "operation" "resetPeakUsage"}
                      {"bean" "java.lang:type=MemoryPool,name=Metaspace", "operation" "resetPeakUsage"}
                      {"bean" "java.lang:type=MemoryPool,name=PS Eden Space", "operation" "resetPeakUsage"}
                      {"bean" "java.lang:type=MemoryPool,name=PS Old Gen", "operation" "resetPeakUsage"}
                      {"bean" "java.lang:type=MemoryPool,name=PS Survivor Space", "operation" "resetPeakUsage"}
                      {"bean" "java.lang:type=Threading", "operation" "dumpAllThreads"}
                      {"bean" "java.lang:type=Threading", "operation" "findDeadlockedThreads"}
                      {"bean" "java.lang:type=Threading", "operation" "findMonitorDeadlockedThreads"}
                      {"bean" "java.lang:type=Threading", "operation" "getThreadAllocatedBytes"}
                      {"bean" "java.lang:type=Threading", "operation" "getThreadCpuTime"}
                      {"bean" "java.lang:type=Threading", "operation" "getThreadInfo"}
                      {"bean" "java.lang:type=Threading", "operation" "getThreadUserTime"}
                      {"bean" "java.lang:type=Threading", "operation" "resetPeakThreadCount"}])))))

(deftest describe-route
  (testing "operation with single signature"
    (request "/describe/gc" {"bean" "java.lang:type=Memory"}
             #(is (= (json/read-str (:body %))
                     [{"name" "gc", "description" "gc", "signature" []}]))))

  (testing "operation with multiple signatures"
    (request "/describe/getThreadCpuTime" {"bean" "java.lang:type=Threading"}
             #(is (= (json/read-str (:body %))
                     [{"name" "getThreadCpuTime"
                       "description" "getThreadCpuTime"
                       "signature" [{"description" "p0", "name" "p0", "type" "[J"}]}
                      {"name" "getThreadCpuTime"
                       "description" "getThreadCpuTime"
                       "signature" [{"description" "p0", "name" "p0", "type" "long"}]}]))))

  (testing "inexistent operation"
    ;TODO: Make api return error (404):
    (request "/describe/inexistent" {"bean" "java.lang:type=Threading"}
             #(is (= (json/read-str (:body %)) [])))))

(deftest invoke-route
  (testing "operation without arguments"
    (request "/invoke/gc" {"bean" "java.lang:type=Memory"}
             #(is (= (json/read-str (:body %)) {"result" nil}))))

  (testing "operation with arguments invoked with wrong signature"
    (request "/invoke/getThreadInfo" {"bean" "java.lang:type=Threading"}
             #(is (= (json/read-str (:body %))
                     {"error" {"class" "class javax.management.ReflectionException"
                               "message" "Operation getThreadInfo exists but not with this signature: ()"}}))))

  (testing "operation with single argument invoked with correct signature"
    ;TODO: Setup mock mbeans
    (request "/invoke/getThreadCpuTime"
             ;Hoping this thread id doesn't exist
             {"bean" "java.lang:type=Threading", "args" "99999999999", "types" "long"}
             #(is (= (json/read-str (:body %)) {"result" -1})))))
