(ns helodali.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [helodali.core :as core]
            [helodali.misc :as misc]
            [cljs-time.format :refer [parse unparse formatters]]))

(deftest time-conversion
  (testing "From epoch to datetime"
    (is (= "Oct 22 2019" (unparse (formatters :date) (misc/epoch-to-datetime 1571737996000))))))
