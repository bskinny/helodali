(ns helodali.cognito
  (:require [clj-http.client :as http]
            [helodali.db :as db]
            [cheshire.core :refer [parse-string generate-string]]
            [buddy.core.keys :as keys]
            [clj-jwt.core :refer [str->jwt verify]]
            [clojure.pprint :refer [pprint]]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import (java.time ZoneId ZonedDateTime)))

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

(def jwks (atom {}))

(def base-url "https://helodali.auth.us-east-1.amazoncognito.com")
(def jwks-url "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_0cJWyWe5Z/.well-known/jwks.json")

(defn get-jwks
  "Fetch public keys for verifying RSA signatures. The jwks response can look like this:
  [{:alg \"RS256\" :e \"AQAB\"  :kid \"2NySOWekVs775bSUsw/N/mUIZNnOvLU5F63I/qMzOcQ=\"
    :kty \"RSA\" :n \"3453453453...45345345\" :use \"sig\"}
   {:alg \"RS256\" :e \"AQAB\" :kid \"TMd6w/1imj8aIiV0IVVKke5RyIctnQ3A750FVT0SLzk=\"
    :kty \"RSA\" :n \"123123123...123123123\" :use \"sig\"}]

  Return a map which includes the above as well as the public key representation:
    <kid> {:jwk <above list element> :public-key <public-key>}."
  []
  (let [key-set (try+
                  (http/get jwks-url options)
                  (catch Object _
                    (pprint (str (:throwable &throw-context) " unexpected error"))
                    (throw+)))]
    (->> (:body key-set)
         (:keys)
         (reduce #(assoc %1 (:kid %2) {:jwk %2 :public-key (keys/jwk->public-key %2)}) {}))))

(defn init
  "Invoked at server startup...
   - Initialize the jwks map."
  []
  (reset! jwks (get-jwks)))

(defn get-access-token-claims
  "Given access-token as a string, return :claims map."
  [token]
  (:claims (str->jwt token)))

(defn get-token
  "Passed an authorization code, request the access, id and refresh tokens from Cognito.
   Return a map keyed with {:access_token :id_token :refresh_token :access-token-exp :access-token-iat}.
   In order to get the access-token-exp we need to crack open the token."
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
                     (pprint (str (:throwable &throw-context) " unexpected error"))
                     (throw+)))
        at-claims (get-access-token-claims (:access_token response))]
     ;; Keys to the access_token :claims map are:
     ;; {:sub :iss :exp :username :scope :cognito:groups :token_use :auth_time :jti :client_id :version :iat}
     ;; The :exp and :iat keys are epoch valued (based on GMT), e.g. 1571666821.
     (merge response {:access-token-exp (:exp at-claims) :access-token-iat (:iat at-claims)})))

(defn remove-session-and-force-login
  [session msg]
  (db/delete-item :sessions (select-keys session [:uuid]))
  (pprint msg)
  {:force-login true})

(defn refresh-token
  "Passed a session which contains an expired token, use the refresh token to request
   a new token and create a new session in the database. The expired session will be
   eventually expunged by the database."
  [session]
  (pprint "Refreshing token")
  (let [params {:client_id (:id client)
                :grant_type "refresh_token"
                :refresh_token (:refresh session)}]
    (try+
      (let [response (-> (http/post (str base-url "/oauth2/token")
                                    (merge options {:form-params params}))
                         (:body))
            at-claims (get-access-token-claims (:access_token response))]
        (db/cache-access-token (merge response
                                      {:refresh_token (:refresh session)
                                       :access-token-exp (:exp at-claims)
                                       :access-token-iat (:iat at-claims)})
                               (:sub session))
        {:access-token (:access_token response)
         :id-token (:id_token response)
         :refresh-aws-creds? true})
      (catch Object _
        (pprint (:throwable &throw-context))
        (remove-session-and-force-login session "Unable to refresh token, forcing login")))))

(defn verify-token
  "Verify the signature of the access token and check expiration. If expired, attempt a refresh
  and return the new access, id-token. Side effect is made in replacing database session.

  Also ensure that the client_id matches our oauth2 client.

  If token verification fails, insert a :force-login key into the return map.

  The JWT, once decoded form the string, looks like this:
      :header {:kid \"TMd6w/1imj8aIiV0IVVKke5RyIctnQ3A750FVT0SLzk=\"
               :alg \"RS256\"}
      :claims {:sub \"c8eb9e68-ad1e-49bd-925d-e96763a7de2c\"
               :iss \"https://cognito-idp.us-east-1.amazonaws.com/us-east-1_0cJWyWe5Z\"
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
  (when (empty? @jwks)
    (reset! jwks (get-jwks)))
  (let [jwt (str->jwt (:token session))
        jwt-header (:header jwt)
        public-key (get-in @jwks [(:kid jwt-header) :public-key])]
    (if (nil? public-key)
      ;; This kid is unknown, refresh the jwks list
      (do
        (reset! jwks (get-jwks))
        (if (nil? (get-in @jwks [(:kid jwt-header) :public-key]))
          (throw (IllegalArgumentException. (str "Unable to find kid in jwks list: " (:kid jwt-header))))
          ;; Reprocess the request
          (verify-token session)))
      ;; Verify the jwt
      (if (verify jwt (keyword (:alg jwt-header)) public-key)
        ;; Signature is valid, check client_id match.
        (if (not= (:id client) (get-in jwt [:claims :client_id]))
          (remove-session-and-force-login session "JWT client_id mismatch!")
          (let [tn (ZonedDateTime/now (ZoneId/of "Z"))]
            (if (> (.toEpochSecond tn) (get-in jwt [:claims :exp]))
              ;; Expired token, try to refresh
              (refresh-token session)
              ;; Token still valid, no update needed
              {})))
        ;; Token signature verification failed. Force login.
        (remove-session-and-force-login session "Signature verification failed for token.")))))