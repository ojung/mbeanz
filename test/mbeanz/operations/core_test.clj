(ns mbeanz.operations.core-test
  (:require [clojure.test :refer :all]
            [mbeanz.operations.core :refer :all])
  (:import [java.lang.IllegalArgumentException]))

(deftest test-list-operations
  (testing "list single mbean"
    (is (= (list-operations "java.lang:type=Memory")
           [{:bean "java.lang:type=Memory" :operation :gc}])))
  (testing "list multiple mbeans"
    (is (= (list-operations "java.lang:type=MemoryPool,name=*")
           [{:bean "java.lang:type=MemoryPool,name=Code Cache" :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=Compressed Class Space"
             :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=Metaspace" :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=PS Eden Space" :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=PS Old Gen" :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=PS Survivor Space"
             :operation :resetPeakUsage}]))))

(deftest test-describe
  (testing "mbean operation without arguments"
    (is (= (describe "java.lang:type=Memory" :gc)
           [{:name "gc", :description "gc", :signature []}])))
  (testing "mbean operation with parameters"
    (is (= (describe "java.lang:type=Threading" :dumpAllThreads)
           [{:name "dumpAllThreads",
             :description "dumpAllThreads",
             :signature [{:description "p0", :name "p0", :type "boolean"}
                         {:description "p1", :name "p1", :type "boolean"}]}]))))

(deftest test-invoke
  (testing "mbean with arguments"
    (is (not (invoke "java.util.logging:type=Logging"
                     :getLoggerLevel
                     ["java.lang.String" "someInexistentLogger"]))))
  (testing "overloaded mbean with arguments"
    (is (invoke "java.lang:type=Threading" :getThreadUserTime ["long" "1"])))
  (testing "mbean without arguments"
    (is (not (invoke "java.lang:type=Memory" :gc)))))
