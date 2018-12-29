(ns helodali.views.documents
    (:require [helodali.routes :refer [route-single-item route-new-item route-view-display]]
              [helodali.misc :refer [trunc compute-bg-color convert-map-to-options max-string-length expired?
                                     sort-by-key-then-created uuid-label-list-to-options
                                     safe-date-string safe-string]]
              [cljs.pprint :refer [pprint]]
              [reagent.core :as r]
              [cljs-time.format :refer [unparse formatters]]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list
                                              button datepicker-dropdown checkbox popover-tooltip
                                              popover-anchor-wrapper popover-content-wrapper handler-fn]])
    (:import goog.date.UtcDateTime))

(defn item-view
  "Display a document"
  [id]
  (let [title (subscribe [:item-key :documents id :title])
        filename (subscribe [:item-key :documents id :filename])
        size (subscribe [:item-key :documents id :size])
        notes (subscribe [:item-key :documents id :notes])
        created (subscribe [:item-key :documents id :created])
        last-modified (subscribe [:item-key :documents id :last-modified])
        processing (subscribe [:item-key :documents id :processing])
        editing (subscribe [:item-key :documents id :editing])
        key (subscribe [:item-key :documents id :key])
        signed-raw-url (subscribe [:item-key :documents id :signed-raw-url])
        signed-raw-url-expiration-time (subscribe [:item-key :documents id :signed-raw-url-expiration-time])
        document-input-id (str "document-upload-" id)
        showing-download-tooltip? (r/atom false)
        showing-document-info? (r/atom false)
        display-type (subscribe [:app-key :display-type])
        image-size "80px"]
    (fn []
      (let [view-control [h-box :gap "12px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[row-button :md-icon-name "zmdi zmdi-edit"
                                          :mouse-over-row? true :tooltip "Edit this item" :tooltip-position :right-center
                                          :on-click #(dispatch [:edit-item [:documents id]])]
                                        [row-button :md-icon-name "zmdi zmdi-delete"
                                          :mouse-over-row? true :tooltip "Delete this item" :tooltip-position :right-center
                                          :on-click #(dispatch [:delete-document-item :documents id])]]]
            view [[h-box :gap "18px" :align :center :justify :between
                    :children [[:span.uppercase.light-grey "Title"]
                               [:span.bold @title]]]
                  [h-box :gap "18px" :align :center :justify :between
                    :children [[:span.uppercase.light-grey "Filename"]
                               [:span @filename]
                               [:span.all-small-caps (str (quot @size 1024) " KB")]]]
                  (when (not (nil? @created))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "created"]
                                       [:span (safe-date-string @created)]]])
                  (when (not (nil? @last-modified))
                    [h-box :gap "8px" :align :center :justify :start
                            :children [[:span.uppercase.light-grey "last modified"]
                                       [:span (safe-date-string @last-modified)]]])
                  (when (not (empty? @notes))
                    [v-box :gap "4px" :align :start :justify :start :max-width "480px"
                            :children [[:span.uppercase.light-grey "notes"]
                                       [:span @notes]]])]
            create-control [h-box :gap "30px" :align :center
                              :children [[button :label "Create" :class "btn-default"
                                           :on-click #(dispatch [:create-from-placeholder :documents []])]
                                         [button :label "Cancel" :class "btn-default"
                                           :on-click #(dispatch [:delete-item :documents id])]]]
            save-control [h-box :gap "20px" :justify :center :align :center :margin "14px" :style {:font-size "18px"}
                             :children [[button :label "Save" :class "btn-default"
                                          :on-click #(dispatch [:save-changes [:documents id]])]
                                        [button :label "Cancel" :class "btn-default"
                                          :on-click #(dispatch [:cancel-edit-item [:documents id]])]]]
            edit [[h-box :gap "4px" :align :center
                     :children [[:span.uppercase.light-grey "Title"]
                                [input-text :width "280px" :model (str @title) :style {:border "none"}
                                     :on-change #(dispatch [:set-local-item-val [:documents id :title] %])]]]
                  [v-box :gap "4px" :align :start :justify :start
                     :children [[:span.uppercase.light-grey "Notes"]
                                [input-textarea :model (str @notes) :width "360px"
                                   :rows 4 :on-change #(dispatch [:set-local-item-val [:documents id :notes] %])]]]]
            url (cond
                  @processing "/image-assets/ajax-loader.gif"
                  (and (not (nil? @signed-raw-url-expiration-time)) (not (expired? @signed-raw-url-expiration-time))) "/image-assets/file-text.png"
                  :else "/image-assets/file-question.png")
            object-fit (cond
                          @processing :fit-none
                          ; (or (= @display-type :contact-sheet) @editing) "cover"
                          :else :fit-contain)
            lhs [v-box :width image-size ;:max-height image-size ;:style {:margin-top "10px" :z-index 0 :position "relative"}
                    :children [[box :max-width image-size :max-height image-size
                                 :child [:img {:src url :width image-size :height image-size :class object-fit}]]
                                        ; [popover-tooltip :label "Download document"
                                        ;      :showing? showing-download-tooltip? :position :below-center
                                        ;      :anchor [:a {:class "zmdi zmdi-download rc-md-icon-button rc-icon-emphasis"
                                        ;                   :on-mouse-over (handler-fn (reset! showing-download-tooltip? true))
                                        ;                   :on-mouse-out  (handler-fn (reset! showing-download-tooltip? false))
                                        ;                   :href @signed-raw-url
                                        ;                   :download ""}]]]
                               (when (or (and @editing (= @display-type :single-item) (not @processing))
                                         (and (not @editing) (empty? @key) (not @processing)))
                                 [h-box :gap "8px" :align :center :justify :center :style {:background-color "#428bca"}
                                    :children [(when-not (empty? @key)
                                                   [popover-anchor-wrapper
                                                      :showing? showing-document-info? :position :below-center
                                                      :anchor [md-icon-button :md-icon-name "zmdi zmdi-info"
                                                                  :emphasise? true :tooltip "Document information"
                                                                  :on-click #(swap! showing-document-info? not)]
                                                      :popover [popover-content-wrapper :width "250px" :close-button? true :title "Document"
                                                                  :body [v-box :gap "4px" :align :start
                                                                           :children [[:span @filename]
                                                                                      [:span (str (quot @size 1024) " KB")]]]]])
                                               ;; hidden input text + button for document upload
                                               [:input {:type "file" :multiple false :name "document-upload"
                                                        :id document-input-id :style {:display "none"}
                                                        :on-change #(let [el (.getElementById js/document document-input-id)]
                                                                      (if (empty? @key)
                                                                        (dispatch [:add-s3-object "helodali-documents" [:documents id] (aget (.-files el) 0)])
                                                                        (dispatch [:replace-s3-object "helodali-documents" [:documents id] (aget (.-files el) 0)])))}]
                                               [md-icon-button :md-icon-name "zmdi zmdi-upload"
                                                  :emphasise? true :tooltip "Replace this document"
                                                  :on-click #(let [el (.getElementById js/document document-input-id)]
                                                               (.click el))]
                                               (when-not (empty? @key)
                                                 [md-icon-button :md-icon-name "zmdi zmdi-delete"
                                                    :emphasise? true :tooltip "Delete this document"
                                                    :on-click #(if-not (empty? @key)
                                                                 (dispatch [:delete-s3-object-from-item ["helodali-documents"] [:documents id]]))])]])
                               (when-not (or @editing (empty? @key) @processing)
                                 [h-box :gap "8px" :align :center :justify :center :style {:background-color "#428bca"}
                                    :children [[popover-tooltip :label "Download document"
                                                  :showing? showing-download-tooltip? :position :below-center
                                                  :anchor [:a {:class "zmdi zmdi-download rc-md-icon-button rc-icon-emphasis"
                                                               :on-mouse-over (handler-fn (reset! showing-download-tooltip? true))
                                                               :on-mouse-out  (handler-fn (reset! showing-download-tooltip? false))
                                                               :href @signed-raw-url
                                                               :download ""}]]]])]]
            rhs [v-box :gap "10px" :align :start :justify :start
                   :children (into (if @editing edit view) (if @editing [save-control] [view-control]))]]
        ;; Perform some dispatching if the document is not in sync with S3 and database
        ; (when @processing  ;; TODO: This should not be necessary for documents since we control the s3/DynamoDB integrity
        ;   (dispatch [:refresh-item [:documents id] #(not (empty? (:filename %)))]))
        (when (and @key (expired? @signed-raw-url-expiration-time))
          (dispatch [:get-signed-url [:documents id] "helodali-documents" @key :signed-raw-url :signed-raw-url-expiration-time]))

        ;; Base UI on new-item versus single-item
        ;; A new-item view does not present the document upload or edit/delete controls
        (if (= @display-type :new-item)
          [v-box :gap "16px" :align :start :justify :start
            :children (into edit [create-control])]
          [h-box :gap "32px" :align :start :justify :start :padding "20px" :style {:flex-flow "row wrap"}
            ; :children (into [lhs] (conj (if @editing edit view) (if @editing [save-control] [view-control])))
            :children (into [lhs] [rhs])])))))

(defn item-list-view
  "Display item properties in single line. The 'widths' map contains the string
   length of the longest document name. We use this to size the associated column of data but
   additionally apply a maximum, e.g. 80ch for name, by truncating the strings over the max."
  [widths id odd-row?]
  (let [uuid (subscribe [:item-key :documents id :uuid])
        bg-color (if odd-row? "#F4F4F4" "#FCFCFC")
        title (subscribe [:item-key :documents id :title])
        filename (subscribe [:item-key :documents id :filename])
        size (subscribe [:item-key :documents id :size])
        created (subscribe [:item-key :documents id :created])
        last-modified (subscribe [:item-key :documents id :last-modified])]
    (fn []
      (let [lm (or @last-modified @created)]
        [h-box :align :center :justify :start :style {:background bg-color} :width "100%"
          :children [[hyperlink :style {:width (str (max 14 (:title widths)) "ch")} :label (trunc (safe-string @title "(no title)") (:title widths))
                         :on-click #(route-single-item :documents @uuid)]
                     [label :width (str (max 14 (:filename widths)) "ch") :label (safe-string @filename "(no file attached)")]
                     [label :width "15ch" :label (safe-date-string @created)]
                     [label :width "15ch" :label (safe-date-string lm)]
                     [label :width "8ch" :label (str (quot @size 1024) " KB")]
                     [h-box :gap "2px" :justify :center :align :center :style {:font-size "18px"}
                         :children [[row-button :md-icon-name "zmdi zmdi-copy"
                                       :mouse-over-row? true :tooltip "Copy this item"
                                       :on-click #(dispatch [:copy-item :documents id :title])]
                                    [row-button :md-icon-name "zmdi zmdi-delete"
                                       :mouse-over-row? true :tooltip "Delete this item"
                                       :on-click #(dispatch [:delete-document-item :documents id])]]]]]))))

(defn list-view
  "Display list of items, one per line"
  []
  (let [sort-key (subscribe [:by-path [:sort-keys :documents]])
        items (subscribe [:items-keys-sorted-by-key :documents sort-by-key-then-created])
        filenames (subscribe [:items-vals :documents :filename])
        titles (subscribe [:items-vals :documents :title])]
    (fn []
      (let [widths (r/atom {:filename (+ 8 (max-string-length @filenames 80))
                            :title (+ 13 (max-string-length @titles 80))}) ;; 13 => leave space for "(no title)"
            header [h-box :align :center :justify :start :width "100%"
                      :children [[hyperlink :class "uppercase" :style {:width (str (max 14 (:title @widths)) "ch")} :label "Title"
                                    :tooltip "Sort by Title" :on-click #(if (= (first @sort-key) :title)
                                                                          (dispatch [:set-local-item-val [:sort-keys :documents 1] (not (second @sort-key))])
                                                                          (dispatch [:set-local-item-val [:sort-keys :documents] [:title true]]))]
                                 [hyperlink :class "uppercase" :style {:width (str (max 14 (:filename @widths)) "ch")} :label "Filename"
                                    :tooltip "Sort by Filename" :on-click #(if (= (first @sort-key) :filename)
                                                                              (dispatch [:set-local-item-val [:sort-keys :documents 1] (not (second @sort-key))])
                                                                              (dispatch [:set-local-item-val [:sort-keys :documents] [:filename true]]))]
                                 [hyperlink :class "uppercase" :style {:width "15ch"} :label "Created"
                                     :tooltip "Sort by Date Created" :on-click #(if (= (first @sort-key) :created)
                                                                                  (dispatch [:set-local-item-val [:sort-keys :documents 1] (not (second @sort-key))])
                                                                                  (dispatch [:set-local-item-val [:sort-keys :documents] [:created false]]))]
                                 [hyperlink :class "uppercase" :style {:width "15ch"} :label "Modified"
                                     :tooltip "Sort by Date Last Modified" :on-click #(if (= (first @sort-key) :last-modified)
                                                                                        (dispatch [:set-local-item-val [:sort-keys :documents 1] (not (second @sort-key))])
                                                                                        (dispatch [:set-local-item-val [:sort-keys :documents] [:last-modified true]]))]
                                 [hyperlink :class "uppercase" :style {:width "8ch"} :label "Size"
                                    :tooltip "Sort by Size" :on-click #(if (= (first @sort-key) :size)
                                                                         (dispatch [:set-local-item-val [:sort-keys :documents 1] (not (second @sort-key))])
                                                                         (dispatch [:set-local-item-val [:sort-keys :documents] [:size false]]))]]]]
        [v-box :gap "4px" :align :center :justify :start
           :children (into [header] (mapv (fn [id bg] ^{:key (str id "-" (:title @widths))} [item-list-view @widths id bg]) @items (cycle [true false])))]))))

(defn view-selection
  "The row of view selection controls: list new-item"
  []
  (fn []
    [h-box :gap "18px" :align :center :justify :center
       :children [[md-icon-button :md-icon-name "zmdi zmdi-view-headline mdc-text-grey" :tooltip "List View"
                                  :on-click #(route-view-display :documents :list)]
                  [md-icon-button :md-icon-name "zmdi zmdi-collection-plus mdc-text-grey" :tooltip "Create New Document"
                                  :on-click #(route-new-item :documents)]]]))

(defn single-item-view
  []
  (let [uuid (subscribe [:app-key :single-item-uuid])
        item-path (subscribe [:item-path-by-uuid :documents @uuid])
        id (last @item-path)]
    (fn []
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" ;:style {:flex-flow "row wrap"}
           :children [[item-view id]]]))))

(defn new-item-view
  []
  (let [id 0
        item-path (subscribe [:by-path [:documents id]])]
    (fn []
      (pprint (str "document 0: " @item-path))
      (when (not (empty? @item-path))
        [v-box :gap "10px" :margin "40px" :align :center :justify :start
           :children [[item-view id]]]))))

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
