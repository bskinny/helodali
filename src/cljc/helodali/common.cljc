(ns helodali.common
  (:require #?(:cljs [cljs.reader :as cljsr])
            #?(:clj  [clj-uuid :as uuid]
               :cljs [cljs-uuid-utils.core :as uuid])
            #?(:clj  [clj-time.core :refer [now days ago date-time]]
               :cljs [cljs-time.core :refer [now days ago date-time]])
            #?(:clj  [clj-time.format :refer [parse unparse formatters]]
               :cljs [cljs-time.format :refer [parse unparse formatters]])))

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

(defn fix-date
  "Called with a map or a vector of maps, such as an artwork's :purchases, converts a date valued kw to/from string
   The type argument is a keyword of either :parse or :unparse."
  [type kw v-or-m]
  (let [parse-unparse (if (= type :parse)
                        parse
                        unparse)
        fix-date-key-val #(if (get % kw)
                            (assoc % kw (parse-unparse (formatters :date) (get % kw)))
                            %)]
    (if (map? v-or-m)
      (fix-date-key-val v-or-m)
      (apply vector (map fix-date-key-val v-or-m)))))

(defn keywordize-vals
  "Given map and set of keys, convert the values associated with keys to keyword.
   E.g. {:role \"person\"} => {:role :person}"
  [m ks]
  (let [ks (set ks)]
    (into {} (map (fn [[k, v]] (if (contains? ks k) [k, (keyword v)] [k, v])) m))))

(defn parse-date
  "Called with timestamp and converted to clj(s) object"
  [format-kw ts]
  (if (not-empty ts)
    (parse (formatters format-kw) ts)
    nil))

(defn unparse-date
  "Called with clj-time or cljs-time object, convert to string"
  [d]
  (unparse (formatters :date) d))

(defn unparse-datetime
  "Called with clj-time or cljs-time object, convert to string"
  [d]
  (unparse (formatters :date-time) d))
