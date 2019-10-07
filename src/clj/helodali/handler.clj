(ns helodali.handler
  (:require [compojure.core :refer [GET POST HEAD ANY defroutes]]
            [compojure.route :refer [resources not-found]]
            [clojure.pprint :refer [pprint]]
            [clj-jwt.core :refer [str->jwt]]
            [helodali.db :as db]
            [helodali.cv :as cv]
            [helodali.s3 :as s3]
            [helodali.common :refer [log]]
            [helodali.instagram :refer [refresh-instagram process-instagram-auth]]
            [helodali.cognito :as cognito]
            [clj-uuid :as uuid]
            ;[ring.logger :as logger]
            ;[ring.logger.protocols :as logger.protocols]
            [ring.logger.tools-logging :refer [make-tools-logging-logger]]
            [ring.util.response :refer [content-type header response resource-response file-response status redirect]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.format-params :refer [wrap-restful-params]]
            [ring.middleware.format-response :refer [wrap-restful-response]]
            [slingshot.slingshot :refer [throw+ try+]]
            [ring.util.response :as response]))


(defn- force-login
  "Trigger the Login flow by disassociating the user's uuid from the session and emptying out
   the cient's access/id tokens. The 'reason' argument is just for logging."
  ([request]
   (force-login request nil))
  ([request reason]
   (when reason (prn (str "Forcing login due to: " reason)))
   (let [session-cookie (-> (:session request)
                            (dissoc :uuid)
                            (dissoc :sub)
                            (dissoc :set-display-type))]
     (-> (response {:authenticated? false :access-token nil :id-token nil :initialized? false})
         (assoc :session (vary-meta session-cookie assoc :recreate true))))))

(defn- process-request
  "Given the user's uuid (uref), access-token provided by the web app, and finally
   the function to be performed (process-fx), confirm the validity of the uref/access-token
   combination and invoke function. If the access-token is expired, a refresh is attempted
   with the new tokens (access and id) being returned in addition to the process-fx result.
   Because of this, we expect process-fx to return a map which can be merged into and absorbed
   by the web app."
  [req uref access-token process-fx]
  (let [session (db/query-session uref access-token)]
    ;; If the session is not available, force a login
    (if (empty? session)
      (force-login req "No matching session found in the database")
      (let [verify-response (cognito/verify-token session)]
        (if (:force-login verify-response)
          (force-login req "Token verification failed")
          (response (merge (process-fx) verify-response)))))))

(defn- process-blob-request
  "Similar to the above process-request except that we expect the process-fx function to return
   binary data and hence we cannot tack on refreshed access/id tokens to the response. We can
   still redirect to force-login."
  [req uref access-token process-fx]
  (let [session (db/query-session uref access-token)]
    ;; If the session is not available, force a login
    (if (empty? session)
      (force-login req "No matching session found in the database")
      (let [verify-response (cognito/verify-token session)]
        ;; If token verification failed or a refresh occurred, then force a login,
        (if (or (:force-login verify-response) (:access-token verify-response))
          (force-login req "Token verification failed or a refresh occurred")
          ;; We expect process-fx to create the response
          (process-fx))))))

(defn- login-response
  [req sub session uref]
  (let [set-display-type (get-in req [:session :set-display-type])
        db (cond-> (db/initialize-db sub session)
                   true (merge (refresh-instagram uref nil))
                   set-display-type (assoc :display-type set-display-type))]
    (cond-> (response db)
            set-display-type (assoc :session (vary-meta (dissoc (:session req) :set-display-type) assoc :recreate true)))))

(defn document-response [filename content-type bytes]
  (with-open [in (java.io.ByteArrayInputStream. bytes)]
   (-> (response/response in)
       (response/header "Content-Disposition" (str "filename=" filename))
       (response/header "Content-Length" (count bytes))
       (response/content-type content-type))))

(defn generate-cv
  [uref]
  (try
    (let [out (java.io.ByteArrayOutputStream.)]
      (cv/generate-cv out)
      (document-response "cv.pdf" "application/pdf" (.toByteArray out)))
    (catch Exception e
      (pprint (str "Exception generating cv: " e))
      (-> (response {})
          (status 500)))))

(defroutes routes
  (GET "/" []
    (-> (resource-response "index.html" {:root "public"})
       (content-type "text/html")))

  (HEAD "/" [] "") ;; For default AWS health checks

  (GET "/health" [] "<html><body><h1>healthy</h1></body></html>")

  (GET "/static/:page" [page]
    (-> (resource-response page {:root "public/static"})
       (content-type "text/html")))

  (GET "/csrf-token" []
    (response {:csrf-token *anti-forgery-token*}))

  (POST "/refresh-instagram" [uref access-token max-id :as req]
    (pprint (str "refresh-instagram with uref/max-id: " uref "/" max-id))
    (process-request req uref access-token #(refresh-instagram uref max-id)))

  (POST "/create-from-instagram" [uref cognito-identity-id access-token media :as req]
    (pprint (str "create-from-instagram media: " media))
    (process-request req uref access-token #(db/create-artwork-from-instragram uref cognito-identity-id media)))

  (GET "/instagram/oauth/callback" [code state]
    (process-instagram-auth code state)
    (-> (redirect "/")
      ;; Stash instruction for the UI to display the Instagram view.
      (assoc-in [:session :set-display-type] :instagram)))

  (POST "/update-user-table" [uuid table path val access-token :as req]
    (pprint (str "update-profile uuid/table/path/val: " uuid "/" table "/" path "/" val))
    (process-request req uuid access-token #(db/update-user-table table uuid path val)))

  (POST "/update-identity-id" [access-token uref sub identity-id :as req]
    (pprint (str "storing uref/sub/identity-id in openid table: " uref "/" sub "/" identity-id))
    ;; This update of the identity-id should only be necessary after login and association with
    ;; an identity in the Cognito Identity Pool. Otherwise, DynamoDB does not create a write
    ;; for the case where there is no change in the attribute value.
    (process-request req uref access-token #(db/update-generic :openid {:sub sub} [:identity-id] identity-id)))

  (POST "/update-item" [uref uuid table path val access-token :as req]
    (pprint (str "update-item uref/uuid/table/path/val: " uref "/" uuid "/" table "/" path "/" val))
    (process-request req uref access-token #(db/update-item table uref uuid path val)))

  (POST "/create-item" [table item access-token :as req]
    (pprint (str "create-item in " table ": " item))
    (process-request req (:uref item) access-token #(db/create-item table item)))

  (POST "/delete-item" [table uref uuid access-token :as req]
    (pprint (str "delete-item table/uref/uuid: " table "/" uref "/" uuid))
    (process-request req uref access-token #(db/delete-item table {:uref uref :uuid uuid})))

  (POST "/refresh-item-path" [uref access-token table item-uuid path :as req]
    (pprint (str "refresh-item-path table/item-uuid/path: " table "/" item-uuid "/" path))
    (process-request req uref access-token #(db/refresh-item-path table uref item-uuid path)))

  ;; Refresh image by uuid of image
  (POST "/refresh-image-data" [uref access-token item-uuid image-uuid :as req]
    (pprint (str "refresh-image-data item-uuid/image-uuid: " item-uuid "/" image-uuid))
    (process-request req uref access-token #(db/refresh-image-data uref item-uuid image-uuid)))

  (POST "/generate-cv" [access-token uref :as req]
    (pprint (str "Handle /generate-cv with req: " req))
    (process-blob-request req uref access-token #(generate-cv uref)))

  (POST "/delete-account" [access-token uref :as req]
    (pprint (str "Handle /delete-account with req: " req))
    (let [session (db/query-session uref access-token)
          session-cookie (-> (:session req)
                             (dissoc :uuid)
                             (dissoc :sub))]
      ;; If the session is found, then delete the user
      (when (not (empty? session))
        (db/delete-item :sessions (select-keys session [:uuid]))
        ;; Delete all items in Dynamo DB associated with the user
        (db/delete-user uref (:sub session))
        ;; Delete all s3 objects (removing helodali-raw-images will trigger the lambda function to
        ;; remove the associated helodali-images object)
        (when (not (empty? (:sub session)))
          ;; Making sure the prefix to s3/delete-objects is not nil
          (s3/delete-objects-by-prefix :helodali-raw-images (:sub session))
          (s3/delete-objects-by-prefix :helodali-documents (:sub session))))
          ; TODO: Remove public pages
      (-> (response {})
        ;; Clear the user's information in the session cookie
        (assoc :session (vary-meta session-cookie assoc :recreate true)))))

  ;; Validate the given tokens and replace the tokens with cognito/verify-token if
  ;; the access token is expired.
  (POST "/validate-token" [access-token id-token :as req]
    (log "Handle /validate-token with req" req)
    (if (or (empty? access-token) (empty? id-token))
      (force-login req "Access or id token is empty")
      (let [userinfo (-> id-token str->jwt :claims)]
        (if (nil? userinfo)
          (force-login req "Userinfo claim in id-token is empty")
          (let [[_ profile] (db/get-profile-by-sub (:sub userinfo))
                uref (:uuid profile)
                session (db/query-session uref access-token)]
            (if (empty? session)
              ;; No session found for access token, force authentication.
              (force-login req "No matching session found in the database")
              (let [verify-response (cognito/verify-token session)]
                (if (:force-login verify-response)
                  (force-login req)
                  ;; Fetch the session again as the tokens might have been refreshed
                  (let [session (db/query-session uref (get verify-response :access-token access-token))]
                    (if (empty? session)
                      ;; Refresh did not work
                      (force-login req)
                      ;; Token is valid
                      (login-response req (:sub userinfo) session uref)))))))))))

  ;; Check for the need to refresh the access token and do so if necessary.
  (POST "/refresh-token" [access-token id-token :as req]
    (log "Handle /refresh-token with req" req)
    (if (or (empty? access-token) (empty? id-token))
      (force-login req "Access or id token is empty")
      (let [userinfo (-> id-token str->jwt :claims)]
        (if (nil? userinfo)
          (force-login req "Userinfo claim in id-token is empty")
          (let [[_ profile] (db/get-profile-by-sub (:sub userinfo))
                uref (:uuid profile)
                session (db/query-session uref access-token)]
            (if (empty? session)
              ;; No session found for access token, force authentication.
              (force-login req "No matching session found in the database")
              (let [verify-response (cognito/verify-token session)]
                (if (:force-login verify-response)
                  (force-login req "Unable to verify token with Cognito")
                  ;; Token is valid or has been refreshed, in the former case the response will be a
                  ;; empty map, the latter will contain the tokens.
                  (response (merge verify-response {:refresh-access-token? false}))))))))))

  (GET "/check-session" req
    (log "Handle /check-session with req" req)
    (let [sub (get-in req [:session :sub])
          session-uuid (get-in req [:session :uuid])]
      (if (or (empty? session-uuid) (empty? sub))
        (response {})
        (let [[_ profile] (db/get-profile-by-sub sub)
              uref (:uuid profile)
              current-session (db/get-session session-uuid)]
          (if (empty? current-session)
            (force-login req "Unable to find session in database")
            (let [verify-response (cognito/verify-token current-session)]
              (if (:force-login verify-response)
                (force-login req "Unable to verify token with Cognito")
                ;; If the verify-response is non-empty, then it contains refreshed tokens which we want to send back
                ;; to the app as part of the app-db initialization.
                (if (empty? verify-response)
                  (let [session (db/query-session uref (get verify-response :access-token (:token current-session)))]
                    (login-response req sub session uref))
                  (login-response req sub verify-response uref)))))))))

  ;; Redirected from Cognito server-side token request
  (GET "/login" [code :as req]
    (log "Handle /login with req" req)
    (let [token-resp (cognito/get-token code)]
      (if (nil? token-resp)
        {:message "Unable to get access token"}
        (let [userinfo (-> (:id_token token-resp) str->jwt :claims)]
          (db/create-user-if-necessary userinfo)
          (let [session-uuid (str (uuid/v1))]
            (db/cache-access-token session-uuid token-resp userinfo)
            (log (str "/login session " session-uuid " with userinfo from id_token") userinfo)
            (->
              (resource-response "index.html" {:root "public"})
              (content-type "text/html")
              ;; Stash the user's uuid in the session
              (assoc-in [:session :sub] (:sub userinfo))
              (assoc-in [:session :uuid] session-uuid)))))))

  (POST "/logout" [access-token uref :as req]
    (pprint (str "Handle /logout with req: " req))
    (let [session (db/query-session uref access-token)
          session-cookie (-> (:session req)
                             (dissoc :uuid)
                             (dissoc :sub))]
      ;; If the session is found, then delete it
      (when (not (empty? session))
        (db/delete-item :sessions (select-keys session [:uuid])))
      (-> (response {})
        ;; Clear the user's information in the session
        (assoc :session (vary-meta session-cookie assoc :recreate true)))))

  ;; Redirect any client side routes to "/".
  (ANY "/view/*" [] (redirect "/"))
  (ANY "/new/*" [] (redirect "/"))
  (ANY "/search/*" [] (redirect "/"))
  (ANY "/instagram/*" [] (redirect "/"))

  (GET "/js/compiled/:js" [js]
    (pprint (str "Handling GET for " js))
    (-> (resource-response js {:root "public/js/compiled"})
       (header "Cache-Control" "max-age=86400, must-revalidate")))

  ;; Everything else is either a public resource or not-found.
  (resources "/")
  (not-found "Not Found"))


;; Don't use secure-site-defaults. We are using http->https redirection via AWS Elastic Beanstalk load balancing
(def handler (-> #'routes
                (wrap-defaults site-defaults)
                (wrap-restful-params)
                (wrap-restful-response)))
