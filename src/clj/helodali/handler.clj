(ns helodali.handler
  (:require [compojure.core :refer [GET POST HEAD ANY defroutes]]
            [compojure.route :refer [resources]]
            [clojure.pprint :refer [pprint]]
            [clj-jwt.core :refer [str->jwt]]
            [helodali.db :as db]
            [helodali.s3 :as s3]
            [helodali.instagram :refer [refresh-instagram process-instagram-auth]]
            [helodali.cognito :as cognito]
            [clj-uuid :as uuid]
            [ring.logger :as logger]
            [ring.logger.protocols :as logger.protocols]
            [ring.logger.tools-logging :refer [make-tools-logging-logger]]
            [ring.util.response :refer [content-type header response resource-response file-response redirect]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.format-params :refer [wrap-restful-params]]
            [ring.middleware.format-response :refer [wrap-restful-response]]
            [slingshot.slingshot :refer [throw+ try+]]))

;; The response to the application which will trigger the login flow
(def force-login-response {:authenticated? false :access-token nil :id-token nil :initialized? false})

(defn- process-request
  [uref access-token process-fx]
  (let [session (db/query-session uref access-token)]
    ;; If the session is not available, force a login
    (if (empty? session)
      (response force-login-response)
      (let [verify-response (cognito/verify-token session)]
        (response (merge (process-fx) verify-response))))))

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
    (pprint "Getting csrf-token.")
    (response {:csrf-token *anti-forgery-token*}))

  (POST "/refresh-instagram" [uref access-token max-id :as req]
    (pprint (str "refresh-instagram with uref/max-id: " uref "/" max-id))
    (process-request uref access-token #(refresh-instagram uref max-id)))

  (POST "/create-from-instagram" [uref sub access-token media :as req]
    (pprint (str "create-from-instagram media: " media))
    (process-request uref access-token #(db/create-artwork-from-instragram uref sub media)))

  (GET "/instagram/oauth/callback" [code state :as req]
    (process-instagram-auth code state)
    (redirect "/"))

  (POST "/update-profile" [uuid path val access-token :as req]
    (pprint (str "update-profile uuid/path/val: " uuid "/" path "/" val))
    (process-request uuid access-token #(db/update-user-table :profiles uuid path val)))

  (POST "/update-item" [uref uuid table path val access-token :as req]
    (pprint (str "update-item uref/uuid/path/val: " uref "/" uuid "/" path "/" val))
    (process-request uref access-token #(db/update-item table uref uuid path val)))

  (POST "/create-item" [table item access-token :as req]
    (pprint (str "create-item item: " item))
    (process-request (:uref item) access-token #(db/create-item table item)))

  (POST "/delete-item" [table uref uuid access-token :as req]
    (pprint (str "delete-item table/uref/uuid: " table "/" uref "/" uuid))
    (process-request uref access-token #(db/delete-item table {:uref uref :uuid uuid})))

  (POST "/refresh-item-path" [uref access-token table item-uuid path :as req]
    (pprint (str "refresh-item-path table/item-uuid/path: " table "/" item-uuid "/" path))
    (process-request uref access-token #(db/refresh-item-path table uref item-uuid path)))

  ;; Refresh image by uuid of image
  (POST "/refresh-image-data" [uref access-token item-uuid image-uuid :as req]
    (pprint (str "refresh-image-data item-uuid/image-uuid: " item-uuid "/" image-uuid " token: " access-token))
    (process-request uref access-token #(db/refresh-image-data uref item-uuid image-uuid)))

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
          (s3/delete-objects :helodali-raw-images (:sub session))
          (s3/delete-objects :helodali-documents (:sub session))))
      (-> (response {})
        ;; Clear the user's information in the session cookie
        (assoc :session (vary-meta session-cookie assoc :recreate true)))))

  ;; Validate the given tokens and replace the tokens with cognito/verify-token if
  ;; the access token is expired.
  (POST "/validate-token" [access-token id-token :as req]
    (let [userinfo (-> id-token str->jwt :claims)]
      (if (nil? userinfo)
        (response force-login-response)
        (let [[_ profile] (db/get-profile-by-sub (:sub userinfo))
              uref (:uuid profile)
              session (db/query-session uref access-token)]
          (if (empty? session)
            ;; No session found for access token, force authentication.
            (response force-login-response)
            (let [verify-response (cognito/verify-token session)
                  ;; Fetch the session again as the tokens might have been refreshed
                  session (db/query-session uref (get verify-response :access-token access-token))]
              (if (empty? session)
                ;; Refresh did not work
                (response force-login-response)
                ;; Token is valid
                (let [db (db/initialize-db (:sub userinfo) session)]
                  (response (merge db (refresh-instagram uref nil)))))))))))

  ;; Check for the need to refresh the access token and do so if necessary.
  (POST "/refresh-token" [access-token id-token :as req]
    (let [userinfo (-> id-token str->jwt :claims)]
      (if (nil? userinfo)
        (response force-login-response)
        (let [[_ profile] (db/get-profile-by-sub (:sub userinfo))
              uref (:uuid profile)
              session (db/query-session uref access-token)]
          (if (empty? session)
            ;; No session found for access token, force authentication.
            (response force-login-response)
            (let [verify-response (cognito/verify-token session)
                  ;; Fetch the session again as the tokens might have been refreshed
                  session (db/query-session uref (get verify-response :access-token access-token))]
              (if (empty? session)
                ;; Refresh did not work
                (response force-login-response)
                ;; Token is valid or has been refreshed, in the former case the response will be a
                ;; empty map, the latter will contain the tokens.
                (response (merge verify-response {:refresh-access-token? false})))))))))

  (GET "/check-session" req
    (pprint (str "Handle /check-session with req: " req))
    (let [sub (get-in req [:session :sub])
          session-uuid (get-in req [:session :uuid])]
      (if (or (empty? session-uuid) (empty? sub))
        (response {})
        (let [[_ profile] (db/get-profile-by-sub sub)
              uref (:uuid profile)
              current-session (db/get-session session-uuid)
              verify-response (cognito/verify-token current-session)
              ;; Fetch the session again as the tokens might have been refreshed
              session (db/query-session uref (get verify-response :access-token (:token current-session)))]
          (if (empty? session)
            ;; Refresh did not work
            (response force-login-response)
            ;; Token is valid
            (let [db (db/initialize-db sub session)]
              (response (merge db (refresh-instagram uref nil)))))))))

  ;; Redirected from Cognito server-side token request
  (GET "/login" [code :as req]
    (pprint (str "Handle /login with req: " req))
    (let [token-resp (cognito/get-token code)]
      (if (nil? token-resp)
        {:message "Unable to get access token"}
        (let [userinfo (-> (:id_token token-resp) str->jwt :claims)]
          (db/create-user-if-necessary userinfo)
          (let [session-uuid (str (uuid/v1))
                uref (db/cache-access-token session-uuid token-resp userinfo)]
            (pprint (str "/login with userinfo from id_token: " userinfo))
            (-> (redirect "/")
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

    ;; Everything else
  (resources "/"))

(defroutes api-routes
  (HEAD "/" [] "") ;; For default AWS health checks
  (GET "/" [] "")
  (GET "/health" [] "<html><body><h1>healthy</h1></body></html>")
  ;; The Instagram subscription callback for GET requests is used for subscription setup.
  (GET "/instagram/subscription-handler" [:as req]
    (pprint (str "REQ: " req))
    (get (:params req) "hub.challenge"))

  (POST "/instagram/subscription-handler" [:as req]
    (pprint (str "POST REQ: " req))
    {:status 200}))


(defn- wrap-debug
  [handler]
  (fn [request]
    (pprint (str "REQUEST: " request))
    (let [resp (handler request)]
      (pprint (str "RESPONSE: " resp))
      resp)))

(def dev-handler (-> #'routes
                    (wrap-defaults site-defaults)
                    (wrap-restful-params)
                    (wrap-restful-response)
                    (wrap-reload)))
                    ; (logger/wrap-with-logger {:logger (let [logger (make-tools-logging-logger)]
                    ;                                     (reify logger.protocols/Logger
                    ;                                       (add-extra-middleware [_ handler] handler)
                    ;                                       (log [_ level throwable message]
                    ;                                         (println "Level " level ": " message)
                    ;                                         (logger.protocols/log logger level throwable (format "%s" message)))))})))

;; Don't use secure-site-defaults. We are using http->https redirection via AWS Elastic Beanstalk load balancing
(def handler (-> #'routes
                (wrap-defaults site-defaults)
                (wrap-restful-params)
                (wrap-restful-response)))

;; The handler for non-browser clients such as Instagram subscriptions. A separate build target
;; references this handler.
(def api-handler (-> #'api-routes
                    (wrap-defaults api-defaults)
                    (wrap-restful-params)
                    (wrap-restful-response)))
