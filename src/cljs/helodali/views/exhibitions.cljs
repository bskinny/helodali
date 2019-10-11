(ns helodali.views.exhibitions
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

(def kind-options [{:id :solo :label "Solo"}
                   {:id :duo :label "Duo"}
                   {:id :group :label "Group"}
                   {:id :selected :label "Selected"}
                   {:id :other :label "Other"}])

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
  (let [uuid (subscribe [:item-key :exhibitions id :uuid])
        cn (subscribe [:item-key :exhibitions id :name])
        location (subscribe [:item-key :exhibitions id :location])
        begin-date (subscribe [:item-key :exhibitions id :begin-date])
        end-date (subscribe [:item-key :exhibitions id :end-date])
        url (subscribe [:item-key :exhibitions id :url])
        include-in-cv? (subscribe [:item-key :exhibitions id :include-in-cv])
        associated-documents (subscribe [:by-path-and-deref-set-sorted-by [:exhibitions id :associated-documents] :documents (partial sort-by-key-then-created :title false)])
        associated-press (subscribe [:by-path-and-deref-set-sorted-by [:exhibitions id :associated-press] :press (partial sort-by-datetime :publication-date true)])
        documents (subscribe [:items-vals-with-uuid :documents :title])
        press (subscribe [:items-vals-with-uuid :press :title])
        kind (subscribe [:item-key :exhibitions id :kind])
        notes (subscribe [:item-key :exhibitions id :notes])
        editing (subscribe [:item-key :exhibitions id :editing])
        display-type (subscribe [:app-key :display-type])]
    (fn []
      (let [view-control [h-box :gap "12px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                           :children [[row-button :md-icon-name "zmdi zmdi-edit"
                                        :mouse-over-row? true :tooltip "Edit this item" :tooltip-position :right-center
                                        :on-click #(dispatch [:edit-item [:exhibitions id]])]
                                      [row-button :md-icon-name "zmdi zmdi-delete"
                                        :mouse-over-row? true :tooltip "Delete this item" :tooltip-position :right-center
                                        :on-click #(dispatch [:delete-item :exhibitions id])]]]
            view [[h-box :gap "18px" :align :center :justify :between
                    :children [[:span.bold @cn]
                               [:span.all-small-caps (clojure.string/replace (name @kind) #"-" " ")]]]
                  (when (not (empty? @location))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "location"]
                                       [:span @location]]])
                  (when (or (not (nil? @begin-date)) (not (nil? @end-date)))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "From"]
                                       [:span (safe-date-string @begin-date)]
                                       [:span.uppercase.light-grey "to"]
                                       [:span (safe-date-string @end-date)]]])
                  (when (not (empty? @url))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "url"]
                                       [re-com/hyperlink-href :label (trunc @url 50) :href (url-to-href @url) :target "_blank"]]])
                  (if @include-in-cv?
                    [:span.italics "Included in your CV"]
                    [:span.italics "Excluded from your CV"])
                  (when (not (empty? @associated-press))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "Press"]
                                       [v-box :gap "4px" :align :start :justify :start
                                          :children (into [] (map display-press-view @associated-press (cycle [true false])))]]])
                  (when (not (empty? @associated-documents))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "Documents"]
                                       [v-box :gap "4px" :align :start :justify :start
                                          :children (into [] (map display-associated-documents-view @associated-documents (cycle [true false])))]]])
                  (when (not (empty? @notes))
                    [v-box :gap "4px" :align :start :justify :start :max-width "480px"
                            :children [[:span.uppercase.light-grey "notes"]
                                       [:pre @notes]]])]
            create-control [h-box :gap "30px" :align :center
                              :children [[button :label "Create" :class "btn-default"
                                           :on-click #(dispatch [:create-from-placeholder :exhibitions])]
                                         [button :label "Cancel" :class "btn-default"
                                           :on-click #(dispatch [:delete-item :exhibitions id])]]]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[button :label "Save" :class "btn-default"
                                          :on-click #(dispatch [:save-changes [:exhibitions id]])]
                                        [button :label "Cancel" :class "btn-default"
                                          :on-click #(dispatch [:cancel-edit-item [:exhibitions id]])]]]
            view-box [v-box :gap "10px" :align :start :justify :start :children view]
            edit [[h-box :gap "8px" :align :center :justify :between
                    :children [[:span.uppercase.bold "Name"]
                               [input-text :model (str @cn) :placeholder "Of exhibition" :width "240px" :style {:border "none"}
                                  :on-change #(dispatch [:set-local-item-val [:exhibitions id :name] %])]
                               [single-dropdown :choices kind-options :width "118px" :model @kind
                                       :on-change #(dispatch [:set-local-item-val [:exhibitions id :kind] %])]]]
                  [h-box :gap "4px" :align :center
                    :children [[:span.uppercase.light-grey "location"]
                               [input-text :width "280px" :model (str @location) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:exhibitions id :location] %])]]]
                  [h-box :gap "4px" :align :center
                    :children [[:span.uppercase.bold (str "From ")]
                               [datepicker-dropdown :model (goog.date.UtcDateTime. @begin-date)
                                     :on-change #(dispatch [:set-local-item-val [:exhibitions id :begin-date] %])]
                               [:span.input-label (str " to ")]
                               [datepicker-dropdown :model (goog.date.UtcDateTime. @end-date)
                                     :on-change #(dispatch [:set-local-item-val [:exhibitions id :end-date] %])]]]
                  [h-box :gap "4px" :align :center
                    :children [[:span.uppercase.light-grey "url"]
                               [input-text :width "280px" :model (str @url) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:exhibitions id :url] %])]]]
                  [checkbox :model include-in-cv? :label "Include in your CV?"
                            :on-change #(dispatch [:set-local-item-val [:exhibitions id :include-in-cv] (not @include-in-cv?)])]
                  [h-box :gap "6px" :align :center
                     :children [[:span.input-label "Press"]
                                (if (empty? @press)
                                  [:span.all-small-caps "must first be defined before associating with this exhibition"]
                                  [selection-list :choices (uuid-label-list-to-options @press false) :model (if (empty? @associated-press) #{} (set @associated-press))
                                         :on-change #(dispatch [:set-local-item-val [:exhibitions id :associated-press] %])])]]
                  [h-box :gap "6px" :align :center
                     :children [[:span.input-label "Documents"]
                                (if (empty? @documents)
                                  [:span.all-small-caps "must first be defined before associating with this exhibition"]
                                  [selection-list :choices (uuid-label-list-to-options @documents false) :model (if (empty? @associated-documents) #{} (set @associated-documents))
                                         :on-change #(dispatch [:set-local-item-val [:exhibitions id :associated-documents] %])])]]
                  [:span.uppercase.light-grey "Notes"]
                  [input-textarea :model (str @notes) :width "360px"
                      :rows 4 :on-change #(dispatch [:set-local-item-val [:exhibitions id :notes] %])]]
            edit-box [v-box :gap "10px" :align :start :justify :start :children edit]]
        [v-box :gap "10px" :align :center :justify :start ;:style {:border "dashed 1px red"}
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
  (let [uuid (subscribe [:item-key :exhibitions id :uuid])
        cn (subscribe [:item-key :exhibitions id :name])
        location (subscribe [:item-key :exhibitions id :location])
        begin-date (subscribe [:item-key :exhibitions id :begin-date])
        end-date (subscribe [:item-key :exhibitions id :end-date])
        include-in-cv? (subscribe [:item-key :exhibitions id :include-in-cv])
        kind (subscribe [:item-key :exhibitions id :kind])]
    (fn []
      [:tr
        [:td [hyperlink :label (trunc (safe-string @cn "(no name)") (get widths :name))
               :on-click #(route-single-item :exhibitions @uuid)]]
        [:td [label :class "all-small-caps" :label (clojure.string/replace (name @kind) #"-" " ")]]
        [:td [label :label (trunc @location (:location widths))]]
        [:td [label :label (safe-date-string @begin-date)]]
        [:td [label :label (safe-date-string @end-date)]]
            ; In case we want to display url in the future
            ;[re-com/hyperlink-href
            ;             :label (trunc (str @url) (:url widths)) :href (str (url-to-href @url)) :target "_blank"]
        [:td (if @include-in-cv?
                [box :width "16ch" :align :center :justify :center :child [md-icon-button :md-icon-name "zmdi zmdi-check mdc-text-green"]]
                [label :width "16ch" :label ""])]
        [:td [h-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                :children [[row-button :md-icon-name "zmdi zmdi-copy"
                             :mouse-over-row? true :tooltip "Copy this item"
                             :on-click #(dispatch [:copy-item :exhibitions id :name])]
                           [row-button :md-icon-name "zmdi zmdi-delete"
                             :mouse-over-row? true :tooltip "Delete this item"
                             :on-click #(dispatch [:delete-item :exhibitions id])]]]]])))

(defn table-view
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :exhibitions]])
        ids (subscribe [:items-keys-sorted-by-key :exhibitions sort-by-key-then-created])
        names (subscribe [:items-vals :exhibitions :name])
        locations (subscribe [:items-vals :exhibitions :location])
        urls (subscribe [:items-vals :exhibitions :url])]
    (fn []
      (if (not-empty @ids)
        (let [widths (r/atom {:name (+ 2 (max-string-length @names 40))
                              :location (+ 2 (max-string-length @locations 40))
                              :url (+ 2 (max-string-length @urls 40))})
              header [:thead
                       [:tr
                          [:th [hyperlink :class "uppercase"
                                :label "Exhibition" :tooltip "Sort by Exhibition"
                                :on-click #(if (= (first @sort-key) :name)
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions 1] (not (second @sort-key))])
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions] [:name true]]))]]
                          [:th [hyperlink :class "uppercase"
                                 :label "Kind" :tooltip "Sort by Kind"
                                 :on-click #(if (= (first @sort-key) :kind)
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions 1] (not (second @sort-key))])
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions] [:kind true]]))]]
                          [:th [hyperlink :class "uppercase"
                                 :label "Location" :tooltip "Sort by Location"
                                 :on-click #(if (= (first @sort-key) :location)
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions 1] (not (second @sort-key))])
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions] [:location true]]))]]
                          [:th [hyperlink :class "uppercase"
                                :label "Begin" :tooltip "Sort by Begin Date"
                                :on-click #(if (= (first @sort-key) :begin-date)
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions 1] (not (second @sort-key))])
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions] [:begin-date true]]))]]
                          [:th [hyperlink :class "uppercase"
                                 :label "End" :tooltip "Sort by End Date"
                                 :on-click #(if (= (first @sort-key) :end-date)
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions 1] (not (second @sort-key))])
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions] [:end-date true]]))]]
                          [:th [hyperlink :class "uppercase"
                                :label "Included in CV" :tooltip "Sort by Included in CV?"
                                :on-click #(if (= (first @sort-key) :include-in-cv)
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions 1] (not (second @sort-key))])
                                             (dispatch [:set-local-item-val [:sort-keys :exhibitions] [:include-in-cv true]]))]]]]]
          [:table
            header
            (into [:tbody] (mapv (fn [id] ^{:key (str "exhibition-row-" id)} [table-row @widths id]) @ids))])
        [h-box :gap "10px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
         :children [[:p "Create your first exhibition with "]
                    [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey"
                     :on-click #(route-new-item :exhibitions)]]]))))

(defn view-selection
  "The row of view selection controls: list new-item"
  []
  (fn []
    [h-box :gap "18px" :align :center :justify :center
       :children [[md-icon-button :md-icon-name "zmdi zmdi-view-headline mdc-text-grey" :tooltip "List View"
                                  :on-click #(route-view-display :exhibitions :list)]
                  [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey" :tooltip "Create New Exhibition"
                                  :on-click #(route-new-item :exhibitions)]]]))

(defn single-item-view
  []
  (let [uuid (subscribe [:app-key :single-item-uuid])
        item-path (subscribe [:item-path-by-uuid :exhibitions @uuid])
        id (last @item-path)]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px"
           :children [[item-view id]]]))))

(defn new-item-view
  []
  (let [id 0
        item-path (subscribe [:by-path [:exhibitions id]])]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" :align :center :justify :start
           :children [[item-view id]]]))))

(defn exhibitions-view
  "Display exhibitions"
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
