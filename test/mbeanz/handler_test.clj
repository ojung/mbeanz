(ns mbeanz.handler_test
  (:require [clojure.test :refer :all]
            [mbeanz.handler :refer :all]
            [ring.mock.request :as mock]
            [clojure.data.json :as json]))

(deftest handler
  (testing "list route"
    (let [response (app (mock/request :get "/list"))]
      (is (= (:status response) 200))
      (is (= (json/read-str (:body response))
             ["java.lang:type=Memory gc",
              "java.lang:type=MemoryPool,name=Code Cache resetPeakUsage",
              "java.lang:type=MemoryPool,name=Compressed Class Space resetPeakUsage",
              "java.lang:type=MemoryPool,name=Metaspace resetPeakUsage",
              "java.lang:type=MemoryPool,name=PS Eden Space resetPeakUsage",
              "java.lang:type=MemoryPool,name=PS Old Gen resetPeakUsage",
              "java.lang:type=MemoryPool,name=PS Survivor Space resetPeakUsage",
              "java.lang:type=Threading getThreadCpuTime",
              "java.lang:type=Threading getThreadCpuTime",
              "java.lang:type=Threading getThreadUserTime",
              "java.lang:type=Threading getThreadUserTime",
              "java.lang:type=Threading getThreadAllocatedBytes",
              "java.lang:type=Threading getThreadAllocatedBytes",
              "java.lang:type=Threading getThreadInfo",
              "java.lang:type=Threading getThreadInfo",
              "java.lang:type=Threading getThreadInfo",
              "java.lang:type=Threading getThreadInfo",
              "java.lang:type=Threading getThreadInfo",
              "java.lang:type=Threading findMonitorDeadlockedThreads",
              "java.lang:type=Threading resetPeakThreadCount",
              "java.lang:type=Threading findDeadlockedThreads",
              "java.lang:type=Threading dumpAllThreads"]))))
  (testing "describe route"
    (let [response (app (mock/request :get "/describe/gc?bean=java.lang:type=Memory"))]
      (is (= (:status response) 200)))))
