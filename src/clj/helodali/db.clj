(ns helodali.db
  (:require [taoensso.faraday :as far]
            [clj-uuid :as uuid]
            [clj-time.core :refer [date-time days ago]]
            [clj-time.format :refer [parse unparse formatters]]
            [helodali.demodata :as demo]
            [helodali.common :refer [coerce-int fix-date keywordize-vals]]
            [clojure.pprint :refer [pprint]]))

;; DynamoDB client-options map - real keys are needed for AWS, not for local use.
; (def co
;   {:access-key "qweasdzxcqweasdzxc"
;    :secret-key "qweasdqweasd"
;    :endpoint "http://localhost:8000"})

(def co
  {:access-key (get (System/getenv) "AWS_DYNAMODB_ACCESS_KEY")
   :secret-key (get (System/getenv) "AWS_DYNAMODB_SECRET_KEY")
   :endpoint   (get (System/getenv) "AWS_DYNAMODB_ENDPOINT")})

(defn- coerce-item
  "This looks for specific cases where we need to convert string
   values back to keywords and numbers to ints"
  ;; TODO: this method of defining each integer attribute is far from ideal. We should instead determine
  ;; the predicate to catch the integer type returned by DynamoDB and walk the map and coerce everything found.
  [table m]
  (condp = table
    :artwork (-> (assoc m :style (set (map keyword (:style m))))
               (assoc :purchases (apply vector (map #(coerce-int % [:price :total-commission-percent]) (:purchases m))))
               (assoc :images (apply vector (map #(assoc % :metadata (coerce-int (:metadata %) [:density :width :height])) (:images m))))
               (keywordize-vals [:type :status])
               (coerce-int [:expenses :list-price :year :editions]))
    :contacts (keywordize-vals m [:role])
    :exhibitions (keywordize-vals m [:kind])
    :profile (-> m
               (coerce-int [:birth-year])
               (assoc :degrees (apply vector (map #(coerce-int % [:year]) (:degrees m))))
               (assoc :awards-and-grants (apply vector (map #(coerce-int % [:year]) (:awards-and-grants m))))
               (assoc :lectures-and-talks (apply vector (map #(coerce-int % [:year]) (:lectures-and-talks m))))
               (assoc :residencies (apply vector (map #(coerce-int % [:year]) (:residencies m)))))
    m))

(defn get-profile-by-sub
  "Given an openid subject identifier (the 'sub' claim), resolve to a profile map
   by searching the openid table for the user's uuid and then getting the items
   from profiles. Return the openid item found and the profile map, an empty map
   if no profile is found."
  [sub]
  (let [openid-item (far/get-item co :openid {:sub sub})
        uref (:uref openid-item)]
    (if (nil? uref)
      [openid-item {}]
      (let [profile (far/get-item co :profiles {:uuid uref})]
        (if (nil? profile)
          [openid-item {}]
          [openid-item (coerce-item :profile profile)])))))

(defn query-by-uref
  "Query on items and clean results"
  [table uref]
  (map (partial coerce-item table) (far/query co table {:uref [:eq uref]})))

(defn- sync-userinfo
   "Update our openid item in the database if the given userinfo map disagrees"
  [userinfo openid-item]
  (let [sub (:sub openid-item)]      ;; TODO: combine these operations into one write
    (if (not= (:email openid-item) (:email userinfo))
      (far/update-item co :openid {:sub sub}
                       {:update-expr     (str "SET #email = :val")
                        :expr-attr-names {"#email" "email"}
                        :expr-attr-vals  {":val" (:email userinfo)}
                        :return          :all-new}))
    (if (not= (:name openid-item) (:name userinfo))
      (far/update-item co :openid {:sub sub}
                       {:update-expr     (str "SET #name = :val")
                        :expr-attr-names {"#name" "name"}
                        :expr-attr-vals  {":val" (:name userinfo)}
                        :return          :all-new}))))

(defn valid-user?
  "Given an openid claims map, with :sub key, resolve the user against :openid and :profiles"
  [userinfo]
  (if (nil? (:sub userinfo))
    false
    (let [[openid-item profile] (get-profile-by-sub (:sub userinfo))
          uref (:uuid profile)]
      (pprint (str "valid-user?: " profile))
      (if (and (not (empty? openid-item)) (not (empty? profile)))
        true
        false))))

(defn initialize-db
  "Given an openid claims map, with :sub key, resolve the user against :openid and :profiles
   tables and then construct the user's app-db for the client. Also take this opportunity to
   make sure our openid table item is in sync with the provided claims map - update the
   email or name in our database."
  [userinfo]
  (if (nil? (:sub userinfo))
    {}
    (let [[openid-item profile] (get-profile-by-sub (:sub userinfo))
          uref (:uuid profile)]
      (pprint (str "Profile: " profile))
      (when (not (empty? openid-item))
        (sync-userinfo userinfo openid-item))
      {:artwork (query-by-uref :artwork uref)
       :documents []       ;; TODO
       :exhibitions (query-by-uref :exhibitions uref)
       :contacts (query-by-uref :contacts uref)
       :press (query-by-uref :press uref)
       :profile profile
       :userinfo userinfo})))

(defn- undash
  "Replace '-' with 'D' in given string"
  [s]
  (clojure.string/replace s #"-" "D"))

(defn convert-path-to-expression-attribute
  "Convert path representing a clojure path for assoc-in to a dotted expression path needed
   by DynamoDB. E.g. [:purchases 1 :date] => \"#purchases[1].#date\"
                  or [\"purchases\" 1 \"date\"] => \"#purchases[1].#date\"
                  or  :notes => \"#notes\"
   Note the path must begin with a keyword, not an integer. Return both the expression
   attribute string, #purchases[1].#date, as well as the attr map
        { \"#purchases\" \"purchases\" \"#date\" \"date\"}

   DynamoDB does not like dashes in expressions so we translate '-' to 'D', E.g. #listDprice"
   [path]
   (if (or (string? path) (keyword? path))
     (let [a (undash (str "#" (name path)))]
       [a {a (name path)}])
     (let [attr-expression (undash (if (= 1 (count path))
                                     (str "#" (name (first path)))
                                     (str "#" (reduce #(str (name %1) (if (integer? %2) (str "[" %2 "]") (str ".#" (name %2)))) path))))
           hashed (map #(undash (str "#" (name %))) (filter #(not (integer? %)) path))
           names (map name (filter #(not (integer? %)) path))]
       [attr-expression (zipmap hashed names)])))

(defn- filter-out-empty
  [in]
  (cond
    (map? in) (into {} (filter (fn [[k v]]
                                (if (or (nil? v) (and (coll? v) (empty? v)))
                                 false
                                 true)) in))
    (vector? in) (apply vector (map filter-out-empty in))
    :else in))

(defn- walk-cleaner
  "Walk the input and:
    - dissoc nil, [] and #{} valued keys, ignore non-map input"
  [in]
  (cond
      (map? in) (clojure.walk/walk (fn [[k v]] [k (filter-out-empty v)]) identity (filter-out-empty in))
      :else in))

(defn update-profile
  "Update artist profile. The table is assumed to be :profiles. If the val is nil or an empty
   set, then perform a REMOVE of the attribute as opposed to a SET of a nil value."
  ;; TODO: Put in condition to assert existence of item. Otherwise a new, inchoate, item will be create
  [uuid path val]
  (let [[attr-expression expression-map] (convert-path-to-expression-attribute path)
        val (walk-cleaner val)
        changes (if (or (nil? val) (and (coll? val) (empty? val)))
                  {:update-expr     (str "REMOVE " attr-expression)
                   :expr-attr-names expression-map
                   :return          :all-new}
                  {:update-expr     (str "SET " attr-expression " = :val")
                   :expr-attr-names expression-map
                   :expr-attr-vals  {":val" val}
                   :return          :all-new})]
    (pprint (str "Performing changes: " changes))
    (far/update-item co :profiles {:uuid uuid} changes)))

(defn update-item
  "Update artwork, press, exhibitions, documents, or contacts table. The 'path'
   argument is a keyword or vector path into the item in given 'table'. E.g. :notes or
   [:purchases 1 :date]
  If the val is nil or an empty set, then perform a REMOVE of the attribute as opposed to a SET of a nil value."
  ;; TODO: Put in condition to assert existence of item. Otherwise a new, inchoate, item will be create
  [table uref uuid path val]
  (let [[attr-expression expression-map] (convert-path-to-expression-attribute path)
        val (walk-cleaner val)
        changes (if (or (nil? val) (and (set? val) (empty? val)))
                  {:update-expr     (str "REMOVE " attr-expression)
                   :expr-attr-names expression-map
                   :return          :all-new}
                  {:update-expr     (str "SET " attr-expression " = :val")
                   :expr-attr-names expression-map
                   :expr-attr-vals  {":val" val}
                   :return          :all-new})]
    (pprint (str "Performing changes: " changes))
    (far/update-item co table {:uref uref :uuid uuid} changes)))

(defn refresh-item-path
  "Fetch item from artwork, press, exhibitions, documents, or contacts table. The 'path'
   argument is a keyword or vector path into the item."
  ;; TODO: think about optimzing the get-item call with projections
  [table uref item-uuid path]
  (pprint (str "refresh-item-path uref/item-uuid: " uref "/" item-uuid))
  (let [item (far/get-item co table {:uref uref :uuid item-uuid})
        val (get-in (coerce-item table item) path)]
    (pprint (str "Refresh returning: " val))
    val))

(defn create-item
  "Create a new item in given table. Drop any nil or #{} valued attributes, this
   requires walking the collection."
  [table item]
  (let [item (walk-cleaner item)]
    (pprint (str "creating cleaned item: " item))
    (far/put-item co table item)))

(defn delete-item
  [table uref uuid]
  (far/delete-item co table {:uref uref :uuid uuid}))

; (pprint (initialize-db "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb"))
(defn create-table
  [name]
  (far/create-table co name
    [:uref :s]  ; Hash key of uuid-valued user reference, (:s => string type)
    {:range-keydef [:uuid :s]
     :throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
     :block? true})) ; Block thread during table creation

(defn put-items
  [table items]
  (for [item items]
    (far/put-item co table item)))

(defn create-demo
  []
  (let [brianw "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb"
        tables (far/list-tables co)
        press (->> demo/press
                 (map #(assoc % :uref brianw))
                 (map #(assoc % :created (unparse (formatters :date) (:created %))))
                 (map #(assoc % :publication-date (unparse (formatters :date) (:publication-date %)))))
        contacts (->> demo/contacts
                    (map #(assoc % :uref brianw))
                    (map #(assoc % :created (unparse (formatters :date) (:created %)))))
        exhibitions (->> demo/exhibitions
                       (map #(assoc % :uref brianw))
                       (map #(assoc % :created (unparse (formatters :date) (:created %))))
                       (map #(assoc % :begin-date (unparse (formatters :date) (:begin-date %))))
                       (map #(assoc % :end-date (unparse (formatters :date) (:end-date %)))))
        artwork (->> demo/artwork
                   (map #(assoc % :uuid (str (uuid/v1))))
                   (map #(assoc % :uref brianw))
                   (map #(assoc % :created (unparse (formatters :date) (:created %))))
                   (map #(assoc % :purchases (fix-date :unparse :date (:purchases %)))))]
    (doall (map #(far/delete-table co %) tables))
    (doall (map #(create-table %) [:press :exhibitions :artwork :contacts]))
    (far/create-table co :profiles
      [:uuid :s]  ; Hash key of uuid-valued user reference, (:s => string type)
      {:throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
       :block? true})
    (far/create-table co :accounts
      [:uuid :s]  ; Hash key of uuid-valued user reference, (:s => string type)
      {:throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
       :block? true})
    (far/create-table co :openid
      [:sub :s]  ; Hash key is the OpenID 'sub' claim (subject identifier, e.g. "google-oauth2|105303869357768353564")
      {:throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
       :block? true
       :gsindexes [{:name "email-index"
                    :hash-keydef [:email :s]
                    :projection :keys-only
                    :throughput {:read 2 :write 2}}]})
    (far/put-item co :accounts (assoc demo/account :created (unparse (formatters :date) (:created demo/account))))
    (far/put-item co :profiles (assoc demo/profile :created (unparse (formatters :date) (:created demo/profile))))
    (far/put-item co :openid {:uref (:uuid demo/profile) :sub "google-oauth2|105303869357768353564" :email "brian.williams@mayalane.com"})
    (far/put-item co :openid {:uref (:uuid demo/profile) :sub "facebook|10208314583117362" :email "brian@mayalane.com"})
    (far/put-item co :openid {:uref "doesnotexist" :sub "facebook|1234" :email "brian@mayalane.com"})
    (doall (put-items :contacts contacts))
    (doall (put-items :exhibitions exhibitions))
    (doall (put-items :artwork artwork))
    (doall (put-items :press press))))

; (pprint (far/query co :openid {:email [:eq "brian.williams@mayalane.com"]} {:index "email-index"}))
; (pprint (far/get-item co :openid {:sub "facebook|10208314583117362"}))
; (pprint (far/update-item co :openid {:sub "facebook|10208314583117362"}
;                          {:update-expr     (str "SET #email = :val")
;                           :expr-attr-names {"#email" "email"}
;                           :expr-attr-vals  {":val" "brian@foo.com"}
;                           :return          :all-new}))
; (pprint (far/get-item co :artwork {:uref brianw :uuid "b8ace141-aeb5-11e6-a116-c83ff47bbdcb"}))
; (pprint (query-by-uref :contacts brianw))
; (pprint (far/query co :profiles {:uuid [:eq brianw]}))
; (pprint (update-profile brianw [:degrees] nil))
; (delete-item :artwork brianw "a5ef27a1-7814-4e37-addc-25c5e54b6d29")
; (pprint (update-item :artwork brianw "b8d80fe3-aeb5-11e6-a116-c83ff47bbdcb" [:status] :not-for-sale))
; (create-demo)

(comment
  (create-demo)
  (far/get-item co :press {:uref brianw :uuid "123123-222-123123-0001"})
  ; (def all (far/scan co :artwork))
  (far/query co :artwork {:uref [:eq brianw]}))
