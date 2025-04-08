(ns helodali.db
  (:require [taoensso.faraday :as far]
            [clojure.string :as str]
            [clj-uuid :as uuid]
            [java-time :as jt]
            [helodali.types :as types]
            [clojure.java.io :as io]
            [helodali.common :refer [log coerce-int coerce-long coerce-decimal-string keywordize-vals]]
            [clojure.pprint :refer [pprint]]
            [helodali.s3 :as s3])
  (:import (com.amazonaws.services.dynamodbv2.model ConditionalCheckFailedException)))

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

;; Batch size when calling batch-write-item to delete items from a table
(def delete-batch-size 25)

;; Session table item expiration time (time to cache access tokens in db): 72 hours
(def session-expiration-seconds (* 3600 72))

(defn- coerce-item
  "This looks for specific cases where we need to convert string values back to keywords and numbers to ints"
  ;; TODO: this method of defining each integer attribute is far from ideal. We should instead determine
  ;; the predicate to catch the integer type returned by DynamoDB and walk the map and coerce everything found.
  [table m]
  (condp = table
    :artwork (-> (assoc m :style (set (map keyword (:style m))))
                 (assoc :purchases (mapv #(coerce-int % [:price :total-commission-percent]) (:purchases m)))
                 (assoc :images (mapv #(-> %
                                           (assoc :metadata (coerce-int (:metadata %) [:density :size :width :height]))
                                           (assoc :palette (mapv int (:palette %)))) (:images m)))
                 (keywordize-vals [:type :status])
                 (coerce-int [:expenses :list-price :year :editions])
                 (assoc :instagram-media-ref (and (:instagram-media-ref m)
                                                  (-> (:instagram-media-ref m)
                                                      (coerce-int [:likes])
                                                      (assoc :media-type (keyword (:media-type (:instagram-media-ref m))))))))
    :contacts (keywordize-vals m [:role])
    :expenses (-> m
                  (keywordize-vals [:expense-type])
                  (coerce-decimal-string [:price]))
    :sessions (-> m
                  (coerce-long [:access-token-exp :access-token-iat]))
    :exhibitions (keywordize-vals m [:kind])
    :documents (coerce-int m [:size])
    :profiles (-> m
                  (coerce-int [:birth-year])
                  (assoc :degrees (mapv #(coerce-int % [:year]) (:degrees m)))
                  (assoc :awards-and-grants (mapv #(coerce-int % [:year]) (:awards-and-grants m)))
                  (assoc :lectures-and-talks (mapv #(coerce-int % [:year]) (:lectures-and-talks m)))
                  (assoc :residencies (mapv #(coerce-int % [:year]) (:residencies m))))
    m))

(defn get-openid-by-sub
  "Given an openid subject identifier (the 'sub' claim), resolve to an openid item
   found in the :openid table."
  [sub]
  (far/get-item co :openid {:sub sub}))

(defn get-profile-by-sub
  "Given an openid subject identifier (the 'sub' claim), resolve to a profile map
   by searching the openid table for the user's uuid and then getting the item
   from profiles. Return the openid item found and the profile map, an empty map
   if no profile is found."
  [sub]
  (when-let [openid-item (get-openid-by-sub sub)]
    (if (nil? (:uref openid-item))
      [openid-item {}]
      (let [profile (far/get-item co :profiles {:uuid (:uref openid-item)})]
        (if (nil? profile)
          [openid-item {}]
          [openid-item (coerce-item :profiles profile)])))))

(defn get-item-by-uref
  "Get the item in table by uref (user's uuid). This works for tables such as
   :accounts, :profiles, and :pages"
  [table uuid]
  (far/get-item co table {:uuid uuid}))

(defn query-by-uref
  "Query on items and clean results"
  ([table uref]
   (query-by-uref table uref {}))
  ([table uref opts]
   (map (partial coerce-item table) (far/query co table {:uref [:eq uref]} opts))))

(defn delete-item
  [table key-map]
  (far/delete-item co table key-map {:return :none}))

(defn delete-items
  "Delete, in batches, the given items from given table. The item-list
   should be of the form [{key} {key}]. E.g. For artwork [{:uref \"abc\" :uuid \"123\"}]"
  [table item-list]
  (loop [items item-list]
    (if (> (count items) 0)
      (do
        (pprint (str "Removing " (vec (take delete-batch-size items))))
        (far/batch-write-item co {table {:delete (vec (take delete-batch-size items))}})
        (recur (drop delete-batch-size items))))))

(defn delete-user
  "Delete everything except for portions of the :accounts needed for later processes
   (e.g. cleanup of public-pages feature)."
  [uref sub]
  (let [query-opts {:proj-expr "#uref, #uuid"
                    :expr-attr-names {"#uref" "uref" "#uuid" "uuid"}}]
    (delete-items :artwork (far/query co :artwork {:uref [:eq uref]} query-opts))
    (delete-items :documents (far/query co :documents {:uref [:eq uref]} query-opts))
    (delete-items :exhibitions (far/query co :exhibitions {:uref [:eq uref]} query-opts))
    (delete-items :groupings (far/query co :groupings {:uref [:eq uref]} query-opts))
    (delete-items :contacts (far/query co :contacts {:uref [:eq uref]} query-opts))
    (delete-items :expenses (far/query co :expenses {:uref [:eq uref]} query-opts))
    (delete-items :press (far/query co :press {:uref [:eq uref]} query-opts))
    (delete-item :profiles {:uuid uref})
    (delete-item :pages {:uuid uref})
    (delete-item :accounts {:uuid uref})
    (delete-item :openid {:sub sub})))


(defn initialize-db
  "Given an openid claims map, with :sub key, resolve the user against :openid and :profiles
   tables and then construct the user's app-db for the client. Also take this opportunity to
   make sure our openid table item is in sync with the provided claims map by updating the
   email or name in the database."
  [sub session]
  (if (nil? sub)
    {}
    (let [[openid-item profile] (get-profile-by-sub sub)
          uref (:uuid profile)
          account (get-item-by-uref :accounts uref)]
      ;; openid-item contains :sub, :uref, :name, and :email keys.
      {:artwork (query-by-uref :artwork uref)
       :documents (query-by-uref :documents uref)
       :exhibitions (query-by-uref :exhibitions uref)
       :groupings (query-by-uref :groupings uref)
       :contacts (query-by-uref :contacts uref)
       :expenses (query-by-uref :expenses uref)
       :press (query-by-uref :press uref)
       :profile profile
       :authenticated? true
       :initialized? true
       :access-token (:token session)
       :access-token-exp (:access-token-exp session)
       :id-token (:id-token session)
       :pages (far/get-item co :pages {:uuid uref})
       :account (-> account
                    (dissoc :instagram-access-token)
                    (assoc :instagram-user (select-keys (:instagram-user account) [:bio :website :full_name :profile_picture :username])))
       :userinfo openid-item})))

(defn- undash
  "Replace '-' with 'D' in given string"
  [s]
  (str/replace s #"-" "D"))

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
    (map? in) (into {} (filter (fn [[_ v]]
                                (if (or (nil? v) (and (string? v) (empty? v)) (and (coll? v) (empty? v)))
                                 false
                                 true)) in))
    (vector? in) (mapv filter-out-empty in)
    :else in))

(defn walk-cleaner
  "Walk the input and:
    - dissoc nil, \"\", [] and #{} valued keys, ignore non-map input"
  [in]
  (cond
      (map? in) (clojure.walk/walk (fn [[k v]] [k (filter-out-empty v)]) identity (filter-out-empty in))
      :else in))

(defn apply-attribute-change
  "Update artwork, press, exhibitions, documents, expenses, or contacts table. The 'path'
   argument is a keyword or vector path into the item in given 'table'. E.g. :notes or
   [:purchases 1 :date]
  If the val is nil or an empty set, then perform a REMOVE of the attribute as opposed to a SET of a nil value.

  A condition expression is attached to all updates to ensure that the item exists, otherwise an inchoate item
  is created. An attribute is taken from the keys map for use in the cond-expr."
  [table primary-key-map path val]
  (let [[attr-expression expression-map] (convert-path-to-expression-attribute path)
        primary-key-name (name (first (keys primary-key-map)))
        expression-map (assoc expression-map (str "#" primary-key-name) primary-key-name)
        val (walk-cleaner val)
        change (if (or (nil? val) (and (set? val) (empty? val)))
                  {:update-expr     (str "REMOVE " attr-expression)
                   :expr-attr-names expression-map
                   :cond-expr (str "attribute_exists(#" primary-key-name ")")
                   :return          :all-new}
                  {:update-expr     (str "SET " attr-expression " = :val")
                   :expr-attr-names expression-map
                   :expr-attr-vals  {":val" val}
                   :cond-expr (str "attribute_exists(#" primary-key-name ")")
                   :return          :all-new})]
    (log "Performing change" change)
    (try
      (far/update-item co table primary-key-map change)
      (catch ConditionalCheckFailedException _ (do (pprint "Attempt to update a non-existing item.")
                                                   {}))
      (catch Exception e (do (log "Unable to update item" e)
                             {}))))) ;; TODO: Need to respond with error to client.

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

;; Below is an example of db-changes computed above
(comment {:expr-attr-names {"#version" "version", "#processing" "processing"},
          :expr-attr-vals {":val0" "2562f477-8a3c-4a11-a7f0-921298595df2", ":val1" true},
          :return :all-new,
          :update-expr "SET #processing = :val1, #version = :val0 ",
          :cond-expr "attribute_exists(pages.uuid)"})

(defn apply-attribute-changes
  "Update artwork, press, exhibitions, documents, expenses, or contacts table. The 'changes'
   argument is a map of changes to apply with keys representing paths, or attribute names, see DynamoDB
   UpdateExpressions (http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.Modifying.html).
   Any nil values will result in REMOVE UpdateExpression.

   A condition expression is attached to all updates to ensure that the item exists, otherwise an inchoate item
   is created. An attribute is taken from the keys map for use in the cond-expr."
  [table primary-key-map changes]
  (let [indexed-changes (zipmap (range (count changes)) changes) ;; Yields {0 [[:purchases 0 :date] "2001-03-14"], 1 [[:name] "Bo"]}
        primary-key-name (name (first (keys primary-key-map)))
        db-changes (-> (reduce-kv build-db-changes {} indexed-changes)
                       ;; Add the primary-key to the list of expression attributes, e.g. {"#uuid" "uuid"}
                       (assoc-in [:expr-attr-names (str "#" primary-key-name)] primary-key-name))
        update-expression (str
                            (when (:set-update-expr db-changes)
                              (str "SET " (str/join ", " (:set-update-expr db-changes)) " "))
                            (when (:remove-update-expr db-changes)
                              (str "REMOVE " (str/join ", " (:remove-update-expr db-changes)))))
        db-changes (-> (assoc db-changes :return :all-new)
                      (assoc :update-expr update-expression)
                      (assoc :cond-expr (str "attribute_exists(#" primary-key-name ")"))
                      (dissoc :set-update-expr)
                      (dissoc :remove-update-expr))]
    (log "Performing changes" db-changes)
    (if (nil? update-expression)
      (log "No changes to apply for" changes)
      (try
        (far/update-item co table primary-key-map db-changes)
        (catch ConditionalCheckFailedException _ (do (pprint "Attempt to update a non-existing item.")
                                                     {}))
        (catch Exception e (do (log "Unable to apply updates to item" e)
                               {})))))) ;; TODO: Need to respond with error to client.

(defn update-item
  "Update items in artwork, press, exhibitions, documents, expenses, or contacts tables. The method of building
   the DynamoDB changeset depends on whether we are called with a single attribute change (path == [path to attribute])
   or multiple attribute changes within an item (path == nil and val is keyed with attribute paths).

   The response should be a map of the form {table [changes]} where table is the (keyword) table name and changes is a vector
   2-tuple [<uuid of item> [path value]] where path is a vector that points into the item or can be nil to represent
   a whole-item overwrite on the client side. E.g. {:artwork [[d4544181-016f-11e9-9cfb-335dc3cb2f41 [[:year] 2017]]]}"
  [table uref uuid path val]
  (if (nil? path)
    {table [[uuid [nil (coerce-item table (apply-attribute-changes table {:uref uref :uuid uuid} val))]]]}
    {table [[uuid [nil (coerce-item table (apply-attribute-change table {:uref uref :uuid uuid} path val))]]]}))

(defn update-user-table
  "Update a user table, such as :profiles or :pages. The method of building
   the DynamoDB changeset depends on whether we are called with a single attribute change (path == [path to attribute])
   or multiple attribute changes within an item (path == nil and val is keyed with attribute paths)

   See 'update-item' for the response format."
  [table uuid path val]
  (if (nil? path)
    {table (coerce-item table (apply-attribute-changes table {:uuid uuid} val))}
    {table (coerce-item table (apply-attribute-change table {:uuid uuid} path val))}))

(defn update-generic
  "Update a table based on given key-map, single attribute change defined in path and val.
   See 'update-item' for the response format."
  [table key-map path val]
  (let [update-result (apply-attribute-change table key-map path val)]
    {table (coerce-item table update-result)}))

(defn refresh-item-path
  "Fetch item from artwork, press, exhibitions, documents, expenses, or contacts table. The 'path'
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
  "Fetch image map from artwork table the image given by item-uuid and image-uuid."
  ;; TODO: think about optimizing the get-item call with projections
  [uref item-uuid image-uuid]
  (pprint (str "refresh-image-data uref/item-uuid/image-uuid: " uref "/" item-uuid "/" image-uuid))
  (let [item (coerce-item :artwork (far/get-item co :artwork {:uref uref :uuid item-uuid}))
        image (filter #(= image-uuid (:uuid %)) (:images item))]
    {:apply-image-refresh (first image)}))

(defn create-item
  "Create a new item in given table. Drop any nil or #{} valued attributes, this
   requires walking the collection."
  [table item]
  (let [item (walk-cleaner item)]
    (pprint (str "creating cleaned item: " item))
    ;; Create the item and respond with nothing - no need to send the item to the client.
    (far/put-item co table item {:return :none})))

(defn match-hashtag
  "Given a list of hashtag strings (such as from the caption of an Instagram media post) and a list of keywords,
   eturn the intersection. If the intersection is empty, return the default set."
  [tags keywords default]
  (let [names (set (map #(-> % name str/lower-case) keywords))
        common (clojure.set/intersection (set (map str/lower-case tags)) names)]
    (if (empty? common)
      default
      ;; Convert back to keyword before returning
      (map keyword common))))

(defn create-artwork-from-instragram
   "Build an artwork item from the given instagram media, copy the image from instagram to our S3
    bucket and return updates to both :artwork and :instagram-media portions of the client's app-db.

    The call to S3 putObject will warn 'WARNING: No content length specified for stream data. Stream
    contents will be buffered in memory and could result in out of memory errors' as we are not
    providing a metadata map with {:content-length N} to the s3/put-object invocation. The Instagram
    api does not provide the content size of images so we would have to buffer the image ourselves
    in order to determine this. Since Instagram images tend to be smallish, we'll let this slide for now."
  [uref cognito-identity-id media]
  (let [artwork-uuid (str (uuid/v1))
        image-uuid (str (uuid/v1))
        caption (or (:caption media) "")
        year-matched (re-find #"[^\d]([12]\d\d\d)[^\d]" caption) ;; Might look like [" 2018," "2018"] or nil
        dimensions-matched (re-find #"(?i)[^\d]*(\d+[\"\']?\s*[xXby]+\s*\d+[\"\']?\s*(inches|in|feet|ft|cm|meters|m)?)" caption)
        year (if year-matched
               (Integer/parseInt (str/trim (second year-matched)))
               (jt/as (jt/local-date) :year))
        item (cond-> {:uref uref
                      :uuid artwork-uuid
                      :created (:created media)
                      :description caption
                      :title (str/trim (first (str/split caption #"[.\-,]" 2)))
                      :year year
                      :status :for-sale
                      :type (first (match-hashtag (:tags media) (keys types/media) #{:mixed-media}))
                      :style (match-hashtag (:tags media) (keys types/styles) #{})
                      :series false
                      :list-price 0
                      :expenses 0
                      :editions 0
                      :sync-with-instagram true
                      :instagram-media-ref media}
                     dimensions-matched (merge {:dimensions (str/trim (second dimensions-matched))}))
        url (java.net.URL. (:image-url media))
        filename (-> (.getPath url)
                    (str/split #"/")
                    (last))
        object-key (str cognito-identity-id "/" artwork-uuid "/" image-uuid "/" filename)
        item (walk-cleaner item)]
    (with-open [ig-is (io/input-stream url)]
      ;; Create the database item and copy the image to S3
      (far/put-item co :artwork item)
      (s3/put-object "helodali-raw-images" object-key ig-is)
      ;; Return an updated version of the :instagram-media-ref item as well as the new :artwork item
      {:artwork [[artwork-uuid [nil (assoc item :images [{:uuid image-uuid :processing true}])]]]
       :instagram-media [[(:instagram-id media) [nil (assoc media :artwork-uuid artwork-uuid)]]]})))

(defn- sync-userinfo
  "Update our openid item in the database if the given userinfo map disagrees. Note the conversion of :email_verified
   to :email-verified."
  [userinfo openid-item]
  (let [sub (:sub openid-item)
        changes (cond-> {}
                        (not= (:email openid-item) (:email userinfo)) (assoc :email (:email userinfo))
                        (not= (:name openid-item) (:name userinfo)) (assoc :name (:name userinfo))
                        (not= (:email-verified openid-item) (:email_verified userinfo)) (assoc :email-verified (:email_verified userinfo)))]
    (when (not-empty changes)
      (apply-attribute-changes :openid {:sub sub} changes))))

(defn create-user-if-necessary
  "Look for the given sub as a user of our application and if she does not exist yet, create her."
  [userinfo]
  (when-not (nil? (:sub userinfo))
    (let [openid-item (far/get-item co :openid {:sub (:sub userinfo)})]
      (if (nil? (:uref openid-item))
        (let [uuid (str (uuid/v1))
              created (jt/format "yyyy-MM-dd" (jt/zoned-date-time))
              ;; Use name from external IdP or username from Cognito native accounts
              name-or-username (or (:cognito:username userinfo) (:name userinfo))]
          (pprint (str "Creating user account for " (:sub userinfo)))
          (far/put-item co :accounts {:uuid uuid :created created})
          (far/put-item co :pages {:uuid uuid :enabled false})
          (far/put-item co :profiles {:uuid uuid :name name-or-username :email (:email userinfo)
                                      :created created :email-verified (:email_verified userinfo)})
          (far/put-item co :openid {:uref uuid :sub (:sub userinfo) :email (:email userinfo)}))
        (sync-userinfo userinfo openid-item)))))

(defn cache-access-token
  "Store the access, id, and refresh tokens in the :sessions table along with an expiration
   time (expire-at) which instructs when DynamoDB should expunge the item. Note the difference
   between the DynamoDB item expiration (expire-at) and the access_token expiration, the latter
   should be sooner than the former of course. The access_token expiration is defined in the
   token and as already been pulled out and placed in the incoming tokens map in :access-token-exp.

   Return the newly created session's uuid.

   NB: the tokens map is a response form an IdP containing keys with underscores: :access_token."
  [tokens sub]
  (when-let [openid-item (get-openid-by-sub sub)]
    (let [uref (:uref openid-item)
          session-uuid (str (uuid/v1))
          tn (jt/with-zone (jt/zoned-date-time) "UTC")]
      (far/put-item co :sessions {:uuid session-uuid
                                  :uref uref
                                  :token (:access_token tokens)
                                  :access-token-exp (:access-token-exp tokens)
                                  :access-token-iat (:access-token-iat tokens)
                                  :refresh (:refresh_token tokens)
                                  :id-token (:id_token tokens)
                                  :sub sub
                                  :expire-at (+ session-expiration-seconds (.getEpochSecond (.toInstant tn)))
                                  :ts (jt/format tn)})
      session-uuid)))

(defn query-session
  "Check if the given uref and access-token are in agreement in the :sessions table,
   i.e. there is an item that matches the pair of values. If so, return the session."
  [uref access-token]
  (if (or (empty? uref) (empty? access-token))
    {}
    (let [sessions (far/query co :sessions {:uref [:eq uref] :token [:eq access-token]}
                              {:index "uref-token-index" :limit 1})]
      ;; The projection will include :uuid, :uref, :token, :sub, :ts, :refresh,
      ;; :access-token-exp, and :access-token-iat
      (if sessions
        (coerce-item :sessions (first sessions))
        {}))))

(defn get-session
  "Retrieve the session table item given the session uuid."
  [uuid]
  (let [session (far/get-item co :sessions {:uuid uuid})]
    (if session
      (coerce-item :sessions session)
      {})))

;;
;; Maintenance functions not invoked by the helodali server-side
;;

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

(defn table-creation
  "This recreates the database and will delete all existing data."
  []
  (let [tables (far/list-tables co)]
    (doall (map #(far/delete-table co %) tables))
    (doall (map #(create-table %) [:press :exhibitions :groupings :documents :artwork :contacts :expenses]))
    (far/create-table co :profiles
      [:uuid :s]  ; Hash key of uuid-valued user reference, (:s => string type)
      {:throughput {:read 2 :write 2} ; Read & write capacity (units/sec)
       :block? true})
    (far/create-table co :pages [:uuid :s] {:throughput {:read 2 :write 2} :block? true})
    (far/create-table co :accounts [:uuid :s] {:throughput {:read 2 :write 2} :block? true})
    (far/create-table co :sessions
      [:uuid :s]  ; Hash key of uuid-valued session reference, (:s => string type)
      {:range-key [:uref :s] ; uuid user reference as second component to primary key
       :throughput {:read 4 :write 2} ; Read & write capacity (units/sec)
       :gsindexes [{:name "uref-token-index"
                    :hash-keydef [:uref :s]
                    :range-keydef [:token :s]
                    :projection [:token :uuid :refresh]  ;; Note this list of attributes is in addition to the key (:uref)
                    :throughput {:read 4 :write 2}}]
       :block? true})
    (far/create-table co :openid
      [:sub :s]  ; Hash key is the OpenID 'sub' claim (subject identifier, e.g. "google-oauth2|1234")
      {:throughput {:read 4 :write 2} ; Read & write capacity (units/sec)
       :block? true
       :gsindexes [{:name "identity-id-uref-index"
                    :hash-keydef [:identity-id :s]
                    :projection :keys-only
                    :throughput {:read 4 :write 2}}]})))

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
  (let [copy-result (try (s3/copy-object bucket old-key bucket new-key)
                      (catch Exception ex
                        (str ex)))]
    (if-not (map? copy-result)
      (do
        (pprint (str "Error attempting to copy " old-key " to " new-key " in bucket " bucket ": " copy-result))
        nil)
      (try (Thread/sleep 800)
           (s3/delete-objects bucket [old-key])
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
                            new-key (str/replace-first (:key image) old-sub new-sub)]
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
        new-key (str/replace-first (:key document) old-sub new-sub)]
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

;; The tables which have a user's uuid defined in a :uref indexed field
(def tables-indexed-by-uref
  [:sessions :press :openid :expenses :exhibitions :groupings :documents :contacts :artwork])

;; The tables which have a user's uuid defined in a :uuid indexed field
(def tables-indexed-by-uuid
  [:profiles :pages :accounts])


(comment
  (associate-user-sub-and-uuid "facebook|10208723122690596" "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb")
  (rename-user-s3-objects "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb" "facebook|10208314583117362" "facebook|10208723122690596")
  (far/get-item co :press {:uref brianw :uuid "123123-222-123123-0001"})
  (def all (far/scan co :artwork))
  (far/query co :artwork {:uref [:eq brianw]}))
