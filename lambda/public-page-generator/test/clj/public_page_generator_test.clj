(ns public_page_generator_test
  (:require [clojure.test :refer :all]
            [public-page-generator :refer [sort-artwork]]))

;; This user uref is for the helotest account
(def uref "11c90d40-5d26-11e9-9d54-5a9ae62eab53")


(deftest test-sort-artwork
  (is (= "2015-10-02"
         (:created (last (sort-artwork uref))))))

