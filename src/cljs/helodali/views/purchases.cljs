(ns helodali.views.purchases
    (:require [helodali.db :as db]
              [helodali.routes :refer [route-single-item]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-date-string
                                     safe-string sort-by-datetime]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink]]))

(defn item-row
  "Display item properties in single row - no image display. The 'widths' map contains the string
   length of the longest title, buyer name, etc. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for title, by truncating the strings over the max.

   The incoming 'item' is a map of the form {:uuid uuid :purchase {purchase map}} where uuid points to an artwork"
  [widths item]
  (let [uuid (:uuid item)
        artwork (subscribe [:item-by-uuid :artwork uuid])
        purchase (:purchase item)
        buyer-contact (if (empty? (:buyer purchase))
                        (r/atom nil)
                        (subscribe [:item-by-uuid :contacts (:buyer purchase)]))
        agent-contact (if (empty? (:agent purchase))
                        (r/atom nil)
                        (subscribe [:item-by-uuid :contacts (:agent purchase)]))
        dealer-contact (if (empty? (:dealer purchase))
                         (r/atom nil)
                         (subscribe [:item-by-uuid :contacts (:dealer purchase)]))]
    [:tr  [:td [label :label (safe-date-string (:date purchase))]]
          [:td [hyperlink :label (trunc (safe-string (:title @artwork) "(no title)") (:title widths))
                          :on-click #(route-single-item :artwork uuid)]]
          (if (not (nil? @buyer-contact))
            [:td [hyperlink :label (trunc (safe-string (:name @buyer-contact) "(no name)") (:buyer widths))
                            :on-click #(route-single-item :contacts (:uuid @buyer-contact))]]
            [:td [label :label ""]])
          (if (not (nil? @agent-contact))
            [:td [hyperlink :label (trunc (safe-string (:name @agent-contact) "(no name)") (:agent widths))
                            :on-click #(route-single-item :contacts (:uuid @agent-contact))]]
            [:td [label :label ""]])
          (if (not (nil? @dealer-contact))
            [:td [hyperlink :label (trunc (safe-string (:name @dealer-contact) "(no name)") (:dealer widths))
                            :on-click #(route-single-item :contacts (:uuid @dealer-contact))]]
            [:td [label :label ""]])
          [:td [label :label (:price purchase)]]
          [:td [label :label (:total-commission-percent purchase)]]]))

(defn table-view
  "Display table of purchased items"
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :purchases]])
        items (subscribe [:search-purchases identity])]
    (fn []
      (if (not-empty @items)
        (let [widths {:buyer 24 :agent 22 :dealer 22 :title 30}
              header [:thead
                       [:tr
                         [:th [hyperlink :class "uppercase"
                                 :label "Date" :tooltip "Sort by Date"
                                 :on-click #(if (= (first @sort-key) :date)
                                              (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                              (dispatch [:set-local-item-val [:sort-keys :purchases] [:date true]]))]]
                         [:th [hyperlink :class "uppercase"
                                 :label "Title" :tooltip "Sort by Title"
                                 :on-click #(if (= (first @sort-key) :title)
                                              (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                              (dispatch [:set-local-item-val [:sort-keys :purchases] [:title true]]))]]
                         [:th [hyperlink :class "uppercase"
                                 :label "Buyer" :tooltip "Sort by Buyer"
                                 :on-click #(if (= (first @sort-key) :buyer-name)
                                              (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                              (dispatch [:set-local-item-val [:sort-keys :purchases] [:buyer-name true]]))]]
                         [:th [hyperlink :class "uppercase"
                                 :label "Agent" :tooltip "Sort by Agent"
                                 :on-click #(if (= (first @sort-key) :agent-name)
                                              (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                              (dispatch [:set-local-item-val [:sort-keys :purchases] [:agent-name true]]))]]
                         [:th [hyperlink :class "uppercase"
                                 :label "Dealer" :tooltip "Sort by Dealer"
                                 :on-click #(if (= (first @sort-key) :dealer-name)
                                              (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                              (dispatch [:set-local-item-val [:sort-keys :purchases] [:dealer-name true]]))]]
                         [:th [hyperlink :class "uppercase"
                                 :label "Price" :tooltip "Sort by Price"
                                 :on-click #(if (= (first @sort-key) :price)
                                              (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                              (dispatch [:set-local-item-val [:sort-keys :purchases] [:price true]]))]]
                         [:th [hyperlink :class "uppercase"
                                :label "Comm %" :tooltip "Sort by Commission Percent"
                                :on-click #(if (= (first @sort-key) :total-commission-percent)
                                             (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                             (dispatch [:set-local-item-val [:sort-keys :purchases] [:total-commission-percent true]]))]]]]]
          [:table
            header
            (into [:tbody] (mapv (fn [idx item] ^{:key (str idx "-purchase-" (:uuid item))} [item-row widths item])
                                 (range (count @items)) @items))])
        [box :margin "40px" :align-self :center
           :child [:p "There are no artwork items with purchase records, create a purchase record on an artwork item."]]))))

(defn view-selection
  "Purchases view does not have any selection controls"
  []
  (fn []
    [h-box :gap "18px" :align :center :justify :center
       :children [[re-com/title :label "Purchases" :level :level3]]]))

(defn purchases-view
  "Display purchases. This is a read-only derived view from data defined within artwork."
  []
  (let [display-type (subscribe [:app-key :display-type])]
    (fn []
      [v-box :gap "16px" :align :center :justify :center
         :children [[view-selection]
                    (condp = @display-type
                      :list [table-view]
                      [:span (str "Unexpected display-type of " @display-type)])]])))
