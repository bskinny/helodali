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
            [helodali.db :refer [get-account update-user-table]]
            [clojure.pprint :refer [pprint]]))

(def client
  {:id (get (System/getenv) "HD_INSTAGRAM_CLIENT_ID")
   :secret (get (System/getenv) "HD_INSTAGRAM_CLIENT_SECRET")
   :redirect-uri (get (System/getenv) "HD_INSTAGRAM_REDIRECT_URI")})

(def options {:timeout 900  ;; ms
              ; :debug true :debug-body true
              :as :json})  ;; Try to automatically coerce the output based on the content-type

(def base-url "https://api.instagram.com")

(defn- convert-to-item
  "Build a helodali item representing the instagram media item"
  [ig]
  {:tags (:tags ig)
   :caption (:text (:caption ig))
   :likes (:count (:likes ig))
   :id (:id ig)
   :type (keyword (:type ig))
   :image-url (:url (:standard_resolution (:images ig)))
   :thumb-url (:url (:thumbnail (:images ig)))
   :created (:created_time ig)})

(defn get-recent-media
  "Return recent media for 'self'
   https://www.instagram.com/developer/endpoints/users/#get_users_media_recent_self"
  [access-token]
  (let [resp (-> (http/get (str base-url "/v1/users/self/media/recent/?access_token=" access-token) {:as :auto})
                 (:body))]
    (if (not= 200 (:code (:meta resp)))
      (pprint (str "Error fetching media: " resp))
      (let [media (:data resp)]
        (map convert-to-item media)))))

(defn refresh-instagram
  "Pull media from Instagram and populate the :instagram portion of the client's app-db"
  [uref]
  (let [account (get-account uref)]
    (if (and account (:instagram-access-token account))
      {:instagram-media (get-recent-media (:instagram-access-token account))}
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
    (pprint (str "request-access-token resp: " resp))
    (if (:access_token resp)
      (update-user-table :accounts uref nil {:instagram-access-token (:access_token resp)
                                             :instagram-user (:user resp)}))))
      ;; TODO: What do we do with failure here?
