(ns helodali.db_test
  (:require [clojure.test :refer :all]
            [taoensso.faraday :as far]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :refer [pprint]]
            [helodali.common :refer [log]]
            [helodali.db :as db]
            [clojure.data :refer [diff]]))

;; We expect a local dynamodb installation
(def co
  {:access-key (or (System/getenv "AWS_ACCESS_KEY")
                   (System/getProperty "AWS_ACCESS_KEY"))
   :secret-key (or (System/getenv "AWS_SECRET_KEY")
                   (System/getProperty "AWS_SECRET_KEY"))
   :endpoint   (or (System/getenv "AWS_DYNAMODB_ENDPOINT")
                   (System/getProperty "AWS_DYNAMODB_ENDPOINT"))})

;; The uuid of a test user
(def uref "48844840-5d26-11e9-9d54-5a9ae62eab53")

;; An artwork item defined for above user
(def artwork-1 (edn/read-string (slurp (io/resource "artwork-1.edn"))))

(defn create-table
  "Create an 'item' table, such as :artwork or :press."
  [name]
  (far/create-table co name
                    [:uref :s]  ; Hash key of uuid-valued user reference, (:s => string type)
                    {:range-keydef [:uuid :s]
                     :throughput {:read 2 :write 2} ; Read & write capacity doesn't factor when running dynamodb-local
                     :block? true})) ; Block thread during table creation

(defn prep-db
  [f]
  (create-table :artwork)
  (f)
  (far/delete-table co :artwork))

(defn prep-items
  [f]
  (far/put-item co :artwork artwork-1)
  (f)
  (far/delete-item co :artwork {:uref (:uref artwork-1) :uuid (:uuid artwork-1)}))

(use-fixtures :once prep-db)
(use-fixtures :each prep-items)

;; The DynamoDB web service will create an item if updateItem is called with a key for a non-existent item.
(deftest update-nonexistent
  (let [updated (db/update-item :artwork uref "this-uuid-does-not-exist" :series false)]
    ;; We expect no :uuid (nor anything else for that matter) in the item map of the response
    (is (= {:artwork [["this-uuid-does-not-exist"
                       [nil {:style #{}, :purchases [], :images [], :instagram-media-ref nil}]]]}
           updated))))

(deftest update-artwork-1
  (let [updated (db/update-item :artwork uref (:uuid artwork-1) [:purchases 0 :price] 400)]
    ;; We expect the returned item to have the updated price.
    (is (= {:artwork [[(:uuid artwork-1)
                       [nil (assoc-in artwork-1 [:purchases 0 :price] 400)]]]}
           updated))))

(deftest coercion-artwork-1
  (let [coerced (first (db/query-by-uref :artwork uref))]
    (is (= artwork-1 coerced))))
