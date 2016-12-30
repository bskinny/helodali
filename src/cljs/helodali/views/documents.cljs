(ns helodali.views.documents
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

(defn- title-string
  "Return a non-empty title"
  [title]
  (if (empty? title)
    "(no title)"
    title))

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
        buyer-uuid (subscribe [:by-path [:documents id :purchases idx :buyer]])
        agent-uuid (subscribe [:by-path [:documents id :purchases idx :agent]])
        dealer-uuid (subscribe [:by-path [:documents id :purchases idx :dealer]])
        contacts (subscribe [:items-vals-with-uuid :contacts :name]) ;; this is a list of 2-tuples [uuid name]
        purchase-date (subscribe [:by-path [:documents id :purchases idx :date]])
        on-public-display (subscribe [:by-path [:documents id :purchases idx :on-public-display]])
        collection (subscribe [:by-path [:documents id :purchases idx :collection]])
        location (subscribe [:by-path [:documents id :purchases idx :location]])
        price (subscribe [:by-path [:documents id :purchases idx :price]])
        total-commission-percent (subscribe [:by-path [:documents id :purchases idx :total-commission-percent]])
        donated (subscribe [:by-path [:documents id :purchases idx :donated]])
        commissioned (subscribe [:by-path [:documents id :purchases idx :commissioned]])
        notes (subscribe [:by-path [:documents id :purchases idx :notes]])]
    [(fn []
      [v-box :gap "4px" :justify :start :align :start :padding "10px"
         :style {:background bg-color :border "1px solid lightgray" :border-radius "4px"} :width "100%"
         :children [[h-box :gap "6px" :align :center
                       :children [[:span.input-label (str "Purchased ")]
                                  [datepicker-dropdown :model (goog.date.UtcDateTime. @purchase-date)
                                        :on-change #(dispatch [:set-local-item-val [:documents id :purchases idx :date] %])]
                                  [h-box :gap "6px" :align :center
                                     :children [[:span "by "]
                                                [single-dropdown :choices (uuid-label-list-to-options @contacts) :model (if (nil? @buyer-uuid) :none buyer-uuid) :width "200px"
                                                       :on-change #(if (and (not= @buyer-uuid %) (not (and (= @buyer-uuid nil) (= % :none))))
                                                                     (dispatch [:set-local-item-val [:documents id :purchases idx :buyer] %]))]]]
                                  [checkbox :model @on-public-display :label "On Public Display?"
                                     :on-change #(dispatch [:set-local-item-val [:documents id :purchases idx :on-public-display] (not @on-public-display)])]
                                  [checkbox :model @collection :label "Part of Collection?"
                                     :on-change #(dispatch [:set-local-item-val [:documents id :purchases idx :collection] (not @collection)])]]]
                    [h-box :gap "6px" :align :center
                      :children [[:span.input-label "Location"]
                                 [input-text :width "384px" :model (str @location) :style {:border "none"}
                                      :on-change #(dispatch [:set-local-item-val [:documents id :purchases idx :location] %])]
                                 [checkbox :model @donated :label "Donated?"
                                    :on-change #(dispatch [:set-local-item-val [:documents id :purchases idx :donated] (not @donated)])]
                                 [checkbox :model @commissioned :label "Commissioned?"
                                    :on-change #(dispatch [:set-local-item-val [:documents id :purchases idx :commissioned] (not @commissioned)])]]]
                    [h-box :gap "8px" :align :center
                       :children [[h-box :gap "4px" :align :center
                                    :children [[:span.input-label "Price "]
                                               [input-text :width "80px" :model (str @price) :style {:border-radius "4px"}
                                                   :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:documents id :purchases idx :price] (js/Number %)])]]]
                                  [h-box :gap "4px" :align :center
                                    :children [[:span.input-label "Total commission percentage given to agents and dealers "]
                                               [input-text :width "70px" :model (str @total-commission-percent) :style {:border-radius "4px"}
                                                   :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:documents id :purchases idx :total-commission-percent] (js/Number %)])]]]]]
                    [h-box :gap "6px" :align :center
                       :children [[:span.input-label "Agent "]
                                  [single-dropdown :choices (uuid-label-list-to-options @contacts) :model (if (nil? @agent-uuid) :none agent-uuid) :width "200px"
                                       :on-change #(if (and (not= @agent-uuid %) (not (and (= @agent-uuid nil) (= % :none))))
                                                     (dispatch [:set-local-item-val [:documents id :purchases idx :agent] (if (= :none %) nil %)]))]
                                  [:span.input-label "Dealer "]
                                  [single-dropdown :choices (uuid-label-list-to-options @contacts) :model (if (nil? @dealer-uuid) :none dealer-uuid) :width "200px"
                                       :on-change #(if (and (not= @dealer-uuid %) (not (and (= @dealer-uuid nil) (= % :none))))
                                                     (dispatch [:set-local-item-val [:documents id :purchases idx :dealer] (if (= :none %) nil %)]))]]]
                    [h-box :gap "6px" :align :center :justify :between :align-self :stretch
                       :children [[h-box :gap "6px" :align :center
                                     :children [[:span.input-label "Notes "]
                                                [input-textarea :model (str @notes) :width "520px" :rows 4
                                                    :on-change #(dispatch [:set-local-item-val [:documents id :purchases idx :notes] %])]]]
                                  [button :label "Delete" :class "btn-default"
                                          :on-click #(dispatch [:delete-local-vector-element [:documents id :purchases] idx])]]]]])]))

(defn item-properties-panel
  [id]
  (let [editing (subscribe [:item-key :documents id :editing])
        document-name (subscribe [:item-key :documents id :name])
        list-price (subscribe [:item-key :documents id :list-price])
        current-location (subscribe [:item-key :documents id :current-location])
        instagram (subscribe [:item-key :documents id :instagram])
        facebook (subscribe [:item-key :documents id :facebook])
        medium (subscribe [:item-key :documents id :medium])
        dimensions (subscribe [:item-key :documents id :dimensions])
        description (subscribe [:item-key :documents id :description])
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
                                           :on-click #(dispatch [:create-from-placeholder :documents])]
                                         [button :label "Cancel" :class "btn-default"
                                           :on-click #(dispatch [:delete-item :documents id])]]]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[button :label "Save" :class "btn-default"
                                          :on-click #(dispatch [:save-changes [:documents id]])]
                                        [button :label "Cancel" :class "btn-default"
                                          :on-click #(dispatch [:cancel-edit-item [:documents id]])]]]
            edit [[v-box :gap "4px" :align :baseline :justify :start
                     :children [[h-box :gap "4px" :align :center
                                   :children [[:span.uppercase.light-grey "title"]
                                              [input-text :model (str @title) :placeholder "Title of piece" :width "330px" :style {:border "none"}
                                                 :on-change #(dispatch [:set-local-item-val [:documents id :title] %])]]]
                                [:span.uppercase.light-grey "description"]
                                [input-textarea :model (str @description) :width "360px"
                                    :rows 7 :on-change #(dispatch [:set-local-item-val [:documents id :description] %])]]]
                  [v-box :gap "4px" :align :start :justify :around
                    :children [[h-box :gap "10px" :align :center :justify :start
                                 :children [[h-box :gap "4px" :align :center
                                              :children [[:span.uppercase.light-grey "year"]
                                                         [input-text :width "60px" :model (str @year) :placeholder "2016" :style {:border "none"}
                                                             :attr {:max-length 4} :on-change #(dispatch [:set-local-item-val [:documents id :year] (js/Number %)])]]]
                                            [single-dropdown :choices status-options :width "118px" :model @status
                                                    :on-change #(dispatch [:set-local-item-val [:documents id :status] %])]]]
                               [h-box :gap "4px" :align :center
                                 :children [[:span.uppercase.light-grey "type"]
                                            [single-dropdown :choices media-options :width "160px" :model @type
                                                    :on-change #(dispatch [:set-local-item-val [:documents id :type] %])]]]
                               [h-box :gap "4px" :align :center
                                 :children [[:span.uppercase.light-grey "medium"]
                                            [input-text :width "160px" :model (str @medium) :placeholder "E.g. oil on canvas" :style {:border "none"}
                                                 :on-change #(dispatch [:set-local-item-val [:documents id :medium] %])]]]
                               [h-box :gap "4px" :align :center
                                 :children [[:span.uppercase.light-grey "dimensions"]
                                            [input-text :width "180px" :model (str @dimensions) :placeholder "E.g. H x W x D in" :style {:border "none"}
                                                 :on-change #(dispatch [:set-local-item-val [:documents id :dimensions] %])]]]
                               [h-box :gap "4px" :align :center
                                 :children [[:span.uppercase.light-grey "condition"]
                                            [input-text :width "148px" :model (str @condition) :style {:border "none"}
                                                 :on-change #(dispatch [:set-local-item-val [:documents id :condition] %])]]]
                               [h-box :gap "10px" :align :center :justify :start
                                 :children [[h-box :gap "4px" :align :center
                                              :children [[:span.uppercase.light-grey "list price"]
                                                         [input-text :width "80px" :model (str @list-price) :style {:border "none"}
                                                             :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:documents id :list-price] (js/Number %)])]]]
                                            [h-box :gap "4px" :align :center
                                              :children [[:span.uppercase.light-grey "expenses"]
                                                         [input-text :width "70px" :model (str @expenses) :style {:border "none"}
                                                             :attr {:max-length 12} :on-change #(dispatch [:set-local-item-val [:documents id :expenses] (js/Number %)])]]]]]]]
                  [h-box :gap "6px" :align :center
                     :children [[:span.uppercase.light-grey "style"]
                                [selection-list :choices style-options :model (if (empty? @style) #{} (set @style)) :height "210px"
                                       :on-change #(dispatch [:set-local-item-val [:documents id :style] %])]]]]
            edit-extended
              [v-box :gap "6px" :align :start :justify :start :align-self :stretch
                  :children [[h-box :gap "6px" :align :center :justify :start
                               :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Add a Purchase record"
                                             :on-click #(dispatch [:create-local-vector-element [:documents id :purchases] (db/default-purchase)])]
                                          [:span "Purchases"]]]
                             (when (not (empty? @purchases))
                               [v-box :gap "16px" :align :start :justify :start :align-self :stretch
                                  :children (into [] (map (partial display-purchase-edit id) (range (count @purchases)) (cycle [true false])))])
                             [re-com/gap :size "4px"]
                             [h-box :gap "6px" :align :center :justify :start
                                :children [[md-icon-button :md-icon-name "zmdi-plus" :tooltip "Add an Exhibition record"
                                              :on-click #(dispatch [:create-local-vector-element [:documents id :exhibition-history] (db/default-exhibition-history)])]
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
  "Display a document"
  [id]
  (let [uuid (subscribe [:item-key :documents id :uuid])
        document-name (subscribe [:item-key :documents id :name])
        size (subscribe [:item-key :documents id :size])
        notes (subscribe [:item-key :documents id :notes])
        created (subscribe [:item-key :documents id :created])
        last-modified (subscribe [:item-key :documents id :last-modified])
        processing (subscribe [:item-key :documents id :processing])
        editing (subscribe [:item-key :documents id :editing])
        signed-raw-url (subscribe [:item-key :documents id :signed-raw-url])
        signed-raw-url-expiration-time (subscribe [:item-key :documents id :signed-raw-url-expiration-time])
        document-input-id (str "document-upload-" id)
        showing-download-tooltip? (r/atom false)
        showing-primary-image-info? (r/atom false)
        display-type (subscribe [:app-key :display-type])]
    [(fn []
      (let [controls [h-box :gap "2px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                        :children [(when (not single-item)
                                     [row-button :md-icon-name "zmdi zmdi-copy"
                                       :mouse-over-row? true :tooltip "Copy this item" :tooltip-position :right-center
                                       :on-click #(dispatch [:copy-item :documents id :name])])
                                   (when single-item
                                     [row-button :md-icon-name "zmdi zmdi-edit"
                                       :mouse-over-row? true :tooltip "Edit this item" :tooltip-position :right-center
                                       :on-click #(dispatch [:edit-item [:documents id]])])
                                   [row-button :md-icon-name "zmdi zmdi-delete"
                                     :mouse-over-row? true :tooltip "Delete this item" :tooltip-position :right-center
                                     :on-click #(dispatch [:delete-artwork-item :documents id])]]]
            url (cond
                  @processing "/image-assets/ajax-loader.gif"
                  (and (not (nil? @signed-raw-url-expiration-time)) (not (expired? @signed-raw-url-expiration-time))) @signed-raw-url
                  :else "/image-assets/thumb-stub.png")  ;; TODO: Need a documents-stub image
            object-fit "none"]
        ;; Perform some dispatching if the document is not in sync with S3 and database
        (if @processing
          (dispatch [:refresh-image [:documents id :images 0]])
          (when (and (not (nil? image)) (or (nil? url) (expired? expiration)))
            (dispatch [:get-signed-url [:documents id :images 0] "helodali-images" (:key image) :signed-thumb-url :signed-thumb-url-expiration-time])))
        (when (and (:key image) (expired? (:signed-raw-url-expiration-time image)))
          (dispatch [:get-signed-url [:documents id :images 0] "helodali-raw-images" (:key image) :signed-raw-url :signed-raw-url-expiration-time]))

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
                                                                               (dispatch [:set-local-item-val [:documents id :expanded] (not @expanded)])
                                                                               nil)}]]
                                                                  ; :style {:z-index 1}
                                                                  ; :onMouseOver #(when (= @display-type :single-item)
                                                                  ;                  (reset! mouse-over-image true))
                                                                  ; :onMouseOut #(when (= @display-type :single-item)
                                                                  ;                 (reset! mouse-over-image false))}]]
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
                                                                  [:input {:type "file" :accept "image/*" :multiple false :name "document-upload"
                                                                           :id document-input-id :style {:display "none"}
                                                                           :on-change #(let [el (.getElementById js/document document-input-id)]
                                                                                         (if (nil? image)
                                                                                           (dispatch [:add-image [:documents id] (aget (.-files el) 0)])
                                                                                           (dispatch [:replace-image [:documents id] (:uuid image) (aget (.-files el) 0)])))}]
                                                                  [md-icon-button :md-icon-name "zmdi zmdi-upload"
                                                                     :emphasise? true :tooltip "Replace this image" ;:tooltip-position :right-center
                                                                     :on-click #(let [el (.getElementById js/document document-input-id)]
                                                                                  (.click el))]
                                                                  (when-not (empty? (:key image))
                                                                    [md-icon-button :md-icon-name "zmdi zmdi-delete"
                                                                       :emphasise? true :tooltip "Delete this image" ;:tooltip-position :right-center
                                                                       :on-click #(if-not (empty? @images)  ;; TODO: fix this when we support multiple images
                                                                                    (dispatch [:delete-s3-vector-element ["helodali-raw-images"]
                                                                                                        [:documents id :images] (:uuid (first @images))]))])
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

(defn item-list-view
  "Display item properties in single line. The 'widths' map contains the string
   length of the longest document name. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for name, by truncating the strings over the max."
  [widths id odd-row?]
  (let [uuid (subscribe [:item-key :documents id :uuid])
        bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        document-name (subscribe [:item-key :documents id :name])
        size (subscribe [:item-key :documents id :size])
        created (subscribe [:item-key :documents id :created])
        last-modified (subscribe [:item-key :documents id :last-modified])]
    [(fn []
      (let [lm (or @last-modified @created)]
        [h-box :align :center :justify :start :style {:background bg-color} :width "100%"
          :children [[hyperlink :class "semibold" :style {:width (str (max 14 (:name widths)) "ch")} :label (trunc (title-string @document-name) (:name widths))
                         :on-click #(route-single-item :documents @uuid)]
                     [label :width "15ch" :label (safe-date-string @created)]
                     [label :width "15ch" :label (safe-date-string lm)]
                     [label :width "8ch" :label (str (quot @size 1024) " KB")]
                     [h-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                         :children [[row-button :md-icon-name "zmdi zmdi-copy"
                                       :mouse-over-row? true :tooltip "Copy this item"
                                       :on-click #(dispatch [:copy-item :documents id :name])]
                                    [row-button :md-icon-name "zmdi zmdi-delete"
                                       :mouse-over-row? true :tooltip "Delete this item"
                                       :on-click #(dispatch [:delete-item :documents id])]]]]]))]))

(defn list-view
  "Display list of items, one per line"
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :documents]])
        items (subscribe [:items-keys-sorted-by-key :documents sort-by-key-then-created])
        names (subscribe [:items-vals :documents :name])]
    (fn []
      (let [widths {:name (+ 4 (max-string-length @names 80))}
            header [h-box :align :center :justify :start :width "100%"
                      :children [[hyperlink :class "bold uppercase" :style {:width (str (max 14 (:title widths)) "ch")} :label "Document Name"
                                    :tooltip "Sort by Name" :on-click #(if (= (first @sort-key) :name)
                                                                          (dispatch [:set-local-item-val [:sort-keys :documents 1] (not (second @sort-key))])
                                                                          (dispatch [:set-local-item-val [:sort-keys :documents] [:name true]]))]
                                 [hyperlink :class "bold uppercase" :style {:width "15ch"} :label "Created"
                                     :tooltip "Sort by Date Created" :on-click #(if (= (first @sort-key) :created))
                                                                          (dispatch [:set-local-item-val [:sort-keys :documents 1] (not (second @sort-key))])
                                                                          (dispatch [:set-local-item-val [:sort-keys :documents] [:created false]])]
                                 [hyperlink :class "bold uppercase" :style {:width "15ch"} :label "Modified"
                                     :tooltip "Sort by Date Last Modified" :on-click #(if (= (first @sort-key) :last-modified))
                                                                            (dispatch [:set-local-item-val [:sort-keys :documents 1] (not (second @sort-key))])
                                                                            (dispatch [:set-local-item-val [:sort-keys :documents] [:last-modified true]])]
                                 [hyperlink :class "bold uppercase" :style {:width "8ch"} :label "Size"
                                    :tooltip "Sort by Size" :on-click #(if (= (first @sort-key) :size)
                                                                         (dispatch [:set-local-item-val [:sort-keys :documents 1] (not (second @sort-key))])
                                                                         (dispatch [:set-local-item-val [:sort-keys :documents] [:size false]]))]]]]
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
                                  :on-click #(route-new-item :documents)]]]))

(defn single-item-view
  []
  (let [uuid (subscribe [:app-key :single-item-uuid])
        item-path (subscribe [:item-path-by-uuid :documents @uuid])
        id (last @item-path)]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" ;:style {:flex-flow "row wrap"}
           :children [(item-view id)]]))))

(defn new-item-view
  []
  (let [id 0
        item-path (subscribe [:by-path [:documents id]])]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" :align :center :justify :start
           :children [(item-view id)]]))))

(defn documents-view
  "Display documents"
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
