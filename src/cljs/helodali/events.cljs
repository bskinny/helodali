(ns helodali.events
  (:require
    [ajax.core :as ajax]
    [helodali.common :refer [coerce-int coerce-decimal empty-string-to-nil fix-date parse-date unparse-date unparse-datetime]]
    [helodali.spec :as hs] ;; Keep this here even though we refer to the namespace directly below
    [helodali.misc :refer [expired? generate-uuid find-element-by-key-value find-item-by-key-value
                           remove-vector-element into-sorted-map trunc search-item-by-key-value]]
    [helodali.routes :refer [route]]
    [helodali.db :refer [s3-key-for-bucket]]
    [cljs-time.core :as ct]
    [cljs-time.coerce :refer [from-long]]
    [cljs.reader]
    [cljs.pprint :refer [pprint]]
    [reagent.core :as r]
    [re-frame.core :refer [reg-event-db reg-event-fx reg-fx inject-cofx path trim-v reg-cofx
                           after debug dispatch]]
    [day8.re-frame.http-fx]
    [cljsjs.aws-sdk-js]
    [cljs.spec.alpha :as s]))

(def ^:private app-db-undo (r/atom nil))

;; A request timeout of 10 seconds
(def TIMEOUT 10000)

(defn next-id
  "Assumes map is a sorted-map"
  [m]
  ((fnil inc 0) (last (keys m))))

(defn filter-out-placeholders
  "Return the db with the placeholder items filtered out."
  [db]
  (-> db
      (assoc :artwork (dissoc (:artwork db) 0))
      (assoc :contacts (dissoc (:contacts db) 0))
      (assoc :exhibitions (dissoc (:exhibitions db) 0))
      (assoc :documents (dissoc (:documents db) 0))
      (assoc :press (dissoc (:press db) 0))
      (assoc :expenses (dissoc (:expenses db) 0))))

(defn check-and-throw
  "throw an exception if db doesn't match the spec."
  [a-spec app-db]
  ;; Suppress any validation errors in the 0-indexed "placeholder" items which are used during
  ;; new item creation. If a user browses away from a new item creation, the spec for the placeholder
  ;; item will likely not be valid (e.g. no :name value).
  (let [db (filter-out-placeholders app-db)]
    (when-not (s/valid? a-spec db)
      (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {})))))

;; An interceptor which looks for expired AWS credentials and sets the app-db key :refresh-aws-creds? appropriately
;; When :refresh-aws-creds? is true, the main-panel will react to it and start a refresh process.
(def check-aws-credentials
  (re-frame.core/->interceptor
    :id      :check-aws-credentials
    :before  (fn [context]
               (let [db (get-in context [:coeffects :db])
                     expired? (ct/after? (ct/now) (ct/plus (:aws-creds-created-time db) (ct/hours 1)))]
                 (if expired?
                   (update-in context [:effects :db :refresh-aws-creds?] true)
                   context)))))

;; This interceptor is run after an event handler has finished and validates app-db against spec
(def check-spec-interceptor (after (partial check-and-throw :helodali.spec/db)))

;; the chain of interceptors we use for all handlers that manipulate the db
(def interceptors [check-spec-interceptor               ;; ensure the spec is still valid
                   (when ^boolean js/goog.DEBUG debug)  ;; look in your browser console for debug logs
                   trim-v])                             ;; removes first (event id) element from the event vec

;; Some effects handlers check spec themselves before submitting requests to the server
(def manual-check-spec [(when ^boolean js/goog.DEBUG debug)
                        trim-v])

(defn- add-message
  "Add a message to :messages (stack of warnings in the UI). If id is nil, generate
   a uuid as the id. The :messages value in app-db is a map keyed by labels which
   provide a means for clearing messages. See the :clear-message event."
  [db id msg]
  (pprint (str "add-message: " id ": " msg))
  (let [k (or id (generate-uuid))]
    (assoc-in db [:messages k] msg)))

(defn- clear-message
   "Remove the message with given id"
  [db id]
  (update-in db [:messages] dissoc id))

(defn- csrf-token-request [on-success]
  {:http-xhrio {:method          :get
                :uri             "/csrf-token"
                :timeout         TIMEOUT
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      on-success
                :on-failure      [:bad-result {} false]}})

(defn- safe-unparse-date
  [v]
  (if (instance? js/goog.date.UtcDateTime v)
    (unparse-date v)
    v))

(defn- safe-unparse-datetime
  [v]
  (if (instance? js/goog.date.UtcDateTime v)
    (unparse-datetime v)
    v))

(reg-event-fx
  :initialize-db
  [(inject-cofx :local-store-tokens)]
  (fn [{:keys [db local-store-tokens]} _]
    (enable-console-print!)
    (merge (csrf-token-request [:update-db-from-result (fn [db] true)])
           {:db (-> helodali.db/default-db
                   (assoc :access-token (:access-token local-store-tokens))
                   (assoc :id-token (:id-token local-store-tokens)))})))

(reg-cofx
  :local-store-tokens
  (fn [cofx _]
      "Read in items from localstore, into a map we can merge into app-db. If we are missing the id-token
       then remove the access-token as we need to reauthenticate"
    (let [access-token (.getItem js/localStorage "helodali.access-token")
          id-token (.getItem js/localStorage "helodali.id-token")]
      (if (and (empty? id-token) (not (empty? access-token)))
        (.removeItem js/localStorage "helodali.access-token");
        (assoc cofx :local-store-tokens
               {:access-token (.getItem js/localStorage "helodali.access-token")
                :id-token (.getItem js/localStorage "helodali.id-token")})))))

(defn- current-signed-urls
  "Generate a map of the signed-urls for artwork and document items in the app-db."
  [db]
  ;; TODO: Verify that the below works when multiple images per artwork is supported.
  (let [images (->> (vals (:artwork db))
                    (reduce (fn [a b] (into a (:images b))) [])
                    (filter #(or (not (empty? (:signed-thumb-url %))) (not (empty? (:signed-raw-url %))) (not (empty? (:signed-image-url %)))))
                    (map (fn [image] {(:uuid image) {:signed-raw-url (:signed-raw-url image)
                                                     :signed-raw-url-expiration-time (safe-unparse-datetime (:signed-raw-url-expiration-time image))
                                                     :signed-thumb-url (:signed-thumb-url image)
                                                     :signed-thumb-url-expiration-time (safe-unparse-datetime (:signed-thumb-url-expiration-time image))
                                                     :signed-image-url (:signed-image-url image)
                                                     :signed-image-url-expiration-time (safe-unparse-datetime (:signed-image-url-expiration-time image))}}))
                    (reduce into {}))
        documents (->> (vals (:documents db))
                       (filter #(not (empty? (:signed-raw-url %))))
                       (map (fn [document] {(:uuid document) {:signed-raw-url (:signed-raw-url document)
                                                              :signed-raw-url-expiration-time (safe-unparse-datetime (:signed-raw-url-expiration-time document))}}))
                       (reduce into {}))]
    (merge images documents)))

(reg-cofx
  :local-store-signed-urls
  (fn [cofx _]
      "Read in the map of signed-urls which is keyed by uuids of images and documents"
    (let [urls (into {} (some->> (.getItem js/localStorage "helodali.signed-urls")
                                 (cljs.reader/read-string)))]
      (assoc cofx :local-store-signed-urls urls))))

(defn- apply-local-storage-change
  [{:keys [k v]}]
  ;; If value (v) is nil, remove the item, otherwise set the value
  (if (empty? v)
    (.removeItem js/localStorage k)
    (.setItem js/localStorage k (str v))))

(reg-fx
  :sync-to-local-storage
  (fn [requests]
    ;; requests is a vector of {:k key-name :v value} maps
    (doall (map #(apply-local-storage-change %) requests))))

(reg-fx
  :route-client
  (fn [{:keys [route-name args]}]
    (route route-name args)))

(defn- apply-document-signed-urls
  "Traverse the cached signed-urls and attach them to documents in the given list (not
   sorted-map) of documents"
  [documents signed-urls]
  (if (empty? signed-urls)
    documents
    (let [update-documents (fn [document]
                             (if-let [url (get signed-urls (:uuid documents))]
                               (merge document {:signed-raw-url-expiration-time
                                                  (parse-date :date-time (:signed-raw-url-expiration-time url))
                                                :signed-raw-url (:signed-raw-url url)})
                               document))]
       (map #(update-documents %) documents))))

(defn- apply-artwork-signed-urls
  "Traverse the cached signed-urls and attach them to images in the given list (not
   sorted-map) of artwork"
  [artwork signed-urls]
  (if (empty? signed-urls)
    artwork
    (let [update-images (fn [item]
                          (let [images (map (fn [image]
                                              (if-let [urls (get signed-urls (:uuid image))]
                                                (merge image {:signed-thumb-url-expiration-time
                                                                (parse-date :date-time (:signed-thumb-url-expiration-time urls))
                                                              :signed-thumb-url (:signed-thumb-url urls) ;; TODO: check expiration?
                                                              :signed-image-url-expiration-time
                                                              (parse-date :date-time (:signed-image-url-expiration-time urls))
                                                              :signed-image-url (:signed-image-url urls)
                                                              :signed-raw-url-expiration-time
                                                                (parse-date :date-time (:signed-raw-url-expiration-time urls))
                                                              :signed-raw-url (:signed-raw-url urls)})
                                                image))
                                            (:images item))]
                            (assoc item :images (apply vector images))))]
       (map #(update-images %) artwork))))

(defn- fix-response
  "Comb through a response from the server and fix problematic formats such as dates. The resp is expected to be a map
   representing the entire app-db."
  [resp]
  (let [fixer (partial fix-date :parse)
        has (partial contains? resp)]
    (cond-> resp
        (has :artwork) (assoc :artwork (->> (:artwork resp)
                                            (fixer :created)
                                            (map #(assoc % :instagram-media-ref (and (:instagram-media-ref %)
                                                                                     (first (fixer :created [(:instagram-media-ref %)])))))
                                            (map #(assoc % :purchases (fixer :date (:purchases %))))))
        (has :account) (assoc :account (if (:account resp)
                                         (assoc-in (:account resp) [:created] (parse-date :date (get-in resp [:account :created])))))
        (has :press) (assoc :press (->> (:press resp)
                                        (fixer :created)
                                        (fixer :publication-date)))
        (has :contacts) (assoc :contacts (->> (:contacts resp)
                                              (fixer :created)))
        (has :expenses) (assoc :expenses (->> (:expenses resp)
                                              (map (fn [m] (coerce-decimal m [:price])))
                                              (fixer :created)
                                              (fixer :date)))
        (has :documents) (assoc :documents (->> (:documents resp)
                                                (fixer :created)
                                                (fixer :last-modified)))
        (has :exhibitions) (assoc :exhibitions (->> (:exhibitions resp)
                                                    (fixer :created)
                                                    (fixer :begin-date)
                                                    (fixer :end-date)))
        ;; Convert the unix time :created to cljs-time objects
        (has :instagram-media) (assoc :instagram-media (mapv #(assoc % :created (from-long (get % :created))) (:instagram-media resp))))))

(reg-event-fx
  :initialize-db-from-result
  [(inject-cofx :local-store-signed-urls)]
  (fn [{:keys [db local-store-signed-urls]} [_ result]]
    ;; Some values in the items need coercion back to DateTime and keyword syntax.
    (when (not (empty? result))
      (let [resp (fix-response result)
            app-db (-> db
                       (assoc :artwork (-> (map #(merge (helodali.db/default-artwork) %) (:artwork resp))
                                           (apply-artwork-signed-urls local-store-signed-urls)
                                           (into-sorted-map)))
                       (assoc :exhibitions (into-sorted-map (map #(merge (helodali.db/default-exhibition) %) (:exhibitions resp))))
                       (assoc :documents (-> (map #(merge (helodali.db/default-document) %) (:documents resp))
                                             (apply-document-signed-urls local-store-signed-urls)
                                             (into-sorted-map)))
                       (assoc :contacts (into-sorted-map (map #(merge (helodali.db/default-contact) %) (:contacts resp))))
                       (assoc :expenses (into-sorted-map (map #(merge (helodali.db/default-expense) %) (:expenses resp))))
                       (assoc :press (into-sorted-map (map #(merge (helodali.db/default-press) %) (:press resp))))
                       (assoc :profile (:profile resp))
                       (assoc :pages (:pages resp))
                       (assoc :account (:account resp))
                       (assoc :userinfo (:userinfo resp))
                       (assoc :access-token (:access-token resp))
                       (assoc :id-token (:id-token resp))
                       (assoc :authenticated? (:authenticated? resp))
                       (assoc :initialized? (:initialized? resp))
                       (assoc :display-type (if (:display-type resp) (:display-type resp) (:display-type db))) ;; Set display-type if it is present in the response
                       (assoc :instagram-media (and (:instagram-media resp) (into-sorted-map (:instagram-media resp))))
                       (assoc :initialized? true))]
        {:db app-db
         :sync-to-local-storage [{:k "helodali.access-token" :v (:access-token resp)}
                                 {:k "helodali.id-token" :v (:id-token resp)}]
         :route-client {:route-name helodali.routes/home :args {}}}))))

;; Update top-level app-db keys if supplied predicate evaluates true. The result can come from the server in the
;; form [type val] with type points to a top-level key into app-db and val is the whole item replacement. The result
;; can also be a map containing top-level keys which should be replaced in app-db. Note: For the former case, we
;; must make one adjustment to the incoming table name, which is plural on the server and singular here in app-dn,
;; e.g. :profiles -> :profile, :accounts -> :account. Note that :pages is a uref based table but the keyword is
;; plural on both server and client.
(reg-event-db
  :update-db-from-result
  manual-check-spec
  (fn [db [predicate-fx result]]
    (if (predicate-fx db)
      (if (map? result)
         (merge db result)
         (let [[type item] result
               ;; Fix table name if necessary.
               type (cond-> = type
                            :profiles :profile
                            :accounts :account
                            type)]
           (assoc db type item)))
      db)))

;; Like above but without the predicate check and with a retry function to dispatch
(reg-event-fx
  :update-csrf-token-and-retry
  manual-check-spec
  (fn [{:keys [db]} [partial-retry-fx result]]
    (let [db (merge db result)] ;; Merge in new csrf-token
      {:db db
       :dispatch [:retry-request #(partial-retry-fx db)]})))

(defn- cleaner
  [in]
  (-> in
    (safe-unparse-date)
    (empty-string-to-nil)))

(defn- walk-cleaner
  "Walk the input and:
    - convert Date objects to strings
    - convert empty strings to nil
    - remove map keys that we do not want visiting the server, such as :images from :artwork"
  [in]
  (cond
    (map? in) (let [in (dissoc in :editing :images)]
                (clojure.walk/walk (fn [[k v]] [k (walk-cleaner v)]) identity in))
    (vector? in) (mapv walk-cleaner in)
    (set? in) (set (map walk-cleaner in))
    :else (cleaner in)))

;; Replace the local app-db with what is defined in 'db' and apply changes to server database
;; 'path' with value 'val'. 'path' may point to an item or a specific attribute.
(defn- update-fx
  [path val is-retry? db]
  (let [_ (check-and-throw :helodali.spec/db db)
        type (first path)
        id (second path)
        inside-item-path (rest (rest path))  ;; hop over type and 'id' which only exists on the client app-db
        val (walk-cleaner val)
        retry-fx (or is-retry? (partial update-fx path val true))
        fx (if is-retry? {} {:db db})]
    ;; Submit change to server for all updates except those to the placeholder item in the client, which
    ;; does not yet exist on the server. Also skip the update if the update is on an item, as a whole, and
    ;; the set of changes, val, is empty
    (if (or (= id 0) (and (empty? inside-item-path) (empty? val)))
      fx
      (merge fx {:http-xhrio {:method          :post
                              :uri             "/update-item"
                              :params          {:uref (get-in db [type id :uref])
                                                :uuid (get-in db [type id :uuid])
                                                :table type :path inside-item-path :val val
                                                :access-token (:access-token db)}
                              :headers         {:x-csrf-token (:csrf-token db)}
                              :timeout         TIMEOUT
                              :format          (ajax/transit-request-format {})
                              :response-format (ajax/transit-response-format {:keywords? true})
                              :on-success      [:update-items]
                              :on-failure      [:bad-result {} retry-fx]}}))))

;; Similar to above but restricted to the app-db's :profile or :pages, which results to writes against
;; the :profiles and :pages tables on the server. 'path' should be path within :pages or :profile.
(defn- update-user-table-fx
  [table path val is-retry? db]
  (let [_ (check-and-throw :helodali.spec/db db)
        retry-fx (or is-retry? (partial update-user-table-fx path val true))
        val (walk-cleaner val)
        fx (if is-retry? {} {:db db})]
    (merge fx {:http-xhrio {:method          :post
                            :uri             "/update-user-table"
                            :params          {:uuid (get-in db [:profile :uuid])
                                              :table table :path path :val val
                                              :access-token (:access-token db)}
                            :headers         {:x-csrf-token (:csrf-token db)}
                            :timeout         TIMEOUT
                            :format          (ajax/transit-request-format {})
                            :response-format (ajax/transit-response-format {:keywords? true})
                            :on-success      [:update-db-from-result (fn [db] true)]
                            :on-failure      [:bad-result {} retry-fx]}})))

(defn- create-fx
  [table item is-retry? db]
  (let [_ (check-and-throw :helodali.spec/db db)
        item (-> item
                (walk-cleaner)
                (assoc :uref (get-in db [:profile :uuid]))
                (dissoc :editing))
        retry-fx (or is-retry? (partial create-fx table item true))
        fx (if is-retry? {} {:db db})]
    (merge fx {:http-xhrio {:method          :post
                            :uri             "/create-item"
                            :params          {:table table :item item :access-token (:access-token db)}
                            :headers         {:x-csrf-token (:csrf-token db)}
                            :timeout         TIMEOUT
                            :format          (ajax/transit-request-format {})
                            :response-format (ajax/transit-response-format {:keywords? true})
                            :on-success      [:update-db-from-result (fn [db] true)]
                            :on-failure      [:bad-result {} retry-fx]}})))

(defn- delete-fx
  [table item is-retry? db]
  (let [_ (check-and-throw :helodali.spec/db db)
        retry-fx (or is-retry? (partial delete-fx table item true))
        fx (if is-retry? {} {:db db})]
    (merge fx {:http-xhrio {:method          :post
                            :uri             "/delete-item"
                            :params          {:table table :uref (:uref item) :uuid (:uuid item) :access-token (:access-token db)}
                            :headers         {:x-csrf-token (:csrf-token db)}
                            :timeout         TIMEOUT
                            :format          (ajax/transit-request-format {})
                            :response-format (ajax/transit-response-format {:keywords? true})
                            :on-success      [:update-db-from-result (fn [db] true)]
                            :on-failure      [:bad-result {} retry-fx]}})))

;; Update the app-db locally (i.e. do not propagate change to server)
;; path is a vector pointing into :profile or items such as :artwork, :contacts, etc. E.g.
;; [:artwork 16 :purchases 0 :price] or [:exhibitions 3 :location]
;; We take this opportunity to update the default value for the field if we are tracking
;; defaults for this field in app-db's :ui-defaults
(reg-event-db
  :set-local-item-val
  manual-check-spec
  (fn [db [path val]]
    ;; The path is a triple [:type num :field] and we want the path into
    ;; ui-defaults [:artwork-defaults :dimensions]
    (let [item-defaults-type (keyword (str (name (first path)) "-defaults")) ;; e.g. :artwork-defaults
          item-defaults (get-in db [:ui-defaults item-defaults-type])
          field (last path)
          set-default? (contains? item-defaults field)
          new-db (assoc-in db path val)]
      (if set-default?
        (assoc-in new-db [:ui-defaults item-defaults-type field] val)
        new-db))))

(reg-event-db
  :set-app-db-val
  interceptors
  (fn [db [path val]]
    ;; The path points into app-db, e.g. [:display-count]
    (assoc-in db path val)))

;; Add a message to the stack, e.g. :form-error "All required fields must have a value before changes can be saved."
(reg-event-db
  :add-message
  manual-check-spec
  (fn [db [id msg]]
    (add-message db id msg)))

;; Trigger a website publishing by updating the :version key of the :pages map. Also set :processing to true.
(reg-event-fx
  :publish-pages
  manual-check-spec
  (fn [{:keys [db]} _]
    (let [new-db (-> db
                     (assoc-in [:pages :version] (generate-uuid))
                     (assoc-in [:pages :processing] true))
          changes {:version (get-in new-db [:pages :version])
                   :processing (get-in new-db [:pages :processing])}]
      (merge (update-user-table-fx :pages [] changes false new-db)))))
             ;{:dispatch-later}))))

;; Switch to edit mode for given item. Stash the current app-db to undo changes
;; that are canceled. 'item-path' is a path to item such as [:press 2]
;; This can also be called with [:profile] as the item-path.
(reg-event-db
  :edit-item
  manual-check-spec
  (fn [db [item-path]]
    (reset! app-db-undo db)
    (-> db
      (assoc-in (conj item-path :editing) true))))

;; Switch from edit to view mode for given item. Reset the current app-db to the
;; state save in app-db-undo. 'item-path' is a path to item such as [:press 2]
;; This can also be called with [:profile] as the item-path.
(reg-event-db
  :cancel-edit-item
  manual-check-spec
  (fn [db [item-path]]
    (let [db @app-db-undo]
      (reset! app-db-undo nil)
      (assoc-in db (conj item-path :editing) false))))

;; Compute the diff between the item in the current app-db and that stored in app-db-undo
;; and apply changes. The item-path is a vector pointing to the item, E.g. [:press 3]
;; The :profile can be used as the item-path.
(reg-event-fx
  :save-changes
  manual-check-spec
  (fn [{:keys [db]} [item-path]]
    (let [type (first item-path)
          item (get-in db item-path)
          new-db (-> db
                     (clear-message :form-error)   ;; Clear any previous form error
                     (assoc-in (conj item-path :editing) false))
          diff (clojure.data/diff (get-in db item-path) (get-in @app-db-undo item-path))
          diffA (first diff)  ;; Only in current db
          diffB (second diff) ;; Only in snapshot (app-db-undo)
          ;; Use diffA as a basis for the changes made to the item. Since we assign nil values as opposed to removing
          ;; map keys/vals, diffA should contain all changes except for those made within collection-valued keys
          ;; (e.g. vector-valued :purchases of artwork or :degrees of profile). In this case, we look for any occurrence
          ;; of the key (e.g. :purchases in artwork) in either diffA or diffB and overwite the entire value instead of
          ;; trying to pinpoint the exact change within the collection.
          ;; TODO: Improve this approach of explicitly naming the vector-of-colls by using (type)
          changes (cond-> diffA
                     (:signed-raw-url diffA) (dissoc :signed-raw-url) ;; For :documents
                     (:signed-raw-url-expiration-time diffA) (dissoc :signed-raw-url-expiration-time) ;; For :documents
                     (:processing diffA) (dissoc :processing) ;; For :documents
                     (:editing diffA) (dissoc :editing)
                     (and (= type :profile) (or (:degrees diffA) (:degrees diffB))) (assoc :degrees (:degrees item))
                     (and (= type :profile) (or (:collections diffA) (:collections diffB))) (assoc :collections (:collections item))
                     (and (= type :profile) (or (:lectures-and-talks diffA) (:lectures-and-talks diffB))) (assoc :lectures-and-talks (:lectures-and-talks item))
                     (and (= type :profile) (or (:residencies diffA) (:residencies diffB))) (assoc :residencies (:residencies item))
                     (and (= type :profile) (or (:awards-and-grants diffA) (:awards-and-grants diffB))) (assoc :awards-and-grants (:awards-and-grants item))
                     (or (:associated-documents diffA) (:associated-documents diffB)) (assoc :associated-documents (:associated-documents item))
                     (or (:associated-press diffA) (:associated-press diffB)) (assoc :associated-press (:associated-press item))
                     (and (= type :artwork) (or (:style diffA) (:style diffB))) (assoc :style (:style item))
                     (and (= type :artwork) (or (:images diffA) (:images diffB))) (assoc :images (:images item))
                     (and (= type :artwork) (or (:purchases diffA) (:purchases diffB))) (assoc :purchases (:purchases item))
                     (and (= type :artwork) (or (:exhibition-history diffA) (:exhibition-history diffB))) (assoc :exhibition-history (:exhibition-history item))
                     (and (= type :pages) (or (:public-exhibitions diffA) (:public-exhibitions diffB))) (assoc :public-exhibitions (:public-exhibitions item)))]
      ;; Ensure required fields have values
      (if (helodali.spec/invalid? type item)
        {:db (add-message db :form-error "All required fields must have a value before changes can be saved.")}
        (do
          (pprint (str "CHANGES: " changes))
          (reset! app-db-undo nil)
          (if (empty? changes)
            {:db new-db} ;; No changes to apply to server
            (condp = type
              :profile (update-user-table-fx :profiles (rest item-path) changes false new-db)
              :pages (update-user-table-fx :pages (rest item-path) changes false new-db)
              (update-fx item-path changes false new-db))))))))

;; Create an artwork item from an Instagram post in our :instagram-media map.
;; This means we include an :instagram-media-ref map in the artwork item, set the artwork's
;; :sync-with-instagram to true and finally copy the high res image from instagram to our
;; S3 bucket for processing. We'll let our sever handle the changes to S3.
(reg-event-fx
  :create-from-instagram
  manual-check-spec
  (fn [{:keys [db]} [id]]
    (let [ig (get-in db [:instagram-media id])
          media (-> ig
                   (assoc-in [:created] (safe-unparse-date (:created ig))))]
      {:http-xhrio {:method          :post
                    :uri             "/create-from-instagram"
                    :params          {:uref (:uuid (:profile db)) :access-token (:access-token db)
                                      :sub (:sub (:userinfo db)) :media media}
                    :headers         {:x-csrf-token (:csrf-token db)}
                    :timeout         TIMEOUT
                    :format          (ajax/transit-request-format {})
                    :response-format (ajax/transit-response-format {:keywords? true})
                    :on-success      [:update-items]
                    :on-failure      [:bad-result {} false]}
       :db (assoc-in db [:instagram-media id :processing] true)})))

(def signed-url-keys [:signed-thumb-url :signed-thumb-url-expiration-time
                      :signed-raw-url :signed-raw-url-expiration-time :signed-image-url
                      :signed-image-url-expiration-time :signed-large-image-url
                      :signed-large-image-url-expiration-time])

(defn copy-signed-urls
  "Copy signed-urls for matching images (matching on :key) from images to new-images."
  [new-images images]
  (let [;; Create a map of images keyed on the current app-db item's images
        signed-urls (reduce (fn [acc image] (assoc acc (:key image) image)) {} images)
        new-images (map (fn [image] (if (contains? signed-urls (:key image))
                                      (merge image (select-keys (get signed-urls (:key image)) signed-url-keys))
                                      image))
                        new-images)]
    (apply vector new-images)))

(defn replace-item
  "Perform a smart replacement of the new-item for the old db item. Specifically, don't throw away valid
   signed urls for artwork. This is a bit tricky for the signed-urls as
   we need to match on the image :key as the image itself may have changed."
  [db type id new-item]
  (let [item (get-in db [type id])]
    (cond-> new-item
            (= :artwork type) (assoc :images (copy-signed-urls (:images new-item) (get-in db [type id :images]))))))

;; Update items under :artwork, :instagram-media, etc. The incoming data is a map keyed by item type
;; with values a list of changes. A change is structured as [<uuid of item> [path value]] where
;; path is a vector that points into the item or can be nil to represent a whole-item overwrite.
(reg-event-db
  :update-items
  interceptors
  (fn [db [result]]
    (let [apply-item-update (fn [type db change]
                               ;; 'change' looks like [<uuid of item> [path-within-item value]]
                               ;; Find the item in the app-db, :instagram-media-ref do not have a
                               ;; a uuid key, instead a instagram-id.
                               (let [kw (condp = type
                                           :instagram-media :instagram-id
                                           :uuid)
                                     ;; TODO: Catch exception if item not found
                                     [path val] (second change)
                                     id (search-item-by-key-value (get db type) kw (first change))] ;; id will be nil if item does not exist
                                 (if (nil? path)
                                   ;; Either a new item or an overwrite of existing
                                   (let [fixed (fix-response {type [val]})
                                         new-item (merge (helodali.db/defaults-for-type db type) (first (get fixed type)))]
                                     (if (nil? id)
                                       ;; new item
                                       (let [id (next-id (get db type))]
                                         (assoc-in db [type id] new-item))
                                       ;; overwrite existing item with some consideration to keeping signed-urls for artwork
                                       (assoc-in db [type id] (replace-item db type id new-item))))
                                   ;; Assoc into existing item
                                   (let [item (assoc (get-in db [type id]) path val)
                                         fixed (fix-response {type [item]})]
                                     (assoc-in db [type id] fixed)))))
          apply-item-updates (fn [db type l]
                               ;; Process the list of items being updated for the item type
                               (reduce (partial apply-item-update type) db l))]
      (reduce-kv apply-item-updates db result))))

;; Retrieve Instagram media. The which-type parameter can be:
;; nil -> Load the first batch of media if existing :instagram map is empty, otherwise use cached
;; :append -> load "more" media
;; :hard-reload -> Refresh from Instagram
(reg-event-fx
  :refresh-instagram
  manual-check-spec
  (fn [{:keys [db]} [which-type]]
    (if (and (nil? which-type) (not (empty? (:instagram-media db))))
      ;; Use cached media
      {:db (assoc db :display-type :instagram)}
      ;; Reload from server either from start or by appending "more", For the former, set
      ;; max-id-val to nil, and the latter, set max-id-val to the last instagram-id we have on the client.
      (let [last-id (last (keys (:instagram-media db)))
            append? (= which-type :append)
            max-id-val (if (= which-type :hard-reload)
                         nil
                         (and which-type last-id (get-in db [:instagram-media last-id :instagram-id])))]
        {:http-xhrio {:method          :post
                      :uri             "/refresh-instagram"
                      :params          {:uref (:uuid (:profile db)) :access-token (:access-token db)
                                        :max-id max-id-val}
                      :headers         {:x-csrf-token (:csrf-token db)}
                      :timeout         TIMEOUT
                      :format          (ajax/transit-request-format {})
                      :response-format (ajax/transit-response-format {:keywords? true})
                      :on-success      [:update-instagram-media append?]
                      :on-failure      [:bad-result {} false]}
         :db (-> db
                 (assoc :display-type :instagram))}))))

;; Update :instagram-media of app-db: either overwrite or append to existing otherwise.
(reg-event-db
  :update-instagram-media
  manual-check-spec
  (fn [db [append? result]]
    (let [instagram-media (if (:instagram-media result)  ;; Convert the unix time :created to cljs-time objects
                            (mapv #(assoc % :created (from-long (get % :created))) (:instagram-media result)))]
      (if append?
        (assoc db :instagram-media (into-sorted-map (concat (vals (:instagram-media db)) instagram-media)))
        (assoc db :instagram-media (and instagram-media (into-sorted-map instagram-media)))))))


;; POST /validate-token request and set authenticated?=true if successful, also
;; storing the retrieved id-token
(reg-event-fx
  :validate-access-token
  manual-check-spec
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/validate-token"
                  :params          {:access-token (:access-token db)
                                    :id-token (:id-token db)
                                    :uref (:uuid (:profile db))}
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         TIMEOUT
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:initialize-db-from-result]
                  :on-failure      [:bad-result {:access-token nil :id-token nil} false]}}))

;; GET /check-session and initialize db if a valid access token is refreshed on the server
(reg-event-fx
  :check-session
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :get
                  :uri             "/check-session"
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         12000     ;; A long timeout is sometimes needed during dev (server running locally)
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:initialize-db-from-result]
                  :on-failure      [:bad-result {} false]}}))

;; Ask the server to refresh the access token and therefore set the refresh-aws-creds? if necessary
(reg-event-fx
  :refresh-access-token
  manual-check-spec
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/refresh-token"
                  :params          {:access-token (:access-token db)
                                    :id-token (:id-token db)
                                    :uref (:uuid (:profile db))}
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         TIMEOUT
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:update-db-from-result (fn [db] true)]
                  :on-failure      [:bad-result {:access-token nil :id-token nil} false]}}))

(reg-event-db
  :set-aws-credentials
  interceptors
  (fn [db [aws-creds-js]]
    (let [aws-creds {:accessKeyId (.-accessKeyId aws-creds-js)
                     :secretAccessKey (.-secretAccessKey aws-creds-js)
                     :sessionToken (.-sessionToken aws-creds-js)}]
      (pprint (str "IdentityId: " (.-identityId aws-creds-js)))
      (-> db
          (assoc :refresh-aws-creds? false)
          (assoc :aws-creds-created-time (ct/now))
          (assoc :aws-s3 (js/AWS.S3. (clj->js aws-creds)))
          (assoc :aws-creds aws-creds)))))

(reg-event-fx
  :logout
  interceptors
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/logout"
                  :params          {:access-token (:access-token db)
                                    :uref (get-in db [:profile :uuid])}
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         TIMEOUT
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:complete-logout]
                  :on-failure      [:bad-result {} #(dispatch [:logout])]}
     :sync-to-local-storage [{:k "helodali.access-token" :v nil}
                             {:k "helodali.id-token" :v nil}]}))

(reg-event-fx
  :delete-account
  manual-check-spec
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/delete-account"
                  :params          {:access-token (:access-token db)
                                    :uref (get-in db [:profile :uuid])}
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         TIMEOUT
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:complete-logout]
                  :on-failure      [:bad-result {} #(dispatch [:delete-account])]}
     :sync-to-local-storage [{:k "helodali.access-token" :v nil}
                             {:k "helodali.id-token" :v nil}
                             {:k "helodali.signed-urls" :v nil}]}))

;; A successful logout: reset the db to the unauthenticated default but keep the csrf-token. Set the
;; :do-cognito-logout? boolean to true to point the browser to cognito's /logout endpoint to clear cookies.
(reg-event-fx
  :complete-logout
  manual-check-spec
  (fn [{:keys [db]} [_ result]]
    {:db (-> helodali.db/default-db
            (assoc :sit-and-spin false)
            (assoc :do-coginito-logout? true)
            (assoc :csrf-token (:csrf-token db)))
     :route-client {:route-name helodali.routes/home :args {}}}))


(reg-event-fx
  :authenticated
  manual-check-spec
  (fn [{:keys [db]} [authenticated? access-token id-token]]
    ; (pprint (str "Event :authenticated with params: authenticated?=" authenticated? ", access-token=" access-token ", id-token=" id-token))
    {:db (-> db
           (assoc :authenticated? authenticated?)
           (assoc :id-token id-token)
           (assoc :access-token access-token))
     :sync-to-local-storage [{:k "helodali.access-token" :v access-token}
                             {:k "helodali.id-token" :v id-token}]}))

(reg-event-fx
  :copy-item
  manual-check-spec
  (fn [{:keys [db]} [type source-id name-attribute]]
    ;; Use the next available integer ID to assign as the key in sorted map for items
    (let [id (next-id (get db type))
          new-db (-> db
                   (assoc-in [type id] (get-in db [type source-id]))
                   (assoc-in [type id :uuid] (generate-uuid))
                   (assoc-in [type id :created] (ct/now))
                   (assoc-in [type id name-attribute] (str (get-in db [type source-id name-attribute]) " Copy")))
          new-db (cond-> new-db
                    (= type :artwork) (assoc-in [type id :images] []) ;; Empty out images as they will be copied separately
                    (= type :documents) (assoc-in [type id] (merge (get-in new-db [type id]) {:key nil
                                                                                              :raw-key nil
                                                                                              :signed-raw-url nil
                                                                                              :signed-raw-url-expiration-time nil
                                                                                              :processing nil})))
          db-changes (create-fx type (get-in new-db [type id]) false new-db)]
      (condp = type
        :artwork (merge db-changes
                      {:dispatch-n (apply list (map (fn [m] [:copy-s3-within-bucket "helodali-raw-images" m [type id]])
                                                    (get-in db [type source-id :images])))})
        :documents (merge db-changes
                      {:dispatch-n (list [:copy-s3-object "helodali-documents" (get-in db [type source-id]) [type id]])})
        db-changes))))

;; Sweep through items of given type and set given (top-level) kw to value
(reg-event-db
  :sweep-and-set
  manual-check-spec
  (fn [db [type kw value]]
    (let [apply-change (fn [new-items id item]
                         (assoc new-items id (assoc item kw value)))]
      (assoc db type (reduce-kv apply-change (sorted-map) (get db type))))))

;; Change view. If 'display' is :default, look up the default view for the item type. Take this opportunity
;; to inspect the age of the aws credentials and force a refresh if necessary.
(reg-event-db
  :change-view
  (fn [db [_ type display]]
    (let [display-type (if (= display :default) (helodali.db/default-view-for-type type) display)
          refresh-access-token? (if (:aws-creds-created-time db)
                                  (ct/after? (ct/now) (ct/plus (:aws-creds-created-time db) (ct/hours 1)))
                                  false)]
      (-> db
        (assoc :refresh-access-token? refresh-access-token?)
        (assoc :display-type display-type)
        (assoc :view type)))))

(reg-event-db
  :display-static-html
  manual-check-spec
  (fn [db [page]]
    (-> db
       (assoc :view :static-page)
       (assoc :static-page (keyword page)))))

(reg-event-db
  :back-to-landing-page
  manual-check-spec
  (fn [db _]
    (-> db
        (assoc :view :landing)
        (dissoc :static-page))))

(reg-event-db
  :display-search-results
  manual-check-spec
  (fn [db [search-pattern]]
    (-> db
       (assoc :display-type :list)
       (assoc :search-pattern search-pattern)
       (assoc :view :search-results))))

(reg-event-db
  :display-new-item
  manual-check-spec
  (fn [db [type]]
    (let [id 0  ;; 0 is the placeholder item in the sorted map for 'type'
          new-db  (-> db
                    (assoc :display-type :new-item)
                    (assoc :view type)
                    (assoc-in [type id] (merge (helodali.db/defaults-for-type db type) (helodali.db/ui-defaults-for-type db type))) ;; This also assigns an uuid
                    (assoc-in [type id :editing] true))]
      (assoc new-db :single-item-uuid (get-in new-db [type id :uuid])))))

;; Create item based on contents of placeholder item (id == 0) but confirm
;; the existence of values for required fields. If confirmation fails, add a warning message.
(reg-event-fx
  :create-from-placeholder
  manual-check-spec
  (fn [{:keys [db]} [type required-fields]]
    (let [placeholder (get-in db [type 0])]
      (if (helodali.spec/invalid? type placeholder)
        {:db (add-message db :form-error "All required fields must have a value before changes can be saved.")}
        ;; Create the item
        (let [id (next-id (get db type))
              new-db (-> db
                        (assoc :display-type :single-item)
                        (assoc-in [type id] placeholder)
                        (assoc-in [type id :uref] (get-in db [:profile :uuid]))
                        (assoc-in [type id :editing] false)
                        (update-in [type] dissoc 0)
                        (clear-message :form-error))]
          (reset! app-db-undo nil)
          (create-fx type (get-in new-db [type id]) false new-db))))))

(defn- reflect-item-deletion
  [db type id]
  (let [item (get-in db [type id])
        new-db (update-in db [type] dissoc id)
        mode (get db :display-type)]
        ;; If view mode is :new-item or :single-item, then switch to default view for type
    (if (or (= mode :new-item) (= mode :single-item))
       (assoc new-db :display-type (helodali.db/default-view-for-type type))
       new-db)))

(defn- referenced-in-purchases?
  "Check the given list of purchases for the presence of the given contact (uuid)."
  [purchases uuid]
  (let [matched (filter #(or (= (:buyer %) uuid) (= (:agent %) uuid) (= (:dealer %) uuid)) purchases)]
    (not (empty? matched))))

(defn- referenced-in-exhibition-history?
  "Check the given list of exhibition-history for the presence of the given exhibition (uuid)."
  [exhibition-history uuid]
  (let [matched (filter #(= (:ref %) uuid) exhibition-history)]
    (not (empty? matched))))

(defn- referential-integrity-check
  "Determine if the given item is referenced elsewhere in the app-db. A non-empty string will be returned if so,
   nil or empty string otherwise."
  [db type id]
  (let [uuid (get-in db [type id :uuid])
        ret-fx (fn [items type name-kw]
                 (if (empty? items)
                   nil
                   (str "The item cannot be removed since it is referenced in the following " (name type) ": "
                        (clojure.string/join ", " (map #(get % name-kw) items)) ". ")))]
    (condp = type
      :press (let [exhibitions (filter #(contains? (get % :associated-press) uuid) (vals (:exhibitions db)))]
               (ret-fx exhibitions :exhibitions :name))
      :documents (let [exhibitions (filter #(contains? (get % :associated-documents) uuid) (vals (:exhibitions db)))
                       artwork (filter #(contains? (get % :associated-documents) uuid) (vals (:artwork db)))
                       press (filter #(contains? (get % :associated-documents) uuid) (vals (:press db)))
                       contacts (filter #(contains? (get % :associated-documents) uuid) (vals (:contacts db)))]
                   (apply str [(ret-fx exhibitions :exhibitions :name)
                               (ret-fx artwork :artwork :title)
                               (ret-fx press :press :title)
                               (ret-fx contacts :contacts :name)]))
      :contacts (let [artwork (filter #(referenced-in-purchases? (get % :purchases) uuid) (vals (:artwork db)))]
                  (ret-fx artwork :artwork :title))
      :exhibitions (let [artwork (filter #(referenced-in-exhibition-history? (get % :exhibition-history) uuid) (vals (:artwork db)))]
                     (ret-fx artwork :artwork :title))
      nil)))

;; Dispatch this event to delete an item of type contacts, exhibitions, press
(reg-event-fx
  :delete-item
  manual-check-spec
  (fn [{:keys [db]} [type id]]
    (let [item (get-in db [type id])
          refint (referential-integrity-check db type id)
          new-db (if (empty? refint)
                   (reflect-item-deletion db type id)
                   (add-message db :form-error refint))]
      ;; Submit change to server for all deletes except those to the placeholder item
      (if (or (= 0 id) (not (empty? refint)))
        {:db new-db}
        (delete-fx type item false new-db)))))

;; Dispatch this event to delete an artwork item, images needs to be removed from S3.
(reg-event-fx
  :delete-artwork-item
  manual-check-spec
  (fn [{:keys [db]} [type id]]
    (let [item (get-in db [type id])
           refint (referential-integrity-check db type id)
           new-db (if (empty? refint)
                    (reflect-item-deletion db type id)
                    (add-message db :form-error refint))]
      ;; Submit change to server for all deletes except those to the placeholder item
      (if (or (= 0 id) (not (empty? refint)))
        {:db new-db}
        (merge (delete-fx type item false new-db)
               {:dispatch-n (list [:delete-s3-objects "helodali-raw-images" (:images item)])})))))

;; Dispatch this event to delete a document item, the file needs to be removed from S3.
(reg-event-fx
  :delete-document-item
  manual-check-spec
  (fn [{:keys [db]} [type id]]
    (let [item (get-in db [type id])
           refint (referential-integrity-check db type id)
           new-db (if (empty? refint)
                    (reflect-item-deletion db type id)
                    (add-message db :form-error refint))]
      ;; Submit change to server for all deletes except those to the placeholder item
      (if (or (= 0 id) (not (empty? refint)))
        {:db new-db}
        (merge (delete-fx type item false new-db)
               {:dispatch-n (list [:delete-s3-objects "helodali-documents" (filter #(not (empty? (:key %))) [item])])})))))

;; Apply this change only to our app-db: Push new 'defaults' element in the front of vector given by 'path'
(reg-event-db
  :create-local-vector-element
  manual-check-spec
  (fn [db [path defaults]] ;; defaults is a map, e.g. the output of helodali.db/default-purchase, inserted at head of vector
    (let [val (into [defaults] (get-in db path))]
      (assoc-in db path val))))

(reg-event-db
  :delete-local-vector-element
  interceptors
  (fn [db [path idx]]
    (let [val (remove-vector-element (get-in db path) idx)]
      (assoc-in db path val))))

;; Like above, but the vector element being deleted is a map with a :key key pointing to
;; a s3 object which needs removing. We need to update the DB as well as issue one or
;; more S3 removals. The buckets argument is a list (e.g. ["bucket1" "bucket2"] that
;; this function will transform into an argument to dispatch-n. We also assume the
;; maps in the vector contain an :uuid key.
(reg-event-fx
  :delete-s3-vector-element
  manual-check-spec
  (fn [{:keys [db]} [buckets path uuid]]
    (let [idx (find-element-by-key-value (get-in db path) :uuid uuid)
          s3-object (get-in db (conj path idx))
          dispatches (apply list (map (fn [bucket] [:delete-s3-objects bucket [s3-object]]) buckets))
          val (remove-vector-element (get-in db path) idx)
          new-db (assoc-in db path val)]
      (merge (update-fx path val false new-db)
             {:dispatch-n dispatches}))))

;; Delete a S3 object from the given item. This involves issuing the S3 delete and
;; the removal of :key, :signed-raw-url and :signed-raw-url-expiration-time keys
;; from the item in app-db. Also remove the :key value from the item in the database.
(reg-event-fx
  :delete-s3-object-from-item
  manual-check-spec
  (fn [{:keys [db]} [buckets path-to-item]]
    (let [s3-object (get-in db path-to-item)
          dispatches (apply list (map (fn [bucket] [:delete-s3-objects bucket [s3-object]]) buckets))
          val (-> s3-object
                 (dissoc :key)
                 (dissoc :filename)
                 (dissoc :size)
                 (dissoc :signed-raw-url)
                 (dissoc :signed-raw-url-expiration-time)
                 (dissoc :signed-thumb-url)
                 (dissoc :signed-thumb-url-expiration-time)
                 (dissoc :signed-image-url)
                 (dissoc :signed-image-url-expiration-time)
                 (dissoc :signed-large-image-url)
                 (dissoc :signed-large-image-url-expiration-time))
          new-db (assoc-in db path-to-item val)]
      (merge (update-fx path-to-item {:key nil :filename nil :size 0} false new-db)
             {:dispatch-n dispatches}))))

(reg-event-db
  :display-single-item
  interceptors
  (fn [db [type uuid editing?]]
    (-> db
      (assoc :display-type :single-item)
      (assoc :view type)
      (assoc :single-item-uuid uuid))))

(reg-event-db
  :noop
  (fn [db _]
    db))

(reg-event-fx
  :retry-request
  manual-check-spec
  (fn [{:keys [db]} [retry-fx]]
    (retry-fx)))

(reg-event-db
  :clear-message
  manual-check-spec
  (fn [db [id]]
    (clear-message db id)))

(reg-event-fx
  :bad-result
  manual-check-spec
  (fn [{:keys [db]} [merge-this retry-fx result]]
    (pprint (str "bad-result: " result))
    (if (and (= 403 (:status result)) retry-fx)
      ;; Refetch the csrf-token and retry the operation
      (merge (csrf-token-request [:update-csrf-token-and-retry retry-fx]))
      ;; else update the message to alert the user of the trouble
      ;; TODO: Need to back out change made to local app-db
      (let [db (merge db merge-this)]
        (if (string? result)
          (add-message db nil result)
          (if (map? result)
            (add-message db nil (str (:reason (:response result))))
            (add-message db nil "An error occurred when processing the request")))))))

;; This event should be dispatched when an item or path into an item needs to be
;; refreshed from the database. We dispatch continually, with a
;; 1000ms delay, until a satisfactory result is returned as determined by the
;; given satisfied function. The 'item-path' argument must contain a table
;; and id at minimum. E.g. [:documents 2]
(reg-event-fx
  :refresh-item
  manual-check-spec
  (fn [{:keys [db]} [item-path satisfied-fn]]
    (let [type (first item-path)
          id (second item-path)]
      {:http-xhrio {:method          :post
                    :uri             "/refresh-item-path"
                    :params          {:uref (get-in db [:profile :uuid])
                                      :access-token (:access-token db)
                                      :table type :path (rest (rest item-path))
                                      :item-uuid (get-in db [type id :uuid])}
                    :headers         {:x-csrf-token (:csrf-token db)}
                    :timeout         TIMEOUT
                    :format          (ajax/transit-request-format {})
                    :response-format (ajax/transit-response-format {:keywords? true})
                    :on-success      [:apply-item-refresh item-path satisfied-fn]
                    :on-failure      [:bad-result {} false]}})))

(reg-event-fx
  :apply-item-refresh
  manual-check-spec
  (fn [{:keys [db]} [item-path satisfied-fn result]]
    ;; If the 'result', which is a map, does not satisfy the given satisfied-fn, then we are still
    ;; waiting on some type of processing and will try again after a delay.
    (if (not (satisfied-fn result))
      {:dispatch-later [{:ms 1000 :dispatch [:refresh-item item-path satisfied-fn]}]}
      ;; else merge the map into the item
      (let [fixed-resp (first (fix-response {(first item-path) [result]}))
            val (-> (get-in db item-path)
                    (merge fixed-resp))]
        {:db (assoc-in db item-path val)}))))

;; This event should be dispatched when an artwork has images being processed and hence
;; a fetch is required from DynamoDB. We dispatch continually, with a
;; 1000ms delay, until a result is returned.
(reg-event-fx
  :refresh-image
  manual-check-spec
  (fn [{:keys [db]} [path-to-image]]
    (let [type (first path-to-image)
          id (second path-to-image)]
      {:http-xhrio {:method          :post
                    :uri             "/refresh-image-data"
                    :params          {:uref (get-in db [:profile :uuid])
                                      :access-token (:access-token db)
                                      :item-uuid (get-in db [type id :uuid])
                                      :image-uuid (get-in db (conj path-to-image :uuid))}
                    :headers         {:x-csrf-token (:csrf-token db)}
                    :timeout         TIMEOUT
                    :format          (ajax/transit-request-format {})
                    :response-format (ajax/transit-response-format {:keywords? true})
                    :on-success      [:apply-image-refresh path-to-image]
                    :on-failure      [:bad-result {} false]}})))

;; This event removes signed url key/vals from maps representing images and documents.
(reg-event-db
  :flush-signed-urls
  manual-check-spec
  (fn [db [path-to-object-map]]
    (let [o (-> (get-in db path-to-object-map)
                (dissoc :signed-raw-url :signed-raw-url-expiration-time
                        :signed-thumb-url :signed-thumb-url-expiration-time
                        :signed-image-url :signed-image-url-expiration-time
                        :signed-large-image-url :signed-large-image-url-expiration-time))]
      (assoc-in db path-to-object-map o))))

(reg-event-fx
  :apply-image-refresh
  manual-check-spec
  (fn [{:keys [db]} [path-to-image result]]
    ;; If the 'result' map is empty, then we are still waiting on image processing and will
    ;; try again after a delay. But make sure the item still exists in our app-db (in case it was deleted).
    (if (and (empty? result) (get-in db [(first path-to-image) (second path-to-image)]))
      {:dispatch-later [{:ms 1000 :dispatch [:refresh-image path-to-image]}]}
      ;; else merge the map into the artwork and unset :processing
      (let [image (-> (get-in db path-to-image)
                     (merge result)
                     (dissoc :signed-raw-url-expiration-time :signed-thumb-url-expiration-time :signed-image-url-expiration-time :signed-large-image-url-expiration-time)
                     (coerce-int [:density :size :width :height])
                     (dissoc :processing))]
        {:db (-> db
                (assoc-in path-to-image image))}))))

;; Get the signed URL to access designated S3 object. Define a 1 hour expiration.
(reg-event-fx
  :get-signed-url
  (conj manual-check-spec check-aws-credentials)
  (fn [{:keys [db]} [path-to-object-map bucket object-key url-key expiration-key]]
    ;; If the AWS Credentials have expired, then delay the execution of this event
    (if (:refresh-aws-creds? db)
      {:dispatch-later [{:ms 400 :dispatch [:get-signed-url path-to-object-map bucket object-key url-key expiration-key]}]}
      ;; Get the signed url
      (let [s3 (:aws-s3 db)
            expiration-seconds (* 60 60) ;; 1 hour
            expiration-time (ct/plus (ct/now) (ct/seconds expiration-seconds))
            params (clj->js {:Bucket bucket :Key object-key :Expires expiration-seconds})
            url (js->clj (.getSignedUrl s3 "getObject" params))
            new-db (-> db
                      (assoc-in (conj path-to-object-map url-key) url)
                      (assoc-in (conj path-to-object-map expiration-key) expiration-time))]
        {:db new-db
         :sync-to-local-storage [{:k "helodali.signed-urls"
                                  :v (current-signed-urls new-db)}]}))))

;; Execute s3 function s3f and on-success if successful, refresh token and retry otherwise
; (reg-fx
;   :s3-request
;   (fn [{:keys [s3f on-success retry]}]
;     (let [callback (fn [err data]
;                       (if (nil? err)
;                         (on-success)
;                         (do
;                           (.getSession ())
;                           (retry))))
;           op (condp = op-type
;                       :put-object #(.putObject s3 params callback)
;                       :copy-object #(.copyObject s3 params callback)
;                       :delete-objects #(.deleteObjects s3 params callback)
;                       (pprint (str "Error, s3-operation unknown op-type: " op-type)))]
;       (op))))
;
; (reg-event-fx
;   :store-signed-url
;   manual-check-spec
;   (fn [{:keys [db]} [path-to-object-map bucket object-key url-key expiration-key]]
;     ;; Get the signed url
;     (let [s3 (:aws-s3 db)
;           expiration-seconds (* 60 60 24) ;; 24 hours
;           expiration-time (ct/plus (ct/now) (ct/seconds expiration-seconds))
;           params (clj->js {:Bucket bucket :Key object-key :Expires expiration-seconds})
;           url (js->clj (.getSignedUrl s3 "getObject" params))
;           new-db (-> db
;                     (assoc-in (conj path-to-object-map url-key) url)
;                     (assoc-in (conj path-to-object-map expiration-key) expiration-time))]
;       (pprint (str "Retrieved signedUrl: " url))
;       {:db new-db
;        :sync-to-local-storage [{:k "helodali.signed-urls"
;                                 :v (current-signed-urls new-db)}]})))
;
; (reg-event-fx
;   :get-signed-url
;   manual-check-spec
;   (fn [{:keys [db]} [path-to-object-map bucket object-key url-key expiration-key]]
;     ;; Get the signed url
;     (let [s3 (:aws-s3 db)
;           expiration-seconds (* 60 60 24) ;; 24 hours
;           expiration-time (ct/plus (ct/now) (ct/seconds expiration-seconds))
;           params (clj->js {:Bucket bucket :Key object-key :Expires expiration-seconds})
;           s3f (partial (.getSignedUrl s3 "getObject" params))]
;       {:s3-request {:s3f s3f :on-success #(dispatch [:store-signed-url path-to-object-map bucket object-key expiration-key])
;                     :retry #(dispatch [:get-signed-url path-to-object-map bucket object-key expiration-key])}})))

;; Upload an image to the helodali-raw-images bucket. The s3 object key ("filename") is composed
;; as follows: sub/artwork-uuid/image-uuid/filename
;; Where sub is the user's openid subject identifier
;;       artwork-uuid is the uuid of the associated artwork item
;;       image-uuid is a newly generated uuid to enforce uniqueness of uploaded keys
;;       filename is the basename of the file selected by the user
(reg-event-fx
  :add-image
  (conj manual-check-spec check-aws-credentials)
  (fn [{:keys [db]} [item-path js-file]]
    ;; If the AWS Credentials have expired, then delay the execution of this event
    (if (:refresh-aws-creds? db)
      {:dispatch-later [{:ms 400 :dispatch [:add-image item-path js-file]}]}
      (let [filename (.-name js-file)
            uuid (generate-uuid)
            object-key (str (:sub (:userinfo db)) "/" (get-in db (conj item-path :uuid))
                            "/" uuid "/" filename)
            params (clj->js {:Bucket "helodali-raw-images" :Key object-key :ContentType (.-type js-file) :Body js-file :ACL "private"})
            images-path (conj item-path :images)
            images (get-in db images-path)]
        {:s3-operation {:op-type :put-object :s3 (:aws-s3 db) :params params}
         :db (assoc-in db images-path (conj images {:uuid uuid :processing true}))}))))

;; Similar to add-image but replace the existing image
(reg-event-fx
  :replace-image
  (conj manual-check-spec check-aws-credentials)
  (fn [{:keys [db]} [item-path image-uuid js-file]]
    ;; If the AWS Credentials have expired, then delay the execution of this event
    (if (:refresh-aws-creds? db)
      {:dispatch-later [{:ms 400 :dispatch [:replace-image item-path image-uuid js-file]}]}
      (let [filename (.-name js-file)
            images-path (conj item-path :images)
            images (get-in db images-path)
            idx (find-element-by-key-value images :uuid image-uuid)
            new-uuid (generate-uuid)
            image-to-delete (get images idx)
            object-key (str (:sub (:userinfo db)) "/" (get-in db (conj item-path :uuid))
                            "/" new-uuid "/" filename)
            params (clj->js {:Bucket "helodali-raw-images" :Key object-key :ContentType (.-type js-file) :Body js-file :ACL "private"})]
        {:s3-operation {:op-type :put-object :s3 (:aws-s3 db) :params params
                        :on-success #(dispatch [:delete-s3-objects "helodali-raw-images" [image-to-delete]])}
         :db (assoc-in db (conj images-path idx) {:uuid new-uuid :processing true})}))))

;; Perform an s3 operation, such as putObject or copyObject. The 'op-type' argument tells us what
;; operation to perform.
(reg-fx
  :s3-operation
  (fn [{:keys [op-type s3 params on-success]}]
    (let [callback (fn [err data]
                      (if (nil? err)
                        (when on-success
                          (on-success))
                        (pprint (str "Err from s3-operation: " err)))) ;; TODO: dispatch error event?
          op (condp = op-type
                      :put-object #(.putObject s3 params callback)
                      :copy-object #(.copyObject s3 params callback)
                      :delete-objects #(.deleteObjects s3 params callback)
                      #(pprint (str "Error, s3-operation unknown op-type: " op-type)))]
      (op))))

(reg-event-fx
  :on-s3-success
  interceptors
  (fn [{:keys [db]} [item-path changes]]
    (let [item (-> (get-in db item-path)
                  (merge changes)
                  (assoc :processing false))
          new-db (assoc-in db item-path item)]
      (update-fx item-path changes false new-db))))

;; Upload an object to the given s3 bucket. The s3 object key ("filename") is composed
;; as follows: sub/item-uuid/new-uuid/filename
;; Where sub is the user's openid subject identifier
;;       item-uuid is the uuid of the associated item, such as a document
;;       new-uuid is unique to this invocation of add/replace object
;;       filename is the basename of the file selected by the user
;; This function differs from add-image in that we are also responsible for updating
;; the database (as opposed to Lambda being triggered).
(reg-event-fx
  :add-s3-object
  (conj manual-check-spec check-aws-credentials)
  (fn [{:keys [db]} [bucket item-path js-file]]
    ;; If the AWS Credentials have expired, then delay the execution of this event
    (if (:refresh-aws-creds? db)
      {:dispatch-later [{:ms 400 :dispatch [:add-s3-object bucket item-path js-file]}]}
      (let [filename (.-name js-file)
            s3 (:aws-s3 db)
            key-name (s3-key-for-bucket bucket)
            this-uuid (generate-uuid)
            object-key (str (:sub (:userinfo db)) "/" (get-in db (conj item-path :uuid)) "/" this-uuid "/" filename)
            params (clj->js {:Bucket bucket :Key object-key :ContentType (.-type js-file) :Body js-file :ACL "private"})
            changes {key-name object-key :filename filename :size (.-size js-file)}]
        {:s3-operation {:op-type :put-object :s3 s3 :params params
                        :on-success #(dispatch [:on-s3-success item-path changes])}
         :db (-> db
               (assoc-in (conj item-path :processing) true)
               (assoc-in (conj item-path :signed-raw-url) nil)
               (assoc-in (conj item-path :signed-raw-url-expiration-time) nil))}))))

;; Similar to add-s3-object but replace the existing object - delete the existing and add the new with a unique object-key
(reg-event-fx
  :replace-s3-object
  (conj manual-check-spec check-aws-credentials)
  (fn [{:keys [db]} [bucket item-path js-file]]
    ;; If the AWS Credentials have expired, then delay the execution of this event
    (if (:refresh-aws-creds? db)
      {:dispatch-later [{:ms 400 :dispatch [:replace-s3-object bucket item-path js-file]}]}
      (let [filename (.-name js-file)
            s3 (:aws-s3 db)
            key-name (s3-key-for-bucket bucket)
            this-uuid (generate-uuid)
            object-key (str (:sub (:userinfo db)) "/" (get-in db (conj item-path :uuid)) "/" this-uuid "/" filename)
            s3-object-to-delete (get-in db item-path)
            params (clj->js {:Bucket bucket :Key object-key :ContentType (.-type js-file) :Body js-file :ACL "private"})
            changes {key-name object-key :filename filename :size (.-size js-file)}]
        (pprint (str "object-key: " object-key))
        (pprint (str "ContentType: " (.-type js-file)))
        {:s3-operation {:op-type :put-object :s3 s3 :params params
                        :on-success #(dispatch [:on-s3-success item-path changes])}
         :db (-> db
               (assoc-in (conj item-path :processing) true)
               (assoc-in (conj item-path :signed-raw-url) nil)
               (assoc-in (conj item-path :signed-raw-url-expiration-time) nil))
         :dispatch [:delete-s3-objects bucket [s3-object-to-delete]]}))))

;; Copy object within same bucket and update database when done.
;; Construct new object key from new item's uuid and generate-uuid.
(reg-event-fx
  :copy-s3-object
  (conj manual-check-spec check-aws-credentials)
  (fn [{:keys [db]} [bucket source-object-map target-item-path]]
    ;; If the AWS Credentials have expired, then delay the execution of this event
    (if (:refresh-aws-creds? db)
      {:dispatch-later [{:ms 400 :dispatch [:copy-s3-object bucket source-object-map target-item-path]}]}
      (let [s3 (:aws-s3 db)
            key-name (s3-key-for-bucket bucket)
            copy-source (str bucket "/" (key-name source-object-map))
            this-uuid (generate-uuid)
            object-key (str (:sub (:userinfo db)) "/" (get-in db (conj target-item-path :uuid)) "/" this-uuid "/" (:filename source-object-map))
            params (clj->js {:Bucket bucket :Key object-key :CopySource copy-source})
            changes {key-name object-key}]
        (pprint (str "object-key: " object-key))
        {:s3-operation {:op-type :copy-object :s3 s3 :params params
                        :on-success #(dispatch [:on-s3-success target-item-path changes])}
         :db (-> db
               (assoc-in (conj target-item-path :processing) true)
               (assoc-in (conj target-item-path :signed-raw-url) nil)
               (assoc-in (conj target-item-path :signed-raw-url-expiration-time) nil))}))))


;; Copy given image. Differs from the above by not needing to update the database (in this case, Lambda
;; is doing that for us).
;; Construct new object key from new item's uuid and generate-uuid. Replace
;; the target image map with just :uuid and :processing=true and is appended to images vector.
(reg-event-fx
  :copy-s3-within-bucket
  (conj manual-check-spec check-aws-credentials)
  (fn [{:keys [db]} [bucket source-object-map target-item-path]]
    ;; If the AWS Credentials have expired, then delay the execution of this event
    (if (:refresh-aws-creds? db)
      {:dispatch-later [{:ms 400 :dispatch [:copy-s3-within-bucket bucket source-object-map target-item-path]}]}
      (let [callback (fn [err data]
                       (if (not (nil? err))
                         (pprint (str "Err from copyObject: " err))
                         (pprint "successful copyObject")))
            s3 (:aws-s3 db)
            copy-source (str bucket "/" (:key source-object-map))
            images-path (conj target-item-path :images)
            images (get-in db images-path)
            new-uuid (generate-uuid)
            object-key (str (:sub (:userinfo db)) "/" (get-in db (conj target-item-path :uuid))
                            "/" new-uuid "/" (:filename source-object-map))
            params (clj->js {:Bucket bucket :Key object-key :CopySource copy-source})]
        (pprint (str "new object-key: " object-key))
        (pprint (str "Copy Source: " copy-source))
        (.copyObject s3 params callback)
        {:db (assoc-in db images-path (conj images {:uuid new-uuid :processing true}))}))))

;; Delete all objects defined in vector argument for given S3 bucket. Each object is a map
;; with a key defining the S3 objectKey value. Note: If deleting from helodali-raw-images, we
;; should reference the :raw-key inside the image map.
(reg-event-fx
  :delete-s3-objects
  (conj manual-check-spec check-aws-credentials)
  (fn [{:keys [db]} [bucket objects]]
    (if (empty? objects)
      {:db db} ;; nothing to do
      ;; If the AWS Credentials have expired, then delay the execution of this event
      (if (:refresh-aws-creds? db)
        {:dispatch-later [{:ms 400 :dispatch [:delete-s3-objects bucket objects]}]}
        (let [s3 (:aws-s3 db)
              key-name (s3-key-for-bucket bucket)
              callback (fn [err data]
                         (if (not (nil? err))
                           (pprint (str "Err from deleteObjects: " err))
                           (pprint (str "successful deleteObjects" data))))
              object-keys (mapv (fn [m] {:Key (get m key-name)}) objects)
              params (clj->js {:Bucket bucket :Delete {:Objects object-keys
                                                       :Quiet false}})
              _ (js->clj (.deleteObjects s3 params callback))]
          {:db db})))))

;; If uploading to our ring handler, which is not planned.
; (reg-event-fx
;   :upload-image
;   (fn [_world [_ js-file]]
;     (let [filename (.-name js-file)
;           form-data (doto
;                       (js/FormData.)
;                       (.append "file" js-file filename))]
;       {:http-xhrio {:method          :post
;                     :uri             "/add-image"
;                     :body            form-data
;                     :timeout         TIMEOUT
;                     :format          (ajax/json-request-format)
;                     :response-format (ajax/json-response-format {:keywords? true})
;                     :on-success      [:noop]
;                     :on-failure      [:bad-result {} false]}})))
