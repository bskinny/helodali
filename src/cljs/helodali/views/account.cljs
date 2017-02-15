(ns helodali.views.account
    (:require [helodali.db :as db]
              [helodali.routes :refer [route-single-item]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-date-string
                                     sort-by-datetime]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list
                                              button title checkbox]]))

(defn item-view
  "Display account"
  [id]
  (let [uuid (subscribe [:by-path [:profile :uuid]])
        cn (subscribe [:by-path [:userinfo :name]])
        email (subscribe [:by-path [:userinfo :email]])
        billing-name (subscribe [:by-path [:account :billing-name]])
        billing-street (subscribe [:by-path [:account :billing-street]])
        billing-city (subscribe [:by-path [:account :billing-city]])
        billing-state (subscribe [:by-path [:account :billing-state]])
        billing-zip (subscribe [:by-path [:account :billing-zip]])
        editing (subscribe [:by-path [:account :editing]])]
    (fn []
      (let [header [title :level :level2 :label "Artist Information"]
            view-control [h-box :gap "2px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[row-button :md-icon-name "zmdi zmdi-edit"
                                          :mouse-over-row? true :tooltip "Edit your profile" :tooltip-position :right-center
                                          :on-click #(dispatch [:edit-item [:profile]])]]]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[button :label "Save" :class "btn-default"
                                          :on-click #(dispatch [:save-changes [:profile]])]
                                        [button :label "Cancel" :class "btn-default"
                                          :on-click #(dispatch [:cancel-edit-item [:profile]])]]]
            view [[h-box :gap "8px" :align :center :justify :start
                     :children [[:span.bold @cn]
                                (when (not (nil? @email))
                                  [label :label @email])]]]
                  ; (when (or (not (empty? @birth-place)))
                  ;   [h-box :gap "8px" :align :center :justify :start
                  ;           :children [[:span.uppercase.light-grey "Birth Place"]
                  ;                      [:span (str @birth-place)]]])
                  ; (when (or (not (empty? @currently-resides)))
                  ;   [h-box :gap "8px" :align :center :justify :start
                  ;           :children [[:span.uppercase.light-grey "Currently Resides"]
                  ;                      [:span (str @currently-resides)]]])
            edit [[h-box :gap "8px" :align :center :justify :start
                     :children [[:span.bold @cn]
                                (when (not (nil? @email))
                                  [label :label @email])]]]]
                  ; [h-box :gap "6px" :align :center
                  ;   :children [[:span.uppercase.light-grey "Birth Year"]
                  ;              [input-text :width "60px" :model (str @birth-year) :style {:border "none"}
                  ;                   :on-change #(dispatch [:set-local-item-val [:profile :birth-year] %])]]]
                  ; [h-box :gap "8px" :align :center :justify :between
                  ;         :children [[:span.uppercase.light-grey "Birth Place"]
                  ;                    [input-text :model (str @birth-place) :placeholder "" :width "320px" :style {:border "none"}
                  ;                       :on-change #(dispatch [:set-local-item-val [:profile :birth-place] %])]]]]]
        [v-box :gap "10px" :align :start :justify :start ;:style {:border "dashed 1px red"}
               :children (concat [header] (if @editing edit view) [(if @editing save-control view-control)])]))))

(defn profile-view
  "Display the user's profile"
  []
  (fn []
    [v-box :gap "16px" :align :center :justify :center
       :children [[item-view]]]))
