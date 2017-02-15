(ns helodali.handler
  (:require [compojure.core :refer [GET POST HEAD ANY defroutes]]
            [compojure.route :refer [resources]]
            [clojure.pprint :refer [pprint]]
            [helodali.db :refer [initialize-db update-item create-item delete-item refresh-image-data
                                 update-user-table valid-user? refresh-item-path cache-access-token
                                 valid-session? delete-access-token create-user-if-necessary
                                 create-artwork-from-instragram]]
            [helodali.instagram :refer [refresh-instagram process-instagram-auth]]
            [helodali.auth0 :as auth0]
            [ring.util.response :refer [content-type response resource-response file-response redirect]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.format-params :refer [wrap-restful-params]]
            [ring.middleware.format-response :refer [wrap-restful-response]]))

(defn- process-request
  [uref access-token process-fx]
  (if (valid-session? uref access-token)
    (response (process-fx))
    {:status 400   ;; else return error
     :body {:reason (str "Invalid session for " uref " and given access token")}}))

(defroutes routes
  (GET "/" []
    (-> (resource-response "index.html" {:root "public"})
       (content-type "text/html")))

  (HEAD "/" [] "") ;; For default AWS health checks

  (GET "/health" [] "<html><body><h1>healthy</h1></body></html>")

  (GET "/static/:page" [page]
    (-> (resource-response page {:root "public/static"})
       (content-type "text/html")))

  (GET "/csrf-token" [] (response {:csrf-token *anti-forgery-token*}))

  (POST "/refresh-instagram" [uref access-token max-id :as req]
    (pprint (str "refresh-instagram with uref/max-id: " uref "/" max-id))
    (process-request uref access-token #(refresh-instagram uref max-id)))

  (POST "/create-from-instagram" [uref sub access-token media :as req]
    (pprint (str "create-from-instagram media: " media))
    (process-request uref access-token #(create-artwork-from-instragram uref sub media)))
  (GET "/instagram/oauth/callback" [code state :as req]
    (process-instagram-auth code state)
    (redirect "/"))

  (POST "/update-profile" [uuid path val access-token :as req]
    (pprint (str "update-profile uuid/path/val: " uuid "/" path "/" val))
    (process-request uuid access-token #(update-user-table :profiles uuid path val)))

  (POST "/update-item" [uref uuid table path val access-token :as req]
    (pprint (str "update-item uref/uuid/path/val: " uref "/" uuid "/" path "/" val))
    (process-request uref access-token #(update-item table uref uuid path val)))

  (POST "/create-item" [table item access-token :as req]
    (pprint (str "create-item item: " item))
    (process-request (:uref item) access-token #(create-item table item)))

  (POST "/delete-item" [table uref uuid access-token :as req]
    (pprint (str "delete-item table/uref/uuid: " table "/" uref "/" uuid))
    (process-request uref access-token #(delete-item table uref uuid)))

  (POST "/refresh-item-path" [uref access-token table item-uuid path :as req]
    (pprint (str "refresh-item-path table/item-uuid/path: " table "/" item-uuid "/" path))
    (process-request uref access-token #(refresh-item-path table uref item-uuid path)))

  ;; Refresh image by uuid of image
  (POST "/refresh-image-data" [uref access-token item-uuid image-uuid :as req]
    (pprint (str "refresh-image-data item-uuid/image-uuid: " item-uuid "/" image-uuid))
    (process-request uref access-token #(refresh-image-data uref item-uuid image-uuid)))

  (POST "/validate-token" [access-token :as req]
    (let [userinfo (auth0/get-userinfo access-token)]
      (if (nil? userinfo)
        (response {:authenticated? false :access-token nil :id-token nil :delegation-token nil})
        ;; Let the client's cached id-token be used and cache this access-token
        (do
          (cache-access-token access-token userinfo)
          (response {:authenticated? true :access-token access-token :delegation-token nil})))))

  (POST "/login" [access-token :as req]
    (let [userinfo (auth0/get-userinfo access-token)]
      (pprint (str "Handle /login with userinfo: " userinfo))
      (if (nil? (:sub userinfo))
        (do
          {:message (str "No :sub claim found in userinfo: " userinfo)})
        (do
          (create-user-if-necessary userinfo)
          (cache-access-token access-token userinfo)
          (let [db (initialize-db userinfo)
                uref (get-in db [:profile :uuid])]
            (response (merge db (refresh-instagram uref nil))))))))

  (POST "/logout" [access-token uref :as req]
    (pprint (str "logout " uref))
    (process-request uref access-token #(delete-access-token access-token uref)))

  ;; Redirect any client side routes to "/".
  (ANY "/view/*" [] (redirect "/"))
  (ANY "/new/*" [] (redirect "/"))
  (ANY "/search/*" [] (redirect "/"))
  (ANY "/instagram/*" [] (redirect "/"))

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


(def dev-handler (-> #'routes
                    (wrap-defaults site-defaults)
                    (wrap-restful-params)
                    (wrap-restful-response)
                    (wrap-reload)))

;; Note: I have not successfully configured secure-site-defaults yet. Even with the :proxy option,
;; AWS is not happy with the secure site defaults.
;; see https://github.com/ring-clojure/ring-defaults/blob/master/src/ring/middleware/defaults.clj
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
