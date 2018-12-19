(ns helodali.views.profile
    (:require [helodali.db :as db]
              [helodali.routes :refer [route-single-item]]
              [helodali.misc :refer [trunc compute-bg-color max-string-length url-to-href safe-date-string
                                     sort-by-datetime]]
              [cljs.pprint :refer [pprint]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list
                                              button title checkbox]]))

(defn- display-year-and-label-view
  [m odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        year (:year m)
        val (:val m)]
   [(fn []
      [h-box :gap "6px" :justify :start :align :center :width "100%"
         :style {:background bg-color :border-radius "4px"}
         :children [[label :label (str year)]
                    [label :label (str val)]]])]))

(defn- display-year-and-label-edit
  [label-string profile-path-to idx odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        year-path (conj profile-path-to idx :year)
        val-path (conj profile-path-to idx :val)
        year (subscribe [:by-path year-path])
        val (subscribe [:by-path val-path])]
    (fn []
      [v-box :gap "10px" :justify :start :align :start :padding "10px"
         :style {:background bg-color :border "1px solid lightgray" :border-radius "4px"} :width "100%"
         :children [[h-box :gap "6px" :align :center :justify :between :align-self :stretch
                       :children [[h-box :gap "6px" :align :center
                                     :children [[:span.uppercase.light-grey "Year"]
                                                [input-text :width "60px" :model (str @year) :style {:border "none"}
                                                      :on-change #(dispatch [:set-local-item-val year-path (int %)])]]]
                                  [button :label "Delete" :class "btn-default"
                                          :on-click #(dispatch [:delete-local-vector-element profile-path-to idx])]]]
                    [h-box :gap "8px" :align :center :justify :between
                        :children [[:span.uppercase.light-grey label-string]
                                   [input-text :model (str @val) :placeholder "" :width "420px" :style {:border "none"}
                                      :on-change #(dispatch [:set-local-item-val val-path %])]]]]])))

(defn item-view
  "Display the profile"
  [id]
  (let [cn (subscribe [:by-path [:profile :name]])
        photo (subscribe [:by-path [:profile :photo]])
        birth-year (subscribe [:by-path [:profile :birth-year]])
        birth-place (subscribe [:by-path [:profile :birth-place]])
        currently-resides (subscribe [:by-path [:profile :currently-resides]])
        email (subscribe [:by-path [:profile :email]])
        phone (subscribe [:by-path [:profile :phone]])
        url (subscribe [:by-path [:profile :url]])
        degrees (subscribe [:by-path-sorted-by [:profile :degrees] #(compare (:year %2) (:year %1))])
        awards-and-grants (subscribe [:by-path-sorted-by [:profile :awards-and-grants] #(compare (:year %2) (:year %1))])
        residencies (subscribe [:by-path-sorted-by [:profile :residencies] #(compare (:year %2) (:year %1))])
        lectures-and-talks (subscribe [:by-path-sorted-by [:profile :lectures-and-talks] #(compare (:year %2) (:year %1))])
        collections (subscribe [:by-path [:profile :collections]])
        editing (subscribe [:by-path [:profile :editing]])]
    (fn []
      (let [header [title :level :level2 :label "Artist Information"]
            view-control [h-box :gap "2px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[row-button :md-icon-name "zmdi zmdi-edit"
                                          :mouse-over-row? true :tooltip "Edit your profile" :tooltip-position :right-center
                                          :on-click #(dispatch [:edit-item [:profile]])]]]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[button :label "Save" :class "btn-default"
                                          :on-click #(dispatch [:save-changes [:profile]])]
                                        [button :label "Cancel" :class "btn-default"
                                          :on-click #(dispatch [:cancel-edit-item [:profile]])]]]
            view [[h-box :gap "8px" :align :center :justify :start
                     :children [[:span.bold @cn]
                                (when (not (nil? @birth-year))
                                  [label :label (str "b. " @birth-year)])]]
                  (when (or (not (empty? @birth-place)))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "Birth Place"]
                                       [:span (str @birth-place)]]])
                  (when (or (not (empty? @currently-resides)))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "Currently Resides"]
                                       [:span (str @currently-resides)]]])
                  (when (or (not (empty? @email)))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "Email"]
                                       [:span (str @email)]]])
                  (when (or (not (empty? @phone)))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "Phone"]
                                       [:span (str @phone)]]])
                  (when (not (empty? @url))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "url"]
                                       [re-com/hyperlink-href :label (trunc @url 50) :href (url-to-href @url) :target "_blank"]]])
                  (when (or (not (empty? @degrees)))
                    [h-box :gap "12px" :align :start :justify :start :padding "8px 0px"
                            :children [[label :width "20ch" :class "uppercase light-grey" :label "Degrees"]
                                       [v-box :gap "8px" :align :start :justify :start
                                          :children (into [] (mapv (fn [idx degree bg] ^{:key (str "degree-" idx)} [display-year-and-label-view degree bg])
                                                                   (range (count @degrees)) @degrees (cycle [true false])))]]])
                  (when (or (not (empty? @awards-and-grants)))
                    [h-box :gap "12px" :align :start :justify :start :padding "8px 0px"
                            :children [[label :width "20ch" :class "uppercase light-grey" :label "Awards & Grants"]
                                       [v-box :gap "8px" :align :start :justify :start
                                          :children (into [] (mapv (fn [idx awards-and-grant bg] ^{:key (str "awards-and-grant-" idx)} [display-year-and-label-view awards-and-grant bg])
                                                                   (range (count @awards-and-grants)) @awards-and-grants (cycle [true false])))]]])
                  (when (or (not (empty? @residencies)))
                    [h-box :gap "12px" :align :start :justify :start :padding "8px 0px"
                            :children [[label :width "20ch" :class "uppercase light-grey" :label "Residencies"]
                                       [v-box :gap "8px" :align :start :justify :start
                                          :children (into [] (mapv (fn [idx residency bg] ^{:key (str "residency-" idx)} [display-year-and-label-view residency bg])
                                                                   (range (count @residencies)) @residencies (cycle [true false])))]]])
                  (when (or (not (empty? @lectures-and-talks)))
                    [h-box :gap "12px" :align :start :justify :start :padding "8px 0px"
                            :children [[label :width "20ch" :class "uppercase light-grey" :label "Lectures & Talks"]
                                       [v-box :gap "8px" :align :start :justify :start
                                          :children (into [] (mapv (fn [idx lecture-and-talk bg] ^{:key (str "lecture-and-talk-" idx)} [display-year-and-label-view lecture-and-talk bg])
                                                                   (range (count @lectures-and-talks)) @lectures-and-talks (cycle [true false])))]]])]
                   ;; TODO: Add collections
            edit [[h-box :gap "8px" :align :center :justify :between
                    :children [[:span.uppercase.bold "Name"]
                               [input-text :model (str @cn) :placeholder "" :width "320px" :style {:border "none"}
                                  :on-change #(dispatch [:set-local-item-val [:profile :name] %])]]]
                  [h-box :gap "6px" :align :center
                    :children [[:span.uppercase.light-grey "Birth Year"]
                               [input-text :width "60px" :model (str @birth-year) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:profile :birth-year] (js/Number %)])]]]
                  [h-box :gap "8px" :align :center :justify :between
                          :children [[:span.uppercase.light-grey "Birth Place"]
                                     [input-text :model (str @birth-place) :placeholder "" :width "320px" :style {:border "none"}
                                        :on-change #(dispatch [:set-local-item-val [:profile :birth-place] %])]]]
                  [h-box :gap "8px" :align :center :justify :between
                          :children [[:span.uppercase.light-grey "Currently Resides"]
                                     [input-text :model (str @currently-resides) :placeholder "" :width "320px" :style {:border "none"}
                                        :on-change #(dispatch [:set-local-item-val [:profile :currently-resides] %])]]]
                  [h-box :gap "8px" :align :center :justify :between
                          :children [[:span.uppercase.light-grey "Email"]
                                     [input-text :model (str @email) :placeholder "" :width "320px" :style {:border "none"}
                                        :on-change #(dispatch [:set-local-item-val [:profile :email] %])]]]
                  [h-box :gap "6px" :align :center
                    :children [[:span.uppercase.light-grey "Phone"]
                               [input-text :width "280px" :model (str @phone) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:profile :phone] %])]]]
                  [h-box :gap "6px" :align :center
                    :children [[:span.uppercase.light-grey "url"]
                               [input-text :width "280px" :model (str @url) :style {:border "none"}
                                    :on-change #(dispatch [:set-local-item-val [:profile :url] %])]]]
                  [v-box :gap "6px" :align :start :justify :start :align-self :stretch
                      :children [[h-box :gap "6px" :align :center :justify :start
                                   :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Add a Degree"
                                                 :on-click #(dispatch [:create-local-vector-element [:profile :degrees] (helodali.db/default-year-val-map)])]
                                              [:span "Degrees"]]]
                                 (when (not (empty? @degrees))
                                   [v-box :gap "16px" :align :start :justify :start :align-self :stretch
                                      :children (into [] (mapv (fn [idx bg] ^{:key (str "degree-" idx)} [display-year-and-label-edit "Degree" [:profile :degrees] idx bg])
                                                               (range (count @degrees)) (cycle [true false])))])]]
                  [v-box :gap "6px" :align :start :justify :start :align-self :stretch
                      :children [[h-box :gap "6px" :align :center :justify :start
                                   :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Add Award or Grant"
                                                 :on-click #(dispatch [:create-local-vector-element [:profile :awards-and-grants] (helodali.db/default-year-val-map)])]
                                              [:span "Awards & Grants"]]]
                                 (when (not (empty? @awards-and-grants))
                                   [v-box :gap "16px" :align :start :justify :start :align-self :stretch
                                      :children (into [] (mapv (fn [idx bg] ^{:key (str "award-or-grant-" idx)} [display-year-and-label-edit "Award or Grant" [:profile :awards-and-grants] idx bg])
                                                               (range (count @awards-and-grants)) (cycle [true false])))])]]
                  [v-box :gap "6px" :align :start :justify :start :align-self :stretch
                      :children [[h-box :gap "6px" :align :center :justify :start
                                   :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Residencies"
                                                 :on-click #(dispatch [:create-local-vector-element [:profile :residencies] (helodali.db/default-year-val-map)])]
                                              [:span "Residencies"]]]
                                 (when (not (empty? @residencies))
                                   [v-box :gap "16px" :align :start :justify :start :align-self :stretch
                                      :children (into [] (mapv (fn [idx bg] ^{:key (str "residency-" idx)} [display-year-and-label-edit "Residency" [:profile :residencies] idx bg])
                                                               (range (count @residencies)) (cycle [true false])))])]]
                  [v-box :gap "6px" :align :start :justify :start :align-self :stretch
                      :children [[h-box :gap "6px" :align :center :justify :start
                                   :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Lectures & Talks"
                                                 :on-click #(dispatch [:create-local-vector-element [:profile :lectures-and-talks] (helodali.db/default-year-val-map)])]
                                              [:span "Lectures & Talks"]]]
                                 (when (not (empty? @lectures-and-talks))
                                   [v-box :gap "16px" :align :start :justify :start :align-self :stretch
                                      :children (into [] (mapv (fn [idx bg] ^{:key (str "lecture-or-talk-" idx)} [display-year-and-label-edit "Lecture or Talk" [:profile :lectures-and-talks] idx bg])
                                                               (range (count @lectures-and-talks)) (cycle [true false])))])]]]]
        [v-box :gap "10px" :align :start :justify :start ;:style {:border "dashed 1px red"}
               :children (concat [header] (if @editing edit view) [(if @editing save-control view-control)])]))))

(defn profile-view
  "Display the user's profile"
  []
  (fn []
    [v-box :gap "16px" :align :center :justify :center
       :children [[item-view]]]))
