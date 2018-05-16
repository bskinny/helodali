(ns helodali.db
  (:require [taoensso.faraday :as far]
            [clj-uuid :as uuid]
            [clj-time.core :refer [now year days ago]]
            [clj-time.format :refer [parse unparse formatters]]
            [aws.sdk.s3 :as s3]
            [clojure.java.io :as io]
            [helodali.common :refer [coerce-int fix-date keywordize-vals]]
            [clojure.pprint :refer [pprint]])
  (:import [java.time ZonedDateTime ZoneId]
           (java.time.format DateTimeFormatter)))

;; These environment variables are only necessary when running the app locally
;; or outside AWS. Within AWS, we assign an IAM role to the ElasticBeanstalk
;; instance housing the application.
(def co
  {:access-key (or (System/getenv "AWS_ACCESS_KEY")
                   (System/getProperty "AWS_ACCESS_KEY"))
   :secret-key (or (System/getenv "AWS_SECRET_KEY")
                   (System/getProperty "AWS_SECRET_KEY"))
   :endpoint   (or (System/getenv "AWS_DYNAMODB_ENDPOINT")
                   (System/getProperty "AWS_DYNAMODB_ENDPOINT"))})

(defn- coerce-item
  "This looks for specific cases where we need to convert string
   values back to keywords and numbers to ints"
  ;; TODO: this method of defining each integer attribute is far from ideal. We should instead determine
  ;; the predicate to catch the integer type returned by DynamoDB and walk the map and coerce everything found.
  [table m]
  (condp = table
    :artwork (-> (assoc m :style (set (map keyword (:style m))))
                 (assoc :purchases (apply vector (map #(coerce-int % [:price :total-commission-percent]) (:purchases m))))
                 (assoc :images (apply vector (map #(assoc % :metadata (coerce-int (:metadata %) [:density :size :width :height])) (:images m))))
                 (keywordize-vals [:type :status])
                 (coerce-int [:expenses :list-price :year :editions])
                 (assoc :instagram-media-ref (and (:instagram-media-ref m)
                                                  (-> (:instagram-media-ref m)
                                                      (coerce-int [:likes])
                                                      (assoc :media-type (keyword (:media-type (:instagram-media-ref m))))))))
    :contacts (keywordize-vals m [:role])
    :exhibitions (keywordize-vals m [:kind])
    :documents (coerce-int m [:size])
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

(defn get-account
  "Get the user's account item."
  [uuid]
  (let [account (far/get-item co :accounts {:uuid uuid})]
    account))

(defn query-by-uref
  "Query on items and clean results"
  ([table uref]
   (query-by-uref table uref {}))
  ([table uref opts]
   (map (partial coerce-item table) (far/query co table {:uref [:eq uref]} opts))))

(defn- sync-userinfo
  "Update our openid item in the database if the given userinfo map disagrees"
  [userinfo openid-item]
  (pprint (str "sync-userinfo: userinfo is " userinfo))
  (pprint (str "sync-userinfo: openid-item is " openid-item))
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

(defn create-user-if-necessary
  "Look for the given sub as a user of our application if she does not exist yet, create her."
  [userinfo]
  (when-not (nil? (:sub userinfo))
    (let [openid-item (far/get-item co :openid {:sub (:sub userinfo)})]
      (if (nil? (:uref openid-item))
        (let [uuid (str (uuid/v1))
              created (unparse (formatters :date) (now))]
          (pprint (str "Creating user account for " (:sub userinfo)))
          (far/put-item co :accounts {:uuid uuid :created created})
          (far/put-item co :profiles {:uuid uuid :name (:name userinfo) :created created})
          (far/put-item co :openid {:uref uuid :sub (:sub userinfo) :email (:email userinfo)}))
        (sync-userinfo userinfo openid-item)))))

(defn valid-user?
  "Given an openid claims map, with :sub key, resolve the user against :openid and :profiles"
  [userinfo]
  (if (nil? (:sub userinfo))
    false
    (let [[openid-item profile] (get-profile-by-sub (:sub userinfo))
          uref (:uuid profile)]
      (if (and (not (empty? openid-item)) (not (empty? profile)))
        true
        false))))

(defn cache-access-token
  [session-uuid token-resp userinfo]
  (let [openid-item (far/get-item co :openid {:sub (:sub userinfo)})
        uref (:uref openid-item)]
    (when uref
      (let [tn (ZonedDateTime/now (ZoneId/of "Z"))]
        (far/put-item co :sessions {:uuid     session-uuid :uref uref :token (:access_token token-resp)
                                    :refresh  (:refresh_token token-resp)
                                    :id-token (:id_token token-resp) :sub (:sub userinfo)
                                    :ts (.format tn DateTimeFormatter/ISO_INSTANT)})
        uref))))

(defn delete-access-token
  [access-token uref]
  (pprint (str "delete-access-token for uuid: " uref))
  (let [session  (far/query co :sessions {:uref [:eq uref] :token [:eq access-token]}
                            {:index "uref-and-token" :limit 1})]
    (when session
      (far/delete-item co :sessions {:uref uref :token access-token}))))

(defn query-session
  "Check if the given uref and access-token are in agreement in the :sessions table,
   i.e. there is an item that matches the pair of values. If so, return the session."
  [uref access-token]
  (if (or (empty? uref) (empty? access-token))
    {}
    (let [session (far/query co :sessions {:uref [:eq uref] :token [:eq access-token]}
                             {:index "uref-and-token" :limit 1})]
      ;; The projection will include :uuid, :uref, :token, :sub, :ts, and :refresh
      (if session
        (first session)
        {}))))

(defn get-session
  "Retrieve the session table item given the session uuid."
  [uuid]
  (let [session (far/get-item co :sessions {:uuid uuid})]
    (if session
      session
      {})))

(defn initialize-db
  "Given an openid claims map, with :sub key, resolve the user against :openid and :profiles
   tables and then construct the user's app-db for the client. Also take this opportunity to
   make sure our openid table item is in sync with the provided claims map - update the
   email or name in our database."
  [sub session]
  (if (nil? sub)
    {}
    (let [[openid-item profile] (get-profile-by-sub sub)
          uref (:uuid profile)]
      {:artwork (query-by-uref :artwork uref)
       :documents (query-by-uref :documents uref)
       :exhibitions (query-by-uref :exhibitions uref)
       :contacts (query-by-uref :contacts uref)
       :press (query-by-uref :press uref)
       :profile profile
       :authenticated? true
       :initialized? true
       :access-token (:token session)
       :id-token (:id-token session)
       :account (-> (get-account uref)
                    (dissoc :instagram-access-token))
       :userinfo openid-item})))   ;; TODO: Fix this, should be userinfo

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
                                (if (or (nil? v) (and (string? v) (empty? v)) (and (coll? v) (empty? v)))
                                 false
                                 true)) in))
    (vector? in) (apply vector (map filter-out-empty in))
    :else in))

(defn- walk-cleaner
  "Walk the input and:
    - dissoc nil, \"\", [] and #{} valued keys, ignore non-map input"
  [in]
  (cond
      (map? in) (clojure.walk/walk (fn [[k v]] [k (filter-out-empty v)]) identity (filter-out-empty in))
      :else in))

(defn apply-attribute-change
  "Update artwork, press, exhibitions, documents, or contacts table. The 'path'
   argument is a keyword or vector path into the item in given 'table'. E.g. :notes or
   [:purchases 1 :date]
  If the val is nil or an empty set, then perform a REMOVE of the attribute as opposed to a SET of a nil value."
  ;; TODO: Put in condition to assert existence of item. Otherwise a new, inchoate, item will be create
  [table primary-key path val]
  (let [[attr-expression expression-map] (convert-path-to-expression-attribute path)
        val (walk-cleaner val)
        change (if (or (nil? val) (and (set? val) (empty? val)))
                  {:update-expr     (str "REMOVE " attr-expression)
                   :expr-attr-names expression-map
                   :return          :all-new}
                  {:update-expr     (str "SET " attr-expression " = :val")
                   :expr-attr-names expression-map
                   :expr-attr-vals  {":val" val}
                   :return          :all-new})]
    (pprint (str "Performing change: " change))
    (far/update-item co table primary-key change)))

(defn- build-db-changes
  "A reducer on a map representing updates to an item."
  [m idx [path val]]
  (let [[attr-expression expression-map] (convert-path-to-expression-attribute path)
        val (walk-cleaner val)
        merged-expr-attr-names (merge (:expr-attr-names m) expression-map)]
    (if (or (nil? val) (and (set? val) (empty? val)))
      (merge m {:remove-update-expr (conj (:remove-update-expr m) (str attr-expression))
                :expr-attr-names merged-expr-attr-names})
      (merge m {:set-update-expr (conj (:set-update-expr m) (str attr-expression " = :val" idx))
                :expr-attr-names merged-expr-attr-names
                :expr-attr-vals  (merge (:expr-attr-vals m) {(str ":val" idx) val})}))))

(defn apply-attribute-changes
  "Update artwork, press, exhibitions, documents, or contacts table. The 'changes'
   argument is a map of changes to apply with keys representing paths, or attribute names, see DynamoDB
   UpdateExpressions (http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.Modifying.html).
   Any nil values will result in REMOVE UpdateExpression."
  ;; TODO: Put in condition to assert existence of item. Otherwise a new, inchoate, item will be create
  [table primary-key changes]
  (let [indexed-changes (zipmap (range (count changes)) changes) ;; Yields {0 [[:purchases 0 :date] "2001-03-14"], 1 [[:name] "Bo"]}
        db-changes (-> (reduce-kv build-db-changes {} indexed-changes))
        update-expression (str
                            (when (:set-update-expr db-changes)
                              (str "SET " (clojure.string/join ", " (:set-update-expr db-changes)) " "))
                            (when (:remove-update-expr db-changes)
                              (str "REMOVE " (clojure.string/join ", " (:remove-update-expr db-changes)))))
        db-changes (-> (assoc db-changes :return :all-new)
                      (assoc :update-expr update-expression)
                      (dissoc :set-update-expr)
                      (dissoc :remove-update-expr))]
    (pprint (str "Performing changes: " db-changes))
    (if (nil? update-expression)
      (pprint (str "No changes to apply for " changes))
      (far/update-item co table primary-key db-changes))))

(defn update-item
  "Update items in artwork, press, exhibitions, documents, or contacts tables. The method of building
   the DynamoDB changeset depends on whether we are called with a single attribute change (path == [path to attribute])
   or multiple attribute changes within an item (path == nil and val is keyed with attribute paths)"
  [table uref uuid path val]
  (if (nil? val)
    {} ;; nothing to do)
    (if (nil? path)
      (apply-attribute-changes table {:uref uref :uuid uuid} val)
      (apply-attribute-change table {:uref uref :uuid uuid} path val))))

(defn update-user-table
  "Update a user table, such as profiles. The method of building
   the DynamoDB changeset depends on whether we are called with a single attribute change (path == [path to attribute])
   or multiple attribute changes within an item (path == nil and val is keyed with attribute paths)"
  [table uuid path val]
  (if (nil? path)
    (apply-attribute-changes table {:uuid uuid} val)
    (apply-attribute-change table {:uuid uuid} path val)))

(defn refresh-item-path
  "Fetch item from artwork, press, exhibitions, documents, or contacts table. The 'path'
   argument is a keyword or vector path into the item and can be nil to return the entire item."
  ;; TODO: think about optimizing the get-item call with projections
  [table uref item-uuid path]
  (pprint (str "refresh-item-path uref/item-uuid: " uref "/" item-uuid))
  (let [item (->> (far/get-item co table {:uref uref :uuid item-uuid})
                  (coerce-item table))
        val (if (empty? path)
              item
              (get-in item path))]
    (pprint (str "Refresh returning: " val))
    val))

(defn refresh-image-data
  "Fetch image map from artwork table the image given by item-uuid and image-uuid"
  ;; TODO: think about optimizing the get-item call with projections
  [uref item-uuid image-uuid]
  (pprint (str "refresh-image-data uref/item-uuid/image-uuid: " uref "/" item-uuid "/" image-uuid))
  (let [item (coerce-item :artwork (far/get-item co :artwork {:uref uref :uuid item-uuid}))
        image (filter #(= image-uuid (:uuid %)) (:images item))]
    (pprint (str "Refresh image data returning: " (first image)))
    (first image)))

(defn create-item
  "Create a new item in given table. Drop any nil or #{} valued attributes, this
   requires walking the collection."
  [table item]
  (let [item (walk-cleaner item)]
    (pprint (str "creating cleaned item: " item))
    (far/put-item co table item)))

(defn create-artwork-from-instragram
   "Build an artwork item from the given instagram media, copy the image from instagram to our S3
    bucket and return updates to both :artwork and :instagram-media portions of the client's app-db."
  [uref sub media]
  (let [cred (dissoc co :endpoint)
        artwork-uuid (str (uuid/v1))
        image-uuid (str (uuid/v1))
        item {:uref uref
              :uuid artwork-uuid
              :created (:created media)
              :description (:caption media)
              :year (year (now))
              :status :for-sale
              :type :mixed-media
              :series false
              :list-price 0
              :expenses 0
              :editions 0
              :sync-with-instagram true
              :instagram-media-ref media}
        url (java.net.URL. (:image-url media))
        filename (-> (.getPath url)
                    (clojure.string/split #"/")
                    (last))
        object-key (str sub "/" artwork-uuid "/" image-uuid "/" filename)
        item (walk-cleaner item)]
    (with-open [ig-is (io/input-stream url)]
      ;; Create the database item and copy the image to S3
      (far/put-item co :artwork item)
      (s3/put-object cred "helodali-raw-images" object-key ig-is)
      ;; Return an updated version of the :instagram-media-ref item as well as the new :artwork item
      {:artwork [[artwork-uuid [nil (assoc item :images [{:uuid image-uuid :processing true}])]]]
       :instagram-media [[(:instagram-id media) [nil (assoc media :artwork-uuid artwork-uuid)]]]})))

(defn delete-item
  [table key-map]
  (far/delete-item co table key-map))

(defn create-table
  [name]
  (far/create-table co name
    [:uref :s]  ; Hash key of uuid-valued user reference, (:s => string type)
    {:range-keydef [:uuid :s]
     :throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
     :block? true})) ; Block thread during table creation

(defn put-items
  [table items]
  (doseq [item items]
    (far/put-item co table item)))

(defn table-and-demo-creation
  "This recreates the database and will delete all existing data."
  []
  (let [brianw "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb"
        tables (far/list-tables co)]
    (doall (map #(far/delete-table co %) tables))
    (doall (map #(create-table %) [:press :exhibitions :documents :artwork :contacts]))
    (far/create-table co :profiles
      [:uuid :s]  ; Hash key of uuid-valued user reference, (:s => string type)
      {:throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
       :block? true})
    (far/create-table co :accounts
      [:uuid :s]  ; Hash key of uuid-valued user reference, (:s => string type)
      {:throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
       :block? true})
    (far/create-table co :sessions
      [:uuid :s]  ; Hash key of uuid-valued session reference, (:s => string type)
      {:range-key [:uref :s] ; uuid user reference as second component to primary key
       :throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
       :gsindexes [{:name "uref-and-token"
                    :hash-keydef [:uref :s]
                    :range-keydef [:token :s]
                    :projection :all
                    :throughput {:read 2 :write 2}}]
       :block? true})
    (far/create-table co :openid
      [:sub :s]  ; Hash key is the OpenID 'sub' claim (subject identifier, e.g. "google-oauth2|1234")
      {:throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
       :block? true
       :gsindexes [{:name "email-index"
                    :hash-keydef [:email :s]
                    :projection :keys-only
                    :throughput {:read 2 :write 2}}]})))

; (far/scan co :sessions)
; (cache-access-token "swizBbwU7cC7x123" {:sub "facebook|10208314583117362"})
; (pprint (far/query co :openid {:email [:eq "brian@mayalane.com"]} {:index "email-index"}))
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


;; These functions interact with DynamoDB as well as S3 objects
(defn rename-s3-object
  "Perform a copy of the current object to the new name followed by a delete.
   Apparently the aws command line tool's mv command uses this approach as well.
   If a failure occurs, return nil, otherwise return the new key."
  [bucket old-key new-key]
  (let [cred (dissoc co :endpoint)
        copy-result (try (s3/copy-object cred bucket old-key new-key)
                      (catch Exception ex
                        (str ex)))]
    (if-not (map? copy-result)
      (do
        (pprint (str "Error attempting to copy " old-key " to " new-key " in bucket " bucket ": " copy-result))
        nil)
      (try (s3/delete-object cred bucket old-key)
           new-key
        (catch Exception ex
          (do
            (pprint (str "Error deleting " old-key " from bucket " bucket ": " ex))
            nil))))))

(defn- rename-images
  "Execute a copy/delete to rename the object keys in the given images list"
  [uref old-sub new-sub item]
  (let [rename-keys (fn [idx image]
                      (let [old-key (:key image)
                            new-key (clojure.string/replace-first (:key image) old-sub new-sub)]
                        (when (not= new-key old-key)
                          ;; Rename the objects in both helodali-images and helodali-raw-images buckets
                          (do
                            (when (rename-s3-object "helodali-raw-images" old-key new-key)
                              (when (rename-s3-object "helodali-images" old-key new-key)
                                (update-item :artwork uref (:uuid item) [:images idx :key] new-key)))))))]
    (doall (map-indexed rename-keys (:images item)))))

(defn- rename-document
  "Execute a copy/delete to rename the object keys in the given document"
  [uref old-sub new-sub document]
  (let [old-key (:key document)
        new-key (clojure.string/replace-first (:key document) old-sub new-sub)]
    (when (not= new-key old-key)
      ;; Rename the objects in the helodali-documents bucket
      (do
        (pprint (str "Renaming document " old-key " to " new-key))
        (when (rename-s3-object "helodali-documents" old-key new-key)
          (update-item :documents uref (:uuid document) [:key] new-key))))))

(defn rename-user-s3-objects
  "Given a user's uuid and old/new sub values, look for all documents and images
   defined in the database and issue copy/delete requests to s3 to rename
   the objects with the old sub in the base of the object name to the new sub at the base.
   For example, given old=facebook|00001 and new=facebook|00002, rename
     facebook|00001/37093235-beae-4731-a4c0-b6307bc52c4b/f07cbe4a-bc65-44fd-bafc-7629ebd6cf8c/art.png to
     facebook|00002/37093235-beae-4731-a4c0-b6307bc52c4b/f07cbe4a-bc65-44fd-bafc-7629ebd6cf8c/art.png"
  [uuid old-sub new-sub]
  (let [artwork (far/query co :artwork {:uref [:eq uuid]})
        artwork-renamer (partial rename-images uuid old-sub new-sub)
        documents (far/query co :documents {:uref [:eq uuid]})
        document-renamer (partial rename-document uuid old-sub new-sub)]
    (doall (map (fn [item] (artwork-renamer item)) artwork))
    (doall (map (fn [item] (document-renamer item)) documents))))

(defn- associate-user-sub-and-uuid
  "A maintenance function to be used when a openid sub changes for an existing user.
   An openid item is assumed to exist for the new sub (which would have been created
   by a login to helodali). This function changes the uuid (uref) within the new openid
   item."
  [sub uuid]
  (pprint (far/update-item co :openid {:sub sub}
                           {:update-expr     (str "SET #uref = :val")
                            :expr-attr-names {"#uref" "uref"}
                            :expr-attr-vals  {":val" uuid}
                            :return          :all-new})))

(comment
  (associate-user-sub-and-uuid "facebook|10208723122690596" "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb")
  (rename-user-s3-objects "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb" "facebook|10208314583117362" "facebook|10208723122690596")
  (far/get-item co :press {:uref brianw :uuid "123123-222-123123-0001"})
  (def all (far/scan co :artwork))
  (far/query co :artwork {:uref [:eq brianw]}))
