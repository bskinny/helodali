(ns helodali.views.pages
  (:require [helodali.db :as db]
            [helodali.routes :refer [route-single-item]]
            [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-date-string
                                   sort-by-datetime uuid-label-list-to-options]]
            [cljs.pprint :refer [pprint]]
            [cljs-time.format :refer [unparse formatters]]
            [reagent.core  :as r]
            [re-frame.core :as re-frame :refer [dispatch subscribe]]
            [re-com.core :as re-com :refer [box v-box h-box label modal-panel hyperlink single-dropdown md-icon-button
                                            input-textarea input-text info-button button title checkbox p border line]]))

;; The page-name value for each exhibition is the relative uri and cannot contain characters such as
;; Space # $ % & @ ` / : ; < = > ? [ \\ ] ^ { | } ~ “ ‘ + ,
;; We will use a white list method instead of simply a-Z 0-9 . - _
(def page-name-info
     [v-box
      :children [[:p.info-heading "Page Name (required)"]
                 [:p "This name defines the exhibition portion of the website URL, such as the value "
                  [:span.info-bold "recent-artwork"] " in the URL http://website/" [:span.info-bold "recent-artwork"]
                     "/images/... class to make text bold."]
                 [:p "The following characters are allowed in the page name:"]
                 [:code "a-z A-Z 0-9 . _ -"]]])

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

(defn display-exhibition-view
  [idx odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        exhibition (subscribe [:by-path [:pages :public-exhibitions idx]])]
    (fn []
      [v-box :gap "8px" :justify :start :align :start :padding "10px" ;:width "100%"
       :style {:background bg-color :border-radius "4px"}
       :children [[h-box :gap "4px" :align :start :justify :center ;:width "100%" ;:style {:border "dashed 1px red"}
                               :children [[label :width "11ch" :class "uppercase light-grey" :label "Exhibition:"]
                                          [label :class "italic" :label (if (not (nil? (:page-name @exhibition)))
                                                                          (str "/" (:page-name @exhibition))
                                                                          "(undefined)")]]]
                  ;; Display the statement from the public-exhibition, not the exhibition itself.
                  (when (not (empty? (:statement @exhibition)))
                    [h-box :gap "6px" :align :start ;:style {:border "dashed 1px red"}
                     :children [[label :width "12ch" :class "uppercase light-grey" :label "Statement: "]
                                [re-com/box :max-width "360px" :child [:p (:statement @exhibition)]]]])]])))

(defn display-exhibition-edit
  [idx odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        exhibition-uuid (subscribe [:by-path [:pages :public-exhibitions idx :ref]])
        exhibitions (subscribe [:items-vals-with-uuid :exhibitions :name]) ;; this is a list of 2-tuples [uuid name]
        statement (subscribe [:by-path [:pages :public-exhibitions idx :statement]])
        page-name (subscribe [:by-path [:pages :public-exhibitions idx :page-name]])]
    (fn []
      [v-box :gap "4px" :justify :start :align :start :padding "10px"
       :style {:background bg-color :border "1px solid lightgray" :border-radius "4px"} :width "100%"
       :children [[h-box :gap "6px" :align :center
                   :children [[:span.uppercase.bold (str "Exhibition")]
                              [h-box :gap "6px" :align :center
                               :children [[single-dropdown :choices (uuid-label-list-to-options @exhibitions) :model (if (nil? @exhibition-uuid) :none exhibition-uuid) :width "200px"
                                           :on-change #(if (and (not= @exhibition-uuid %) (not (and (= @exhibition-uuid nil) (= % :none))))
                                                         (dispatch [:set-local-item-val [:pages :public-exhibitions idx :ref] (if (= :none %) nil %)]))]]]
                              [h-box :gap "6px" :align :center
                               :children [[:span.uppercase.bold "Page Name (single word)"]
                                          [input-text :width "280px" :model (str @page-name) :style {:border "none"} :validation-regex #"^[\w\.\-]*$"
                                                :on-change #(dispatch [:set-local-item-val [:pages :public-exhibitions idx :page-name] %])]
                                          [info-button :info page-name-info]]]]]
                  [h-box :gap "6px" :align :center :justify :between :align-self :stretch
                   :children [[h-box :gap "6px" :align :center
                                 :children [[:span.uppercase.light-grey "Statement "]
                                            [input-textarea :model (str @statement) :width "520px"
                                             :rows 4 :on-change #(dispatch [:set-local-item-val [:pages :public-exhibitions idx :statement] %])]]]
                              [button :label "Delete" :class "btn-default"
                               :on-click #(dispatch [:delete-local-vector-element [:pages :public-exhibitions] idx])]]]]])))

(defn item-view
  "Display form"
  []
  (let [enabled (subscribe [:by-path [:pages :enabled]])
        public-exhibitions (subscribe [:by-path-and-deref-sorted-by [:pages :public-exhibitions] :exhibitions (partial sort-by-datetime :begin-date true)])
        display-name (subscribe [:by-path [:pages :display-name]])
        description (subscribe [:by-path [:pages :description]])
        editing (subscribe [:by-path [:pages :editing]])]
    (fn []
      (let [view-control [h-box :gap "12px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                          :children [[md-icon-button :md-icon-name "zmdi zmdi-edit" ;:size :larger
                                        :tooltip "Edit Settings" :tooltip-position :right-center ; :mouse-over-row? true
                                        :on-click #(dispatch [:edit-item [:pages]])]]]
                                     ;(when @enabled
                                     ;  [button :label "Publish Changes" :class "btn-default" :on-click #(dispatch [:publish-pages []])])]]
            view [[checkbox :model enabled :label (if @enabled "Website Enabled" "Website Disabled") :disabled? true :on-change #(dispatch [:noop []])]
                  (when @enabled
                    [button :label "Publish Updates" :class "btn-info" :on-click #(dispatch [:publish-pages])])
                  (when @display-name
                    [title :level :level2 :label @display-name])
                  (when @description
                    [p @description])
                  (when (not (empty? @public-exhibitions))
                    [v-box :gap "16px" :align :start :justify :start
                     :children (into [] (mapv (fn [idx bg] ^{:key (str "exhibition-" idx)} [display-exhibition-view idx bg])
                                              (range (count @public-exhibitions)) (cycle [true false])))])]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                           :children [[button :label "Save" :class "btn-default" :on-click #(dispatch [:save-changes [:pages]])]
                                      [button :label "Cancel" :class "btn-default" :on-click #(dispatch [:cancel-edit-item [:pages]])]]]
            edit [[checkbox :model enabled :label "Enable Website"
                   :on-change #(dispatch [:set-local-item-val [:pages :enabled] (not @enabled)])]
                  [h-box :gap "8px" :align :center :justify :between
                   :children [[:span.uppercase.bold "Artist Display Name"]
                              [input-text :model (str @display-name) :placeholder "" :width "320px" :style {:border "none"}
                                          :on-change #(dispatch [:set-local-item-val [:pages :display-name] %])]]]
                  [:span.uppercase.light-grey "Description (optional)"]
                  [input-textarea :model (str @description) :width "360px"
                        :rows 4 :on-change #(dispatch [:set-local-item-val [:pages :description] %])]
                  [h-box :gap "6px" :align :center :justify :start
                   :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Add an Exhibition record"
                               :on-click #(dispatch [:create-local-vector-element [:pages :public-exhibitions] (db/default-public-exhibition)])]
                              [:span "Exhibitions"]]]
                  (when (not (empty? @public-exhibitions))
                    [v-box :gap "16px" :align :start :justify :start :align-self :stretch
                     :children (into [] (mapv (fn [idx bg] ^{:key (str "exhibition-" idx)} [display-exhibition-edit idx bg])
                                              (range (count @public-exhibitions)) (cycle [true false])))])]]
        [v-box :gap "10px" :align :start :justify :start ;:style {:border "dashed 1px red"}
         :children (concat (if @editing edit view)
                           (if @editing [save-control] [view-control]))]))))

(defn pages-view
  "Display the user's public page form."
  []
  (fn []
    [v-box :gap "16px" :align :center :justify :center
     :children [[title :level :level2 :label "Publish as a Website"]
                [p "Enabling this feature creates a publicly accessible copy of your data organized as a website
                    with a list of exhibitions. Only the images you assign to exhibitions are visible to the public
                    and only relevant artwork information such as title, date, sold, etc. is included."]
                [item-view]
                [re-com/gap :size "24px"]]]))
                ;[delete-account-modal-dialog]]]))
