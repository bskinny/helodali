(ns helodali.views.artwork
    (:require [helodali.db :as db]
              [helodali.types :as types]
              [helodali.routes :refer [route-view-display route-single-item route-new-item route-instagram-refresh]]
              [helodali.misc :refer [trunc compute-bg-color convert-map-to-options max-string-length expired?
                                     sort-by-datetime sort-by-key-then-created uuid-label-list-to-options
                                     safe-date-string remove-vector-element title-string]]
              [cljs.pprint :refer [pprint]]
              [cljs-time.format :refer [unparse formatters]]
              [reagent.core  :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list
                                              button datepicker-dropdown checkbox popover-tooltip
                                              popover-anchor-wrapper popover-content-wrapper handler-fn
                                              md-circle-icon-button hyperlink-href]])
    (:import goog.date.UtcDateTime))


(def status-options [{:id :for-sale :label "For Sale"}
                     {:id :sold :label "Sold"}
                     {:id :private :label "Private"}
                     {:id :not-for-sale :label "Not For Sale"}
                     {:id :destroyed :label "Destroyed"}])

(def media-options (convert-map-to-options types/media))
(def style-options (convert-map-to-options types/styles))

(defn display-exhibition-history-view
  [exhibition-history odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        exhibition (if (empty? (:ref exhibition-history))
                      (r/atom nil)
                      (subscribe [:item-by-uuid :exhibitions (:ref exhibition-history)]))]
    (fn []
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
                                      [re-com/box :max-width "360px" :child [:pre (:notes exhibition-history)]]]])]])))

(defn display-exhibition-history-edit
  [id idx odd-row?]
  (let [bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        exhibition-uuid (subscribe [:by-path [:artwork id :exhibition-history idx :ref]])
        exhibitions (subscribe [:items-vals-with-uuid :exhibitions :name]) ;; this is a list of 2-tuples [uuid name]
        notes (subscribe [:by-path [:artwork id :exhibition-history idx :notes]])]
    (fn []
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
                                          :on-click #(dispatch [:delete-local-vector-element [:artwork id :exhibition-history] idx])]]]]])))

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
    (fn []
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
                                                   [:pre (:notes purchase)]]])]])))

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
    (fn []
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
                                               [input-text :width "80px" :model (str @price) :style {:border-radius "4px"} :validation-regex #"^\d*$"
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
                                          :on-click #(dispatch [:delete-local-vector-element [:artwork id :purchases] idx])]]]]])))

(defn display-secondary-image
  [id editing idx image odd-row?]
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
    (fn []
      [v-box ;:max-width image-size :max-height image-size ;:style {:margin-top "10px" :z-index 0 :position "relative"}
         :children [[box :max-width image-size :max-height image-size
                      :child [:img {:src url}]] ; :on-click #()}]]  TODO: on-click should present a larger image
                    (when (and @editing (not (:processing image)))
                      [h-box :gap "8px" :align :center :justify :center :style {:background-color "#428bca"}
                         :children [;; hidden input text + button for image upload
                                    [:input {:type "file" :accept "image/*" :multiple false :name "image-upload"
                                             :id image-input-id :style {:display "none"}
                                             :on-change #(let [el (.getElementById js/document image-input-id)]
                                                           (dispatch [:replace-image [:artwork id] (:uuid image) (aget (.-files el) 0)]))}]
                                    [md-icon-button :md-icon-name "zmdi zmdi-upload"
                                       :emphasise? true :tooltip "Replace this image"
                                       :on-click #(let [el (.getElementById js/document image-input-id)]
                                                    (.click el))]
                                    [md-icon-button :md-icon-name "zmdi zmdi-delete"
                                       :emphasise? true :tooltip "Delete this image"
                                       :on-click #(dispatch [:delete-s3-vector-element ["helodali-raw-images"] [:artwork id :images] (:uuid image)])]]])]])))

(defn item-properties-panel
  [id]
  (let [editing (subscribe [:item-key :artwork id :editing])
        images (subscribe [:item-key :artwork id :images])
        year (subscribe [:item-key :artwork id :year])
        status (subscribe [:item-key :artwork id :status])
        style (subscribe [:item-key :artwork id :style])
        type (subscribe [:item-key :artwork id :type])
        title (subscribe [:item-key :artwork id :title])
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
                                     [:span (str "List Price: $" @list-price)]]]
            view [[v-box :gap "6px" :align :start :justify :start :padding "8px" ;:max-width "300px"
                       :children [[re-com/box :max-width "360px" :child [:p.bold (title-string @title)]]
                                  ;; The placement of view-basics depends on an existence of description
                                  (if (empty? @description)
                                    view-basics
                                    [re-com/box :max-width "360px" :child [:p.italic @description]])]]
                  (when (not (empty? @description)) view-basics)]
            view-extended
              [v-box :gap "12px" :align :start :justify :start
                  :children [(when (not (empty? @purchases))
                               [v-box :gap "16px" :align :start :justify :start
                                  :children (into [] (mapv (fn [idx purchase bg] ^{:key (str "purchase-" idx)} [display-purchase-view purchase bg])
                                                           (range (count @purchases)) @purchases (cycle [true false])))])
                             (when (not (empty? @exhibition-history))
                               [v-box :gap "16px" :align :start :justify :start
                                  :children (into [] (mapv (fn [idx exhibition bg] ^{:key (str "exhibition-" idx)} [display-exhibition-history-view exhibition bg])
                                                           (range (count @exhibition-history)) @exhibition-history (cycle [true false])))])
                             (when (> (count @images) 1)
                               [h-box :gap "16px" :align :start :justify :start :padding "20px" :style {:flex-flow "row wrap"}
                                  :children (into [] (mapv (fn [idx image bg] ^{:key (str "image-" idx)} [display-secondary-image id editing idx image bg]) (range 1 (count @images)) (rest @images) (cycle [true false])))])]]
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
                                   :children [[:span.uppercase.bold "title"]
                                              [input-text :model (str @title) :placeholder "Title of piece" :width "330px" :style {:border "none"}
                                                 :on-change #(dispatch [:set-local-item-val [:artwork id :title] %])]]]
                                [:span.uppercase.light-grey "description"]
                                [input-textarea :model (str @description) :width "360px"
                                    :rows 7 :on-change #(dispatch [:set-local-item-val [:artwork id :description] %])]]]
                  [v-box :gap "4px" :align :start :justify :around
                    :children [[h-box :gap "10px" :align :center :justify :start
                                 :children [[h-box :gap "4px" :align :center
                                              :children [[:span.uppercase.bold "year"]
                                                         [input-text :width "60px" :model (str @year) :placeholder "2016" :style {:border "none"}
                                                             :attr {:max-length 4} :validation-regex #"^\d*$" ;; validation: only numbers allowed
                                                             :on-change #(dispatch [:set-local-item-val [:artwork id :year] (js/Number %)])]]]
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
                               [h-box :gap "4px" :align :center
                                 :children [[:span.uppercase.light-grey "list price"]
                                            [input-text :width "80px" :model (str @list-price) :style {:border "none"} :validation-regex #"^\d*$"
                                                :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:artwork id :list-price] (js/Number %)])]]]]]
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
                                  :children (into [] (mapv (fn [idx bg] ^{:key (str "purchase-" idx)} [display-purchase-edit id idx bg])
                                                           (range (count @purchases)) (cycle [true false])))])
                             [re-com/gap :size "4px"]
                             [h-box :gap "6px" :align :center :justify :start
                               :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Add an Exhibition record"
                                             :on-click #(dispatch [:create-local-vector-element [:artwork id :exhibition-history] (db/default-exhibition-history)])]
                                          [:span "Exhibitions"]]]
                             (when (not (empty? @exhibition-history))
                               [v-box :gap "16px" :align :start :justify :start :align-self :stretch
                                  :children (into [] (mapv (fn [idx bg] ^{:key (str "exhibition-" idx)} [display-exhibition-history-edit id idx bg])
                                                           (range (count @exhibition-history)) (cycle [true false])))])]]]
        [v-box :gap "8px" :align :start :justify :start :margin "20px" ;:style {:border "dashed 1px #ddd"}
           :children [(when (= @display-type :new-item)
                        [box :align-self :center :margin "0px 10px" :child [:span.all-small-caps "an image can be uploaded after creating the item"]])
                      [h-box :gap "10px"  :align :start :justify :start
                         :children (if (and @editing single-item) edit view)]
                      (when single-item
                        (if @editing edit-extended view-extended))
                      (when (and single-item @editing)
                        (if (= @display-type :new-item)
                          create-control
                          save-control))]]))))

;; TODO: This function is far too convoluted, having grown this way over time. Separate functions should be defined
;; for each of the :single-item and non-:single-item contexts.
(defn item-view
  "Display an item in one of multiple contexts: as part of the row or sheet or as an individual item with details."
  [id]
  (let [uuid (subscribe [:item-key :artwork id :uuid])
        title (subscribe [:item-key :artwork id :title])
        year (subscribe [:item-key :artwork id :year])
        status (subscribe [:item-key :artwork id :status])
        dimensions (subscribe [:item-key :artwork id :dimensions])
        images (subscribe [:item-key :artwork id :images])
        editing (subscribe [:item-key :artwork id :editing])
        signed-thumb-url (subscribe [:by-path [:artwork id :images 0 :signed-thumb-url]])
        thumb-expiration (subscribe [:by-path [:artwork id :images 0 :signed-thumb-url-expiration-time]])
        signed-image-url (subscribe [:by-path [:artwork id :images 0 :signed-image-url]])
        image-expiration (subscribe [:by-path [:artwork id :images 0 :signed-image-url-expiration-time]])
        signed-large-image-url (subscribe [:by-path [:artwork id :images 0 :signed-large-image-url]])
        large-image-expiration (subscribe [:by-path [:artwork id :images 0 :signed-large-image-url-expiration-time]])
        raw-expiration (subscribe [:by-path [:artwork id :images 0 :signed-raw-url-expiration-time]])
        raw-image-url (subscribe [:by-path [:artwork id :images 0 :signed-raw-url]])
        processing (subscribe [:by-path [:artwork id :images 0 :processing]])
        palette (subscribe [:by-path [:artwork id :images 0 :palette]])
        image-input-id (str "image-upload-" id "-0")
        showing-download-tooltip? (r/atom false)
        showing-primary-image-info? (r/atom false)
        enlarged (r/atom false)
        display-type (subscribe [:app-key :display-type])]
    (fn []
      (let [single-item (or (= @display-type :single-item) (= @display-type :new-item))
            controls [h-box :gap "2px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                        :children [(when (not single-item)
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
            ;; Set image size and source based on summary-view vs. other views
            thumb-config ["240px" "helodali-thumbs" signed-thumb-url thumb-expiration :signed-thumb-url :signed-thumb-url-expiration-time]
            images-config ["480px" "helodali-images" signed-image-url image-expiration :signed-image-url :signed-image-url-expiration-time]
            large-images-config ["960px" "helodali-large-images" signed-large-image-url large-image-expiration :signed-large-image-url :signed-large-image-url-expiration-time]
            [image-size image-bucket signed-url url-expiration signed-url-key signed-url-expiration-key] (cond
                                                                                                           (or @editing (and (= @display-type :summary-view) (not @enlarged))) thumb-config
                                                                                                           (and (= @display-type :single-item) @enlarged) large-images-config
                                                                                                           :else images-config)
            url (cond
                  @processing "/image-assets/ajax-loader.gif"
                  (not (nil? @signed-url)) @signed-url
                  :else "/image-assets/thumb-stub.png")
            object-fit (cond
                          @processing :fit-none
                          (or (= @display-type :summary-view) @editing) :fit-contain
                          :else :fit-contain)]
        ;; Perform some dispatching if the artwork is not in sync with S3 and database
        (if @processing
          (dispatch [:refresh-image [:artwork id :images 0]])
          (when (and (not (nil? image)) (nil? @url-expiration))
            (dispatch [:get-signed-url [:artwork id :images 0] image-bucket (:key image) signed-url-key signed-url-expiration-key])))

        ;; Update the signed-url for the raw image if necessary. It has been observed that the signed-raw-url gets assigned "https://aws.amazon.com/s3/" somehow,
        ;; so this value is included in a check for re-dispatch.
        (when (and (:raw-key image) (or (expired? @raw-expiration) (= "https://aws.amazon.com/s3/" @raw-image-url)))
          (dispatch [:get-signed-url [:artwork id :images 0] "helodali-raw-images" (:raw-key image) :signed-raw-url :signed-raw-url-expiration-time]))

        ;(pprint (str "Raw image expiration " @raw-expiration " expired? " (expired? @raw-expiration)))
        ;(pprint (str "Raw image url: " @raw-image-url))

        ;; Base UI on new-item versus single-item versus inline display within :summary-view
        ;; A new-item view does not present the image or edit/delete controls
        (if (= @display-type :new-item)
          [h-box :gap "4px" :align :start :justify :start :style {:flex-flow "row wrap"} ; :style container-style
            :children [[item-properties-panel id]]]
          [h-box :gap "4px" :align :start :justify :start :padding "20px" :style {:flex-flow "row wrap"} ; :style container-style
            :children [[v-box :gap "2px" :width image-size :align :center :justify :center
                         :children [[v-box
                                       :children [[box :max-width image-size :max-height image-size
                                                    :child [:img {:src url :class object-fit
                                                                  :on-error #(if (expired? @url-expiration)
                                                                               (dispatch [:flush-signed-urls [:artwork id :images 0]])
                                                                               (dispatch [:refresh-image [:artwork id :images 0]]))
                                                                  :on-click #(when (not= @display-type :new-item) ;; nothing to do for new-item context
                                                                               (if (not= @display-type :single-item)
                                                                                 (route-single-item :artwork @uuid)
                                                                                 (swap! enlarged not)))}]]
                                                  (when (or (and @editing (= @display-type :single-item) (not @processing))
                                                            (and (not @editing) (= @display-type :single-item) (nil? image)))
                                                    [h-box :gap "8px" :align :center :justify :center :style {:background-color "#428bca"}
                                                       :children [(when-not (empty? (:metadata image))
                                                                    (let [md (:metadata image)]
                                                                      [popover-anchor-wrapper
                                                                         :showing? showing-primary-image-info? :position :below-center
                                                                         :anchor [md-icon-button :md-icon-name "zmdi zmdi-info"
                                                                                     :emphasise? true :tooltip "Original image information"
                                                                                     :on-click #(swap! showing-primary-image-info? not)]
                                                                         :popover [popover-content-wrapper :width "360px" :close-button? true :title "Original Image"
                                                                                     :body [v-box :gap "4px" :align :start
                                                                                              :children [[:span (str "Filename: " (trunc (:filename image) 40))]
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
                                                                     :emphasise? true :tooltip "Replace this image"
                                                                     :on-click #(let [el (.getElementById js/document image-input-id)]
                                                                                  (.click el))]
                                                                  (when-not (empty? (:key image))
                                                                    [md-icon-button :md-icon-name "zmdi zmdi-delete"
                                                                       :emphasise? true :tooltip "Delete this image"
                                                                       :on-click #(if-not (empty? @images)  ;; TODO: fix this if we support multiple images
                                                                                    (dispatch [:delete-s3-vector-element ["helodali-raw-images"]
                                                                                                        [:artwork id :images] (:uuid (first @images))]))])]])
                                                  ;; Display button for downloading original image
                                                  (when (and (= @display-type :single-item) (not (or @enlarged @editing)) @raw-image-url)
                                                    [h-box :gap "8px" :align :center :justify :center :style {:background-color "#428bca"}
                                                       :children [[popover-tooltip :label "Download original image"
                                                                     :showing? showing-download-tooltip? :position :below-center
                                                                     :anchor [:a {:class "zmdi zmdi-download rc-md-icon-button rc-icon-emphasis"
                                                                                  :target "_blank" :rel "noopener noreferrer"
                                                                                  :on-mouse-over (handler-fn (reset! showing-download-tooltip? true))
                                                                                  :on-mouse-out  (handler-fn (reset! showing-download-tooltip? false))
                                                                                  :href @raw-image-url}]]]])]]
                                    (when (and (not @enlarged) (not @editing) (= @display-type :single-item)) controls)
                                    (when (and (not @editing) single-item @palette)
                                      ;; Display the color palette. @palette is a vec of ints where every five ints represent r g b a w
                                      ;; where w is the weight or occurrence of the color in the palette.
                                      (let [colors (partition 5 5 @palette)
                                            palette-color-display (fn [v]
                                                                    (let [[r g b a w] v]
                                                                      [re-com/line :size "20px"
                                                                         :style {:width "40px"}
                                                                         :color (str "rgb(" (clojure.string/join "," [r g b]) ")")]))]
                                        [v-box :align :start :justify :start
                                            :children (into [] (mapv (fn [color] ^{:key (clojure.string/join "-" color)} (palette-color-display color)) colors))]))
                                    (when (or (not single-item) @enlarged) [v-box :gap "2px" :align :start :justify :start
                                                                                :children [[:span (title-string @title)]
                                                                                           [:span (str @year (when @dimensions (str " - " @dimensions)))]
                                                                                           (when (not= @status :for-sale)
                                                                                             [:span.uppercase (clojure.string/replace (name @status) #"-" " ")])]])]]
                       (when (and single-item (not @enlarged)) [item-properties-panel id])]])))))

(defn item-contact-view
  "Display an item image only, clicking on image transitions to single item view."
  [id]
  (let [uuid (subscribe [:item-key :artwork id :uuid])
        images (subscribe [:item-key :artwork id :images])
        signed-thumb-url (subscribe [:by-path [:artwork id :images 0 :signed-thumb-url]])
        thumb-expiration (subscribe [:by-path [:artwork id :images 0 :signed-thumb-url-expiration-time]])
        processing (subscribe [:by-path [:artwork id :images 0 :processing]])]
    (fn []
      (let [image (first @images) ;; Note that images may be empty, hence image is nil
            ;; Set image size and source based on contact-sheet vs. other views
            [image-size image-bucket signed-url-key signed-url-expiration-key] ["240px" "helodali-thumbs" :signed-thumb-url :signed-thumb-url-expiration-time]
            url (cond
                  @processing "/image-assets/ajax-loader.gif"
                  (not (nil? @signed-thumb-url)) @signed-thumb-url
                  :else "/image-assets/thumb-stub.png")
            object-fit (cond
                         @processing :fit-none
                         :else :fit-contain)]
        ;; Perform some dispatching if the artwork is not in sync with S3 and database
        (if @processing
          (dispatch [:refresh-image [:artwork id :images 0]])
          (when (and (not (nil? image)) (nil? @thumb-expiration))
            (dispatch [:get-signed-url [:artwork id :images 0] image-bucket (:key image) signed-url-key signed-url-expiration-key])))

        [box :max-width image-size :max-height image-size :class "contact-sheet-element"
              :child [:img {:src url :class object-fit
                            :on-error #(dispatch [:flush-signed-urls [:artwork id :images 0]])
                            :on-click #(route-single-item :artwork @uuid)}]]))))

(defn item-row-view-not-in-use
  "NOT IN USE:
   Display item as thumbnail and selective properties. The 'widths' map contains the string
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
    (fn []
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
                       :child [:img {:src url :class :fit-contain :width thumb-size :height thumb-size
                                     :on-error #(dispatch [:flush-signed-urls [:artwork id :images 0]])
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
                                      :on-click #(dispatch [:delete-artwork-item :artwork id])]]]]]))))

(defn item-row
  "Display item properties in single line - no image display. The 'widths' map contains the string
   length of the longest title, medium, etc. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for title, by truncating the strings over the max.

   Some columns are not displayed if there is no data available. E.g. if 'condition' is not defined
   for any artwork"
  [selectable? widths id]
  (let [uuid (subscribe [:item-key :artwork id :uuid])
        title (subscribe [:item-key :artwork id :title])
        year (subscribe [:item-key :artwork id :year])
        status (subscribe [:item-key :artwork id :status])
        type (subscribe [:item-key :artwork id :type])
        list-price (subscribe [:item-key :artwork id :list-price])
        current-location (subscribe [:item-key :artwork id :current-location])
        instagram (subscribe [:item-key :artwork id :instagram])
        facebook (subscribe [:item-key :artwork id :facebook])
        medium (subscribe [:item-key :artwork id :medium])
        dimensions (subscribe [:item-key :artwork id :dimensions])
        selected? (r/atom false)]
    (fn []
      [:tr  (when selectable?
              [:td [checkbox :model selected? :label "Include in Presentation?"
                    :on-change #(dispatch [:update-grouping-selection [:groupings 0 :uuid] @selected?]
                                          (swap! selected? not))]])
            [:td [hyperlink :label (trunc (title-string @title) (:title widths))
                   :on-click #(route-single-item :artwork @uuid)]]
            [:td [label :label @year]]
            [:td [label :label (clojure.string/replace (name @status) #"-" " ")]]
            [:td [label :label (trunc @medium (:medium widths))]]
            [:td [label :label (str @dimensions)]]
            [:td [label :label (str @list-price)]]
            [:td [label :label (name @type)]]
            (when selectable?
              [:td [h-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                       :children [[row-button :md-icon-name "zmdi zmdi-copy"
                                      :mouse-over-row? true :tooltip "Copy this item"
                                      :on-click #(dispatch [:copy-item :artwork id :title])]
                                  [row-button :md-icon-name "zmdi zmdi-delete"
                                    :mouse-over-row? true :tooltip "Delete this item"
                                    :on-click #(dispatch [:delete-artwork-item :artwork id])]]]])])))

(defn row-view-not-in-use
  "Display items one per row with a small thumbnail - NOT IN USE and should be converted to :table if put back in use."
  []
  (let [items (subscribe [:items-keys-sorted-by-key :artwork sort-by-key-then-created])
        titles (subscribe [:items-vals :artwork :title])
        types (subscribe [:items-vals :artwork :type])
        styles (subscribe [:items-vals :artwork :style])
        mediums (subscribe [:items-vals :artwork :medium])]
    (fn []
      (let [widths (r/atom {:title (+ 4 (max-string-length @titles 40))
                            :style (+ (max-string-length (map name @types) 40) (max-string-length (map #(str " | " (clojure.string/join ", " (map name %))) @styles) 60))
                            :medium (+ 4 (max-string-length @mediums 20))})]
        [v-box :gap "4px" :align :center :justify :start
           :children (into [] (mapv (fn [id bg] ^{:key id} [item-row-view-not-in-use @widths id bg]) @items (cycle [true false])))]))))

(def instagram-api-url "https://api.instagram.com/oauth/authorize/?client_id=cbfda8d4f3c445af9dbf79dd90f03b90&redirect_uri=")

(defn instagram-auth
  [uuid]
  (set! (.-location js/document) (str instagram-api-url (.-origin (.-location js/document))
                                      "/instagram/oauth/callback&response_type=code&state=" uuid)))

(defn view-selection
  "The row of view selection controls: contact-sheet row list"
  []
  (let [instagram-media (subscribe [:items-keys :instagram-media])
        uuid (subscribe [:by-path [:profile :uuid]])]
    (fn []
      [h-box :gap "18px" :align :center :justify :center
         :children [[md-icon-button :md-icon-name "zmdi zmdi-view-module mdc-text-grey" :tooltip "Summary View"
                                    :on-click #(route-view-display :artwork :summary-view)]
                    [md-icon-button :md-icon-name "zmdi zmdi-view-comfy mdc-text-grey" :tooltip "Contact Sheet"
                                    :on-click #(route-view-display :artwork :contact-sheet)]
                    [md-icon-button :md-icon-name "zmdi zmdi-view-headline mdc-text-grey" :tooltip "List View"
                                    :on-click #(route-view-display :artwork :list)]
                    [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey" :tooltip "Create New Item"
                                    :on-click #(route-new-item :artwork)]
                    [md-icon-button :md-icon-name "zmdi zmdi-instagram mdc-text-grey" :tooltip "Instagram Media"
                                    :on-click #(if @instagram-media
                                                 (route-instagram-refresh)
                                                 (instagram-auth @uuid))]
                    [md-icon-button :md-icon-name "zmdi zmdi-collection-pdf mdc-text-grey" :tooltip "Generate PDF"
                                    :on-click #(route-view-display :artwork :pdf-generation)]]])))

(defn single-item-view
  []
  (let [uuid (subscribe [:app-key :single-item-uuid])
        item-path (subscribe [:item-path-by-uuid :artwork @uuid])
        item (subscribe [:by-path @item-path])
        id (last @item-path)]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px"
           :children [[item-view id]]]))))
                      ;; TODO: plug in the following as a debug mode view
                      ;[:pre [:code (with-out-str (pprint @item))]]]]))))

(defn new-item-view
  []
  (let [id 0
        item-path (subscribe [:by-path [:artwork id]])]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" :align :center :justify :start
           :children [[item-view id]]]))))

(defn- strip-tags
  "Given an instagram caption, return both caption stripped of tags
   as well as the list of tags"
  [caption]
  (let [matches (re-seq #"#([^\s]+)" caption)] ;; returns (["#tag1" "tag1"] ["#tag2" "tag2"] ["#tag3" "tag3"])
     {:tags (map second matches)
      :text (clojure.string/trim (clojure.string/replace caption #"#[^ ]+" ""))}))

(defn instagram-item-view
  "Display an Instagram media item"
  [id]
  (let [instagram-id (subscribe [:item-key :instagram-media id :instagram-id])
        artwork-uuid (subscribe [:item-key-valid-ref :instagram-media id :artwork-uuid :artwork])
        caption (subscribe [:item-key :instagram-media id :caption])
        image-url (subscribe [:item-key :instagram-media id :image-url])
        thumb-url (subscribe [:item-key :instagram-media id :thumb-url])
        likes (subscribe [:item-key :instagram-media id :likes])
        processing (subscribe [:item-key :instagram-media id :processing])
        image-size "240px"]  ;; The thumb is the instragram 'low resolution image' and 320px, while the normal resolution is 640.
    (fn []
      (let [title (if @artwork-uuid
                    (subscribe [:item-attribute-by-uuid :artwork @artwork-uuid :title])
                    (r/atom "(no title)"))]
        [v-box :gap "6px" :padding "20px" :width image-size :align :center :justify :start
            :children [[box :max-width image-size :max-height image-size
                         :child [:img {:src @image-url :class :fit-contain :width image-size :height image-size}]]
                       (if (nil? @artwork-uuid)
                         [md-circle-icon-button :md-icon-name "zmdi-plus" :tooltip "Import to artwork"
                                   :emphasise? true :size :smaller :on-click #(dispatch [:create-from-instagram id])]
                         (if @processing
                           [re-com/throbber]
                           [hyperlink :label (trunc (title-string @title) 30)
                                      :on-click #(route-single-item :artwork @artwork-uuid)]))
                       (if @caption
                         (let [{:keys [tags text]} (strip-tags @caption)]
                           [v-box :gap "4px" :align :center :justify :start
                              :children [[:span text]
                                         [h-box :gap "4px" :align :center :justify :start :style {:flex-flow "row wrap"}
                                           :children (mapv (fn [tag]
                                                             [hyperlink-href :target "_blank" :label tag
                                                                :href (str "https://www.instagram.com/explore/tags/" tag "/")])
                                                           tags)]]])
                         [:span "(no caption)"])]]))))

(defn instagram-view
  "Display contact sheet of items imported from Instagrem"
  []
  (let [items (subscribe [:items-keys :instagram-media])]
    (fn []
      ;; Dispatch a refresh of instagram media if items is nil. Note that empty items, {}, means the instagram
      ;; media has been fetched and is empty.
      (if (nil? @items)
        [h-box :gap "20px" :margin "40px" :align :start :justify :start :children [[re-com/throbber]]]
        (if (empty? @items)
          [label :label "There is no media in your Instagram account."]
          (let [controls [[v-box :gap "60px" :align :center :justify :center :width "240px" :height "300px"
                              :children [[box :child [md-circle-icon-button :md-icon-name "zmdi-refresh-sync" :size :larger
                                                        :tooltip "Refresh media"
                                                        :on-click #(dispatch [:refresh-instagram :hard-reload])]]
                                         [box :child [md-circle-icon-button :md-icon-name "zmdi-more" :size :larger
                                                        :tooltip "Load more media"
                                                        :on-click #(dispatch [:refresh-instagram :append])]]]]]]
            [h-box :gap "20px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
               :children (concat (mapv (fn [id] ^{:key id} [instagram-item-view id]) @items)
                                 controls)]))))))

(defn no-items-to-display
  "Return the display elements when there are no artwork items yet in the db"
  [uuid instagram-media]
  [[:p "Create your first artwork entry with "]
   [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey"
    :on-click #(route-new-item :artwork)]
   [:p " or import from Instagram with "]
   [md-icon-button :md-icon-name "zmdi zmdi-instagram mdc-text-grey"
    :on-click #(if instagram-media
                 (route-instagram-refresh)
                 (instagram-auth uuid))]])

(defn table-header
  "Display list of items as a table"
  [columns sort-key]
  [:thead
   [:tr
    [:th [hyperlink :class "uppercase" :label "Artwork Title"
          :tooltip "Sort by Title" :on-click #(if (= (first @sort-key) :title)
                                                (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                (dispatch [:set-local-item-val [:sort-keys :artwork] [:title true]]))]]
    [:th [hyperlink :class "uppercase" :label "year"
          :tooltip "Sort by Year" :on-click #(if (= (first @sort-key) :year)
                                               (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                               (dispatch [:set-local-item-val [:sort-keys :artwork] [:year false]]))]]
    [:th [hyperlink :class "uppercase" :label "status"
          :tooltip "Sort by Status" :on-click #(if (= (first @sort-key) :status)
                                                 (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :artwork] [:status true]]))]]
    [:th [hyperlink :class "uppercase" :label "medium"
          :tooltip "Sort by Medium" :on-click #(if (= (first @sort-key) :medium)
                                                 (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                 (dispatch [:set-local-item-val [:sort-keys :artwork] [:medium false]]))]]
    [:th [hyperlink :class "uppercase" :label "dimensions"
          :tooltip "Sort by Dimensions" :on-click #(if (= (first @sort-key) :dimensions)
                                                     (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                     (dispatch [:set-local-item-val [:sort-keys :artwork] [:dimensions false]]))]]
    [:th [hyperlink :class "uppercase" :label "list price"
          :tooltip "Sort by Price" :on-click #(if (= (first @sort-key) :list-price)
                                                (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                                (dispatch [:set-local-item-val [:sort-keys :artwork] [:list-price false]]))]]
    [:th [hyperlink :class "uppercase" :label "type"
          :tooltip "Sort by Type" :on-click #(if (= (first @sort-key) :type)
                                               (dispatch [:set-local-item-val [:sort-keys :artwork 1] (not (second @sort-key))])
                                               (dispatch [:set-local-item-val [:sort-keys :artwork] [:type false]]))]]]])

(defn pdf-generation-view
  "Display list of selected items to generate a pdf from. The columns are customizable."
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :artwork]])
        items (subscribe [:items-keys-sorted-by-key :artwork sort-by-key-then-created])
        instagram-media (subscribe [:items-keys :instagram-media])
        titles (subscribe [:items-vals :artwork :title])
        mediums (subscribe [:items-vals :artwork :medium])
        uuid (subscribe [:by-path [:profile :uuid]])]
    (fn []
      (if (not-empty @items)
        (let [columns [:title :year :status :medium :dimensions :list-price :type]
              widths (r/atom {:title (+ 4 (max-string-length @titles 40))
                              :medium (+ 4 (max-string-length @mediums 16))})]
          [v-box :gap "16px" :align :center :justify :start
             :children [[:p "Select existing artwork to include in PDF generation. Use the search feature to quickly find items."]
                        [:table
                         (table-header columns sort-key)
                         (into [:tbody] (mapv (fn [id] ^{:key (str "artwork-" id)} [item-row true @widths id]) @items))]]])
        [h-box :gap "10px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
         :children (no-items-to-display @uuid @instagram-media)]))))

(defn table-view
  "Display list of items as a table"
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :artwork]])
        items (subscribe [:items-keys-sorted-by-key :artwork sort-by-key-then-created])
        instagram-media (subscribe [:items-keys :instagram-media])
        titles (subscribe [:items-vals :artwork :title])
        mediums (subscribe [:items-vals :artwork :medium])
        uuid (subscribe [:by-path [:profile :uuid]])]
    (fn []
      (if (not-empty @items)
        (let [widths (r/atom {:title (+ 4 (max-string-length @titles 40))
                              :medium (+ 4 (max-string-length @mediums 16))})
              columns [:title :year :status :medium :dimensions :list-price :type]]
          [:table
            (table-header columns sort-key)
            (into [:tbody] (mapv (fn [id] ^{:key (str "artwork-" id)} [item-row false @widths id]) @items))])
        [h-box :gap "10px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
         :children (no-items-to-display @uuid @instagram-media)]))))

(defn artwork-summary-view
  "Display summary view of items"
  []
  (let [items (subscribe [:items-keys-sorted-by-key :artwork sort-by-key-then-created])
        instagram-media (subscribe [:items-keys :instagram-media])
        summary-display-count (subscribe [:app-key :summary-display-count])
        items-batch-size 10
        uuid (subscribe [:by-path [:profile :uuid]])]
    (fn []
      (if-not (empty? @items)
        (let [display-items (into [] (map (fn [id] ^{:key id} [item-view id]) (take @summary-display-count @items)))
              less-more-options [v-box :margin "40px" :gap "20px" :align-self :center
                                   :children [(when (< @summary-display-count (count @items))
                                                [md-circle-icon-button :md-icon-name "zmdi-plus-circle-o" :size :larger
                                                 :tooltip "Display more items"
                                                 :on-click #(dispatch [:set-app-db-val [:summary-display-count] (+ @summary-display-count items-batch-size)])])
                                              (when (> @summary-display-count items-batch-size)
                                                [md-circle-icon-button :md-icon-name "zmdi-minus-circle-outline" :size :larger
                                                 :tooltip "Display fewer items"
                                                 :on-click #(dispatch [:set-app-db-val [:summary-display-count] (- @summary-display-count items-batch-size)])])]]]
          [h-box :gap "10px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
           :children (into display-items [less-more-options])])
        [h-box :gap "10px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
         :children (no-items-to-display @uuid @instagram-media)]))))

(defn artwork-contact-sheet
  "Display contact sheet of items"
  []
  (let [items (subscribe [:items-keys-sorted-by-key :artwork sort-by-key-then-created])
        instagram-media (subscribe [:items-keys :instagram-media])
        contact-sheet-display-count (subscribe [:app-key :contact-sheet-display-count])
        items-batch-size 100
        uuid (subscribe [:by-path [:profile :uuid]])]
    (fn []
      (if-not (empty? @items)
        (let [display-items (into [] (map (fn [id] ^{:key id} [item-contact-view id]) (take @contact-sheet-display-count @items)))
              less-more-options [v-box :margin "40px" :gap "20px" :align-self :center
                                 :children [(when (< @contact-sheet-display-count (count @items))
                                              [md-circle-icon-button :md-icon-name "zmdi-plus-circle-o" :size :larger
                                               :tooltip "Display more items"
                                               :on-click #(dispatch [:set-app-db-val [:contact-sheet-display-count] (+ @contact-sheet-display-count items-batch-size)])])
                                            (when (> @contact-sheet-display-count items-batch-size)
                                              [md-circle-icon-button :md-icon-name "zmdi-minus-circle-outline" :size :larger
                                               :tooltip "Display fewer items"
                                               :on-click #(dispatch [:set-app-db-val [:contact-sheet-display-count] (- @contact-sheet-display-count items-batch-size)])])]]]
          [h-box :gap "10px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
             :children (into display-items [less-more-options])])
        [h-box :gap "10px" :margin "40px" :align :start :justify :start :style {:flex-flow "row wrap"}
         :children (no-items-to-display @uuid @instagram-media)]))))

(defn artwork-view
  "Display artwork"
  []
  (let [display-type (subscribe [:app-key :display-type])]
    (fn []
      [v-box :gap "16px" :align :center :justify :start
         :children [[view-selection]
                    (condp = @display-type
                      :summary-view [artwork-summary-view]
                      :contact-sheet [artwork-contact-sheet]
                      :list [table-view]
                      :single-item [single-item-view]
                      :new-item [new-item-view]
                      :instagram [instagram-view]
                      :pdf-generation [pdf-generation-view]
                      [:span (str "Unexpected display-type of " @display-type)])]])))
