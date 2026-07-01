(ns tilemap-test
  (:require [clojure.test :refer [deftest is testing]]
            [tilemap]))
(deftest namespace-loads
  (testing "the restored CLJC namespace loads"
    (is (some? tilemap))))
