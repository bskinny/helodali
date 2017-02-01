(ns helodali.routes
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [re-frame.core :as re-frame :refer [dispatch]]
            [accountant.core :as accountant]
            [clojure.pprint :refer [pprint]]))

;; This approach uses Html5History, which is not availble in older browsers

(accountant/configure-navigation! {:nav-handler (fn [path] (secretary/dispatch! path)) :path-exists? (fn [path] true)})

;; /
(defroute home "/" []
  (dispatch [:change-view :artwork :default]))

;; Display default view for type, e.g. /view/artwork or /view/contacts
(defroute view "/view/:type" [type]
  (dispatch [:change-view (keyword type) :default]))

;; Display specific view for type, e.g. /view/artwork/list
(defroute view-display "/view/:type/:display" [type display]
  (dispatch [:change-view (keyword type) (keyword display)]))

;; Display single item of given type, e.g. /view/artwork/id/<uuid>
(defroute view-single-item "/view/:type/id/:uuid" [type uuid]
  (dispatch [:display-single-item (keyword type) uuid]))

;; Display new item form for given type, e.g. /new/artwork
(defroute new-item "/new/:type" [type]
  (dispatch [:display-new-item (keyword type)]))

;; Display new item form for given type, e.g. /new/artwork
(defroute search "/search/:pattern" [pattern]
  (dispatch [:display-search-results pattern]))

;; Catch all
(defroute "*" []
  (pprint "CATCH ALL, Setting route to artwork")
  (dispatch [:change-view :artwork :default]))

(defn route
  [route-name params]
  (let [path (route-name params)]
    (when (nil? path)
      (pprint (str "Invalid route request: " route-name ", " params)))
    (accountant/navigate! path)
    (accountant/dispatch-current!)))

(defn route-view-display
  [type display]
  (route view-display {:type (name type) :display (name display)}))

(defn route-profile
  []
  (route view {:type (name :profile)}))

(defn route-single-item
  [type uuid]
  (route view-single-item {:type (name type) :uuid uuid}))

(defn route-new-item
  [type]
  (route new-item {:type (name type)}))

(defn route-search
  [search-pattern]
  (route search {:pattern search-pattern}))
