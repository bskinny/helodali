(ns helodali.views.groupings
    (:require [helodali.views.referred-artwork :refer [referred-artwork-list-view]]
              [helodali.routes :refer [route-single-item route-new-item route-view-display]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href sort-by-datetime
                                     safe-date-string uuid-label-list-to-options sort-by-key-then-created
                                     safe-string title-string]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list
                                              button datepicker-dropdown checkbox]]))

(defn display-press-view
  [uuid odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        press (subscribe [:item-by-uuid :press uuid])]
    [(fn []
      [h-box :gap "6px" :justify :start :align :center :padding "4px" :width "100%"
         :style {:background bg-color :border-radius "4px"}
         :children [(when (not (nil? (:publication-date @press)))
                      [:span (safe-date-string (:publication-date @press))])
                    (when (not (empty? (:title @press)))
                      [hyperlink :class "semibold italic" :label (safe-string (:title @press) "(no title)")
                                 :on-click #(route-single-item :press uuid)])
                    (when (not (empty? (:publication @press)))
                      [:span (:publication @press)])]])]))

(defn display-associated-documents-view
  [uuid odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        document (subscribe [:item-by-uuid :documents uuid])]
    [(fn []
      [h-box :gap "6px" :justify :start :align :center :padding "4px" :width "100%"
         :style {:background bg-color :border-radius "4px"}
         :children [(when (not (nil? (:created @document)))
                      [:span (safe-date-string (:created @document))])
                    (when (not (empty? (:title @document)))
                      [hyperlink :class "semibold italic" :label (safe-string (:title @document) "(no title)")
                                 :on-click #(route-single-item :documents uuid)])
                    (when (not (empty? (:filename @document)))
                      [:span (:filename @document)])]])]))

(defn exhibition-referenced-in-artwork?
  "Filter function to determine if given exhibition is referred to in given artwork item"
  [exhibition-uuid item]
  (not (empty? (filter #(= exhibition-uuid (:ref %)) (:exhibition-history item)))))

(defn item-view
  "Display an item"
  [id]
  (let [uuid (subscribe [:item-key :groupings id :uuid])
        cn (subscribe [:item-key :groupings id :name])
        associated-artwork (subscribe [:by-path-and-deref-set-sorted-by [:groupings id :associated-artwork] :artwork (partial sort-by-key-then-created :title false)])
        notes (subscribe [:item-key :groupings id :notes])
        artwork (subscribe [:items-vals-with-uuid :artwork :title])
        editing (subscribe [:item-key :groupings id :editing])
        display-type (subscribe [:app-key :display-type])]
    (fn []
      (let [view-control [h-box :gap "12px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                           :children [[row-button :md-icon-name "zmdi zmdi-edit"
                                        :mouse-over-row? true :tooltip "Edit this item" :tooltip-position :right-center
                                        :on-click #(dispatch [:edit-item [:groupings id]])]
                                      [row-button :md-icon-name "zmdi zmdi-delete"
                                        :mouse-over-row? true :tooltip "Delete this item" :tooltip-position :right-center
                                        :on-click #(dispatch [:delete-item :groupings id])]]]
            view [[h-box :gap "18px" :align :center :justify :between
                    :children [[:span.bold @cn]]]
                  (when (not (empty? @associated-artwork))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "Artwork"]
                                       [v-box :gap "4px" :align :start :justify :start
                                          :children (into [] (map display-press-view @associated-artwork (cycle [true false])))]]])
                  (when (not (empty? @notes))
                    [v-box :gap "4px" :align :start :justify :start :max-width "480px"
                            :children [[:span.uppercase.light-grey "notes"]
                                       [:pre @notes]]])]
            create-control [h-box :gap "30px" :align :center
                              :children [[button :label "Create" :class "btn-default"
                                           :on-click #(dispatch [:create-from-placeholder :groupings])]
                                         [button :label "Cancel" :class "btn-default"
                                           :on-click #(dispatch [:delete-item :groupings id])]]]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[button :label "Save" :class "btn-default"
                                          :on-click #(dispatch [:save-changes [:groupings id]])]
                                        [button :label "Cancel" :class "btn-default"
                                          :on-click #(dispatch [:cancel-edit-item [:groupings id]])]]]
            view-box [v-box :gap "10px" :align :start :justify :start :children view]
            edit [[h-box :gap "8px" :align :center :justify :between
                    :children [[:span.uppercase.bold "Name"]
                               [input-text :model (str @cn) :placeholder "Of grouping" :width "240px" :style {:border "none"}
                                  :on-change #(dispatch [:set-local-item-val [:groupings id :name] %])]]]
                  [h-box :gap "6px" :align :center
                     :children [[:span.input-label "Artwork"]
                                (if (empty? @artwork)
                                  [:span.all-small-caps "must first be defined before associating with this grouping"]
                                  [selection-list :choices (uuid-label-list-to-options @artwork false) :model (if (empty? @associated-artwork) #{} (set @associated-artwork))
                                         :on-change #(dispatch [:set-local-item-val [:groupings id :associated-press] %])])]]
                  [:span.uppercase.light-grey "Notes"]
                  [input-textarea :model (str @notes) :width "360px"
                      :rows 4 :on-change #(dispatch [:set-local-item-val [:groupings id :notes] %])]]
            edit-box [v-box :gap "10px" :align :start :justify :start :children edit]]
        [v-box :gap "10px" :align :center :justify :start
               :children (concat (if @editing [edit-box] [view-box])
                                 (if (= @display-type :new-item)
                                   [create-control]
                                   (if @editing [save-control] [view-control]))
                                 (when (not @editing)
                                   [[referred-artwork-list-view (partial exhibition-referenced-in-artwork? @uuid)]]))]))))

(defn table-row
  "Display item properties in single line - no image display. The 'widths' map contains the string
   length of the longest name, location, etc. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for name, by truncating the strings over the max."
  [widths id]
  (let [uuid (subscribe [:item-key :groupings id :uuid])
        cn (subscribe [:item-key :groupings id :name])]
    (fn []
      [:tr
        [:td [hyperlink :label (trunc (safe-string @cn "(no name)") (get widths :name))
               :on-click #(route-single-item :groupings @uuid)]]
        [:td [h-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                :children [[row-button :md-icon-name "zmdi zmdi-copy"
                             :mouse-over-row? true :tooltip "Copy this item"
                             :on-click #(dispatch [:copy-item :groupings id :name])]
                           [row-button :md-icon-name "zmdi zmdi-delete"
                             :mouse-over-row? true :tooltip "Delete this item"
                             :on-click #(dispatch [:delete-item :groupings id])]]]]])))

(defn table-view
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :groupings]])
        ids (subscribe [:items-keys-sorted-by-key :groupings sort-by-key-then-created])
        names (subscribe [:items-vals :groupings :name])
        createdTimes (subscribe [:items-vals :groupings :created])]
    (fn []
      (if (not-empty @ids)
        (let [widths (r/atom {:name (+ 2 (max-string-length @names 40))})
              header [:thead
                       [:tr
                          [:th [hyperlink :class "uppercase"
                                :label "Exhibition" :tooltip "Sort by Name"
                                :on-click #(if (= (first @sort-key) :name)
                                             (dispatch [:set-local-item-val [:sort-keys :groupings 1] (not (second @sort-key))])
                                             (dispatch [:set-local-item-val [:sort-keys :groupings] [:name true]]))]]
                          [:th [hyperlink :class "uppercase"
                                :label "Created" :tooltip "Sort by Created Date"
                                :on-click #(if (= (first @sort-key) :created)
                                             (dispatch [:set-local-item-val [:sort-keys :groupings 1] (not (second @sort-key))])
                                             (dispatch [:set-local-item-val [:sort-keys :groupings] [:created true]]))]]]]]

          [:table
            header
            (into [:tbody] (mapv (fn [id] ^{:key (str "groupings-row-" id)} [table-row @widths id]) @ids))])
        [h-box :gap "10px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
         :children [[:p "Create your first grouping of items with "]
                    [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey"
                     :on-click #(route-new-item :groupings)]]]))))

(defn view-selection
  "The row of view selection controls: list new-item"
  []
  (fn []
    [h-box :gap "18px" :align :center :justify :center
       :children [[md-icon-button :md-icon-name "zmdi zmdi-view-headline mdc-text-grey" :tooltip "List View"
                                  :on-click #(route-view-display :groupings :list)]
                  [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey" :tooltip "Create New Grouping"
                                  :on-click #(route-new-item :groupings)]]]))

(defn single-item-view
  []
  (let [uuid (subscribe [:app-key :single-item-uuid])
        item-path (subscribe [:item-path-by-uuid :groupings @uuid])
        id (last @item-path)]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px"
           :children [[item-view id]]]))))

(defn new-item-view
  []
  (let [id 0
        item-path (subscribe [:by-path [:groupings id]])]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" :align :center :justify :start
           :children [[item-view id]]]))))

(defn groupings-view
  "Display groupings"
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
