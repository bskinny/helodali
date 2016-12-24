(ns helodali.views.purchases
    (:require [helodali.db :as db]
              [helodali.routes :refer [route-single-item]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-date-string
                                     sort-by-datetime]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink]]))

(defn item-list-view
  "Display item properties in single line - no image display. The 'widths' map contains the string
   length of the longest title, buyer name, etc. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for title, by truncating the strings over the max.

   The incoming 'item' is a map of the form {:uuid uuid :purchase {purchase map}} where uuid points to an artwork"
  [widths item odd-row?]
  (let [uuid (:uuid item)
        artwork (subscribe [:item-by-uuid :artwork uuid])
        purchase (:purchase item)
        bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        buyer-contact (if (empty? (:buyer purchase))
                        (r/atom nil)
                        (subscribe [:item-by-uuid :contacts (:buyer purchase)]))
        agent-contact (if (empty? (:agent purchase))
                        (r/atom nil)
                        (subscribe [:item-by-uuid :contacts (:agent purchase)]))
        dealer-contact (if (empty? (:dealer purchase))
                         (r/atom nil)
                         (subscribe [:item-by-uuid :contacts (:dealer purchase)]))]
    [(fn []
      [h-box :align :center :justify :start :style {:background bg-color} :width "100%"
        :children [[label :width "15ch" :label (safe-date-string (:date purchase))]
                   [hyperlink :class "semibold" :style {:width (str (max 18 (:title widths)) "ch")} :label (trunc (:title @artwork) (:title widths))
                              :on-click #(route-single-item :artwork uuid)]
                   (if (not (nil? @buyer-contact))
                     [hyperlink :class "semibold" :style {:width (str (max 18 (:buyer widths)) "ch")} :label (trunc (:name @buyer-contact) (:buyer widths))
                                :on-click #(route-single-item :contacts (:uuid @buyer-contact))]
                     [label :width (str (max 18 (:buyer widths)) "ch") :label ""])
                   (if (not (nil? @agent-contact))
                     [hyperlink :class "semibold" :style {:width (str (max 18 (:agent widths)) "ch")} :label (trunc (:name @agent-contact) (:agent widths))
                                :on-click #(route-single-item :contacts (:uuid @agent-contact))]
                     [label :width (str (max 18 (:agent widths)) "ch") :label ""])
                   (if (not (nil? @dealer-contact))
                     [hyperlink :class "semibold" :style {:width (str (max 18 (:dealer widths)) "ch")} :label (trunc (:name @dealer-contact) (:dealer widths))
                                :on-click #(route-single-item :contacts (:uuid @dealer-contact))]
                     [label :width (str (max 18 (:dealer widths)) "ch") :label ""])
                   [label :width "8ch" :label (:price purchase)]
                   [label :width "10ch" :label (:total-commission-percent purchase)]
                   (if (:commissioned purchase)
                     [box :width "18ch" :align :center :justify :center :child [md-icon-button :md-icon-name "zmdi zmdi-check mdc-text-green"]]
                     [label :width "18ch" :label ""])
                   (if (:donated purchase)
                     [box :width "8ch" :align :center :justify :center :child [md-icon-button :md-icon-name "zmdi zmdi-check mdc-text-green"]]
                     [label :width "8ch" :label ""])]])]))

(defn list-view
  "Display list of items, one per line"
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :purchases]])
        items (subscribe [:search-purchases identity])]
    (fn []
      (let [widths {:buyer 30 :agent 24 :dealer 24 :title 30}
            header [h-box :align :center :justify :start :width "100%"
                      :children [[hyperlink :class "bold uppercase" :style {:width "15ch"}
                                    :label "Date" :tooltip "Sort by Date"
                                    :on-click #(if (= (first @sort-key) :date)
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases] [:date true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width (str (max 18 (:title widths)) "ch")}
                                    :label "Title" :tooltip "Sort by Title"
                                    :on-click #(if (= (first @sort-key) :title)
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases] [:title true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width (str (max 18 (:buyer widths)) "ch")}
                                    :label "Buyer" :tooltip "Sort by Buyer"
                                    :on-click #(if (= (first @sort-key) :buyer-name)
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases] [:buyer-name true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width (str (max 18 (:agent widths)) "ch")}
                                    :label "Agent" :tooltip "Sort by Agent"
                                    :on-click #(if (= (first @sort-key) :agent-name)
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases] [:agent-name true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width (str (max 18 (:dealer widths)) "ch")}
                                    :label "Dealer" :tooltip "Sort by Dealer"
                                    :on-click #(if (= (first @sort-key) :dealer-name)
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases] [:dealer-name true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "8ch"}
                                    :label "Price" :tooltip "Sort by Price"
                                    :on-click #(if (= (first @sort-key) :price)
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases] [:price true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "10ch"}
                                    :label "Comm %" :tooltip "Sort by Commission Percent"
                                    :on-click #(if (= (first @sort-key) :total-commission-percent)
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases] [:total-commission-percent true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "18ch"}
                                    :label "Commissioned?" :tooltip "Sort by Commissioned?"
                                    :on-click #(if (= (first @sort-key) :commissioned)
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases] [:commissioned true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "8ch"}
                                    :label "Donated?" :tooltip "Sort by Donated?"
                                    :on-click #(if (= (first @sort-key) :donated)
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :purchases] [:donated true]]))]]]]
        [v-box :gap "4px" :align :center :justify :start
           :children (into [header] (map (partial item-list-view widths) @items (cycle [true false])))]))))

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
                      :list [list-view]
                      [:span (str "Unexpected display-type of " @display-type)])]])))
