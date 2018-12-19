(ns helodali.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame :refer [reg-sub]]
              [helodali.misc :refer [find-item-by-key-value sort-by-datetime-only sort-by-key-then-created
                                     fetch-item-by-key-value search-item-by-key-value]]
              [cljs.pprint :refer [pprint]]))

(reg-sub
  :name
  (fn [db]
    (:name db)))

(reg-sub
  :app-key
  (fn [db [_ k]]
    (get db k)))

(reg-sub
  :items-keys
  (fn [db [_ type]]
    (let [items (get db type)]
      (if (nil? items)
        nil ;; Case where we want nil to represent an unpopulated items map (e.g. :instagram-media)
        (filter (partial < 0) (keys items)))))) ;; filter out placeholder entry with id == 0

;; Return list of ids in sorted order, using :created as the tie-breaker
(reg-sub
  :items-keys-sorted-by
  (fn [db [_ type comparator]]
    (sort #(comparator (get-in db [type %1]) (get-in db [type %2])) (filter (partial < 0) (keys (get db type))))))

;; List of ids sorted by the kw and direction defined in app-db's :sort-keys maps
(reg-sub
  :items-keys-sorted-by-key
  (fn [db [_ type comparator]]
    (let [kw (get-in db [:sort-keys type 0])
          reverse? (not (get-in db [:sort-keys type 1]))]
      (sort #(comparator kw reverse? (get-in db [type %1]) (get-in db [type %2])) (filter (partial < 0) (keys (get db type)))))))

;; Filtered list of ids sorted by the kw and direction defined in app-db's :sort-keys maps
;; Also differs from the above with the use of a separate sort-key which can differ from item type
(reg-sub
  :filtered-items-keys-sorted-by-key
  (fn [db [_ filter-fx type sort-key comparator]]
    (let [kw (get-in db [:sort-keys sort-key 0])
          reverse? (not (get-in db [:sort-keys sort-key 1]))
          filtered-items (filter #(and (< 0 %) (filter-fx (get-in db [type %]))) (keys (get db type)))]
      (sort #(comparator kw reverse? (get-in db [type %1]) (get-in db [type %2])) filtered-items))))

;; Return list of maps (e.g. an artwork's purchases) in sorted order, using :created as the tie-breaker
(reg-sub
  :by-path-sorted-by
  (fn [db [_ path comparator]]
    (sort #(comparator %1 %2) (get-in db path))))

;; Same as above but the map contains a :ref key which needs to be dereferenced to get the map of type 'type'
(reg-sub
  :by-path-and-deref-sorted-by
  (fn [db [_ path type comparator]]
    (let [resolve (fn [uuid]
                    (if (empty? uuid)
                      uuid
                      (let [id (find-item-by-key-value (get db type) :uuid uuid)]
                        (get-in db [type id]))))]
      (sort #(comparator (resolve (:ref %1)) (resolve (:ref %2))) (get-in db path)))))

;; Another variation of the above but where we expect a set of uuid values as opposed
;; to a vector of maps.
(reg-sub
  :by-path-and-deref-set-sorted-by
  (fn [db [_ path type comparator]]
    (let [resolve (fn [uuid]
                    (if (empty? uuid)
                      uuid
                      (let [id (find-item-by-key-value (get db type) :uuid uuid)]
                        (get-in db [type id]))))]
      (sort #(comparator (resolve %1) (resolve %2)) (get-in db path)))))

(defn- assoc-uuid-purchases
  ;; Given a uuid and list of purchase maps, produce a vector of maps keyed by artwork uuid
  ;; like so: [{:uuid uuid :purchase <purchase-map>}} ...]. The purchase map includes the
  ;; artwork title to allow sorting on title form the :search-purchases subscription.
  [db uuid title l]
  (apply vector (map (fn [m]
                        (let [buyer-contact (if (nil? (:buyer m))
                                              nil
                                              (fetch-item-by-key-value (get db :contacts) :uuid (:buyer m)))
                              agent-contact (if (nil? (:agent m))
                                              nil
                                              (fetch-item-by-key-value (get db :contacts) :uuid (:agent m)))
                              dealer-contact (if (nil? (:dealer m))
                                               nil
                                               (fetch-item-by-key-value (get db :contacts) :uuid (:dealer m)))
                              m (-> (assoc m :title title)
                                   (assoc :buyer-name (:name buyer-contact))
                                   (assoc :agent-name (:name agent-contact))
                                   (assoc :dealer-name (:name dealer-contact)))]
                          {:uuid uuid :purchase m}))
                   l)))

;; Search across artwork items and return all purchases in a list with the associated
;; artwork uuid: As in [{:uuid <uuid of artwork> :purchase {purchase map}} ...]
;; Filter above list based on provided predicate and sort by current sort-key.
(reg-sub
  :search-purchases
  (fn [db [_ predicate]]
    (let [kw (get-in db [:sort-keys :purchases 0])
          reverse? (not (get-in db [:sort-keys :purchases 1]))
          artwork (filter #(not (empty? (:purchases %))) (vals (get db :artwork)))
          expanded (map #(assoc-uuid-purchases db (:uuid %) (:title % ) (:purchases %)) artwork)
          purchases (apply concat expanded)]
      (sort #(sort-by-key-then-created kw reverse? (:purchase %1) (:purchase %2)) (filter predicate purchases)))))

(def various-keys-to-filter-out-of-search-results
  #{:key :signed-thumb-url :signed-thumb-url-expiration-time :signed-raw-url :signed-raw-url-expiration-time
    :uuid :uref})

(defn- tagged-match
  "Given a pattern and tag (k), return a concatenated string 'k': 'in' if the pattern
  is matched in the string and nil otherwise. Skip the tagging if k is nil."
  [pattern k in]
  ;; First check if the given keyword (k) is something we do not want to search on
  (if (contains? various-keys-to-filter-out-of-search-results k)
    nil
    ;; Because re-matches wants to match the entire input and matching across lines is problematic,
    ;; we will break the input into a list of lines and match on each.
    (let [lines (clojure.string/split-lines in)
          matches (filter #(not (empty? %)) (map (partial re-matches pattern) lines))]
      (if (empty? matches)
        nil
        (if (nil? k)
          in
          (str (name k) ": " in))))))

(defn- walk-searcher
  "Walk the input search for pattern, tagging matches with the closest keyword (kw) in the map."
  [pattern kw in]
  (cond
    (map? in) (->> (clojure.walk/walk (fn [[k v]] [k ((partial walk-searcher pattern k) v)]) identity in)
                 (filter #(not (empty? (second %))))
                 (map second))
    (vector? in) (apply vector (filter #(not (empty? %)) (map (partial walk-searcher pattern kw) in)))
    (set? in) (filter #(not (empty? %)) (set (map (partial walk-searcher pattern kw) in)))
    (string? in) (tagged-match pattern kw in)
    (int? in) (tagged-match pattern kw (str in))
    (keyword? in) (tagged-match pattern kw (name in))
    :else nil))

; This version of walk-searcher does not tag the matches.
; (defn- walk-searcher
;   "Walk the input search for pattern"
;   [pattern in]
;   (cond
;     (map? in) (->> (clojure.walk/walk (fn [[k v]] [k ((partial walk-searcher pattern) v)]) identity in)
;                  (filter #(not (empty? (second %))))
;                  (map second))
;     (vector? in) (apply vector (filter #(not (empty? %)) (map (partial walk-searcher pattern) in)))
;     (set? in) (filter #(not (empty? %)) (set (map (partial walk-searcher pattern) in)))
;     (string? in) (re-matches pattern in)
;     (int? in) (re-matches pattern (str in))
;     (keyword? in) (re-matches pattern (name in))
;     :else nil))

(defn- search-acc
  "Accumulate matches for given pattern and item, using title-kw as the 'name' of the item."
  [item-type pattern item title-kw]
  (let [results (walk-searcher pattern nil item)]
    ;; Results can look something like this:
    ;; ("New3asdsd- that took awhile" [("This took more time to sell than imagined.")])
    ;; We assume the flatten function is trustworthy for flattening this.
    {:type item-type :uuid (:uuid item) :title (get item title-kw) :match (flatten results)}))

;; Search across all items in app-db, including :profile, matching against any string
;; and integer value in the app-db. The search pattern is assumed to be defined in
;; app-db at this time. Return a list of matches of the form:
;; [{:type <item type> :uuid <uuid of item> :title <whatever can serve as a title> :match <matched value> } ...]
(reg-sub
  :search
  (fn [db _]
    (if (empty? (:search-pattern db))
      []
      (let [kw (get-in db [:sort-keys :search-results 0])
            reverse? (not (get-in db [:sort-keys :search-results 1]))
            pattern (re-pattern (str "(?im).*" (:search-pattern db) ".*"))
            artwork (filter #(not (empty? (:match %))) (map #(search-acc :artwork pattern % :title) (vals (get db :artwork))))
            contacts (filter #(not (empty? (:match %))) (map #(search-acc :contacts pattern % :name) (vals (get db :contacts))))
            expenses (filter #(not (empty? (:match %))) (map #(search-acc :expenses pattern % :name) (vals (get db :expenses))))
            exhibitions (filter #(not (empty? (:match %))) (map #(search-acc :exhibitions pattern % :expense-type) (vals (get db :exhibitions))))
            press (filter #(not (empty? (:match %))) (map #(search-acc :press pattern % :title) (vals (get db :press))))
            profile (filter #(not (empty? (:match %))) (list (search-acc :profile pattern (get db :profile) :name)))
            matches (concat artwork contacts expenses exhibitions press profile)]
        (if (= kw :match) ;; Handle the sorting on 'Match' column differently than the others
          (let [reverse (if reverse? -1 1)]
            (sort #(* reverse (compare (first (:match %1)) (first (:match %2)))) matches))
          (sort #(sort-by-key-then-created kw reverse? %1 %2) matches))))))

;; Subscribe to items as a list of maps. We must filter out the placeholder which has id == 0.
(reg-sub
  :items-vals
  (fn [db [_ type k]]
    (map #(get % k) (vals (dissoc (get db type) 0)))))

;; Get a specific kw value from all items and include the uuid of the item-key
;; returning a vector of [uuid val]. Ignore item with id == 0 as that is the
;; placeholder for item creation.
(reg-sub
  :items-vals-with-uuid
  (fn [db [_ type k]]
    (map (fn [item] [(get item :uuid) (get item k)]) (vals (dissoc (get db type) 0)))))

(reg-sub
  :item-key
  (fn [db [_ type id k]]
    (get-in db [type id k])))

;; A subscription to a :uuid (or similar) value which refers to another item in
;; app-db. If the reference is invalid (item no longer exists) then return a nil value.
(reg-sub
  :item-key-valid-ref
  (fn [db [_ type id k ref-type]]
    (let [ref (get-in db [type id k])
          ref-kw (condp = ref-type
                   :instagram-media :instagram-id
                   :uuid)
          id (search-item-by-key-value (get db ref-type) ref-kw ref)]
      (if id
        ref
        nil))))

;; Subscribe to an element of the database by path, e.g. [:artwork id :purchases 2 :price]
(reg-sub
  :by-path
  (fn [db [_ path]]
    (get-in db path)))

(reg-sub
  :item-by-uuid
  (fn [db [_ type uuid]]
    ;; Find the item by uuid and return the item map
    (let [id (find-item-by-key-value (get db type) :uuid uuid)]
      (get-in db [type id]))))

(reg-sub
  :item-attribute-by-uuid
  (fn [db [_ type uuid kw]]
    ;; Find the item by uuid and return the item map
    (let [id (find-item-by-key-value (get db type) :uuid uuid)]
      (get-in db [type id kw]))))

(reg-sub
  :item-path-by-uuid
  (fn [db [_ type uuid]]
    ;; Find the item by uuid and return the path to the item
    (let [id (find-item-by-key-value (get db type) :uuid uuid)]
      [type id])))
