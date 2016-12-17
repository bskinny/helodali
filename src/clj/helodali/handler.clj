(ns helodali.handler
  (:require [compojure.core :refer [GET POST ANY defroutes]]
            [compojure.route :refer [resources]]
            [clojure.pprint :refer [pprint]]
            [helodali.db :refer [initialize-db update-item create-item delete-item
                                 update-profile valid-user? refresh-item-path]]
            [helodali.auth0 :as auth0]
            [ring.util.response :refer [content-type response resource-response file-response redirect]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.format-params :refer [wrap-restful-params]]
            [ring.middleware.format-response :refer [wrap-restful-response]]))

(defroutes routes
  (GET "/" []
    (-> (file-response "index.html" {:root "resources/public"})
       (content-type "text/html")))

  (GET "/csrf-token" [] (response {:csrf-token *anti-forgery-token*}))

  (GET "/callback" [:as req]
    (pprint (str "/callback with req: " req))
    (response "Got callback"))

  (POST "/add-image" [file :as req]
    (pprint (str "Handling add-image: " req))
    (response {}))

  (POST "/update-profile" [uuid path val :as req]  ;; TODO: validate uref with login ID
    (pprint (str "update-profile uuid/path/val: " uuid "/" path "/" val))
    (response (update-profile uuid path val)))

  (POST "/update-item" [uref uuid table path val :as req]  ;; TODO: validate uref with login ID
    (pprint (str "update-item uref/uuid/path/val: " uref "/" uuid "/" path "/" val))
    (response (update-item table uref uuid path val)))

  (POST "/create-item" [table item :as req]  ;; TODO: validate uref (within item) with login ID
    (pprint (str "create-item item: " item))
    (response (create-item table item)))

  (POST "/delete-item" [table uref uuid :as req]  ;; TODO: validate uref with login ID
    (pprint (str "delete-item table/uref/uuid: " table "/" uref "/" uuid))
    (response (delete-item table uref uuid)))

  (POST "/refresh-item-path" [uref access-token table item-uuid path :as req]  ;; TODO: validate uref with login ID
      (pprint (str "refresh-item-path table/item-uuid/path: " table "/" item-uuid "/" path))
      (response (refresh-item-path table uref item-uuid path)))

  (POST "/validate-token" [access-token :as req]
    (let [userinfo (auth0/get-userinfo access-token)]
      (pprint (str "/validate-token access-token result: " userinfo))
      (if (nil? userinfo)
        (response {:authenticated? false :access-token nil :id-token nil :delegation-token nil})
        ;; Let the client's cached id-token be used
        (response {:authenticated? true :access-token access-token :delegation-token nil}))))

  (POST "/login" [access-token :as req]
    (let [userinfo (auth0/get-userinfo access-token)]
      (pprint (str "Handle /login with userinfo: " userinfo))
      (when (nil? (:sub userinfo))
        (pprint "No :sub claim found in userinfo: " userinfo))
      (response (initialize-db userinfo))))

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
