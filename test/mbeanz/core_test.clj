(ns mbeanz.core-test
  (:require [clojure.test :refer :all]
            [mbeanz.core :refer :all]
            [mbeanz.common :refer :all])
  (:import [java.lang.IllegalArgumentException]))

(deftest index
  (testing "list single mbean"
    (is (= (list-beans "java.lang:type=Memory") [{:bean "java.lang:type=Memory" :operation :gc}])))
  (testing "list multiple mbeans"
    (is (= (list-beans "java.lang:type=MemoryPool,name=*")
           [{:bean "java.lang:type=MemoryPool,name=Code Cache" :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=Compressed Class Space"
             :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=Metaspace" :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=PS Eden Space" :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=PS Old Gen" :operation :resetPeakUsage}
            {:bean "java.lang:type=MemoryPool,name=PS Survivor Space"
             :operation :resetPeakUsage}]))))

(deftest show
  (testing "mbean description"
    (is (= (describe "java.lang:type=Memory" :gc) "gc")))
  (testing "mbean parameters"
    (is (= (get-params "java.lang:type=Threading" :dumpAllThreads)
           (list {:name "p0", :type "boolean", :description "p0"}
                 {:name "p1", :type "boolean", :description "p1"})))))

(deftest test-invoke
  (testing "type deduction for monadic operations"
    (is (= (get-typed-args "java.util.logging:type=Logging" :getLoggerLevel "asd")
           [[:java.lang.String "asd"]])))
  (testing "type deduction for variadic operations"
    (is (= (get-typed-args "java.lang:type=Threading" :dumpAllThreads "true" "true")
           [[:boolean "true"] [:boolean "true"]])))
  (testing "mbean with arguments"
    (is (not (invoke "java.util.logging:type=Logging" :getLoggerLevel "asdfg"))))
  ;TODO: handle overloaded signatures
  ;(testing "overloaded mbean with arguments"
    ;(is (invoke "java.lang:type=Threading" :getThreadUserTime "1")))
  (testing "mbean without arguments"
    (is (= (invoke "java.lang:type=Memory" :gc) nil))))

(deftest type-casting
  (testing "stringify function"
    (is (= (stringify :int) "int"))
    (is (= (stringify :Boolean) "Boolean")))
  (testing "int"
    (is (= (cast-type [:int "1"]) (int 1))))
  (testing "long"
    (is (= (cast-type [:long "1"]) (long 1))))
  (testing "boolean"
    (is (= (cast-type [:boolean "true"]) (boolean true))))
  (testing "string"
    (is (= (cast-type [:java.lang.String "hello world"]) "hello world")))
  (testing "unsupported type"
    (is (thrown-with-msg? IllegalArgumentException
                          #"Unsupported argument type"
                          (cast-type '(:asdjl "asd"))))))

