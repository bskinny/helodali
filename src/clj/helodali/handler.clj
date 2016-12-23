(ns helodali.handler
  (:require [compojure.core :refer [GET POST ANY defroutes]]
            [compojure.route :refer [resources]]
            [clojure.pprint :refer [pprint]]
            [helodali.db :refer [initialize-db update-item create-item delete-item refresh-image-data
                                 update-user-table valid-user? refresh-item-path cache-access-token
                                 valid-session?]]
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
     :headers {}
     :body (str "Invalid session for " uref " and given access token")}))

(defroutes routes
  (GET "/" []
    (-> (file-response "index.html" {:root "resources/public"})
       (content-type "text/html")))

  (GET "/csrf-token" [] (response {:csrf-token *anti-forgery-token*}))

  (POST "/update-profile" [uuid path val access-token :as req]
    (pprint (str "update-profile uuid/path/val: " uuid "/" path "/" val))
    (process-request uuid access-token (partial (update-user-table :profiles uuid path val))))

  (POST "/update-item" [uref uuid table path val access-token :as req]
    (pprint (str "update-item uref/uuid/path/val: " uref "/" uuid "/" path "/" val))
    (process-request uref access-token (partial (update-item table uref uuid path val))))

  (POST "/create-item" [table item access-token :as req]
    (pprint (str "create-item item: " item))
    (process-request (:uref item) access-token (partial (create-item table item))))

  (POST "/delete-item" [table uref uuid access-token :as req]
    (pprint (str "delete-item table/uref/uuid: " table "/" uref "/" uuid))
    (process-request uref access-token (partial (delete-item table uref uuid))))

  (POST "/refresh-item-path" [uref access-token table item-uuid path :as req]
    (pprint (str "refresh-item-path table/item-uuid/path: " table "/" item-uuid "/" path))
    (process-request uref access-token (partial (refresh-item-path table uref item-uuid path))))

  ;; Refresh image by uuid of image
  (POST "/refresh-image-data" [uref access-token item-uuid image-uuid :as req]
    (pprint (str "refresh-image-data item-uuid/image-uuid: " item-uuid "/" image-uuid))
    (process-request uref access-token (partial (refresh-image-data uref item-uuid image-uuid))))

  (POST "/validate-token" [access-token :as req]
    (let [userinfo (auth0/get-userinfo access-token)]
      (pprint (str "/validate-token access-token result: " userinfo))
      (if (nil? userinfo)
        (response {:authenticated? false :access-token nil :id-token nil :delegation-token nil})
        ;; Let the client's cached id-token be used and cache this access-token
        (do
          (cache-access-token access-token userinfo)
          (response {:authenticated? true :access-token access-token :delegation-token nil})))))

  (POST "/login" [access-token :as req]
    (let [userinfo (auth0/get-userinfo access-token)]
      (pprint (str "Handle /login with userinfo: " userinfo))
      ; (pprint (str "  And req: " req))
      (if (nil? (:sub userinfo))
        (do
          (pprint "No :sub claim found in userinfo: " userinfo)
          {})    ;; TODO: send back an error message?
        (do
          (cache-access-token access-token userinfo)
          (response (initialize-db userinfo))))))

  ;; Redirect any client side routes to "/".
  (ANY "/view/*" [] (redirect "/"))
  (ANY "/new/*" [] (redirect "/"))
  (ANY "/search/*" [] (redirect "/"))

  ;; Everything else
  (resources "/"))

(def dev-handler (-> #'routes
                    ; (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
                    (wrap-defaults site-defaults)
                    (wrap-restful-params)
                    (wrap-restful-response)
                    (wrap-reload)))

;; TODO eventually switch from site-defaults to secure-site-defaults
;; see https://github.com/ring-clojure/ring-defaults/blob/master/src/ring/middleware/defaults.clj
(def handler (-> #'routes
                ; (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
                (wrap-defaults site-defaults)
                (wrap-restful-params)
                (wrap-restful-response)))
