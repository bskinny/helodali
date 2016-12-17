(ns helodali.views.press
    (:require [helodali.db :as db]
              [helodali.routes :refer [route-single-item route-new-item]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-date-string
                                     sort-by-datetime sort-by-key-then-created]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list
                                              button datepicker-dropdown checkbox]]))

(defn item-view
  "Display an item"
  [id]
  (let [uuid (subscribe [:item-key :press id :uuid])
        title (subscribe [:item-key :press id :title])
        author-first-name (subscribe [:item-key :press id :author-first-name])
        author-last-name (subscribe [:item-key :press id :author-last-name])
        publication (subscribe [:item-key :press id :publication])
        volume (subscribe [:item-key :press id :volume])
        url (subscribe [:item-key :press id :url])
        publication-date (subscribe [:item-key :press id :publication-date])
        page-numbers (subscribe [:item-key :press id :page-numbers])
        include-in-cv? (subscribe [:item-key :press id :include-in-cv])
        notes (subscribe [:item-key :press id :notes])
        editing (subscribe [:item-key :press id :editing])]
    [(fn []
      (let [controls [h-box :gap "2px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                        :children [[row-button :md-icon-name "zmdi zmdi-edit"
                                     :mouse-over-row? true :tooltip "Toggle Editing" :tooltip-position :right-center
                                     :on-click #(dispatch [:set-local-item-val [:press id :editing] (not @editing)])]
                                   [row-button :md-icon-name "zmdi zmdi-delete"
                                     :mouse-over-row? true :tooltip "Delete this item" :tooltip-position :right-center
                                     :on-click #(dispatch [:delete-item :press id])]]]
            view [[:span.bold @title]
                  (when (not (empty? @publication))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "publication"]
                                       [:span @publication]]])
                  (when (or (not (empty? @author-first-name)) (not (empty? @author-last-name)))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "Author"]
                                       [:span (str @author-first-name " " @author-last-name)]]])
                  (when (not (empty? @url))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "url"]
                                       [re-com/hyperlink-href :label @url :href (url-to-href @url) :target "_blank"]]])
                  (when (not (empty? @volume))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "volume"]
                                       [:span.italic @volume]]])
                  (when (not (nil? @publication-date))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "published"]
                                       [:span.italic (safe-date-string @publication-date)]]])
                  (when (not (empty? @page-numbers))
                    [v-box :gap "4px" :align :start :justify :start :max-width "480px"
                            :children [[:span.uppercase.light-grey "page numbers"]
                                       [:span @page-numbers]]])
                  (if @include-in-cv?
                    [:span.italics "Included in your CV"]
                    [:span.italics "Excluded from your CV"])
                  (when (not (empty? @notes))
                    [v-box :gap "4px" :align :start :justify :start :max-width "480px"
                            :children [[:span.uppercase.light-grey "notes"]
                                       [:span @notes]]])]
            edit [[h-box :gap "8px" :align :center :justify :between
                    :children [[:span.uppercase.bold "Title"]
                               [input-text :model (str @title) :placeholder "Of article or piece" :width "320px" :style {:border "none"}
                                  :on-change #(dispatch [:set-item-val [:press id :title] %])]]]
                  [h-box :gap "6px" :align :center
                    :children [[:span.uppercase.light-grey "publication"]
                               [input-text :width "280px" :model (str @publication) :style {:border "none"}
                                    :on-change #(dispatch [:set-item-val [:press id :publication] %])]]]
                  [h-box :gap "6px" :align :center
                    :children [[:span.uppercase.light-grey "Author's Name"]
                               [input-text :width "140px" :model (str @author-first-name) :placeholder "First name" :style {:border "none"}
                                    :on-change #(dispatch [:set-item-val [:press id :author-first-name] %])]
                               [input-text :width "140px" :model (str @author-last-name) :placeholder "Last name" :style {:border "none"}
                                    :on-change #(dispatch [:set-item-val [:press id :author-last-name] %])]]]
                  [h-box :gap "6px" :align :center
                    :children [[:span.uppercase.light-grey "url"]
                               [input-text :width "280px" :model (str @url) :style {:border "none"}
                                    :on-change #(dispatch [:set-item-val [:press id :url] %])]]]
                  [h-box :gap "6px" :align :center
                    :children [[:span.uppercase.light-grey "volume"]
                               [input-text :width "140px" :model (str @volume) :style {:border "none"}
                                    :on-change #(dispatch [:set-item-val [:press id :volume] %])]]]
                  [h-box :gap "6px" :align :center
                    :children [[:span.uppercase.light-grey "Published on"]
                               [datepicker-dropdown :model (goog.date.UtcDateTime. @publication-date)
                                     :on-change #(dispatch [:set-item-val [:press id :publication-date] %])]]]
                  [h-box :gap "6px" :align :center
                    :children [[:span.uppercase.light-grey "page numbers"]
                               [input-text :width "140px" :model (str @page-numbers) :style {:border "none"}
                                    :on-change #(dispatch [:set-item-val [:press id :page-numbers] %])]]]
                  [checkbox :model @include-in-cv? :label "Include in your CV?"
                     :on-change #(dispatch [:set-item-val [:press id :include-in-cv] (not @include-in-cv?)])]
                  [:span.uppercase.light-grey "Notes"]
                  [input-textarea :model (str @notes) :width "360px"
                      :rows 4 :on-change #(dispatch [:set-item-val [:press id :notes] %])]]]
        [v-box :gap "10px" :align :start :justify :start ;:style {:border "dashed 1px red"}
               :children (into (if @editing edit view) [controls])]))]))

(defn item-list-view
  "Display item properties in single line - no image display. The 'widths' map contains the string
   length of the longest title, publication, etc. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for title, by truncating the strings over the max."
  [widths id odd-row?]
  (let [uuid (subscribe [:item-key :press id :uuid])
        bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        title (subscribe [:item-key :press id :title])
        publication (subscribe [:item-key :press id :publication])
        url (subscribe [:item-key :press id :url])
        publication-date (subscribe [:item-key :press id :publication-date])
        include-in-cv? (subscribe [:item-key :press id :include-in-cv])
        notes (subscribe [:item-key :press id :notes])]
    [(fn []
      [h-box :align :center :justify :start :style {:background bg-color} :width "100%"
        :children [[hyperlink :class "semibold" :style {:width (str (max 18 (get widths :title)) "ch")} :label (trunc @title (get widths :title))
                       :on-click #(route-single-item :press @uuid)]
                   [label :width (str (max 18 (:publication widths)) "ch") :label (trunc @publication (:publication widths))]
                   [label :width "15ch" :label (safe-date-string @publication-date)]
                   [re-com/hyperlink-href :style {:width (str (max 18 (get widths :url)) "ch")}
                               :label (trunc (str @url) (:url widths)) :href (str (url-to-href @url)) :target "_blank"]
                   (if @include-in-cv?
                     [box :width "16ch" :align :center :justify :center :child [md-icon-button :md-icon-name "zmdi zmdi-check mdc-text-green"]]
                     [label :width "16ch" :label ""])
                   [h-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                      :children [[row-button :md-icon-name "zmdi zmdi-copy"
                                   :mouse-over-row? true :tooltip "Copy this item"
                                   :on-click #(dispatch [:copy-item :press id :title])]
                                 [row-button :md-icon-name "zmdi zmdi-delete"
                                   :mouse-over-row? true :tooltip "Delete this item"
                                   :on-click #(dispatch [:delete-item :press id])]]]]])]))

(defn list-view
  "Display list of items, one per line"
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :press]])
        items (subscribe [:items-keys-sorted-by-key :press sort-by-key-then-created])
        ; items (subscribe [:items-keys-sorted-by :press (partial sort-by-datetime :publication-date true)])
        titles (subscribe [:items-vals :press :title])
        publications (subscribe [:items-vals :press :publication])
        urls (subscribe [:items-vals :press :url])]
    (fn []
      (let [widths {:title (+ 4 (max-string-length @titles 80))
                    :publication (+ 4 (max-string-length @publications 40))
                    :url (+ 4 (max-string-length @urls 40))}
            header [h-box :align :center :justify :start :width "100%"
                      :children [[hyperlink :class "bold uppercase" :style {:width (str (max 18 (:title widths)) "ch")}
                                    :label "Title" :tooltip "Sort by Title"
                                    :on-click #(if (= (first @sort-key) :title)
                                                 (dispatch [:set-local-item-val [:sort-keys :press 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :press] [:title true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width (str (max 18 (:publication widths)) "ch")}
                                    :label "Publication" :tooltip "Sort by Publication"
                                    :on-click #(if (= (first @sort-key) :publication)
                                                 (dispatch [:set-local-item-val [:sort-keys :press 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :press] [:publication true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "15ch"}
                                    :label "Date" :tooltip "Sort by Date"
                                    :on-click #(if (= (first @sort-key) :publication-date)
                                                 (dispatch [:set-local-item-val [:sort-keys :press 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :press] [:publication-date true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width (str (max 18 (:url widths)) "ch")}
                                    :label "url" :tooltip "Sort by URL"
                                    :on-click #(if (= (first @sort-key) :url)
                                                 (dispatch [:set-local-item-val [:sort-keys :press 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :press] [:url true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "16ch"}
                                    :label "Included in CV" :tooltip "Sort by Included in CV?"
                                    :on-click #(if (= (first @sort-key) :include-in-cv)
                                                 (dispatch [:set-local-item-val [:sort-keys :press 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :press] [:include-in-cv true]]))]]]]
        [v-box :gap "4px" :align :center :justify :start
           :children (into [header] (map (partial item-list-view widths) @items (cycle [true false])))]))))

(defn view-selection
  "The row of view selection controls: list new-item"
  []
  (fn []
    [h-box :gap "18px" :align :center :justify :center
       :children [[md-icon-button :md-icon-name "zmdi zmdi-view-headline mdc-text-grey"
                                  :on-click #(dispatch [:set-app-val [:display-type] :list])]
                  [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey"
                                  :on-click #(route-new-item :press)]]]))

(defn single-item-view
  []
  (let [uuid (subscribe [:app-key :single-item-uuid])
        item-path (subscribe [:item-path-by-uuid :press @uuid])
        id (last @item-path)]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" ;:style {:flex-flow "row wrap"}
           :children [(item-view id)]]))))

(defn new-item-view
  []
  (let [id 0
        item-path (subscribe [:by-path [:press id]])]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" :align :center :justify :start
           :children [(item-view id)
                      [h-box :gap "30px" :align :center
                         :children [[button :label "Create" :class "btn-default"
                                       :on-click #(dispatch [:create-from-placeholder :press])]
                                    [button :label "Cancel" :class "btn-default"
                                       :on-click #(dispatch [:delete-item :press id])]]]]]))))

(defn press-view
  "Display press articles"
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
