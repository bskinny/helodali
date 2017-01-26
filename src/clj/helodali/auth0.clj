(ns helodali.auth0
  (:require [clj-http.client :as http]
            [cheshire.core :refer [parse-string generate-string]]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [slingshot.slingshot :refer [throw+ try+]]))

(def options {:timeout 900  ;; ms
              ; :debug true
              :user-agent "helodali.com-webapp"
              :trust-store (io/resource "comodo-trust.jks")  ;; The CA signing Auth0 ssl servers
              :throw-exception false
              :as :auto  ;; Try to automatically coerce the output based on the content-type
              :content-type :json
              :headers {:Accept "application/json"}})

(def base-url "https://helodali.auth0.com/")

(defn get-userinfo
  "Given an oauth2 access token issued by Auth0, hit the userinfo endpoint to retrieve an
   openid claims map"
  [access-token]
  (let [userinfo (try+
                   (http/get (str base-url "userinfo/?access_token=" access-token) options)
                   (catch [:status 401] {:keys [request-time headers body]}
                     (pprint (str "access-token invalid: " access-token " at " request-time))
                     nil)
                   (catch Object _
                     (pprint (str (:throwable &throw-context) "unexpected error"))
                     (throw+)))]
     (:body userinfo)))
