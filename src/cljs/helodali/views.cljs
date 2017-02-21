(ns helodali.views
    (:require [helodali.misc :refer [expired?]]
              [helodali.routes :refer [route route-profile route-search]]
              [helodali.views.artwork :refer [artwork-view]]
              [helodali.views.contacts :refer [contacts-view]]
              [helodali.views.press :refer [press-view]]
              [helodali.views.documents :refer [documents-view]]
              [helodali.views.profile :refer [profile-view]]
              [helodali.views.purchases :refer [purchases-view]]
              [helodali.views.exhibitions :refer [exhibitions-view]]
              [helodali.views.search-results :refer [search-results-view]]
              [helodali.views.static-pages :refer [static-pages-view]]
              [cljs.pprint :refer [pprint]]
              [cljsjs.auth0-lock]
              [reagent.core :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list]]))

(defn title []
  (let [name (subscribe [:name])]
    (fn []
      [re-com/title
       :label (str "Hello from " @name)
       :level :level1])))

(defn- gap
  "Return a re-com/gap of the given size"
  [size]
  [re-com/gap :size (str size "px")])

(defn more-popover-body
  []
  (fn [showing-more? & {:keys [showing-injected? position-injected]}]
    (let [select-fn (fn [view]
                       (route helodali.routes/view {:type (name view)})
                       (reset! showing-more? false))]
      [re-com/popover-content-wrapper :showing-injected? showing-more? :position-injected position-injected
        :width "160px" :backdrop-opacity 0.3 :on-cancel #(reset! showing-more? false) :style {:cursor "pointer"}
        :body [v-box
                :children [[label :label "Exhibitions" :on-click #(select-fn :exhibitions)]
                           [label :label "Contacts" :on-click #(select-fn :contacts)]
                           [label :label "Documents" :on-click #(select-fn :documents)]
                           [label :label "Press" :on-click #(select-fn :press)]
                           [label :label "Purchases" :on-click #(select-fn :purchases)]]]])))

(defn account-popover-body
  []
  (fn [showing-account-popover? & {:keys [showing-injected? position-injected]}]
    (let [select-fn (fn [view]
                       (route helodali.routes/view {:type (name view)})
                       (reset! showing-account-popover? false))]
      [re-com/popover-content-wrapper :showing-injected? showing-account-popover? :position-injected position-injected
        :width "160px" :backdrop-opacity 0.3 :on-cancel #(reset! showing-account-popover? false) :style {:cursor "pointer"}
        :body [v-box
                :children [[label :label "Artist Profile" :on-click #(select-fn :profile)]
                           ; [label :label "Account" :on-click #(select-fn :account)]
                           [label :label "Logout" :on-click (fn []
                                                               (dispatch [:logout])
                                                               (reset! showing-account-popover? false))]]]])))

(defn header
  "Display main page header"
  []
  (let [showing-more? (r/atom false)
        showing-account-popover? (r/atom false)
        search-pattern (r/atom "")]
    (fn []
      [h-box :size "0 0 auto" :height "100px" :gap "10px" :align :center :justify :around :class "header"
         :children [[re-com/title :level :level2 :label "helodali"]
                    [input-text :width "200px" :model search-pattern :placeholder "Search" :style {:border "none"}
                         :on-change #(if (not (empty? %))
                                       (route-search %))]
                    [h-box :gap "8px" :justify :around
                      :children [[md-icon-button :md-icon-name "zmdi zmdi-collection-image-o" :size :larger
                                                 :on-click #(do (dispatch [:sweep-and-set :artwork :expanded false])
                                                                (route helodali.routes/view {:type (name :artwork)}))]
                                 [re-com/popover-anchor-wrapper :showing? showing-account-popover? :position :below-right
                                   :anchor   [md-icon-button :md-icon-name "zmdi zmdi-account-o" :size :larger
                                                    :on-click #(reset! showing-account-popover? true)]
                                   :popover  [account-popover-body showing-account-popover?]]
                                 [re-com/popover-anchor-wrapper :showing? showing-more? :position :below-center
                                   :anchor   [md-icon-button :md-icon-name "zmdi zmdi-more" :size :larger
                                              ; label :label "more" :style {:cursor "pointer"}
                                                    :on-click #(reset! showing-more? true)]
                                   :popover  [more-popover-body showing-more?]]]]]])))

(defn footer
  []
  (let [showing-privacy? (r/atom false)]
    [h-box :width "100%" :class "header" :height "100px" :gap "40px" :align :center :justify :center
              :children [[re-com/hyperlink-href :class "uppercase"
                            :label "Contact" :href "mailto:support@helodali.com"]
                         [hyperlink :class "uppercase" :label "privacy"
                            :on-click #(dispatch [:display-static-html :privacy-policy])]]]))

(defn show-spinner
  []
  [v-box :gap "0px" :width "100%" :height "100%" :margin "0"
         :align :center :justify :center ;:style {:border "dashed 1px red"}
     :children [[re-com/throbber :size :large]]])

(defn- our-title [] [re-com/title :level :level1 :label "helodali"])

(defn- login-button
  [lock]
  [md-icon-button :md-icon-name "zmdi zmdi-brush" :size :larger
       :on-click #(.show lock (clj->js {:initialScreen "login" :rememberLastLogin true}))])

(defn- handle-authenticated
  "Auth0 has authenticated the user. The auth-result contains accessToken, idToken and idTokenPayload"
  [auth-result]
  (pprint (str "auth-result: " (js->clj auth-result)))
  (let [result (js->clj auth-result)]
    (dispatch [:authenticated true (get result "accessToken") (get result "idToken")])))

(defn display-message
  [id msg]
  [box :width "50%" :align-self :center
     :child [re-com/alert-box :alert-type :warning :closeable? true :body msg
                :on-close #(dispatch [:clear-message id])]])

(defn main-panel []
 (let [msgs (subscribe [:app-key :messages])
       view (subscribe [:app-key :view])
       authenticated? (subscribe [:app-key :authenticated?])
       initialized? (subscribe [:app-key :initialized?])
       csrf-token (subscribe [:app-key :csrf-token])
       profile (subscribe [:app-key :profile])
       access-token (subscribe [:app-key :access-token])
       id-token (subscribe [:app-key :id-token])
       delegation-token (subscribe [:app-key :delegation-token])
       sit-and-spin (subscribe [:app-key :sit-and-spin])]
   (fn []
     (let [lock (js/Auth0Lock. "UNQ9LKBRomyn7hLPKKJmdK2mI7RNphGs" "helodali.auth0.com"
                               (clj->js {:auth {:params {:scope "openid name email"}}
                                         :rememberLastLogin true
                                         :theme {:logo "/image-assets/logo.png"
                                                 :primaryColor "red"}}))
           _ (.on lock "authenticated" handle-authenticated)]
       ;;
       ;; Perform whatever dispatching is needed depending on current state
       ;;
       (when (and (not @authenticated?) (not (empty? @csrf-token)) (not (empty? @access-token)))
         ;; We have an access token in local storage, validate it and possibly avoid login prompt
         (dispatch [:validate-access-token]))

       (when (and @authenticated? (not (empty? @csrf-token)) (empty? @profile))
         ;; Everything is ready for the login request which results in the population of the app-db
         ;; and the setting of initialized?
         (dispatch [:login]))

       (when (and @authenticated? @initialized? (empty? @delegation-token) (not (empty? @id-token))
                  (expired? (get @delegation-token "Expiration")))
         ;; Fetch the delegation token which is used to authenticate with S3
         (dispatch [:fetch-aws-delegation-token]))

       ;;
       ;; UI states
       ;;
       (cond ;; Note: some of the clauses below are formatted oddly because of parinfer and the desire not to let the code run as wide as this comment.
         @sit-and-spin (show-spinner)

         (and (not @authenticated?) (empty? @access-token) (not= @view :static-page))
         ;; Display login widget front and center
         [v-box :gap "20px" :width "100%" :height "100%" :margin "0" :class "login-page"
                :align :center :justify :between
            :children [[h-box :size "0 0 auto" :width "100%" :align :center :justify :around :class "header" ; :style {:border "dashed 1px red"}
                          :children [(our-title)  (login-button lock)]]
                       [gap :size "1px"]
                       [footer]]]

         (and (not @authenticated?) (empty? @access-token) (= @view :static-page))
         ;; Display static html with login header
         [v-box :gap "20px" :width "100%" :height "100%" :margin "0" :justify :between
            :children [[h-box :align :center :justify :around :children [(our-title) (login-button lock)]]
                       [static-pages-view]
                       [footer]]]


         (and @authenticated? @initialized?)
         ;; Finally, display the app
         [v-box :gap "0px" :margin "0px" :justify :between :width "100%" ; :style {:border "dashed 1px red"}
            :children [[v-box
                         :children [[header]
                                    (if (not (empty? @msgs))
                                      [v-box :gap "12px" :justify :start :align :center
                                          :children (mapv (fn [[id msg]] ^{:key (str id)} [display-message id msg]) @msgs)])
                                    [re-com/gap :size "18px"]
                                    (condp = @view
                                      :artwork [artwork-view]
                                      :contacts [contacts-view]
                                      :press [press-view]
                                      :profile [profile-view]
                                      :static-page [static-pages-view]
                                      :account [box :width "50%" :align-self :center
                                                  :child [re-com/alert-box :alert-type :warning :closeable? true :body "The account page is still being developed"]]
                                      :purchases [purchases-view]
                                      :documents [documents-view]
                                      :exhibitions [exhibitions-view]
                                      :search-results [search-results-view]
                                      [box :width "50%" :align-self :center
                                         :child [re-com/alert-box :alert-type :warning :closeable? true :body (str "Unexpected value for :view of " @view)]])]]
                       [footer]]]

         :else (show-spinner))))))
