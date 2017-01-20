(ns helodali.views.search-results
    (:require [helodali.db :as db]
              [helodali.routes :refer [route-single-item route-profile]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-date-string
                                     sort-by-datetime]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink]]))

(defn display-match
  "Display the match with the matched portion of the string highlighted"
  [pattern width match]
  (fn []
     (let [[tag s] (clojure.string/split match #":" 2)
           tag (if (= tag "val") "" tag)] ;; Avoid displaying the meaningless tag "VAL"
           ; highlighted (clojure.string/replace s pattern #(str " [:b " % "] "))
           ; truncated (trunc highlighted (- width 16))
           ; width-string (str (max 18 (- width 16) "ch"))]
       [h-box :gap "4px" :align :center :justify :start
          :children [[label :width "16ch" :class "input-label uppercase" :label tag]
                     ; (cljs.reader/read-string (str "[:p " truncated "]"))  ;; Failed attempt to highlight matches
                     [label :width (str (max 18 (- width 16) "ch")) :label (trunc s (- width 16))]]])))

(defn item-list-view
  "Display item properties in single line - no image display. The 'widths' map contains the string
   length of the longest title, match, etc. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for title, by truncating the strings over the max.

   The incoming 'match' is a map of the form {:type type :uuid uuid :match (<list of strings>) :title 'string'} where
   uuid points to an item of type 'type' and 'title' is something from the item that can stand for a name
   or title (e.g. Contact's name, Artwork's title). The value of match is a list containing all matches within
   the item. For example (\"title: Unfortunately...\" \"style: biomorphic\")"
  [pattern widths match odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")]
    (fn []
      (let [title (or (:title match) "none")] ;; Guard against nil-valued hyperlink labels
        [h-box :align :center :justify :start :style {:background bg-color} :padding "8px" :width "100%"
          :children [[label :width "10ch" :label (clojure.string/capitalize (name (:type match)))]
                     [hyperlink :style {:width (str (max 18 (:title widths)) "ch")}
                                :label (trunc title (:title widths))
                                :on-click #(if (= :profile (:type match))
                                             (route-profile)
                                             (route-single-item (:type match) (:uuid match)))]
                     [v-box :gap "6px" :align :start :justify :start
                         :children (into [] (mapv (fn [idx match-line] ^{:key (str (:uuid match) "-lines-" idx)} [display-match pattern (:match widths) match-line])
                                                  (range (count (:match match))) (:match match)))]]]))))

(defn list-view
  "Display list of matches, one row at a time."
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :search-results]])
        matches (subscribe [:search])
        search-pattern (subscribe [:by-path [:search-pattern]])]
    (fn []
      (if (empty? @matches)
        [re-com/title :label "No Results Found" :level :level4]
        (let [widths {:match 80 :title 30}
              pattern (re-pattern @search-pattern)
              header [h-box :align :center :justify :start :padding "8px" :width "100%"
                        :children [[hyperlink :class "uppercase" :style {:width "10ch"}
                                      :label "Type" :tooltip "Sort by Type"
                                      :on-click #(if (= (first @sort-key) :item-type)
                                                   (dispatch [:set-local-item-val [:sort-keys :search-results 1] (not (second @sort-key))])
                                                   (dispatch [:set-local-item-val [:sort-keys :search-results] [:item-type true]]))]
                                   [hyperlink :class "uppercase" :style {:width (str (max 18 (:title widths)) "ch")}
                                      :label "Title" :tooltip "Sort by Title"
                                      :on-click #(if (= (first @sort-key) :title)
                                                   (dispatch [:set-local-item-val [:sort-keys :search-results 1] (not (second @sort-key))])
                                                   (dispatch [:set-local-item-val [:sort-keys :search-results] [:title true]]))]
                                   [hyperlink :class "uppercase" :style {:width (str (max 18 (:match widths)) "ch")}
                                      :label "Match" :tooltip "Sort by Match"
                                      :on-click #(if (= (first @sort-key) :match)
                                                   (dispatch [:set-local-item-val [:sort-keys :search-results 1] (not (second @sort-key))])
                                                   (dispatch [:set-local-item-val [:sort-keys :search-results] [:match true]]))]]]]
          [v-box :gap "4px" :align :center :justify :start
             :children (into [header] (mapv (fn [idx match bg] ^{:key (str (:uuid match) "-" idx)} [item-list-view pattern widths match bg])
                                            (range (count @matches)) @matches (cycle [true false])))])))))

(defn view-selection
  "Search results view does not have any selection controls"
  []
  (fn []
    [h-box :gap "18px" :align :center :justify :center
       :children [[re-com/title :label "Search Results" :level :level3]]]))

(defn search-results-view
  "Display search results based on the search string already defined in app-db's :search-pattern.
   This is a read-only derived view from data defined within artwork."
  []
  (let [display-type (subscribe [:app-key :display-type])]
    (fn []
      [v-box :gap "16px" :align :center :justify :center
         :children [[view-selection]
                    (condp = @display-type
                      :list [list-view]
                      [:span (str "Unexpected display-type of " @display-type)])]])))
