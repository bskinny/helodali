(ns helodali.views.account
    (:require [helodali.db :as db]
              [helodali.routes :refer [route-single-item]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-date-string
                                     sort-by-datetime]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label modal-panel hyperlink
                                              input-text row-button button title checkbox p border line]]))

(defn delete-account-dialog-markup
  [form-data process-ok process-cancel]
  [v-box :padding  "0px" ;:style    {:background-color "cornsilk"}
      :children [[title :label "Delete My Account" :level :level2]
                 [p "Taking this action will result in the permanent deletion of all data associated with
                            your account."]
                 [checkbox
                  :label     "Please confirm your request to delete all account data"
                  :model     (:confirmation @form-data)
                  :on-change #(swap! form-data assoc :confirmation %)]
                 [line :color "#ddd" :style {:margin "10px 0 10px"}]
                 [h-box :gap "12px"
                   :children [[button :label "Delete" :on-click process-ok :disabled? (not (:confirmation @form-data))]
                              [button :label "Cancel" :on-click process-cancel]]]]])

(defn delete-account-modal-dialog
  "A modal dialog to capture confirmation of account deletion"
  []
  (let [show? (r/atom false)
        initial-form-data {:confirmation false}
        form-data (r/atom initial-form-data)
        process-ok (fn [event]
                     (reset! show? false)
                     ;; Process the deletion
                     (dispatch [:delete-account])
                     false) ;; Prevent default "GET" form submission (if used)
        process-cancel (fn [event]
                         (reset! form-data initial-form-data)
                         (reset! show? false)
                         false)]
    (fn []
      [v-box
       :children [[button :label "Delete Account" :class "btn-info"
                          :on-click #(do
                                       (reset! show? true))]
                  (when @show?
                    [modal-panel :backdrop-color "grey" :backdrop-opacity 0.4
                         :child [delete-account-dialog-markup form-data process-ok process-cancel]])]])))

(defn item-view
  "Display account"
  [id]
  (let [cn (subscribe [:by-path [:userinfo :name]])
        created (subscribe [:by-path [:account :created]])
        instagram-user (subscribe [:by-path [:account :instagram-user]])]
        ;billing-stuff (subscribe [:by-path [:account :billing-stuff]])
        ;editing (subscribe [:by-path [:account :editing]])]
    (fn []
      (let [header [title :level :level2 :label "My Account"]
            view [[h-box :gap "8px" :align :center :justify :start
                     :children [[:span.bold "Created"]
                                (when (not (nil? @created))
                                  [:span (safe-date-string @created)])]]]]
                   ;(when (not (empty? @instagram-user))
                   ;  [h-box :gap "8px" :align :center :justify :start
                   ;          :children [[:span.uppercase.light-grey ""]
                   ;                     [:span (str @birth-place)]]])]]
        [v-box :gap "10px" :align :start :justify :start ;:style {:border "dashed 1px red"}
               :children (concat [header] view)]))))

(defn account-view
  "Display the user's account"
  []
  (fn []
    [v-box :gap "16px" :align :center :justify :center
       :children [[item-view]
                  [re-com/gap :size "24px"]
                  [delete-account-modal-dialog]]]))
