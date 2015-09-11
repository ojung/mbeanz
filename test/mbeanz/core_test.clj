(ns mbeanz.core-test
  (:require [clojure.test :refer :all]
            [mbeanz.core :refer :all])
  (:import [java.lang.IllegalArgumentException]))

(deftest index
  (testing "list single mbean"
    (is (= (list-beans "java.lang:type=Memory") ["java.lang:type=Memory" :gc])))
  (testing "list multiple mbeans"
    (is (= (list-beans "java.lang:type=MemoryPool,name=*")
           ["java.lang:type=MemoryPool,name=Code Cache" :resetPeakUsage
            "java.lang:type=MemoryPool,name=Compressed Class Space" :resetPeakUsage
            "java.lang:type=MemoryPool,name=Metaspace" :resetPeakUsage
            "java.lang:type=MemoryPool,name=PS Eden Space" :resetPeakUsage
            "java.lang:type=MemoryPool,name=PS Old Gen" :resetPeakUsage
            "java.lang:type=MemoryPool,name=PS Survivor Space" :resetPeakUsage]))))

(deftest show
  (testing "mbean description"
    (is (= (describe "java.lang:type=Memory" :gc) "gc")))
  (testing "mbean parameters"
    (is (= (get-params "java.lang:type=Threading" :dumpAllThreads)
           (list {:name "p0", :type "boolean", :description "p0"}
                 {:name "p1", :type "boolean", :description "p1"})))))

(deftest test-invoke
  (testing "mbean with arguments"
    (is (invoke "java.lang:type=Threading" :getThreadUserTime [:long "1"])))
  (testing "mbean without arguments"
    (is (= (invoke "java.lang:type=Memory" :gc) nil))))

(deftest type-casting
  (testing "stringify function"
    (is (= (stringify :int) "int"))
    (is (= (stringify :Boolean) "Boolean")))
  (testing "int"
    (is (= (cast-type [:Integer "1"]) (Integer. 1)))
    (is (= (cast-type [:int "1"]) (int 1))))
  (testing "long"
    (is (= (cast-type [:Long "1"]) (Long. 1)))
    (is (= (cast-type [:long "1"]) (long 1))))
  (testing "boolean"
    (is (= (cast-type [:Boolean "true"]) (Boolean. true)))
    (is (= (cast-type [:boolean "true"]) (boolean true))))
  (testing "string"
    (is (= (cast-type [:String "hello world"]) "hello world")))
  (testing "unsupported type"
    (is (thrown-with-msg? IllegalArgumentException
                          #"Unsupported argument type"
                          (cast-type '(:asdjl "asd"))))))

