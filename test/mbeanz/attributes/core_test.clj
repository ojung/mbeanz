(ns mbeanz.attributes.core-test
  (:require [clojure.test :refer :all]
            [mbeanz.attributes.core :refer :all]))

(deftest test-list-attributes
  (testing "list attributes of a single mbean"
    (is (= (list-attributes "java.lang:type=Memory")
           [{:bean "java.lang:type=Memory", :attribute :HeapMemoryUsage}
            {:bean "java.lang:type=Memory", :attribute :NonHeapMemoryUsage}
            {:bean "java.lang:type=Memory", :attribute :ObjectName}
            {:bean "java.lang:type=Memory", :attribute :ObjectPendingFinalizationCount}
            {:bean "java.lang:type=Memory", :attribute :Verbose}])))
  (testing "list attributes of multiple mbeans"
    (is (= (list-attributes "java.lang:type=GarbageCollector,name=*")
           [{:bean "java.lang:type=GarbageCollector,name=PS MarkSweep"
             :attribute :CollectionCount}
            {:bean "java.lang:type=GarbageCollector,name=PS MarkSweep", :attribute :CollectionTime}
            {:bean "java.lang:type=GarbageCollector,name=PS MarkSweep", :attribute :LastGcInfo}
            {:bean "java.lang:type=GarbageCollector,name=PS MarkSweep"
             :attribute :MemoryPoolNames}
            {:bean "java.lang:type=GarbageCollector,name=PS MarkSweep", :attribute :Name}
            {:bean "java.lang:type=GarbageCollector,name=PS MarkSweep", :attribute :ObjectName}
            {:bean "java.lang:type=GarbageCollector,name=PS MarkSweep", :attribute :Valid}
            {:bean "java.lang:type=GarbageCollector,name=PS Scavenge", :attribute :CollectionCount}
            {:bean "java.lang:type=GarbageCollector,name=PS Scavenge", :attribute :CollectionTime}
            {:bean "java.lang:type=GarbageCollector,name=PS Scavenge", :attribute :LastGcInfo}
            {:bean "java.lang:type=GarbageCollector,name=PS Scavenge", :attribute :MemoryPoolNames}
            {:bean "java.lang:type=GarbageCollector,name=PS Scavenge", :attribute :Name}
            {:bean "java.lang:type=GarbageCollector,name=PS Scavenge", :attribute :ObjectName}
            {:bean "java.lang:type=GarbageCollector,name=PS Scavenge", :attribute :Valid}]))))

(deftest test-read-attribute
  (testing "read value of an attribute"
    (is (= (.getCanonicalName (read-attribute "java.lang:type=Memory" :ObjectName))
           "java.lang:type=Memory"))))

(deftest test-write-attribute
  (testing "write value of an attribute"
    (is (= (write-attribute "java.lang:type=ClassLoading" :Verbose "true" "boolean") nil))
    (is (= (write-attribute "java.lang:type=ClassLoading" :Verbose "false" "boolean") nil))
    (is (thrown? javax.management.AttributeNotFoundException
                 (write-attribute "java.lang:type=ClassLoading" :inexistent "false" "boolean")))))
