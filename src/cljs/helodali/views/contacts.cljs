(ns helodali.views.contacts
    (:require [helodali.views.referred-artwork :refer [referred-artwork-list-view]]
              [helodali.routes :refer [route-single-item route-new-item route-view-display]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-string sort-by-key-then-created]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list
                                              button]]))

(def role-options [{:id :person :label "Person"}
                   {:id :company :label "Company"}
                   {:id :dealer :label "Dealer"}
                   {:id :agent :label "Agent"}
                   {:id :gallery :label "Gallery"}
                   {:id :institution :label "Institution"}])

(defn contact-referenced-in-artwork?
  "Filter function to determine if given contact is referred to in given artwork item, namely the purchases."
  [contact-uuid item]
  (not (empty? (filter #(or (= contact-uuid (:buyer %)) (= contact-uuid (:dealer %)) (= contact-uuid (:agent %))) (:purchases item)))))

(defn item-view
  "Display an item"
  [id]
  (let [uuid (subscribe [:item-key :contacts id :uuid])
        cn (subscribe [:item-key :contacts id :name])
        role (subscribe [:item-key :contacts id :role])
        email (subscribe [:item-key :contacts id :email])
        phone (subscribe [:item-key :contacts id :phone])
        url (subscribe [:item-key :contacts id :url])
        address (subscribe [:item-key :contacts id :address])
        instagram (subscribe [:item-key :contacts id :instagram])
        facebook (subscribe [:item-key :contacts id :facebook])
        notes (subscribe [:item-key :contacts id :notes])
        editing (subscribe [:item-key :contacts id :editing])
        display-type (subscribe [:app-key :display-type])]
    (fn []
      (let [view-control [h-box :gap "12px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                           :children [[row-button :md-icon-name "zmdi zmdi-edit"
                                        :mouse-over-row? true :tooltip "Edit this item" :tooltip-position :right-center
                                        :on-click #(dispatch [:edit-item [:contacts id]])]
                                      [row-button :md-icon-name "zmdi zmdi-delete"
                                        :mouse-over-row? true :tooltip "Delete this item" :tooltip-position :right-center
                                        :on-click #(dispatch [:delete-item :contacts id])]]]
            view [[h-box :gap "18px" :align :center :justify :between
                    :children [[:span.bold @cn]
                               [:span.all-small-caps (clojure.string/replace (name @role) #"-" " ")]]]
                  (when (not (empty? @email))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "email"]
                                       [:span @email]]])
                  (when (not (empty? @phone))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "phone"]
                                       [:span @phone]]])
                  (when (not (empty? @url))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "url"]
                                       [re-com/hyperlink-href :label (trunc (str @url) 50) :href (url-to-href @url) :target "_blank"]]])
                  (when (not (empty? @instagram))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "instagram"]
                                       [:span.italic @instagram]]])
                  (when (not (empty? @facebook))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "facebook"]
                                       [:span.italic @facebook]]])
                  (when (not (empty? @address))
                    [v-box :gap "4px" :align :start :justify :start :max-width "480px"
                            :children [[:span.uppercase.light-grey "address"]
                                       [:span @address]]])
                  (when (not (empty? @notes))
                    [v-box :gap "4px" :align :start :justify :start :max-width "480px"
                            :children [[:span.uppercase.light-grey "notes"]
                                       [:pre @notes]]])]
            view-box [v-box :gap "10px" :align :start :justify :start :children view]
            create-control [h-box :gap "30px" :align :center
                              :children [[button :label "Create" :class "btn-default"
                                           :on-click #(dispatch [:create-from-placeholder :contacts [:name]])]
                                         [button :label "Cancel" :class "btn-default"
                                           :on-click #(dispatch [:delete-item :contacts id])]]]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[button :label "Save" :class "btn-default"
                                          :on-click #(dispatch [:save-changes [:contacts id]])]
                                        [button :label "Cancel" :class "btn-default"
                                          :on-click #(dispatch [:cancel-edit-item [:contacts id]])]]]
            edit [[h-box :gap "8px" :align :center :justify :between
                    :children [[:span.uppercase.bold "Name"]
                               [input-text :model (str @cn) :placeholder "Of person, gallery or institution" :width "240px" :style {:border "none"}
                                  :on-change #(dispatch [:set-local-item-val [:contacts id :name] %])]
                               [single-dropdown :choices role-options :width "118px" :model @role
                                       :on-change #(dispatch [:set-local-item-val [:contacts id :role] %])]]]
                  [h-box :gap "4px" :align :center
                    :children [[:span.uppercase.light-grey "email"]
                               [input-text :width "280px" :model (str @email) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:contacts id :email] %])]]]
                  [h-box :gap "4px" :align :center
                    :children [[:span.uppercase.light-grey "phone"]
                               [input-text :width "280px" :model (str @phone) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:contacts id :phone] %])]]]
                  [h-box :gap "4px" :align :center
                    :children [[:span.uppercase.light-grey "url"]
                               [input-text :width "280px" :model (str @url) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:contacts id :url] %])]]]
                  [h-box :gap "4px" :align :center
                    :children [[:span.uppercase.light-grey "instagram"]
                               [input-text :width "140px" :model (str @instagram) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:contacts id :instagram] %])]]]
                  [h-box :gap "4px" :align :center
                    :children [[:span.uppercase.light-grey "facebook"]
                               [input-text :width "140px" :model (str @facebook) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:contacts id :facebook] %])]]]
                  [:span.uppercase.light-grey "Address"]
                  [input-textarea :model (str @address) :width "360px"
                      :rows 4 :on-change #(dispatch [:set-local-item-val [:contacts id :address] %])]
                  [:span.uppercase.light-grey "Notes"]
                  [input-textarea :model (str @notes) :width "360px"
                      :rows 4 :on-change #(dispatch [:set-local-item-val [:contacts id :notes] %])]]
            edit-box [v-box :gap "10px" :align :start :justify :start :children edit]]
        [v-box :gap "10px" :align :center :justify :start ;:style {:border "dashed 1px red"}
               :children (concat (if @editing [edit-box] [view-box])
                                 (if (= @display-type :new-item)
                                   [create-control]
                                   (if @editing [save-control] [view-control]))
                                 (when (not @editing)
                                   [[referred-artwork-list-view (partial contact-referenced-in-artwork? @uuid)]]))]))))

(defn item-list-view
  "Display item properties in single line - no image display. The 'widths' map contains the string
   length of the longest name, email, etc. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for name, by truncating the strings over the max.

   Some columns are not displayed if there is no data available. E.g. if 'condition' is not defined
   for any contacts"
  [widths id odd-row?]
  (let [uuid (subscribe [:item-key :contacts id :uuid])
        bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        cn (subscribe [:item-key :contacts id :name])
        email (subscribe [:item-key :contacts id :email])
        phone (subscribe [:item-key :contacts id :phone])
        role (subscribe [:item-key :contacts id :role])
        url (subscribe [:item-key :contacts id :url])
        instagram (subscribe [:item-key :contacts id :instagram])]
    (fn []
      [h-box :align :center :justify :start :style {:background bg-color} :width "100%"
        :children [[hyperlink :style {:width (str (max 18 (get widths :name)) "ch")} :label (trunc (safe-string @cn "(no name)") (get widths :name))
                       :on-click #(route-single-item :contacts @uuid)]
                   [label :width "12ch" :class "all-small-caps" :label (clojure.string/replace (name @role) #"-" " ")]
                   [label :width (str (max 18 (:email widths)) "ch") :label (trunc @email (:email widths))]
                   [label :width "15ch" :label @phone]
                   [re-com/hyperlink-href :style {:width (str (max 18 (get widths :url)) "ch")}
                               :label (trunc (str @url) (:url widths)) :href (str (url-to-href @url)) :target "_blank"]
                   [label :width "18ch" :label (str @instagram)]
                   [h-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                      :children [[row-button :md-icon-name "zmdi zmdi-copy"
                                   :mouse-over-row? true :tooltip "Copy this item"
                                   :on-click #(dispatch [:copy-item :contacts id :name])]
                                 [row-button :md-icon-name "zmdi zmdi-delete"
                                   :mouse-over-row? true :tooltip "Delete this item"
                                   :on-click #(dispatch [:delete-item :contacts id])]]]]])))

(defn list-view
  "Display list of items, one per line"
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :contacts]])
        items (subscribe [:items-keys-sorted-by-key :contacts sort-by-key-then-created])
        names (subscribe [:items-vals :contacts :name])
        emails (subscribe [:items-vals :contacts :email])
        urls (subscribe [:items-vals :contacts :url])]
    (fn []
      (let [widths (r/atom {:name (+ 2 (max-string-length @names 80))
                            :email (+ 2 (max-string-length @emails 40))
                            :url (+ 2 (max-string-length @urls 40))})
            header [h-box :align :center :justify :start :width "100%"
                      :children [[hyperlink :class "uppercase" :style {:width (str (max 18 (:name @widths)) "ch")}
                                     :label "Contact" :tooltip "Sort by Title"
                                     :on-click #(if (= (first @sort-key) :name)
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts 1] (not (second @sort-key))])
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts] [:name true]]))]
                                 [hyperlink :class "uppercase" :style {:width "12ch"}
                                     :label "role" :tooltip "Sort by Role"
                                     :on-click #(if (= (first @sort-key) :role)
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts 1] (not (second @sort-key))])
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts] [:role true]]))]
                                 [hyperlink :class "uppercase" :style {:width (str (max 18 (:email @widths)) "ch")}
                                     :label "email" :tooltip "Sort by Email"
                                     :on-click #(if (= (first @sort-key) :email)
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts 1] (not (second @sort-key))])
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts] [:email true]]))]
                                 [hyperlink :class "uppercase" :style {:width "15ch"}
                                     :label "phone" :tooltip "Sort by Phone"
                                     :on-click #(if (= (first @sort-key) :phone)
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts 1] (not (second @sort-key))])
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts] [:phone true]]))]
                                 [hyperlink :class "uppercase" :style {:width (str (max 18 (:url @widths)) "ch")}
                                     :label "url" :tooltip "Sort by URL"
                                     :on-click #(if (= (first @sort-key) :url)
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts 1] (not (second @sort-key))])
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts] [:url true]]))]
                                 [hyperlink :class "uppercase" :style {:width "18ch"}
                                     :label "instagram" :tooltip "Sort by Instagram Name"
                                     :on-click #(if (= (first @sort-key) :instagram)
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts 1] (not (second @sort-key))])
                                                  (dispatch [:set-local-item-val [:sort-keys :contacts] [:instagram true]]))]]]]
        [v-box :gap "4px" :align :center :justify :start
           :children (into [header] (mapv (fn [id bg] ^{:key (str id "-" (:name @widths))} [item-list-view @widths id bg]) @items (cycle [true false])))]))))

(defn view-selection
  "The row of view selection controls: list new-item"
  []
  (fn []
    [h-box :gap "18px" :align :center :justify :center
       :children [[md-icon-button :md-icon-name "zmdi zmdi-view-headline mdc-text-grey" :tooltip "List View"
                                  :on-click #(route-view-display :contacts :list)]
                  [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey" :tooltip "Create New Contact"
                                  :on-click #(route-new-item :contacts)]]]))

(defn single-item-view
  []
  (let [uuid (subscribe [:app-key :single-item-uuid])
        item-path (subscribe [:item-path-by-uuid :contacts @uuid])
        id (last @item-path)]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" ;:style {:flex-flow "row wrap"}
           :children [[item-view id]]]))))

(defn new-item-view
  []
  (let [id 0
        item-path (subscribe [:by-path [:contacts id]])]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" :align :center :justify :start
           :children [[item-view id]]]))))

(defn contacts-view
  "Display contacts"
  []
  (let [display-type (subscribe [:app-key :display-type])]
    (fn []
      [v-box :gap "16px" :align :center :justify :center
         :children [[view-selection]
                    (condp = @display-type
                      :list [list-view]
                      :single-item [single-item-view]
                      :new-item [new-item-view]
                      [:span (str "Unexpected display-type of " @display-type)])]])))
