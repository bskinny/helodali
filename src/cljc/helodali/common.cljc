(ns helodali.common
  (:require [clojure.pprint :refer [pprint]]
            #?(:cljs [cljs.reader :as cljsr])
            #?(:clj  [clj-uuid :as uuid]
               :cljs [cljs-uuid-utils.core :as uuid])))

(defn coerce-int
  "Given map and set of keys, coerce the values associated with keys to int
   E.g. {:year 2014N} => {:year 2014}"
  [m ks]
  (let [ks (set ks)]
    (into {} (map (fn [[k, v]] (if (contains? ks k) [k, (int v)] [k, v])) m))))

(defn coerce-decimal
  "Given map and set of keys, coerce the values associated with keys to decimal
   E.g. {:price 100.99N} => {:price \"100.99\"}"
  [m ks]
  (let [ks (set ks)
        convert-to-decimal #?(:clj bigdec
                              :cljs cljsr/read-string)]
    (into {} (map (fn [[k, v]] (if (contains? ks k) [k, (convert-to-decimal v)] [k, v])) m))))

(defn coerce-decimal-string
  "Given map and set of keys, coerce the values associated with keys to decimal
   E.g. {:price 100.99N} => {:price \"100.99\"}"
  [m ks]
  (let [ks (set ks)
        convert-to-decimal #?(:clj bigdec
                              :cljs cljsr/read-string)]
    (into {} (map (fn [[k, v]] (if (contains? ks k) [k, (str (convert-to-decimal v))] [k, v])) m))))

(defn empty-string-to-nil
  [v]
  (if (and (string? v) (= "" v))
    nil
    v))

(defn keywordize-vals
  "Given map and set of keys, convert the values associated with keys to keyword.
   E.g. {:role \"person\"} => {:role :person}"
  [m ks]
  (let [ks (set ks)]
    (into {} (map (fn [[k, v]] (if (contains? ks k) [k, (keyword v)] [k, v])) m))))

(defn log
  [message data]
  (prn (str message ":"))
  (pprint data))
