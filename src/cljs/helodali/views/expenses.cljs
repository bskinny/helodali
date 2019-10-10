(ns helodali.views.expenses
    (:require [helodali.routes :refer [route-single-item route-new-item route-view-display]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-string safe-date-string sort-by-key-then-created]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box gap v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list
                                              button datepicker-dropdown]]))


(def expense-type-options [{:id :advertising :label "Adverting and Promotion"}
                           {:id :dues-subscriptions :label "Dues and Subscriptions"}
                           {:id :materials :label "Materials"}
                           {:id :miscellaneous :label "Miscellaneous"}
                           {:id :packaging-shipping :label "Packaging and Shipping"}
                           {:id :other :label "Other"}])

;; A map of the above labels keyed by expense-type keyword. E.g {:materials "Materials"}
(def expense-type-to-display-string (reduce (fn [acc m] (assoc acc (:id m) (:label m))) {} expense-type-options))

(defn price-format
  "Convert number (integer or float) to two-decimal precision and string format"
  [n]
  (.toFixed (js/Number n) 2))

(defn item-view
  "Display an expense item"
  [id]
  (let [price (subscribe [:item-key :expenses id :price])
        expense-type (subscribe [:item-key :expenses id :expense-type])
        date (subscribe [:item-key :expenses id :date])
        notes (subscribe [:item-key :expenses id :notes])
        editing (subscribe [:item-key :expenses id :editing])
        display-type (subscribe [:app-key :display-type])]
    (fn []
      (let [view-control [h-box :gap "12px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                           :children [[row-button :md-icon-name "zmdi zmdi-edit"
                                        :mouse-over-row? true :tooltip "Edit this item" :tooltip-position :right-center
                                        :on-click #(dispatch [:edit-item [:expenses id]])]
                                      [row-button :md-icon-name "zmdi zmdi-delete"
                                        :mouse-over-row? true :tooltip "Delete this item" :tooltip-position :right-center
                                        :on-click #(dispatch [:delete-item :expenses id])]]]
            view [[h-box :gap "18px" :align :center :justify :between
                    :children [[:span.bold (safe-date-string @date)]
                               [:span.all-small-caps (get expense-type-to-display-string @expense-type)]]]
                  [h-box :gap "4px" :align :start :justify :start :max-width "480px"
                   :children [[:span.uppercase.light-grey "price"]
                              [:span (price-format @price)]]]
                  (when (not (empty? @notes))
                    [v-box :gap "4px" :align :start :justify :start :max-width "480px"
                     :children [[:span.uppercase.light-grey "notes"]
                                [:pre @notes]]])]
            view-box [v-box :gap "10px" :align :start :justify :start :children view]
            create-control [h-box :gap "30px" :align :center
                              :children [[button :label "Create" :class "btn-default"
                                           :on-click #(dispatch [:create-from-placeholder :expenses])]
                                         [button :label "Cancel" :class "btn-default"
                                           :on-click #(dispatch [:delete-item :expenses id])]]]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[button :label "Save" :class "btn-default"
                                          :on-click #(dispatch [:save-changes [:expenses id]])]
                                        [button :label "Cancel" :class "btn-default"
                                          :on-click #(dispatch [:cancel-edit-item [:expenses id]])]]]
            edit [[h-box :gap "8px" :align :center :justify :between
                    :children [[:span.uppercase.bold "Date"]
                               [datepicker-dropdown :model (goog.date.UtcDateTime. @date)
                                  :on-change #(dispatch [:set-local-item-val [:expenses id :date] %])]
                               [single-dropdown :choices expense-type-options :width "158px" :model @expense-type
                                       :on-change #(dispatch [:set-local-item-val [:expenses id :expense-type] %])]]]
                  [h-box :gap "4px" :align :center
                    :children [[:span.uppercase.bold "price"]
                               [input-text :width "80px" :model (price-format @price) :style {:border "none"} :validation-regex #"^\d*\.?\d*?$"
                                     :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:expenses id :price] (js/Number %)])]]]
                  [:span.uppercase.light-grey "Notes"]
                  [input-textarea :model (str @notes) :width "360px"
                      :rows 4 :on-change #(dispatch [:set-local-item-val [:expenses id :notes] %])]]
            edit-box [v-box :gap "10px" :align :start :justify :start :children edit]]
        [v-box :gap "10px" :align :center :justify :start
               :children (concat (if @editing [edit-box] [view-box])
                                 (if (= @display-type :new-item)
                                   [create-control]
                                   (if @editing [save-control] [view-control])))]))))

(defn item-row
  "Display item properties in single line - no image display. The 'widths' map contains the display
   length of fields. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for name, by truncating the strings over the max."
  [widths id]
  (let [uuid (subscribe [:item-key :expenses id :uuid])
        date (subscribe [:item-key :expenses id :date])
        notes (subscribe [:item-key :expenses id :notes])
        expense-type (subscribe [:item-key :expenses id :expense-type])
        price (subscribe [:item-key :expenses id :price])]
    (fn []
      [:tr  [:td [hyperlink :label (safe-date-string @date) :on-click #(route-single-item :expenses @uuid)]]
            [:td [label :class "all-small-caps" :label (get expense-type-to-display-string @expense-type)]]
            [:td [label :label (trunc (safe-string @notes "") (:notes widths))]]
            [:td [box :justify :end :class "all-small-caps" :child (price-format @price)]]
            [:td [h-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                    :children [[row-button :md-icon-name "zmdi zmdi-copy"
                                 :mouse-over-row? true :tooltip "Copy this item"
                                 :on-click #(dispatch [:copy-item :expenses id :notes])]
                               [row-button :md-icon-name "zmdi zmdi-delete"
                                 :mouse-over-row? true :tooltip "Delete this item"
                                 :on-click #(dispatch [:delete-item :expenses id])]]]]])))

(defn table-view
  "Display table of expenses"
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :expenses]])
        items (subscribe [:items-keys-sorted-by-key :expenses sort-by-key-then-created])
        notes (subscribe [:items-vals :expenses :notes])]
    (fn []
      (let [widths (r/atom {:date 12 :expense-type 20 :price 5
                            :notes (max 10 (+ 2 (max-string-length @notes 40)))})
            header [:thead
                    [:tr
                      [:th [hyperlink :class "uppercase"
                            :label "Date" :tooltip "Sort by Date"
                            :on-click #(if (= (first @sort-key) :date)
                                         (dispatch [:set-local-item-val [:sort-keys :expenses 1] (not (second @sort-key))])
                                         (dispatch [:set-local-item-val [:sort-keys :expenses] [:date true]]))]]
                      [:th [hyperlink :class "uppercase"
                            :label "Type" :tooltip "Sort by Type"
                            :on-click #(if (= (first @sort-key) :expense-type)
                                         (dispatch [:set-local-item-val [:sort-keys :expenses 1] (not (second @sort-key))])
                                         (dispatch [:set-local-item-val [:sort-keys :expenses] [:expense-type true]]))]]
                      ;; (trunc (safe-string @cn "(no name)") (get widths :name))
                      [:th [hyperlink :class "uppercase"
                            :label "Notes" :tooltip "Sort by Notes"
                            :on-click #(if (= (first @sort-key) :notes)
                                         (dispatch [:set-local-item-val [:sort-keys :expenses 1] (not (second @sort-key))])
                                         (dispatch [:set-local-item-val [:sort-keys :expenses] [:notes true]]))]]
                      [:th [hyperlink :class "uppercase"
                            :label "Price" :tooltip "Sort by Price"
                            :on-click #(if (= (first @sort-key) :price)
                                         (dispatch [:set-local-item-val [:sort-keys :expenses 1] (not (second @sort-key))])
                                         (dispatch [:set-local-item-val [:sort-keys :expenses] [:price true]]))]]]]]
        [:table
          header
          (into [:tbody] (mapv (fn [id] ^{:key (str "expense-row-" id)} [item-row @widths id]) @items))]))))

(defn view-selection
  "The row of view selection controls: list new-item"
  []
  (fn []
    [h-box :gap "18px" :align :center :justify :center
       :children [[md-icon-button :md-icon-name "zmdi zmdi-view-headline mdc-text-grey" :tooltip "List View"
                                  :on-click #(route-view-display :expenses :list)]
                  [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey" :tooltip "Create New Expense"
                                  :on-click #(route-new-item :expenses)]]]))

(defn single-item-view
  []
  (let [uuid (subscribe [:app-key :single-item-uuid])
        item-path (subscribe [:item-path-by-uuid :expenses @uuid])
        id (last @item-path)]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" ;:style {:flex-flow "row wrap"}
           :children [[item-view id]]]))))

(defn new-item-view
  []
  (let [id 0
        item-path (subscribe [:by-path [:expenses id]])]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" :align :center :justify :start
           :children [[item-view id]]]))))

(defn expenses-view
  "Display expenses"
  []
  (let [display-type (subscribe [:app-key :display-type])]
    (fn []
      [v-box :gap "16px" :align :center :justify :center
         :children [[view-selection]
                    (condp = @display-type
                      :list [table-view]
                      :single-item [single-item-view]
                      :new-item [new-item-view]
                      [:span (str "Unexpected display-type of " @display-type)])]])))
