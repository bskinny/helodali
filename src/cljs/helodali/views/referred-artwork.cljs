(ns helodali.views.referred-artwork
    (:require [helodali.routes :refer [route-single-item route-new-item]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href sort-by-key-then-created title-string]]
              [cljs.pprint :refer [pprint]]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink]]))


(defn referred-artwork-item-list-view
  "Display item properties in single line - no image display. The 'widths' map contains the string
   length of the longest title, medium, etc. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for title, by truncating the strings over the max."
  [widths id odd-row?]
  (let [uuid (subscribe [:item-key :artwork id :uuid])
        bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        title (subscribe [:item-key :artwork id :title])
        year (subscribe [:item-key :artwork id :year])
        status (subscribe [:item-key :artwork id :status])
        list-price (subscribe [:item-key :artwork id :list-price])]
    (fn []
      [h-box :align :center :justify :start :style {:background bg-color} :width "100%"
        :children [[hyperlink :class "semibold" :style {:width (str (:title widths) "ch")} :label (trunc (title-string @title) (:title widths))
                       :on-click #(route-single-item :artwork @uuid)]
                   [label :width "6ch" :label @year]
                   [label :width "12ch" :label (clojure.string/replace (name @status) #"-" " ")]
                   [label :width "12ch" :label (str @list-price)]]])))

(defn referred-artwork-list-view
  "Display list of artwork referring to this exhibition, one per line"
  [filter-fx]
  (let [sort-key (subscribe [:by-path [:sort-keys :referred-artwork]])
        items (subscribe [:filtered-items-keys-sorted-by-key filter-fx :artwork :referred-artwork sort-by-key-then-created])]
    (fn []
      (if (empty? @items)
        [re-com/gap :size "1px"]
        (let [widths {:title 54}
              title [re-com/title :label "Referenced Artwork" :level :level3]
              header [h-box :align :center :justify :start :width "100%"
                        :children [[hyperlink :class "uppercase" :style {:width (str (:title widths) "ch")} :label "Artwork Title"
                                      :tooltip "Sort by Title" :on-click #(if (= (first @sort-key) :title)
                                                                            (dispatch [:set-local-item-val [:sort-keys :referred-artwork 1] (not (second @sort-key))])
                                                                            (dispatch [:set-local-item-val [:sort-keys :referred-artwork] [:title true]]))]
                                   [hyperlink :class "uppercase" :style {:width "6ch"} :label "year"
                                       :tooltip "Sort by Year" :on-click #(if (= (first @sort-key) :year)
                                                                            (dispatch [:set-local-item-val [:sort-keys :referred-artwork 1] (not (second @sort-key))])
                                                                            (dispatch [:set-local-item-val [:sort-keys :referred-artwork] [:year false]]))]
                                   [hyperlink :class "uppercase" :style {:width "12ch"} :label "status"
                                       :tooltip "Sort by Status" :on-click #(if (= (first @sort-key) :status)
                                                                              (dispatch [:set-local-item-val [:sort-keys :referred-artwork 1] (not (second @sort-key))])
                                                                              (dispatch [:set-local-item-val [:sort-keys :referred-artwork] [:status true]]))]
                                   [hyperlink :class "uppercase" :style {:width "12ch"} :label "list price"
                                      :tooltip "Sort by Price" :on-click #(if (= (first @sort-key) :list-price)
                                                                            (dispatch [:set-local-item-val [:sort-keys :referred-artwork 1] (not (second @sort-key))])
                                                                            (dispatch [:set-local-item-val [:sort-keys :referred-artwork] [:list-price false]]))]]]]
          [v-box :gap "4px" :align :center :justify :start :margin "20px"
             :children (into [title header] (mapv (fn [id bg] ^{:key id} [referred-artwork-item-list-view widths id bg]) @items (cycle [true false])))])))))
