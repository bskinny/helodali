(ns helodali.cognito
  (:require [clj-http.client :as http]
            [helodali.db :as db]
            [cheshire.core :refer [parse-string generate-string]]
            [clojure.java.io :as io]
            [buddy.core.keys :as keys]
            [clj-jwt.core :refer [str->jwt verify]]
            [clojure.pprint :refer [pprint]]
            [slingshot.slingshot :refer [throw+ try+]]
            [clj-uuid :as uuid])
  (:import (java.time.format DateTimeFormatter)
           (java.time ZoneId ZonedDateTime)))

(def client
  {:id (or (System/getenv "HD_COGNITO_CLIENT_ID")
           (System/getProperty "HD_COGNITO_CLIENT_ID"))
   :secret (or (System/getenv "HD_COGNITO_CLIENT_SECRET")
               (System/getProperty "HD_COGNITO_CLIENT_SECRET"))
   :redirect-uri (or (System/getenv "HD_COGNITO_REDIRECT_URI")
                     (System/getProperty "HD_COGNITO_REDIRECT_URI"))})

(def options {:timeout 900  ;; ms
              ;:debug true
              ;:debug-body true
              :basic-auth [(:id client) (:secret client)]
              :as :auto  ;; Try to automatically coerce the output based on the content-type
              :headers {:Accept "application/json"}})

(def base-url "https://helodali.auth.us-east-1.amazoncognito.com")
(def jwks-url "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_Xct1ioypu/.well-known/jwks.json")

(defn get-jwks
  "Fetch public keys for verifying RSA signatures. The jwks response can look like this:
  [{:alg \"RS256\" :e \"AQAB\"  :kid \"2NySOWekVs775bSUsw/N/mUIZNnOvLU5F63I/qMzOcQ=\"
    :kty \"RSA\" :n \"3453453453...45345345\" :use \"sig\"}
   {:alg \"RS256\" :e \"AQAB\" :kid \"TMd6w/1imj8aIiV0IVVKke5RyIctnQ3A750FVT0SLzk=\"
    :kty \"RSA\" :n \"123123123...123123123\" :use \"sig\"}]

  Return a map which includes the above as well as the public key representation:
    <kid> {:jwk <above list element> :public-key <public-key>}."
  []
  (let [jwks (try+
               (http/get jwks-url options)
               (catch Object _
                 (pprint (str (:throwable &throw-context) "unexpected error"))
                 (throw+)))]
    (->> (:body jwks)
         (:keys)
         (reduce #(assoc %1 (:kid %2) {:jwk %2 :public-key (keys/jwk->public-key %2)}) {}))))

(def jwks (atom (get-jwks)))

(defn get-token
  "Passed an authorization code, request the access, id and refresh tokens from Cognito"
  [code]
  (let [params {:client_id (:id client)
                :redirect_uri (:redirect-uri client)
                :grant_type "authorization_code"
                :code code}
        response (try+
                   (-> (http/post (str base-url "/oauth2/token")
                                  (merge options {:form-params params}))
                       (:body))
                   (catch Object _
                     (pprint (str (:throwable &throw-context) "unexpected error"))
                     (throw+)))]
     response))

(defn refresh-token
  "Passed a session which contains an expired token, use the refresh token to request
   a new token and replace the session in the database."
  [session]
  (pprint "Refreshing token")
  (let [params {:client_id (:id client)
                :grant_type "refresh_token"
                :refresh_token (:refresh session)}
        response (try+
                   (-> (http/post (str base-url "/oauth2/token")
                                  (merge options {:form-params params}))
                       (:body))
                   (catch Object _
                     (pprint (str (:throwable &throw-context) "unexpected error"))
                     (throw+)))
        tn (ZonedDateTime/now (ZoneId/of "Z"))]
    (db/delete-item :sessions (select-keys session [:uuid]))
    (db/put-items :sessions [{:uuid     (str (uuid/v1)) :uref (:uref session) :token (:access_token response)
                              :refresh  (:refresh session)
                              :id-token (:id_token response) :sub (:sub session)
                              :ts       (.format tn DateTimeFormatter/ISO_INSTANT)}])
    {:access-token (:access_token response)
     :id-token (:id_token response)
     :refresh-aws-creds? true}))

(defn verify-token
  "Verify the signature of the access token and check expiration. If expired, attempt a refresh
  and return the new access, id-token. Side effect is made in replacing database session.

  The JWT, once decoded form the string, looks like this:
      :header {:kid \"TMd6w/1imj8aIiV0IVVKke5RyIctnQ3A750FVT0SLzk=\"
               :alg \"RS256\"}
      :claims {:sub \"c8eb9e68-ad1e-49bd-925d-e96763a7de2c\"
               :iss \"https://cognito-idp.us-east-1.amazonaws.com/us-east-1_Xct1ioypu\"
               :exp 1525804610
               :username \"Google_105303869357768353564\"
               :scope \"openid profile email\"
               :cognito:groups [\"us-east-1_Xct1ioypu_Google\"]
               :token_use \"access\"
               :auth_time 1525801010
               :jti \"ad4fb253-4087-49eb-acea-21052c4dba1e\"
               :client_id \"7uddoehg2ov8abqro8sud6i9ag\"
               :version 2 :iat 1525801010}
      :signature \"lkjhlj...asdasdasd\"
      :encoded-data \"qwoiuyeqe...qweqweqwe.\"}
  Pull the kid and alg from the JWT header and pass to the verify function. Then check expiration.
  If the  token is expired, attempt to refresh."
  [session]
  (let [jwt (str->jwt (:token session))
        jwt-header (:header jwt)
        public-key (get-in @jwks [(:kid jwt-header) :public-key])]
    (if (nil? public-key)
      ;; This kid is unknown, refresh the jwks list
      (do
        (swap! jwks (get-jwks))
        (if (nil? (get-in @jwks [(:kid jwt-header) :public-key]))
          (throw (IllegalArgumentException. (str "Unable to find kid in jwks list: " (:kid jwt-header))))
          ;; Reprocess the request
          (verify-token session)))
      ;; Verify the jwt
      (if (verify jwt (keyword (:alg jwt-header)) public-key)
        ;; Signature is valid
        (let [tn (ZonedDateTime/now (ZoneId/of "Z"))]
          (pprint (str "Comparing " (.toEpochSecond tn) " and " (get-in jwt [:claims :exp])))
          (if (> (.toEpochSecond tn) (get-in jwt [:claims :exp]))
            ;; Expired token, try to refresh
            (refresh-token session)
            ;; Token still valid, no update needed
            {}))
        ;; Token signature verification failed.
        (throw (IllegalArgumentException. "Signature verification failed for token."))))))