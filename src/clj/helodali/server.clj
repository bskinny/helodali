(ns helodali.server
  (:require [helodali.handler :refer [handler]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3449"))]
    (run-jetty handler {:port port :join? false})))
