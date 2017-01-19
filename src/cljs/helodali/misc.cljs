(ns helodali.misc
   (:require [cljs-uuid-utils.core :as uuid]
             [clojure.pprint :refer [pprint]]
             [cljs-time.core :refer [before? after? now]]
             [cljs-time.format :refer [parse unparse formatters]]))

(defn compute-bg-color
  "Eech color in color-set is a rgb triple (vector) and if non-empty will be added together"
  [color-set default-color]
  (if (empty? color-set)
    default-color
    (let [c (reduce (fn [res v] (map #(mod (+ %1 %2) 255) res v)) color-set)]
      (str "rgb(" (first c) "," (second c) "," (nth c 2) ")"))))

(defn convert-map-to-options
  "Convert map of (kw string) to vector of 'options' for selection lists. E.g.
     [{:id :for-sale :label <string>}
      {:id :sold :label <string>}]"
  [m]
  (into [] (map (fn [[k, v]] {:id k :label v}) m)))

(defn expired?
  "Given a :date-time timestamp as a string or instance of goog.date.Date, check if it
   has expired (older than current time). Note, :date-time is of the form 2016-12-03T03:38:03.668Z"
  [ts]
  (if (or (nil? ts) (and (string? ts) (empty? ts)))
    true
    (if #(instance? goog.date.Date ts)
      (before? ts (now))
      (let [dt (parse (formatters :date-time) ts)]
        (before? dt (now))))))

(defn find-element-by-key-value
  "Given a list of maps, return the index into the vector where the given key
   and value match. E.g. find the image with :uuid 1234"
  [l kw val]
  (let [matches (into {} (filter #(= val (get (second %) kw)) (map-indexed vector l)))]
    ;; 'matches' will look like this: ([1 {:uuid 1234, :name "something"}])
    (cond
      (= 0 (count matches)) (throw (ex-info (str "No item found with " kw) {kw val}))
      (> (count matches) 1) (throw (ex-info (str "More than one item has the same " kw) matches))
      :else (first (first matches)))))

(defn find-item-by-key-value
  "Given a sorted-map with numeric keys and map values, return the numeric key
   value for the map matching the given kw/val, e.g. :uuid as the kw"
  [items kw val]
  (let [item (into {} (filter #(= val (get (second %) kw)) items))]
    (cond
      (= 0 (count (keys item))) (throw (ex-info (str "No item found with " kw) {kw val}))
      (> (count (keys item)) 1) (throw (ex-info (str "More than one item has the same " kw) item))
      :else (first (keys item)))))

(defn fetch-item-by-key-value
  "Perform the find-item-by-key-value and return the associated item map."
  [items kw val]
  (let [id (find-item-by-key-value items kw val)]
    (get items id)))

(defn generate-uuid
  []
  (-> (uuid/make-random-uuid)
      (uuid/uuid-string)))

(defn into-sorted-map
  [m]
  (into (sorted-map) (map (fn [k v] {(+ 1 k) v}) (range (count m)) m)))

(defn max-string-length
  "Find max string length in the list of strings and return the max if it is less
   than or equal to 'limit', otherwise return 'limit'"
  [l limit]
  (if (empty? l)
    0
    (min limit (apply max (map count l)))))

(defn remove-vector-element
  "Remove the i'th element from vector, zero-based"
  [v i]
  (into (subvec v 0 i) (subvec v (inc i))))

(defn safe-date-string
  "Return string version of date in YYYY-MM-DD format"
  [dt]
  (if (nil? dt)
    ""
    (unparse (formatters :date) dt)))

(defn safe-string
  "Return a non-empty string: either input string or provided 'default' if input string is empty"
  [s default]
  (if (empty? s)
    default
    s))

(defn sort-by-datetime
  "Used as a comparator to sort, comparing two datetime key values and
   falling back to :created time of the item. If the input maps do not
   have :created, use sort-by-datetime-only instead"
  [k reverse? m1 m2]
  ; (pprint (str "Sort-by-datetime: " k " of " m1 " " m2))
  (let [before-after (if reverse? after? before?)]
    (if (or (nil? m1) (nil? m2))
      (compare m1 m2)
      (if (or (nil? (get m1 k)) (nil? (get m2 k)) (cljs-time.core/= (get m1 k) (get m2 k)))
        (if (or (nil? (get m1 :created)) (nil? (get m2 :created)))
          (compare (get m1 :created) (get m2 :created))
          (before-after (get m1 :created) (get m2 :created)))
        (before-after (get m1 k) (get m2 k))))))

(defn sort-by-datetime-only
  "Used as a comparator to sort, comparing two datetime key values."
  [k reverse? m1 m2]
  (let [before-after (if reverse? after? before?)]
    (if (or (nil? m1) (nil? m2))
      (compare m1 m2)
      (if (or (nil? (get m1 k)) (nil? (get m2 k)))
        (compare (get m1 k) (get m2 k))
        (before-after (get m1 k) (get m2 k))))))

(defn sort-by-key-then-created
  "Used as a comparator to sort, comparing key values between two maps. If the maps do not
   contain 'created' keys, it is harmless nil <> nil comparison."
  [k reverse? m1 m2]
  (if (or (instance? js/goog.date.UtcDateTime (get m1 k))
          (instance? js/goog.date.UtcDateTime (get m2 k)))
    (sort-by-datetime k reverse? m1 m2)
    (let [key-comparison (compare (get m1 k) (get m2 k))
          before-after (if reverse? after? before?)]
      (if (= 0 key-comparison)
        (if (or (nil? (get m1 :created)) (nil? (get m2 :created)))
          (compare (get m1 :created) (get m2 :created))
          (before-after (get m1 :created) (get m2 :created)))
        (if reverse?
          (* -1 key-comparison)
          key-comparison)))))

(defn title-string
  "Return a non-empty title"
  [title]
  (if (empty? title)
    "(no title)"
    title))

(defn trunc
   "Truncate given string and use ... to designate truncating occurred"
   [s l]
   (if (> (count s) l)
     (str (apply str (take (- l 3) s)) "...")
     s))

(def url-with-protocol-regex #"^.*://.*$")
(defn url-to-href
  "Prefix given url string with http:// if no protocol is defined"
  [url]
  (when (not (empty? url))
    (if (nil? (re-matches url-with-protocol-regex url))
      ;; prepend http://, else return the input url
      (str "http://" url)
      url)))

(defn uuid-label-list-to-options
  "Given a list of [uuid name] pairs, create a sorted list, by name, suitable for selection list options"
  ([tuples]
   (uuid-label-list-to-options tuples true))
  ([tuples with-none-option?]
   (let [sorted (sort #(compare (last %1) (last %2)) tuples)
         base (if with-none-option? [{:id :none :label "None"}] [])]
     (into base (map (fn [[uuid, label]] {:id uuid :label label}) sorted)))))
