(ns software-alchemist.main-test
  (:require [clojure.test :as t :refer [deftest is testing]]))

(deftest test-okay
  (testing "Context of the test assertions"
    (is (= #{} {}) "set should not equal map")))
