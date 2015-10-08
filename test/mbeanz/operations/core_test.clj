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

(deftest type-casting
  (testing "int"
    (is (= (cast-type ["int" "1"]) (int 1))))
  (testing "Integer"
    (is (= (cast-type ["java.lang.Integer" "1"]) (Integer. 1))))
  (testing "long"
    (is (= (cast-type ["long" "1"]) 1)))
  (testing "Long"
    (is (= (cast-type ["java.lang.Long" "1"]) 1)))
  (testing "boolean"
    (is (= (cast-type ["boolean" "true"]) true)))
  (testing "Boolean"
    (is (= (cast-type ["java.lang.Boolean" "true"]) true)))
  (testing "double"
    (is (= (cast-type ["double" "1"]) (double 1))))
  (testing "Double"
    (is (= (cast-type ["java.lang.Double" "1"]) (double 1))))
  (testing "float"
    (is (= (cast-type ["float" "1"]) (float 1))))
  (testing "Float"
    (is (= (cast-type ["java.lang.Float" "1"]) (float 1))))
  (testing "string"
    (is (= (cast-type ["java.lang.String" "hello world"]) "hello world")))
  (testing "number format exception"
    (is (thrown? NumberFormatException (cast-type ["int" "asd"]))))
  (testing "unsupported type"
    (is (thrown-with-msg? IllegalArgumentException
                          #"Unsupported argument type"
                          (cast-type ["short" "asd"])))))
