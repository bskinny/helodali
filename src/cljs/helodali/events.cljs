(ns helodali.events
  (:require
    [ajax.core :as ajax]
    [helodali.common :refer [coerce-int empty-string-to-nil fix-date parse-date unparse-date]]
    [helodali.spec :as hs] ;; Keep this here even though we refer to the namespace directly below
    [helodali.misc :refer [expired? generate-uuid find-element-by-key-value find-item-by-key-value
                           remove-vector-element into-sorted-map]]
    [cljs-time.core :as ct]
    [cljs.pprint :refer [pprint]]
    [reagent.core :as r]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path trim-v reg-cofx
                           after debug dispatch]]
    [day8.re-frame.http-fx]
    [cljsjs.aws-sdk-js]
    [cljs.spec :as s]))

(def ^:private app-db-undo (r/atom nil))

(defn next-id
  "Assumes map is a sorted-map"
  [m]
  ((fnil inc 0) (last (keys m))))

(defn check-and-throw
  "throw an exception if db doesn't match the spec."
  [a-spec app-db]
  (when-not (s/valid? a-spec app-db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec app-db)) {}))))

;; This interceptor is run after each event handler has finished, and checks
;; app-db against a spec.
(def check-spec-interceptor (after (partial check-and-throw :helodali.spec/db)))

;; the chain of interceptors we use for all handlers that manipulate the db
(def interceptors [check-spec-interceptor               ;; ensure the spec is still valid
                   (when ^boolean js/goog.DEBUG debug)  ;; look in your browser console for debug logs
                   trim-v])                             ;; removes first (event id) element from the event vec

;; Some effects handlers check spec themselves before submitting requests to the server
(def manual-check-spec [(when ^boolean js/goog.DEBUG debug)
                        trim-v])

; For demo and development purposes
; (reg-event-db
;  :initialize-db
;  (fn  [_ _]
;    helodali.db/default-db))

(reg-event-fx
  :initialize-db
  [(inject-cofx :local-store-items)]
  (fn [{:keys [db local-store-items]} _]
    (enable-console-print!)
    ; (pprint (str "initialize-db with localStorage: " local-store-items))
    {:http-xhrio {:method          :get
                  :uri             "/csrf-token"
                  :timeout         5000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:update-db-from-result]
                  :on-failure      [:bad-result {}]}
     :db (-> helodali.db/default-db
            (assoc :access-token (:access-token local-store-items))
            (assoc :id-token (:id-token local-store-items)))}))

(reg-cofx
  :local-store-items
  (fn [cofx _]
      "Read in items from localstore, into a map we can merge into app-db. If we are missing the id-token
       then remove the access-token as we need to reauthenticate"
    (let [access-token (.getItem js/localStorage "helodali.access-token")
          id-token (.getItem js/localStorage "helodali.id-token")]
      (if (and (empty? id-token) (not (empty? access-token)))
        (.removeItem js/localStorage "helodali.access-token");
        (assoc cofx :local-store-items
               {:access-token (.getItem js/localStorage "helodali.access-token")
                :id-token (.getItem js/localStorage "helodali.id-token")})))))

(reg-event-db
  :initialize-db-from-result
  (fn [db [_ result]]
    ;; Some values in the items need coercion back to DateTime and keyword syntaxes.
    ; (pprint (str "Raw: " result))
    (pprint (str "initialize-db-from-result, access-token: " (:access-token db)))
    (let [fixer (partial fix-date :parse)
          artwork (->> (:artwork result)
                      (fixer :created)
                      (map #(assoc % :purchases (fixer :date (:purchases %)))))
          press (->> (:press result)
                   (fixer :created)
                   (fixer :publication-date))
          contacts (->> (:contacts result)
                     (fixer :created))
          exhibitions (->> (:exhibitions result)
                        (fixer :created)
                        (fixer :begin-date)
                        (fixer :end-date))]
      (-> db
        (assoc :artwork (into-sorted-map (map #(merge (helodali.db/default-artwork) %) artwork)))
        (assoc :exhibitions (into-sorted-map (map #(merge (helodali.db/default-exhibition) %) exhibitions)))
        (assoc :contacts (into-sorted-map (map #(merge (helodali.db/default-contact) %) contacts)))
        (assoc :press (into-sorted-map (map #(merge (helodali.db/default-press) %) press)))
        (assoc :profile (:profile result))
        (assoc :userinfo (:userinfo result))
        (assoc :initialized? true)))))

;; Update top-level app-db keys
(reg-event-db
  :update-db-from-result
  (fn [db [_ result]]
    ; (pprint (str "Result: " result))
    (merge db result)))

;; path is a vector pointing into the app db, e.g. [:display-type]
(reg-event-db
  :set-app-val
  interceptors
  (fn [db [path val]]
    (assoc-in db path val)))

;; path is a vector pointing into :profile or items, e.g. :artwork, :contacts, etc. E.g.
;; [:artwork 16 :purchases 0 :price] or [:exhibitions 3 :location]
(reg-event-db
  :set-local-item-val
  interceptors        ;; TODO: consider not checking spec for this event
  (fn [db [path val]]
    (assoc-in db path val)))

(defn- safe-unparse-date
  [v]
  (if (instance? js/goog.date.UtcDateTime v)
    (unparse-date v)
    v))

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
    (map? in) (let [in (dissoc in :editing :expanded :images)]
                (clojure.walk/walk (fn [[k v]] [k (walk-cleaner v)]) identity in))
    (vector? in) (apply vector (map walk-cleaner in))
    (set? in) (set (map walk-cleaner in))
    :else (cleaner in)))

(defn- update-fx
  [db path val]
  (let [spec-it (check-and-throw :helodali.spec/db db)
        type (first path)
        id (second path)
        inside-item-path (rest (rest path))  ;; hop over type and 'id' which only exists on the client app-db
        val (walk-cleaner val)
        fx {:db db}]
    ;; Submit change to server for all updates except those to the placeholder item in the client, which
    ;; does not yet exist on the server. Also skip the update if the update is on an item, as a whole, and
    ;; the set of changes, val, is empty
    (if (or (= id 0) (and (empty? inside-item-path) (empty? val)))
      fx
      (merge fx {:http-xhrio {:method          :post
                              :uri             "/update-item"
                              :params          {:uref (get-in db [type id :uref]) ;; TODO: server must confirm this uref matches user's login profile
                                                :uuid (get-in db [type id :uuid])
                                                :table type :path inside-item-path :val val
                                                :access-token (:access-token db)}
                              :headers         {:x-csrf-token (:csrf-token db)}
                              :timeout         5000
                              :format          (ajax/transit-request-format {})
                              :response-format (ajax/transit-response-format {:keywords? true})
                              :on-success      [:noop]
                              :on-failure      [:bad-result {}]}}))))

;; Similar to above but restricted to the app-db's :profile, which results to writes against
;; the :profiles table on the server. 'path' should be [:profile] or start as such.
(defn- update-profile-fx
  [db path val]
  (let [ _ (check-and-throw :helodali.spec/db db)
        inside-profile-path (rest path)
        val (walk-cleaner val)]
    {:db db
     :http-xhrio {:method          :post
                  :uri             "/update-profile"
                  :params          {:uuid (get-in db [:profile :uuid])
                                    :path inside-profile-path :val val
                                    :access-token (:access-token db)}
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         5000
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:noop]
                  :on-failure      [:bad-result {}]}}))

(defn- create-fx
  [db table item]
  (let [spec-it (check-and-throw :helodali.spec/db db)
        item (-> item
                (walk-cleaner)
                (assoc :uref (get-in db [:profile :uuid]))
                (dissoc :editing :expanded))]
    {:db db
     :http-xhrio {:method          :post
                  :uri             "/create-item"
                  :params          {:table table :item item :access-token (:access-token db)}
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         5000
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:noop]
                  :on-failure      [:bad-result {}]}}))

(defn- delete-fx
  [db table item]
  (let [spec-it (check-and-throw :helodali.spec/db db)]
    {:db db
     :http-xhrio {:method          :post
                  :uri             "/delete-item"
                  :params          {:table table :uref (:uref item) :uuid (:uuid item) :access-token (:access-token db)}
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         5000
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:noop]
                  :on-failure      [:bad-result {}]}}))

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
          new-db (assoc-in db (conj item-path :editing) false)
          diff (clojure.data/diff (get-in db item-path) (get-in @app-db-undo item-path))
          diffA (first diff)  ;; Only in current db
          diffB (second diff) ;; Only in snapshot (app-db-undo)
          ;; Use diffA as a basis for the changes made to the item. Since we assign nil values as opposed to removing
          ;; map keys/vals, diffA should contain all changes except for those made within collection-valued keys
          ;; (e.g. vector-valued :purchases of artwork or :degrees of profile). In this case, we look for any occurrence
          ;; of the key (e.g. :purchases in artwork) in either diffA or diffB and overwite the entire value instead of
          ;; trying to pinpoint the exact change within the collection. TODO: We should reconsider vector-valued keys because of this?
          changes (cond-> diffA
                     (:editing diffA) (dissoc :editing)
                     (and (= type :profile) (or (:degrees diffA) (:degrees diffB))) (assoc :degrees (:degrees item))
                     (and (= type :profile) (or (:collections diffA) (:collections diffB))) (assoc :collections (:collections item))
                     (and (= type :profile) (or (:lectures-and-talks diffA) (:lectures-and-talks diffB))) (assoc :lectures-and-talks (:lectures-and-talks item))
                     (and (= type :profile) (or (:residencies diffA) (:residencies diffB))) (assoc :residencies (:residencies item))
                     (and (= type :profile) (or (:awards-and-grants diffA) (:awards-and-grants diffB))) (assoc :awards-and-grants (:awards-and-grants item))
                     (and (= type :artwork) (or (:images diffA) (:images diffB))) (assoc :images (:images item))
                     (and (= type :artwork) (or (:purchases diffA) (:purchases diffB))) (assoc :purchases (:purchases item))
                     (and (= type :artwork) (or (:exhibition-history diffA) (:exhibition-history diffB))) (assoc :exhibition-history (:exhibition-history item)))]
      (pprint (str "CHANGES: " changes))
      (reset! app-db-undo nil)
      (if (empty? changes)
        {:db new-db} ;; No changes to apply to server
        (condp = type
          :profile (update-profile-fx new-db item-path changes)
          (update-fx new-db item-path changes))))))

;; Update the app-db locally (i.e. do not propogate change to server)
;; path is a vector pointing into :profile or items, e.g. :artwork, :contacts, etc. E.g.
;; [:artwork 16 :purchases 0 :price] or [:exhibitions 3 :location]
(reg-event-db
  :set-local-item-val
  manual-check-spec
  (fn [db [path val]]
    (assoc-in db path val)))

;; path is a vector pointing into items, e.g. :artwork, :contacts, etc. E.g.
;; [:artwork 16 :purchases 0 :price] or [:exhibitions 3 :location]
;; The update is applied in memory and the new database is checked against spec before
;; submitting the update to the server.
(reg-event-fx
  :set-item-val
  manual-check-spec
  (fn [_world [path val]]
    (let [db (:db _world)
          new-db (assoc-in db path val)]
      (update-fx new-db path val))))

;; POST /validate-token request and set authenticated?=true if successful, also
;; storing the retrieved id-token
(reg-event-fx
  :validate-access-token
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/validate-token"
                  :params          {:access-token (:access-token db)}
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         5000
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:update-db-from-result]
                  :on-failure      [:bad-result {:access-token nil :id-token nil}]}}))

;; POST /login request and retrieve :profile
(reg-event-fx
  :login
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/login"
                  :params          {:access-token (:access-token db)}
                  :headers         {:x-csrf-token (:csrf-token db)}
                  :timeout         5000
                  :format          (ajax/transit-request-format {})
                  :response-format (ajax/transit-response-format {:keywords? true})
                  :on-success      [:initialize-db-from-result]
                  :on-failure      [:bad-result {}]}}))

(defn- handle-delegation-token-retrieval
  "The delegation-result contains 'Credentials'"
  [err delegation-result]
  (if (not (nil? err))
    ;; TODO: Handle the 401 resultCode error for expired id token
    (pprint (str "Error from getDelegationToken: " (js->clj err)))
    (let [result (js->clj delegation-result)]
      (dispatch [:setup-aws-delegation (get result "Credentials")]))))

(reg-event-db
  :fetch-aws-delegation-token
  (fn [db _]
   (let [auth0 (js/Auth0. (clj->js {:domain "helodali.auth0.com"
                                    :clientID "UNQ9LKBRomyn7hLPKKJmdK2mI7RNphGs"
                                    :callbackURL "dummy"}))
         options {:id_token (:id-token db) :api "aws"
                  :principal "arn:aws:iam::128225160927:saml-provider/auth0-provider"
                  :role "arn:aws:iam::128225160927:role/access-to-s3-per-user"}]
     (.getDelegationToken auth0 (clj->js options) handle-delegation-token-retrieval)
     (-> db
        (assoc :delegation-token nil)
        (assoc :delegation-token-expiration nil)
        (assoc :aws-s3 nil)))))

(reg-event-db
  :setup-aws-delegation
  interceptors
  (fn [db [credentials]]
    (pprint (str "credentials: " credentials))
    (let [aws-creds (js/AWS.Credentials. (get credentials "AccessKeyId")
                                         (get credentials "SecretAccessKey")
                                         (get credentials "SessionToken"))
          s3 (js/AWS.S3. (clj->js {:credentials aws-creds}))]
      (-> db
         (assoc :delegation-token credentials)
         (assoc :delegation-token-expiration (parse-date :date-time (get credentials "Expiration")))
         (assoc :aws-s3 s3)))))

(reg-event-db
  :authenticated
  (fn [db [_ authenticated? access-token id-token]]
    (pprint (str "Event :authenticated with params: authenticated?=" authenticated? ", access-token=" access-token ", id-token=" id-token))
    (-> db
      (assoc :authenticated? authenticated?)
      (assoc :id-token id-token)
      (assoc :access-token access-token))))

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
                    (= type :artwork) (assoc-in [type id :images] [])) ;; Empty out images as they will be copied separately
          db-changes (create-fx new-db type (get-in new-db [type id]))]
      (condp = type
        :artwork (merge db-changes
                      {:dispatch-n (apply list (map (fn [m] [:copy-s3-within-bucket "helodali-raw-images" m [type id]])
                                                    (get-in db [type source-id :images])))})
        db-changes))))


;; Change view. If 'display' is :default, look up the default view for the item type
(reg-event-db
  :change-view
  (fn [db [_ type display]]
    (let [display-type (if (= display :default) (helodali.db/default-view-for-type type) display)]
      (-> db
        (assoc :display-type display-type)
        (assoc :view type)))))

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
  interceptors
  (fn [db [type]]
    (let [id 0  ;; 0 is the placeholder item in the sorted map for 'type'
          new-db  (-> db
                    (assoc :display-type :new-item)
                    (assoc :view type)
                    (assoc-in [type id] (helodali.db/defaults-for-type type)) ;; This also assigns an uuid
                    (assoc-in [type id :editing] true)
                    (assoc-in [type id :expanded] true))]
      (assoc new-db :single-item-uuid (get-in new-db [type id :uuid])))))

(reg-event-fx
  :create-from-placeholder
  manual-check-spec
  (fn [{:keys [db]} [type]]
    (let [id (next-id (get db type))
          new-db (-> db
                    (assoc :display-type :single-item)
                    (assoc-in [type id] (get-in db [type 0]))
                    (assoc-in [type id :uref] (get-in db [:profile :uuid]))
                    (assoc-in [type id :editing] false)
                    (update-in [type] dissoc 0))]
      (reset! app-db-undo nil)
      (create-fx new-db type (get-in new-db [type id])))))

;; Dispatch this event to delete an item of type contacts, exhibitions, press
(reg-event-fx
  :delete-item
  interceptors
  (fn [{:keys [db]} [type id]]
    (let [item (get-in db [type id])
          new-db (update-in db [type] dissoc id)
          mode (get db :display-type)
          ;; If view mode is :new-item or :single-item, then switch to default view for type
          new-db (if (or (= mode :new-item) (= mode :single-item))
                   (assoc new-db :display-type (helodali.db/default-view-for-type type))
                   new-db)]
      ;; Submit change to server for all deletes except those to the placeholder item
      (if (= 0 id)
        {:db new-db}
        (delete-fx new-db type item)))))

;; Dispatch this event to delete an artwork item, images needs to be removed from S3.
(reg-event-fx
  :delete-artwork-item
  interceptors
  (fn [{:keys [db]} [type id]]
    (let [item (get-in db [type id])
          images (:images item)
          new-db (update-in db [type] dissoc id)
          mode (get db :display-type)
          ;; If view mode is :new-item or :single-item, then switch to default view for type
          new-db (if (or (= mode :new-item) (= mode :single-item))
                   (assoc new-db :display-type (helodali.db/default-view-for-type type))
                   new-db)]
      ;; Submit change to server for all deletes except those to the placeholder item
      (if (= 0 id)
        {:db new-db}
        (merge (delete-fx new-db type item)
               {:dispatch-n (list [:delete-s3-objects "helodali-raw-images" images])})))))

;; Push new 'defaults' element in the front of vector given by 'path'
(reg-event-fx
  :create-vector-element
  manual-check-spec
  (fn [{:keys [db]} [path defaults]] ;; defaults is a map, e.g. the output of helodali.db/default-purchase, inserted at head of vector
    (let [val (into [defaults] (get-in db path))
          new-db (assoc-in db path val)]
      (update-fx new-db path val))))

;; Apply this change only to our app-db: Push new 'defaults' element in the front of vector given by 'path'
(reg-event-db
  :create-local-vector-element
  interceptors
  (fn [db [path defaults]] ;; defaults is a map, e.g. the output of helodali.db/default-purchase, inserted at head of vector
    (let [val (into [defaults] (get-in db path))]
      (assoc-in db path val))))

(reg-event-fx
  :create-profile-vector-element
  manual-check-spec
  (fn [{:keys [db]} [path defaults]]       ;; 'path' should be relative to app-db's :profile, e.g. [:degrees]
    (let [val (into [defaults] (get-in db (into [:profile] path)))
          new-db (assoc-in db (into [:profile] path) val)
          _ (check-and-throw :helodali.spec/db db)
          val (walk-cleaner val)]
      {:db new-db
       :http-xhrio {:method          :post
                    :uri             "/update-profile"
                    :params          {:uuid (get-in db [:profile :uuid])
                                      :path path :val val
                                      :access-token (:access-token db)}
                    :headers         {:x-csrf-token (:csrf-token db)}
                    :timeout         5000
                    :format          (ajax/transit-request-format {})
                    :response-format (ajax/transit-response-format {:keywords? true})
                    :on-success      [:noop]
                    :on-failure      [:bad-result {}]}})))

(reg-event-fx
  :delete-vector-element
  manual-check-spec
  (fn [{:keys [db]} [path idx]]
    (let [val (remove-vector-element (get-in db path) idx)
          new-db (assoc-in db path val)]
      (update-fx new-db path val))))

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
      (merge (update-fx new-db path val)
             {:dispatch-n dispatches}))))

(reg-event-fx
  :delete-profile-vector-element
  manual-check-spec
  (fn [{:keys [db]} [path idx]]       ;; 'path' should be relative to app-db's :profile, e.g. [:degrees]
    (let [val (remove-vector-element (get-in db (into [:profile] path)) idx)
          new-db (assoc-in db (into [:profile] path) val)
          _ (check-and-throw :helodali.spec/db db)
          val (walk-cleaner val)]
      {:db new-db
       :http-xhrio {:method          :post
                    :uri             "/update-profile"
                    :params          {:uuid (get-in db [:profile :uuid])
                                      :path path :val val
                                      :access-token (:access-token db)}
                    :headers         {:x-csrf-token (:csrf-token db)}
                    :timeout         5000
                    :format          (ajax/transit-request-format {})
                    :response-format (ajax/transit-response-format {:keywords? true})
                    :on-success      [:noop]
                    :on-failure      [:bad-result {}]}})))

(reg-event-db
  :display-single-item
  interceptors
  (fn [db [type uuid editing?]]
    (-> db
      (assoc :display-type :single-item)
      (assoc :view type)
      (assoc :single-item-uuid uuid)
      (assoc-in [type (find-item-by-key-value (get db type) :uuid uuid) :expanded] true))))

(reg-event-db
  :set-message
  interceptors
  (fn [db [msg]]
    (assoc db :message msg)))

(reg-event-db
  :noop
  (fn [db _]
    db))

(reg-event-db
  :bad-result
  (fn [db [_ merge-this result]]
    (let [db (merge db merge-this)]
      (if (string? result)
        (assoc db :message result)
        (if (map? result)
          (assoc db :message (str (:status-text result)))
          (assoc db :message "An error occurred when processing the request"))))))

(reg-event-db
  :good-image-upload-result
  (fn [db [_ result]]
    (assoc db :message result)))

;; This event should be dispatched when an artwork has images being processed and hence
;; a fetch is required from DynamoDB. We dispatch continually, with a
;; 400ms delay, until a result is returned.
(reg-event-fx
  :refresh-image
  (fn [{:keys [db]} [_ path-to-image]]
    (let [type (first path-to-image)
          id (second path-to-image)]
      {:http-xhrio {:method          :post
                    :uri             "/refresh-image-data"
                    :params          {:uref (get-in db [:profile :uuid])
                                      :access-token (:access-token db)
                                      :item-uuid (get-in db [type id :uuid])
                                      :image-uuid (get-in db (conj path-to-image :uuid))}
                    :headers         {:x-csrf-token (:csrf-token db)}
                    :timeout         5000
                    :format          (ajax/transit-request-format {})
                    :response-format (ajax/transit-response-format {:keywords? true})
                    :on-success      [:apply-image-refresh path-to-image]
                    :on-failure      [:bad-result {}]}})))

(reg-event-fx
  :apply-image-refresh
  (fn [{:keys [db]} [_ path-to-image result]]
    ;; If the 'result' map is empty, then we are still waiting on image processing and will
    ;; try again after a delay.
    (pprint (str "apply-image-refresh result: " result))
    (if (empty? result)
      {:dispatch-later [{:ms 1000 :dispatch [:refresh-image path-to-image]}]}
      ;; else merge the map into the artwork and unset :processing
      (let [image (-> (get-in db path-to-image)
                     (merge result)
                     (walk-cleaner)
                     (coerce-int [:density :width :height])
                     (dissoc :processing))]
        {:db (-> db
                (assoc-in path-to-image image))}))))

;; Get the signed URL to access designated S3 object. Define a one week
;; expiration. If the delegation token has not been fetched yet, then trigger
;; another attempt at this event in 400 ms.
(reg-event-fx
  :get-signed-url
  (fn [{:keys [db]} [_ path-to-object-map bucket object-key url-key expiration-key]]
    (if (nil? (:delegation-token db))
      ;; Wait for the delegation-token to be fetched
      {:dispatch-later [{:ms 400 :dispatch [:get-signed-url path-to-object-map bucket object-key url-key expiration-key]}]}
      (if (expired? (:delegation-token-expiration db))
        ;; Refresh the expired delegation-token
        {:dispatch-n (list [:fetch-aws-delegation-token] [:get-signed-url path-to-object-map bucket object-key url-key expiration-key])}
        ;; Get the signed url
        (let [s3 (:aws-s3 db)
              expiration-time (ct/plus (ct/now) (ct/days 7))
              params (clj->js {:Bucket bucket :Key object-key :Expires (* 3600 24 7)})
              url (js->clj (.getSignedUrl s3 "getObject" params))]
          (pprint (str "Retrieved signedUrl: " url))
          {:db (-> db
                  (assoc-in (conj path-to-object-map url-key) url)
                  (assoc-in (conj path-to-object-map expiration-key) expiration-time))})))))

;; Upload an image to the helodali-raw-images bucket. The object key ("filename") is composed
;; as follows: sub/artwork-uuid/image-uuid/filename
;; Where sub is the user's openid subject identifier
;;       artwork-uuid is the uuid of the associated artwork item
;;       image-uuid is a newly generated uuid to enforce uniqueness of uploaded keys
;;       filename is the basename of the file selected by the user
(reg-event-fx
  :add-image
  (fn [{:keys [db]} [_ item-path js-file]]
    (if (expired? (:delegation-token-expiration db))
      ;; Refresh the expired delegation-token
      {:dispatch-n (list [:fetch-aws-delegation-token] [:add-image item-path js-file])}
      (let [filename (.-name js-file)
            callback (fn [err data]
                       (if (not (nil? err))
                         (pprint (str "Err from putObject: " err))
                         (pprint "successful putObject")))
            s3 (:aws-s3 db)
            uuid (generate-uuid)
            object-key (str (:sub (:userinfo db)) "/" (get-in db (conj item-path :uuid))
                            "/" uuid "/" filename)
            params (clj->js {:Bucket "helodali-raw-images" :Key object-key :ContentType (.-type js-file) :Body js-file :ACL "private"})
            images-path (conj item-path :images)
            images (get-in db images-path)]
        (pprint (str "object-key: " object-key))
        (pprint (str "ContentType: " (.-type js-file)))
        (.putObject s3 params callback)
        {:db (assoc-in db images-path (conj images {:uuid uuid :processing true}))}))))

;; Similar to add-image but replace the existing image
(reg-event-fx
  :replace-image
  (fn [{:keys [db]} [_ item-path image-uuid js-file]]
    (if (expired? (:delegation-token-expiration db))
      ;; Refresh the expired delegation-token
      {:dispatch-n (list [:fetch-aws-delegation-token] [:replace-image item-path image-uuid js-file])}
      (let [filename (.-name js-file)
            callback (fn [err data]
                       (if (not (nil? err))
                         (pprint (str "Err from putObject: " err))
                         (pprint "successful putObject")))
            s3 (:aws-s3 db)
            images-path (conj item-path :images)
            images (get-in db images-path)
            idx (find-element-by-key-value images :uuid image-uuid)
            new-uuid (generate-uuid)
            image-to-delete (get images idx)
            object-key (str (:sub (:userinfo db)) "/" (get-in db (conj item-path :uuid))
                            "/" new-uuid "/" filename)
            params (clj->js {:Bucket "helodali-raw-images" :Key object-key :ContentType (.-type js-file) :Body js-file :ACL "private"})]
        (pprint (str "object-key: " object-key))
        (pprint (str "ContentType: " (.-type js-file)))
        (.putObject s3 params callback)
        {:db (assoc-in db (conj images-path idx) {:uuid new-uuid :processing true})
         :dispatch [:delete-s3-objects "helodali-raw-images" [image-to-delete]]}))))

;; Copy given object. Construct new object key from new item's uuid and generate-uuid. Replace
;; the target image map with just :uuid and :processing=true and is appended to images vector.
(reg-event-fx
  :copy-s3-within-bucket
  (fn [{:keys [db]} [_ bucket source-object-map target-item-path]]
    (if (expired? (:delegation-token-expiration db))
      ;; Refresh the expired delegation-token
      {:dispatch-n (list [:fetch-aws-delegation-token] [:copy-s3-within-bucket bucket source-object-map target-item-path])}
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
;; with a mandatory :key key with objectKey value.
(reg-event-fx
  :delete-s3-objects
  (fn [{:keys [db]} [_ bucket objects]]
    (if (empty? objects)
      ;; nothing to do
      {:db db}
      (if (nil? (:delegation-token db))
        {:dispatch-later [{:ms 400 :dispatch [:delete-images bucket objects]}]}
        (if (expired? (:delegation-token-expiration db))
          ;; Refresh the expired delegation-token
          {:dispatch-n (list [:fetch-aws-delegation-token] [:delete-images bucket objects])}
          (let [s3 (:aws-s3 db)
                callback (fn [err data]
                           (if (not (nil? err))
                             (pprint (str "Err from deleteObjects: " err))
                             (pprint (str "successful deleteObjects" data))))
                object-keys (apply vector (map (fn [m] {:Key (get m :key)}) objects))
                params (clj->js {:Bucket bucket :Delete {:Objects object-keys
                                                         :Quiet false}})
                _ (js->clj (.deleteObjects s3 params callback))]
            {:db db}))))))

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
;                     :timeout         5000
;                     :format          (ajax/json-request-format)
;                     :response-format (ajax/json-response-format {:keywords? true})
;                     :on-success      [:good-image-upload-result]
;                     :on-failure      [:bad-result {}]}})))
