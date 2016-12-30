(ns helodali.views.artwork
    (:require [helodali.db :as db]
              [helodali.routes :refer [route-single-item route-new-item]]
              [helodali.misc :refer [trunc compute-bg-color convert-map-to-options max-string-length expired?
                                     sort-by-datetime sort-by-key-then-created uuid-label-list-to-options
                                     remove-vector-element]]
              [cljs.pprint :refer [pprint]]
              [cljs-time.format :refer [unparse formatters]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list
                                              button datepicker-dropdown checkbox popover-tooltip
                                              popover-anchor-wrapper popover-content-wrapper handler-fn]])
    (:import goog.date.UtcDateTime))


(def status-options [{:id :for-sale :label "For Sale"}
                     {:id :sold :label "Sold"}
                     {:id :private :label "Private"}
                     {:id :not-for-sale :label "Not For Sale"}
                     {:id :destroyed :label "Destroyed"}])

(def media-options (convert-map-to-options db/media))

(def style-options (convert-map-to-options db/styles))

(defn- title-string
  "Return a non-empty title"
  [title]
  (if (empty? title)
    "(no title)"
    title))

(defn display-exhibition-history-view
  [exhibition-history odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        exhibition (if (empty? (:ref exhibition-history))
                      (r/atom nil)
                      (subscribe [:item-by-uuid :exhibitions (:ref exhibition-history)]))]
    [(fn []
      [v-box :gap "8px" :justify :start :align :start :padding "10px" ;:width "100%"
         :style {:background bg-color :border-radius "4px"}
         :children [[h-box :gap "6px" :align :center ;:style {:border "dashed 1px red"}
                       :children [[label :width "11ch" :class "uppercase light-grey" :label "Exhibition:"]
                                  [v-box :gap "2px" :align :start :justify :center ;:width "100%" ;:style {:border "dashed 1px red"}
                                     :children [[label :class "italic" :label (if (not (nil? (:name @exhibition)))
                                                                                 (:name @exhibition)
                                                                                 "(undefined)")]
                                                (when (not (empty? (:location @exhibition)))
                                                   [label :label (:location @exhibition)])
                                                (when (and (not (nil? (:begin-date @exhibition))) (not (nil? (:end-date @exhibition))))
                                                  [h-box :gap "8px"
                                                     :children [[label :label (str (unparse (formatters :date) (:begin-date @exhibition))
                                                                                   " to " (unparse (formatters :date) (:end-date @exhibition)))]]])]]]]
                    ;; Display the notes from the exhibition-history, not the exhibition itself.
                    (when (not (empty? (:notes exhibition-history)))
                      [h-box :gap "6px" :align :start ;:style {:border "dashed 1px red"}
                           :children [[label :width "11ch" :class "uppercase light-grey" :label "Notes: "]
                                      [re-com/box :max-width "360px" :child [:p (:notes exhibition-history)]]]])]])]))

(defn display-exhibition-history-edit
  [id idx odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        exhibition-uuid (subscribe [:by-path [:artwork id :exhibition-history idx :ref]])
        exhibitions (subscribe [:items-vals-with-uuid :exhibitions :name]) ;; this is a list of 2-tuples [uuid name]
        notes (subscribe [:by-path [:artwork id :exhibition-history idx :notes]])]
    [(fn []
      [v-box :gap "4px" :justify :start :align :start :padding "10px"
         :style {:background bg-color :border "1px solid lightgray" :border-radius "4px"} :width "100%"
         :children [[h-box :gap "6px" :align :center
                       :children [[:span.input-label (str "Exhibition ")]
                                  [h-box :gap "6px" :align :center
                                     :children [[single-dropdown :choices (uuid-label-list-to-options @exhibitions) :model (if (nil? @exhibition-uuid) :none exhibition-uuid) :width "200px"
                                                       :on-change #(if (and (not= @exhibition-uuid %) (not (and (= @exhibition-uuid nil) (= % :none))))
                                                                     (dispatch [:set-local-item-val [:artwork id :exhibition-history idx :ref] (if (= :none %) nil %)]))]]]]]
                    [h-box :gap "6px" :align :center :justify :between :align-self :stretch
                       :children [[h-box :gap "6px" :align :center
                                     :children [[:span.input-label "Notes "]
                                                [input-textarea :model (str @notes) :width "520px"
                                                    :rows 4 :on-change #(dispatch [:set-local-item-val [:artwork id :exhibition-history idx :notes] %])]]]
                                  [button :label "Delete" :class "btn-default"
                                          :on-click #(dispatch [:delete-local-vector-element [:artwork id :exhibition-history] idx])]]]]])]))

(defn display-purchase-view
  [purchase odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
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
      [v-box :gap "4px" :justify :start :align :start :padding "10px" :width "100%"
         :style {:background bg-color :border-radius "4px"}
         :children [[h-box :gap "6px" :align :center ;:style {:border "dashed 1px red"}
                       :children [[:span.uppercase.light-grey "Purchased: "]
                                  [:span (unparse (formatters :date) (:date purchase))]
                                  (when (not (empty? @buyer-contact))
                                     [h-box :gap "6px" :align :center :children [[:span "by "]
                                                                                 [:span.bold (:name @buyer-contact)]]])
                                  (when (:on-public-display purchase)
                                    [:span.uppercase " | on public display"])
                                  (when (:collection purchase)
                                    [:span.uppercase " | part of collection"])
                                  (when (not (empty? (:location purchase)))
                                    [:span (str " | " (:location purchase))])]]
                    [h-box :gap "8px"
                       :children [[h-box :gap "6px" :align :center
                                      :children [[:span.uppercase.light-grey "Price: "]
                                                 [:span (:price purchase)]]]
                                  (when (> (:total-commission-percent purchase) 0)
                                    [h-box :gap "6px" :align :center
                                      :children [[:span.uppercase.light-grey "  Total Commision Percent: "]
                                                 [:span (:total-commission-percent purchase)]]])
                                  (when (:donated purchase)
                                    [:span.bold.uppercase " |  donated"])
                                  (when (:commissioned purchase)
                                    [:span.bold.uppercase " |  commissioned"])]]
                    (when (not (empty? @agent-contact))
                      [h-box :gap "6px" :align :center
                         :children [[:span.uppercase.light-grey "agent: "]
                                    [:span (:name @agent-contact)]]])
                    (when (not (empty? @dealer-contact))
                      [h-box :gap "6px" :align :center
                         :children [[:span.uppercase.light-grey "dealer: "]
                                    [:span (:name @dealer-contact)]]])
                    (when (not (empty? (:notes purchase)))
                      [h-box :gap "6px" :children [[:span.uppercase.light-grey "Notes: "]
                                                   [:span (:notes purchase)]]])]])]))

(defn display-purchase-edit
  [id idx odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        buyer-uuid (subscribe [:by-path [:artwork id :purchases idx :buyer]])
        agent-uuid (subscribe [:by-path [:artwork id :purchases idx :agent]])
        dealer-uuid (subscribe [:by-path [:artwork id :purchases idx :dealer]])
        contacts (subscribe [:items-vals-with-uuid :contacts :name]) ;; this is a list of 2-tuples [uuid name]
        purchase-date (subscribe [:by-path [:artwork id :purchases idx :date]])
        on-public-display (subscribe [:by-path [:artwork id :purchases idx :on-public-display]])
        collection (subscribe [:by-path [:artwork id :purchases idx :collection]])
        location (subscribe [:by-path [:artwork id :purchases idx :location]])
        price (subscribe [:by-path [:artwork id :purchases idx :price]])
        total-commission-percent (subscribe [:by-path [:artwork id :purchases idx :total-commission-percent]])
        donated (subscribe [:by-path [:artwork id :purchases idx :donated]])
        commissioned (subscribe [:by-path [:artwork id :purchases idx :commissioned]])
        notes (subscribe [:by-path [:artwork id :purchases idx :notes]])]
    [(fn []
      [v-box :gap "4px" :justify :start :align :start :padding "10px"
         :style {:background bg-color :border "1px solid lightgray" :border-radius "4px"} :width "100%"
         :children [[h-box :gap "6px" :align :center
                       :children [[:span.input-label (str "Purchased ")]
                                  [datepicker-dropdown :model (goog.date.UtcDateTime. @purchase-date)
                                        :on-change #(dispatch [:set-local-item-val [:artwork id :purchases idx :date] %])]
                                  [h-box :gap "6px" :align :center
                                     :children [[:span "by "]
                                                [single-dropdown :choices (uuid-label-list-to-options @contacts) :model (if (nil? @buyer-uuid) :none buyer-uuid) :width "200px"
                                                       :on-change #(if (and (not= @buyer-uuid %) (not (and (= @buyer-uuid nil) (= % :none))))
                                                                     (dispatch [:set-local-item-val [:artwork id :purchases idx :buyer] %]))]]]
                                  [checkbox :model @on-public-display :label "On Public Display?"
                                     :on-change #(dispatch [:set-local-item-val [:artwork id :purchases idx :on-public-display] (not @on-public-display)])]
                                  [checkbox :model @collection :label "Part of Collection?"
                                     :on-change #(dispatch [:set-local-item-val [:artwork id :purchases idx :collection] (not @collection)])]]]
                    [h-box :gap "6px" :align :center
                      :children [[:span.input-label "Location"]
                                 [input-text :width "384px" :model (str @location) :style {:border "none"}
                                      :on-change #(dispatch [:set-local-item-val [:artwork id :purchases idx :location] %])]
                                 [checkbox :model @donated :label "Donated?"
                                    :on-change #(dispatch [:set-local-item-val [:artwork id :purchases idx :donated] (not @donated)])]
                                 [checkbox :model @commissioned :label "Commissioned?"
                                    :on-change #(dispatch [:set-local-item-val [:artwork id :purchases idx :commissioned] (not @commissioned)])]]]
                    [h-box :gap "8px" :align :center
                       :children [[h-box :gap "4px" :align :center
                                    :children [[:span.input-label "Price "]
                                               [input-text :width "80px" :model (str @price) :style {:border-radius "4px"}
                                                   :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:artwork id :purchases idx :price] (js/Number %)])]]]
                                  [h-box :gap "4px" :align :center
                                    :children [[:span.input-label "Total commission percentage given to agents and dealers "]
                                               [input-text :width "70px" :model (str @total-commission-percent) :style {:border-radius "4px"}
                                                   :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:artwork id :purchases idx :total-commission-percent] (js/Number %)])]]]]]
                    [h-box :gap "6px" :align :center
                       :children [[:span.input-label "Agent "]
                                  [single-dropdown :choices (uuid-label-list-to-options @contacts) :model (if (nil? @agent-uuid) :none agent-uuid) :width "200px"
                                       :on-change #(if (and (not= @agent-uuid %) (not (and (= @agent-uuid nil) (= % :none))))
                                                     (dispatch [:set-local-item-val [:artwork id :purchases idx :agent] (if (= :none %) nil %)]))]
                                  [:span.input-label "Dealer "]
                                  [single-dropdown :choices (uuid-label-list-to-options @contacts) :model (if (nil? @dealer-uuid) :none dealer-uuid) :width "200px"
                                       :on-change #(if (and (not= @dealer-uuid %) (not (and (= @dealer-uuid nil) (= % :none))))
                                                     (dispatch [:set-local-item-val [:artwork id :purchases idx :dealer] (if (= :none %) nil %)]))]]]
                    [h-box :gap "6px" :align :center :justify :between :align-self :stretch
                       :children [[h-box :gap "6px" :align :center
                                     :children [[:span.input-label "Notes "]
                                                [input-textarea :model (str @notes) :width "520px" :rows 4
                                                    :on-change #(dispatch [:set-local-item-val [:artwork id :purchases idx :notes] %])]]]
                                  [button :label "Delete" :class "btn-default"
                                          :on-click #(dispatch [:delete-local-vector-element [:artwork id :purchases] idx])]]]]])]))

(defn display-secondary-image
  [id editing? idx image odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        image-size "240px"
        image-input-id (str "image-upload-" id "-" idx)
        expiration (:signed-thumb-url-expiration-time image)
        url (cond
              (:processing image) "/image-assets/ajax-loader.gif"
              (and (not (nil? expiration)) (not (expired? expiration))) (:signed-thumb-url image)
              :else "/image-assets/thumb-stub.png")]
    ;; Perform some dispatching if the artwork is not in sync with S3 and database
    (if (:processing image)
      (dispatch [:refresh-image [:artwork id :images idx]])
      (when (and (not (nil? image)) (or (nil? url) (expired? expiration)))
        (dispatch [:get-signed-url [:artwork id :images idx] (:key image)])))
    [(fn []
      [v-box ;:max-width image-size :max-height image-size ;:style {:margin-top "10px" :z-index 0 :position "relative"}
         :children [[box :max-width image-size :max-height image-size
                      :child [:img {:src url}]] ; :on-click #()}]]  TODO: on-click should present a larger image
                    (when (and @editing? (not (:processing image)))
                      [h-box :gap "8px" :align :center :justify :center :style {:background-color "#428bca"}
                         :children [;; hidden input text + button for image upload
                                    [:input {:type "file" :accept "image/*" :multiple false :name "image-upload"
                                             :id image-input-id :style {:display "none"}
                                             :on-change #(let [el (.getElementById js/document image-input-id)]
                                                           (dispatch [:replace-image [:artwork id] (:uuid image) (aget (.-files el) 0)]))}]
                                    [md-icon-button :md-icon-name "zmdi zmdi-upload"
                                       :emphasise? true :tooltip "Replace this image" ;:tooltip-position :right-center
                                       :on-click #(let [el (.getElementById js/document image-input-id)]
                                                    (.click el))]
                                    [md-icon-button :md-icon-name "zmdi zmdi-delete"
                                       :emphasise? true :tooltip "Delete this image" ;:tooltip-position :right-center
                                       :on-click #(dispatch [:delete-s3-vector-element ["helodali-raw-images"] [:artwork id :images] (:uuid image)])]]])]])]))

(defn item-properties-panel
  [id]
  (let [editing (subscribe [:item-key :artwork id :editing])
        images (subscribe [:item-key :artwork id :images])
        year (subscribe [:item-key :artwork id :year])
        status (subscribe [:item-key :artwork id :status])
        style (subscribe [:item-key :artwork id :style])
        type (subscribe [:item-key :artwork id :type])
        title (subscribe [:item-key :artwork id :title])
        expenses (subscribe [:item-key :artwork id :expenses])
        purchases (subscribe [:by-path-sorted-by [:artwork id :purchases] (partial sort-by-datetime :date true)])
        exhibition-history (subscribe [:by-path-and-deref-sorted-by [:artwork id :exhibition-history] :exhibitions (partial sort-by-datetime :begin-date true)])
        condition (subscribe [:item-key :artwork id :condition])
        list-price (subscribe [:item-key :artwork id :list-price])
        current-location (subscribe [:item-key :artwork id :current-location])
        instagram (subscribe [:item-key :artwork id :instagram])
        facebook (subscribe [:item-key :artwork id :facebook])
        medium (subscribe [:item-key :artwork id :medium])
        dimensions (subscribe [:item-key :artwork id :dimensions])
        description (subscribe [:item-key :artwork id :description])
        display-type (subscribe [:app-key :display-type])
        single-item (or (= @display-type :single-item) (= @display-type :new-item))]
    (fn item-properties-fn []
      (let [view-basics [v-box :gap "2px" :align :start :justify :around
                          :children [[h-box :gap "4px" :justify :start :align :center
                                        :children [[label :label (str @year)]
                                                   [:span.uppercase.bold (str " | " (clojure.string/replace (name @status) #"-" " "))]
                                                   (when (not (empty? @condition)) [:span (str " | " @condition)])]]
                                     [h-box :gap "4px" :align :center :justify :start
                                         :children [[label :label (name @type)]
                                                    (when (not (empty? @style))
                                                      [label :label (str " | " (clojure.string/join ", " (map name @style)))])]]
                                     [:span.italic @medium]
                                     [:span @dimensions]
                                     [:span (str "List Price: $" @list-price)]
                                     (when (not (= 0 @expenses)) [:span (str "Expenses: $" @expenses)])]]
            view [[v-box :gap "6px" :align :start :justify :start :padding "8px" ;:max-width "300px"
                       :children [[re-com/title :label (title-string @title) :level :level3]
                                  ;; The placement of view-basics depends on an existence of description
                                  (if (empty? @description)
                                    view-basics
                                    [re-com/box :max-width "360px" :child [:p.italic @description]])]]
                  (when (not (empty? @description)) view-basics)]
            view-extended
              [v-box :gap "12px" :align :start :justify :start
                  :children [(when (not (empty? @purchases))
                               [v-box :gap "16px" :align :start :justify :start
                                  :children (into [] (map display-purchase-view @purchases (cycle [true false])))])
                             (when (not (empty? @exhibition-history))
                               [v-box :gap "16px" :align :start :justify :start
                                  :children (into [] (map display-exhibition-history-view @exhibition-history (cycle [true false])))])
                             (when (> (count @images) 1)
                               [h-box :gap "16px" :align :start :justify :start :padding "20px" :style {:flex-flow "row wrap"}
                                  :children (into [] (map (partial display-secondary-image id false) (range 1 (count @images)) (rest @images) (cycle [true false])))])]]
            create-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                              :children [[button :label "Create" :class "btn-default"
                                           :on-click #(dispatch [:create-from-placeholder :artwork])]
                                         [button :label "Cancel" :class "btn-default"
                                           :on-click #(dispatch [:delete-item :artwork id])]]]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[button :label "Save" :class "btn-default"
                                          :on-click #(dispatch [:save-changes [:artwork id]])]
                                        [button :label "Cancel" :class "btn-default"
                                          :on-click #(dispatch [:cancel-edit-item [:artwork id]])]]]
            edit [[v-box :gap "4px" :align :baseline :justify :start
                     :children [[h-box :gap "4px" :align :center
                                   :children [[:span.uppercase.light-grey "title"]
                                              [input-text :model (str @title) :placeholder "Title of piece" :width "330px" :style {:border "none"}
                                                 :on-change #(dispatch [:set-local-item-val [:artwork id :title] %])]]]
                                [:span.uppercase.light-grey "description"]
                                [input-textarea :model (str @description) :width "360px"
                                    :rows 7 :on-change #(dispatch [:set-local-item-val [:artwork id :description] %])]]]
                  [v-box :gap "4px" :align :start :justify :around
                    :children [[h-box :gap "10px" :align :center :justify :start
                                 :children [[h-box :gap "4px" :align :center
                                              :children [[:span.uppercase.light-grey "year"]
                                                         [input-text :width "60px" :model (str @year) :placeholder "2016" :style {:border "none"}
                                                             :attr {:max-length 4} :on-change #(dispatch [:set-local-item-val [:artwork id :year] (js/Number %)])]]]
                                            [single-dropdown :choices status-options :width "118px" :model @status
                                                    :on-change #(dispatch [:set-local-item-val [:artwork id :status] %])]]]
                               [h-box :gap "4px" :align :center
                                 :children [[:span.uppercase.light-grey "type"]
                                            [single-dropdown :choices media-options :width "160px" :model @type
                                                    :on-change #(dispatch [:set-local-item-val [:artwork id :type] %])]]]
                               [h-box :gap "4px" :align :center
                                 :children [[:span.uppercase.light-grey "medium"]
                                            [input-text :width "160px" :model (str @medium) :placeholder "E.g. oil on canvas" :style {:border "none"}
                                                 :on-change #(dispatch [:set-local-item-val [:artwork id :medium] %])]]]
                               [h-box :gap "4px" :align :center
                                 :children [[:span.uppercase.light-grey "dimensions"]
                                            [input-text :width "180px" :model (str @dimensions) :placeholder "E.g. H x W x D in" :style {:border "none"}
                                                 :on-change #(dispatch [:set-local-item-val [:artwork id :dimensions] %])]]]
                               [h-box :gap "4px" :align :center
                                 :children [[:span.uppercase.light-grey "condition"]
                                            [input-text :width "148px" :model (str @condition) :style {:border "none"}
                                                 :on-change #(dispatch [:set-local-item-val [:artwork id :condition] %])]]]
                               [h-box :gap "10px" :align :center :justify :start
                                 :children [[h-box :gap "4px" :align :center
                                              :children [[:span.uppercase.light-grey "list price"]
                                                         [input-text :width "80px" :model (str @list-price) :style {:border "none"}
                                                             :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:artwork id :list-price] (js/Number %)])]]]
                                            [h-box :gap "4px" :align :center
                                              :children [[:span.uppercase.light-grey "expenses"]
                                                         [input-text :width "70px" :model (str @expenses) :style {:border "none"}
                                                             :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:artwork id :expenses] (js/Number %)])]]]]]]]
                  [h-box :gap "6px" :align :center
                     :children [[:span.uppercase.light-grey "style"]
                                [selection-list :choices style-options :model (if (empty? @style) #{} (set @style)) :height "210px"
                                       :on-change #(dispatch [:set-local-item-val [:artwork id :style] %])]]]]
            edit-extended
              [v-box :gap "6px" :align :start :justify :start :align-self :stretch
                  :children [[h-box :gap "6px" :align :center :justify :start
                               :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Add a Purchase record"
                                             :on-click #(dispatch [:create-local-vector-element [:artwork id :purchases] (db/default-purchase)])]
                                          [:span "Purchases"]]]
                             (when (not (empty? @purchases))
                               [v-box :gap "16px" :align :start :justify :start :align-self :stretch
                                  :children (into [] (map (partial display-purchase-edit id) (range (count @purchases)) (cycle [true false])))])
                             [re-com/gap :size "4px"]
                             [h-box :gap "6px" :align :center :justify :start
                                :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Add an Exhibition record"
                                              :on-click #(dispatch [:create-local-vector-element [:artwork id :exhibition-history] (db/default-exhibition-history)])]
                                           [:span "Exhibitions"]]]
                             (when (not (empty? @exhibition-history))
                               [v-box :gap "16px" :align :start :justify :start :align-self :stretch
                                  :children (into [] (map (partial display-exhibition-history-edit id) (range (count @exhibition-history)) (cycle [true false])))])]]]
        [v-box :gap "8px" :align :start :justify :start :margin "20px" ;:style {:border "dashed 1px #ddd"}
           :children [[h-box :gap "10px"  :align :start :justify :start
                         :children (if (and @editing single-item) edit view)]
                      (when single-item
                        (if @editing edit-extended view-extended))
                      (when (and single-item @editing)
                        (if (= @display-type :new-item)
                          create-control
                          save-control))]]))))

(defn item-view
  "Display an item"
  [id]
  (let [uuid (subscribe [:item-key :artwork id :uuid])
        title (subscribe [:item-key :artwork id :title])
        images (subscribe [:item-key :artwork id :images])
        expanded (subscribe [:item-key :artwork id :expanded])
        container-style (if @expanded {:background-color "#fff"} {})
        editing (subscribe [:item-key :artwork id :editing])
        signed-thumb-url (subscribe [:by-path [:artwork id :images 0 :signed-thumb-url]])
        delegation-token (subscribe [:app-key :delegation-token])
        image-input-id (str "image-upload-" id "-0")
        showing-download-tooltip? (r/atom false)
        showing-primary-image-info? (r/atom false)
        display-type (subscribe [:app-key :display-type])
        single-item (or (= @display-type :single-item) (= @display-type :new-item))
        image-size (if (or (= @display-type :contact-sheet) @editing) "240px" "480px")]  ;; Set image size based on contact-sheet vs. other views
    [(fn []
      (let [controls [h-box :gap "2px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                        :children [;(when (not (= @display-type :contact-sheet))
                                   ;  [row-button :disabled? (not @editing) :md-icon-name "zmdi zmdi-plus-circle-o"
                                   ;    :mouse-over-row? true :tooltip "Add image" :tooltip-position :right-center
                                   ;    :on-click #(dispatch [:set-local-item-val [:artwork id :show-add-image-input] true])
                                   (when (not single-item)
                                     [row-button :md-icon-name "zmdi zmdi-copy"
                                       :mouse-over-row? true :tooltip "Copy this item" :tooltip-position :right-center
                                       :on-click #(dispatch [:copy-item :artwork id :title])])
                                   (when single-item
                                     [row-button :md-icon-name "zmdi zmdi-edit"
                                       :mouse-over-row? true :tooltip "Edit this item" :tooltip-position :right-center
                                       :on-click #(dispatch [:edit-item [:artwork id]])])
                                   [row-button :md-icon-name "zmdi zmdi-delete"
                                     :mouse-over-row? true :tooltip "Delete this item" :tooltip-position :right-center
                                     :on-click #(dispatch [:delete-artwork-item :artwork id])]
                                   (when (not single-item)
                                     [md-icon-button :md-icon-name "zmdi zmdi-more mdc-text-blue"  :tooltip "Show more..."
                                        :on-click #(route-single-item :artwork @uuid)])]]
            image (first @images) ;; Note that images may be empty, hence image is nil
            have-delegation-token? (not (empty? @delegation-token))
            raw-image-url (:signed-raw-url image)
            expiration (:signed-thumb-url-expiration-time image)
            url (cond
                  (:processing image) "/image-assets/ajax-loader.gif"
                  (and (not (nil? expiration)) (not (expired? expiration))) @signed-thumb-url
                  :else "/image-assets/thumb-stub.png")
            object-fit (cond
                          (:processing image) "none"
                          (or (= @display-type :contact-sheet) @editing) "cover"
                          :else "contain")]
        ;; Perform some dispatching if the artwork is not in sync with S3 and database
        (if (:processing image)
          (dispatch [:refresh-image [:artwork id :images 0]])
          (when (and have-delegation-token? (not (nil? image)) (or (nil? url) (expired? expiration)))
            (dispatch [:get-signed-url [:artwork id :images 0] "helodali-images" (:key image) :signed-thumb-url :signed-thumb-url-expiration-time])))
        (when (and (:key image) have-delegation-token? (expired? (:signed-raw-url-expiration-time image)))
          (dispatch [:get-signed-url [:artwork id :images 0] "helodali-raw-images" (:key image) :signed-raw-url :signed-raw-url-expiration-time]))

        ;; Base UI on new-item versus single-item versus inline display within contact-sheet
        ;; A new-item view does not present the image or edit/delete controls
        (if (= @display-type :new-item)
          [h-box :gap "4px" :align :start :justify :start :style container-style :style {:flex-flow "row wrap"}
            :children [[item-properties-panel id]]]
          [h-box :gap "4px" :align :start :justify :start :style container-style :padding "20px" :style {:flex-flow "row wrap"}
            :children [[v-box :gap "2px" :width image-size :align :center :justify :center :height "100%"
                         :children [[v-box ;:max-width image-size :max-height image-size ;:style {:margin-top "10px" :z-index 0 :position "relative"}
                                       :children [[box :max-width image-size :max-height image-size
                                                    :child [:img {:src url :class object-fit :width image-size :height image-size ;:style {:object-fit "cover"}
                                                                  :on-click #(if (not (= @display-type :new-item)) ;; Don't toggle 'expanded' when in :new-item mode
                                                                               (dispatch [:set-local-item-val [:artwork id :expanded] (not @expanded)])
                                                                               nil)}]]
                                                  (when (and @editing (= @display-type :single-item) (not (:processing image)))
                                                    [h-box :gap "8px" :align :center :justify :center :style {:background-color "#428bca"}
                                                       :children [(when-not (empty? (:metadata image))
                                                                    (let [md (:metadata image)]
                                                                      [popover-anchor-wrapper
                                                                         :showing? showing-primary-image-info? :position :below-center
                                                                         :anchor [md-icon-button :md-icon-name "zmdi zmdi-info"
                                                                                     :emphasise? true :tooltip "Original image information"
                                                                                     :on-click #(swap! showing-primary-image-info? not)]
                                                                         :popover [popover-content-wrapper :width "250px" :close-button? true :title "Original Image"
                                                                                     :body [v-box :gap "4px" :align :start
                                                                                              :children [[:span (:filename image)]
                                                                                                         [:span (str (quot (:size md) 1024) " KB")]
                                                                                                         [:span (str (:width md) " x " (:height md))]
                                                                                                         [:span (str "Format: " (:format md))]
                                                                                                         [:span (str "Color space: " (:space md))]]]]]))
                                                                  ;; hidden input text + button for image upload
                                                                  [:input {:type "file" :accept "image/*" :multiple false :name "image-upload"
                                                                           :id image-input-id :style {:display "none"}
                                                                           :on-change #(let [el (.getElementById js/document image-input-id)]
                                                                                         (if (nil? image)
                                                                                           (dispatch [:add-image [:artwork id] (aget (.-files el) 0)])
                                                                                           (dispatch [:replace-image [:artwork id] (:uuid image) (aget (.-files el) 0)])))}]
                                                                  [md-icon-button :md-icon-name "zmdi zmdi-upload"
                                                                     :emphasise? true :tooltip "Replace this image" ;:tooltip-position :right-center
                                                                     :on-click #(let [el (.getElementById js/document image-input-id)]
                                                                                  (.click el))]
                                                                  (when-not (empty? (:key image))
                                                                    [md-icon-button :md-icon-name "zmdi zmdi-delete"
                                                                       :emphasise? true :tooltip "Delete this image" ;:tooltip-position :right-center
                                                                       :on-click #(if-not (empty? @images)  ;; TODO: fix this when we support multiple images
                                                                                    (dispatch [:delete-s3-vector-element ["helodali-raw-images"]
                                                                                                        [:artwork id :images] (:uuid (first @images))]))])
                                                                  (when-not (empty? (:key image))
                                                                    [popover-tooltip :label "Download original image"
                                                                         :showing? showing-download-tooltip? :position :below-center
                                                                         :anchor [:a {:class "zmdi zmdi-download rc-md-icon-button rc-icon-emphasis"
                                                                                      :on-mouse-over (handler-fn (reset! showing-download-tooltip? true))
                                                                                      :on-mouse-out  (handler-fn (reset! showing-download-tooltip? false))
                                                                                      :href (:signed-raw-url image)
                                                                                      :download ""}]])]])]]
                                    (when (and @expanded (or (not @editing) (not single-item))) controls)
                                    (when (not @expanded) [:span (title-string @title)])]]
                       (when @expanded [item-properties-panel id])]])))]))

(defn item-row-view
  "Display item as thumbnail and selective properties. The 'widths' map contains the string
   length of the longest title and medium. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for title, by truncating the strings over the max."
  [widths id odd-row?]
  (let [uuid (subscribe [:item-key :artwork id :uuid])
        bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        images (subscribe [:item-key :artwork id :images])
        thumb-size "80px"
        title (subscribe [:item-key :artwork id :title])
        year (subscribe [:item-key :artwork id :year])
        status (subscribe [:item-key :artwork id :status])
        style (subscribe [:item-key :artwork id :style])
        type (subscribe [:item-key :artwork id :type])
        purchases (subscribe [:item-key :artwork id :purchases])
        list-price (subscribe [:item-key :artwork id :list-price])
        instagram (subscribe [:item-key :artwork id :instagram])
        facebook (subscribe [:item-key :artwork id :facebook])
        medium (subscribe [:item-key :artwork id :medium])
        dimensions (subscribe [:item-key :artwork id :dimensions])]
    [(fn []
      (let [col1-wdith (max (:title widths) (:style widths))
            image (first @images)
            expiration (:signed-thumb-url-expiration-time image)
            url (cond
                  (:processing image) "/image-assets/ajax-loader.gif"
                  (and (not (nil? expiration)) (not (expired? expiration))) (:signed-thumb-url image)
                  :else "/image-assets/thumb-stub.png")]
        ;; Perform some dispatching if the artwork is not in sync with S3 and database
        (if (:processing image)
          (dispatch [:refresh-image [:artwork id :images 0]])
          (when (and (not (nil? image)) (or (nil? url) (expired? expiration)))
            (dispatch [:get-signed-url [:artwork id :images 0] "helodali-images" (:key image) :signed-thumb-url :signed-thumb-url-expiration-time])))
        [h-box :gap "6px" :align :center :justify :start :style {:background bg-color} :padding "8px" :width "100%"
          :children [[box :max-width thumb-size :max-height thumb-size :margin "10px"
                       :child [:img {:src url :class "cover" :width thumb-size :height thumb-size
                                     :on-click #(route-single-item :artwork @uuid)}]]
                     ; [re-com/gap :size "20px"]
                     [v-box :gap "2px" :align :start :justify :around
                        :children [[label :class "bold" :width (str (max 8 col1-wdith) "ch") :label (trunc (title-string @title) (:title widths))]
                                   [h-box :gap "4px" :justify :start :align :center
                                      :children [[:span (str @year)]
                                                 [:span.uppercase.bold (str " | " (clojure.string/replace (name @status) #"-" " "))]]]
                                   [label :width (str (max 8 col1-wdith) "ch")
                                          :label (cond-> (name @type)
                                                    (not (empty? @style)) (str (trunc (str " | " (clojure.string/join ", " (map name @style))) (:style widths))))]]]
                     [v-box :gap "2px" :align :start :justify :around
                         :children [[label :width (str (max 8 (:medium widths)) "ch") :label (trunc @medium (:medium widths))]
                                    [label :width "20ch" :label @dimensions]
                                    [label :width "20ch" :label (str "List Price: " @list-price)]]]
                     [v-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                         :children [[row-button :md-icon-name "zmdi zmdi-copy"
                                        :mouse-over-row? true :tooltip "Copy this item"
                                        :on-click #(dispatch [:copy-item :artwork id :title])]
                                    [row-button :md-icon-name "zmdi zmdi-delete"
                                      :mouse-over-row? true :tooltip "Delete this item"
                                      :on-click #(dispatch [:delete-item :artwork id])]]]]]))]))

(defn item-list-view
  "Display item properties in single line - no image display. The 'widths' map contains the string
   length of the longest title, medium, etc. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for title, by truncating the strings over the max.

   Some columns are not displayed if there is no data available. E.g. if 'condition' is not defined
   for any artwork"
  [widths id odd-row?]
  (let [uuid (subscribe [:item-key :artwork id :uuid])
        bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        title (subscribe [:item-key :artwork id :title])
        year (subscribe [:item-key :artwork id :year])
        status (subscribe [:item-key :artwork id :status])
        style (subscribe [:item-key :artwork id :style])
        type (subscribe [:item-key :artwork id :type])
        expenses (subscribe [:item-key :artwork id :expenses])
        purchases (subscribe [:item-key :artwork id :purchases])
        condition (subscribe [:item-key :artwork id :condition])
        list-price (subscribe [:item-key :artwork id :list-price])
        current-location (subscribe [:item-key :artwork id :current-location])
        instagram (subscribe [:item-key :artwork id :instagram])
        facebook (subscribe [:item-key :artwork id :facebook])
        medium (subscribe [:item-key :artwork id :medium])
        dimensions (subscribe [:item-key :artwork id :dimensions])]
    [(fn []
      [h-box :align :center :justify :start :style {:background bg-color} :width "100%"
        :children [[hyperlink :class "semibold" :style {:width (str (max 14 (:title widths)) "ch")} :label (trunc (title-string @title) (:title widths))
                       :on-click #(route-single-item :artwork @uuid)]
                   [label :width "6ch" :label @year]
                   [label :width "12ch" :label (clojure.string/replace (name @status) #"-" " ")]
                   ; [label :width "16ch" :label (clojure.string/join ", " (map name @style))]
                   [label :width (str (max 8 (:medium widths)) "ch") :label (trunc @medium (:medium widths))]
                   [label :width "20ch" :label (str @dimensions)]
                   [label :width "12ch" :label (str @list-price)]
                   [label :width "12ch" :label (str @expenses)]
                   [label :width "10ch" :label (name @type)]
                   [h-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                       :children [[row-button :md-icon-name "zmdi zmdi-copy"
                                      :mouse-over-row? true :tooltip "Copy this item"
                                      :on-click #(dispatch [:copy-item :artwork id :title])]
                                  [row-button :md-icon-name "zmdi zmdi-delete"
                                    :mouse-over-row? true :tooltip "Delete this item"
                                    :on-click #(dispatch [:delete-item :artwork id])]]]]])]))

(defn row-view
  "Display items one per row with a small thumbnail"
  []
  (let [items (subscribe [:items-keys-sorted-by :artwork (partial sort-by-key-then-created :year true)])
        titles (subscribe [:items-vals :artwork :title])
        types (subscribe [:items-vals :artwork :type])
        styles (subscribe [:items-vals :artwork :style])
        mediums (subscribe [:items-vals :artwork :medium])]
    (fn []
      (let [widths {:title (+ 4 (max-string-length @titles 80))
                    :style (+ (max-string-length (map name @types) 80) (max-string-length (map #(str " | " (clojure.string/join ", " (map name %))) @styles) 60))
                    :medium (+ 4 (max-string-length @mediums 30))}]
        (pprint (str "style width: " (:style widths)))
        [v-box :gap "4px" :align :center :justify :start
           :children (into [] (map (partial item-row-view widths) @items (cycle [true false])))]))))

(defn list-view
  "Display list of items, one per line"
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :artwork]])
        items (subscribe [:items-keys-sorted-by-key :artwork sort-by-key-then-created])
        titles (subscribe [:items-vals :artwork :title])
        mediums (subscribe [:items-vals :artwork :medium])]
    (fn []
      (let [widths {:title (+ 4 (max-string-length @titles 80))
                    :medium (+ 4 (max-string-length @mediums 30))}
            header [h-box :align :center :justify :start :width "100%"
                      :children [[hyperlink :class "bold uppercase" :style {:width (str (max 14 (:title widths)) "ch")} :label "Artwork Title"
                                    :tooltip "Sort by Title" :on-click #(if (= (first @sort-key) :title)
                                                                          (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                                          (dispatch [:set-local-item-val [:sort-keys :artwork] [:title true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "6ch"} :label "year"
                                     :tooltip "Sort by Year" :on-click #(if (= (first @sort-key) :year)
                                                                          (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                                          (dispatch [:set-local-item-val [:sort-keys :artwork] [:year false]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "12ch"} :label "status"
                                     :tooltip "Sort by Status" :on-click #(if (= (first @sort-key) :status)
                                                                            (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                                            (dispatch [:set-local-item-val [:sort-keys :artwork] [:status true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width (str (max 8 (get widths :medium)) "ch")} :label "medium"
                                    :tooltip "Sort by Medium" :on-click #(if (= (first @sort-key) :medium)
                                                                          (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                                          (dispatch [:set-local-item-val [:sort-keys :artwork] [:medium false]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "20ch"} :label "dimensions"
                                    :tooltip "Sort by Dimensions" :on-click #(if (= (first @sort-key) :dimensions)
                                                                              (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                                              (dispatch [:set-local-item-val [:sort-keys :artwork] [:dimensions false]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "12ch"} :label "list price"
                                    :tooltip "Sort by Price" :on-click #(if (= (first @sort-key) :list-price)
                                                                          (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                                          (dispatch [:set-local-item-val [:sort-keys :artwork] [:list-price false]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "12ch"} :label "expenses"
                                    :tooltip "Sort by Expenses" :on-click #(if (= (first @sort-key) :expenses)
                                                                             (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                                             (dispatch [:set-local-item-val [:sort-keys :artwork] [:expenses false]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "10ch"} :label "type"
                                    :tooltip "Sort by Type" :on-click #(if (= (first @sort-key) :type)
                                                                         (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                                         (dispatch [:set-local-item-val [:sort-keys :artwork] [:type false]]))]]]]
        [v-box :gap "4px" :align :center :justify :start
           :children (into [header] (map (partial item-list-view widths) @items (cycle [true false])))]))))

(defn view-selection
  "The row of view selection controls: contact-sheet row list"
  []
  (fn []
    [h-box :gap "18px" :align :center :justify :center
       :children [;[md-icon-button :md-icon-name "zmdi zmdi-view-dashboard mdc-text-grey"
                  ;     :on-click #(dispatch [:set-app-val [:display-type] :large-contact-sheet])
                  [md-icon-button :md-icon-name "zmdi zmdi-apps mdc-text-grey"
                       :on-click #(dispatch [:set-app-val [:display-type] :contact-sheet])]
                  [md-icon-button :md-icon-name "zmdi zmdi-view-list mdc-text-grey"
                                  :on-click #(dispatch [:set-app-val [:display-type] :row])]
                  [md-icon-button :md-icon-name "zmdi zmdi-view-headline mdc-text-grey"
                                  :on-click #(dispatch [:set-app-val [:display-type] :list])]
                  [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey"
                                  :on-click #(route-new-item :artwork)]]]))

(defn single-item-view
  []
  (let [uuid (subscribe [:app-key :single-item-uuid])
        item-path (subscribe [:item-path-by-uuid :artwork @uuid])
        id (last @item-path)]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" ;:style {:flex-flow "row wrap"}
           :children [(item-view id)]]))))

(defn new-item-view
  []
  (let [id 0
        item-path (subscribe [:by-path [:artwork id]])]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" :align :center :justify :start
           :children [(item-view id)]]))))

(defn artwork-contact-sheet
  "Display contact sheet of items"
  []
  (let [items (subscribe [:items-keys-sorted-by :artwork (partial sort-by-key-then-created :year true)])]
    (fn []
      (when (not (empty? @items))
        [h-box :gap "10px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
           :children (into [] (map (partial item-view) @items))]))))

(defn artwork-view
  "Display artwork"
  []
  (let [display-type (subscribe [:app-key :display-type])]
    (fn []
      [v-box :gap "16px" :align :center :justify :center
         :children [[view-selection]
                    (condp = @display-type
                      :contact-sheet [artwork-contact-sheet]
                      :list [list-view]
                      :row [row-view]
                      :single-item [single-item-view]
                      :new-item [new-item-view]
                      [:span (str "Unexpected display-type of " @display-type)])]])))
