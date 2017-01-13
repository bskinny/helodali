(ns helodali.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              ; [devtools.core :as devtools]
              [helodali.events]
              [helodali.subs]
              [helodali.views :as views]
              [helodali.config :as config]))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
