(ns helodali.server
  (:require [helodali.handler :refer [handler]]
            [config.core :refer [env]]
            [helodali.cognito :as cognito]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3449"))]
    (cognito/init)
    (run-jetty handler {:port port :join? false})))
