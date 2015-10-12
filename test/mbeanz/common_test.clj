(ns mbeanz.common-test
  (:require [clojure.test :refer :all]
            [mbeanz.common :refer :all]))

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
