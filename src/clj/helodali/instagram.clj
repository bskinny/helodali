(ns helodali.instagram
  (:require [clj-http.client :as http]
            [cheshire.core :refer [parse-string generate-string]]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [slingshot.slingshot :refer [throw+ try+]]
            [clj-uuid :as uuid]
            [clj-time.core :refer [now days ago]]
            [clj-time.format :refer [parse unparse formatters]]
            [helodali.common :refer [coerce-int fix-date keywordize-vals]]
            [helodali.db :refer [get-account query-by-uref update-user-table]])
  (:import (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec)))

(def client
  {:id (get (System/getenv) "HD_INSTAGRAM_CLIENT_ID")
   :secret (get (System/getenv) "HD_INSTAGRAM_CLIENT_SECRET")
   :redirect-uri (get (System/getenv) "HD_INSTAGRAM_REDIRECT_URI")})

(def options {:timeout 2000  ;; ms
              ; :debug true :debug-body true
              :as :auto})

(def base-url "https://api.instagram.com")

(defn- create-params-string
  "Given a map of params {:count 10 :access_token \"123\"},
   convert it to the format ready for signing: access_token=123&count=10"
  [params]
  (let [sorted-params (sort-by #(name (key %)) params) ;; Creates a list of vector tuples
        params-strings (map (fn [v] (str (name (first v)) "=" (second v))) sorted-params)]
    (clojure.string/join "&" params-strings)))

(defn- sign-request
  "Sign the request according to https://www.instagram.com/developer/secure-api-requests/
   That is: sign <endpoint>|param1=value1|param2=value2 where params are placed in sorted order.
   The incoming params-string looks like \"access_token=123&count=10\""
  [endpoint params-string]
  (let [key-spec (SecretKeySpec. (.getBytes (:secret client)) "HmacSHA256")
        mac (doto (Mac/getInstance "HmacSHA256")
               (.init key-spec))
        sign-this (str endpoint "|" (clojure.string/replace params-string "&" "|"))
        bytes (.doFinal mac (.getBytes sign-this))]
    (apply str (map #(format "%x" %) bytes))))

(defn- by-instagram-id
  "Convert list of maps like ({:uuid \"123\" :instagram-media-ref {:instagram-id ...}} ...) to a map keyed by
   instagram-id with the artwork uuid as value."
  [l]
  (reduce (fn [a b]
             (if (:instagram-media-ref b)
               (into a {(:instagram-id (:instagram-media-ref b)) (:uuid b)})
               a))
          {} l))

(defn- convert-to-item
  "Build a helodali item representing the instagram media item. Also look for an associated artwork
   item in the DB."
  [user-artwork-lookup ig]
  {:tags (:tags ig)
   :caption (:text (:caption ig))
   :likes (:count (:likes ig))
   :instagram-id (:id ig)
   :artwork-uuid (get user-artwork-lookup (:id ig))
   :media-type (keyword (:type ig))
   :image-url (:url (:standard_resolution (:images ig)))
   :thumb-url (:url (:thumbnail (:images ig)))
   :created (:created_time ig)})

(defn get-recent-media
  "Return recent media for 'self'
   https://www.instagram.com/developer/endpoints/users/#get_users_media_recent_self"
  [uref access-token]
  (let [endpoint "/users/self/media/recent"
        params-string (create-params-string {:access_token access-token})
        sig (sign-request endpoint params-string)
        resp (try+
               (-> (http/get (str base-url "/v1" endpoint "?" params-string "&sig=" sig) options)
                   (:body))
               (catch Object _
                 (pprint (str (:throwable &throw-context) "unexpected error"))
                 (throw+)))]
    (if (not= 200 (:code (:meta resp)))
      (pprint (str "Error fetching media: " resp)) ;; Likely will not get here due to try/catch
      (let [media (:data resp)
            user-artwork (query-by-uref :artwork uref {:proj-expr "#uuid, #instagramMediaRef"
                                                       :expr-attr-names {"#uuid" "uuid" "#instagramMediaRef" "instagram-media-ref"}})
            user-artwork-lookup (by-instagram-id user-artwork)]
        (pprint (str "user-artwork-lookup: " user-artwork-lookup))
        (map (partial convert-to-item user-artwork-lookup) media)))))

(defn refresh-instagram
  "Pull media from Instagram and populate the :instagram portion of the client's app-db"
  [uref]
  (let [account (get-account uref)]
    (if (and account (:instagram-access-token account))
      {:instagram-media (get-recent-media uref (:instagram-access-token account))}
      {})))

(defn request-access-token
  [code]
  (let [params {:client_id (:id client)
                :client_secret (:secret client)
                :redirect_uri (:redirect-uri client)
                :grant_type "authorization_code"
                :code code}
        response (-> (http/post (str base-url "/oauth/access_token")
                                (merge options {:form-params params}))
                   (:body))]
     response))

(defn process-instagram-auth
  "Get an access token for the user, store it in the database. The 'state' used in the
   authorization-url is the user's uuid."
  [code uref]
  (let [resp (request-access-token code)]
    ;; A response might look like this:
    ;; {:access_token \"1234", :user {:username \"helodali\", :bio \"Painter...\", :website \"http://www.mayalane.com/\",
    ;;                                :profile_picture \"https://scontent-lga3-1.cdninstagram.com/....jpg\",
    ;;                                :full_name \"Brian Williams\", :id \"234"}}
    (if (:access_token resp)
      (update-user-table :accounts uref nil {:instagram-access-token (:access_token resp)
                                             :instagram-user (:user resp)}))))
      ;; TODO: What do we do with failure here?
