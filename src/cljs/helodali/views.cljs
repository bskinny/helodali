(ns helodali.views
    (:require [helodali.misc :refer [expired?]]
              [cljs-time.core :as ct]
              [helodali.routes :refer [route route-profile route-search]]
              [helodali.views.artwork :refer [artwork-view]]
              [helodali.views.contacts :refer [contacts-view]]
              [helodali.views.expenses :refer [expenses-view]]
              [helodali.views.press :refer [press-view]]
              [helodali.views.documents :refer [documents-view]]
              [helodali.views.profile :refer [profile-view]]
              [helodali.views.account :refer [account-view]]
              [helodali.views.pages :refer [pages-view]]
              [helodali.views.purchases :refer [purchases-view]]
              [helodali.views.exhibitions :refer [exhibitions-view]]
              [helodali.views.search-results :refer [search-results-view]]
              [helodali.views.static-pages :refer [static-pages-view]]
              [cljs.pprint :refer [pprint]]
              [reagent.core :as r]
              [re-frame.core :as re-frame :refer [dispatch subscribe]]
              [re-com.core :as re-com :refer [box v-box h-box label md-icon-button row-button hyperlink
                                              input-text input-textarea single-dropdown selection-list]]))


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
                           [label :label "Expenses" :on-click #(select-fn :expenses)]
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
                           [label :label "My Account" :on-click #(select-fn :account)]
                           [label :label "Publishing" :on-click #(select-fn :pages)]
                           [label :label "Logout" :on-click (fn []
                                                               (dispatch [:logout])
                                                               (reset! showing-account-popover? false))]]]])))

(defn header
  "Display in-app main page header"
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
              :children [[re-com/hyperlink-href :class "uppercase" :style {:color :black}
                            :label "Contact" :href "mailto:support@helodali.com"]
                         [hyperlink :class "uppercase" :label "privacy" :style {:color :black}
                            :on-click #(dispatch [:display-static-html :privacy-policy])]]]))

(defn show-spinner
  []
  [v-box :gap "0px" :width "100%" :height "100%" :margin "0"
         :align :center :justify :center ;:style {:border "dashed 1px red"}
     :children [[re-com/throbber :size :large]]])

(def cognito-base-url "https://helodali.auth.us-east-1.amazoncognito.com")
(def cognito-client-id "4pbu2aidkc3ev5er82j6in8q96")
(def origin (.-origin (.-location js/document)))

;; on-click event handler which performs login
(def do-login
  #(set! (.. js/window -location -href)
         (str cognito-base-url "/oauth2/authorize?redirect_uri=" origin
              "/login&response_type=code&client_id=" cognito-client-id "&state=" (rand)
              "&scope=openid%20email%20profile")))

(defn- our-title
  "Display the HELODALI top-left title and link to either the landing page or login depending on context."
  [view]
  (let [style {:padding-top "10px" :color "rgb(208, 187, 187)" :text-decoration "none"}]
    (if (= view :landing)
      [hyperlink :class "level1" :label "helodali" :style style :on-click do-login]
      [hyperlink :class "level1" :label "helodali" :style style :on-click #(dispatch [:back-to-landing-page])])))

(defn- login-button
  []
  ;; We are not checking the state value of the authorize request on the return visit.
  [md-icon-button :md-icon-name "zmdi zmdi-brush" :size :larger :on-click do-login])

(defn display-message
  [id msg]
  [box :width "50%" :align-self :center
     :child [re-com/alert-box :alert-type :warning :closeable? true :body msg
                :on-close #(dispatch [:clear-message id])]])

(defn main-panel []
 (let [msgs (subscribe [:app-key :messages])
       view (subscribe [:app-key :view])
       aws-creds (subscribe [:app-key :aws-creds])
       refresh-access-token? (subscribe [:app-key :refresh-access-token?])
       aws-s3 (subscribe [:app-key :aws-s3])
       authenticated? (subscribe [:app-key :authenticated?])
       do-cognito-logout? (subscribe [:app-key :do-coginito-logout?])
       refresh-aws-creds? (subscribe [:app-key :refresh-aws-creds?])
       initialized? (subscribe [:app-key :initialized?])
       csrf-token (subscribe [:app-key :csrf-token])
       access-token (subscribe [:app-key :access-token])
       id-token (subscribe [:app-key :id-token])
       sit-and-spin (subscribe [:app-key :sit-and-spin])]
   (fn []
     ;;
     ;; Perform whatever dispatching is needed depending on current state
     ;;
     (when @do-cognito-logout?
       (set! (.. js/window -location -href)
             (str cognito-base-url "/logout?logout_uri=" origin "/&client_id=" cognito-client-id)))

     (when (and (not @authenticated?) (not (empty? @access-token)) (not (empty? @csrf-token)))
       (dispatch [:validate-access-token]))

     (when (and (not @authenticated?) (empty? @access-token) (not (empty? @csrf-token)))
       (dispatch [:check-session]))

     ;; Force the fetch of a access token refresh in the event the signed urls expire and we have not hit the
     ;; (helodali) server for a pass through verify-token.
     (when @refresh-access-token?
       (dispatch [:refresh-access-token]))

     ;; Fetch AWS Credentials if first time in or if needing a refresh after credential expiration
     (when (or (and @authenticated? @initialized? (empty? @aws-creds) (not (empty? @id-token)))
               @refresh-aws-creds?)
       ;; Fetch the AWS credentials from Cognito and initialize AWS services like S3
       (let [aws-config (.-config js/AWS)
             logins {:IdentityPoolId "us-east-1:c5e15cf1-df1d-48df-85ba-f67d1ff45016"
                     :Logins {"cognito-idp.us-east-1.amazonaws.com/us-east-1_0cJWyWe5Z" @id-token}}]
         (set! (.-region aws-config) "us-east-1")
         (set! (.-credentials aws-config) (js/AWS.CognitoIdentityCredentials. (clj->js logins)))
         ;; This clearCachedId and recreation of credentials is a workaround still required as of 06012018
         ;; and described here:https://github.com/aws/aws-sdk-js/issues/609.
         (.clearCachedId (.-credentials aws-config))
         (set! (.-credentials aws-config) (js/AWS.CognitoIdentityCredentials. (clj->js logins)))
         (.get (.-credentials aws-config) (clj->js (fn [err] (dispatch [:set-aws-credentials (.-credentials js/AWS.config)]))))))

     ;;
     ;; UI states
     ;;
     (cond ;; Note: some of the clauses below are formatted oddly because of parinfer and the desire not to let the code run as wide as this comment.
       @sit-and-spin (show-spinner)

       (and (not @authenticated?) (empty? @access-token) (not= @view :static-page))
       ;; Display login widget front and center
       [v-box :gap "20px" :width "100%" :height "100%" :margin "0" :class "login-page"
              :align :center :justify :between
          :children [[h-box :size "0 0 auto" :width "100%" :align :center :justify :around :class "header"
                        :children [(our-title @view)  (login-button)]]
                     [gap :size "1px"]
                     [footer]]]

       (and (not @authenticated?) (empty? @access-token) (= @view :static-page))
       ;; Display static html with login header
       [v-box :gap "20px" :width "100%" :height "100%" :margin "0" :justify :between
          :children [[h-box :align :center :justify :around :children [(our-title @view) (login-button)]]
                     [static-pages-view]
                     [footer]]]

       (and @authenticated? @initialized? (not (empty? @aws-creds)) (not (nil? @aws-s3)))
       ;; Finally, display the app
       [v-box :gap "0px" :margin "0px" :justify :between :width "100%"
          :children [[v-box
                       :children [[header]
                                  (if (not (empty? @msgs))
                                    [v-box :gap "12px" :justify :start :align :center
                                        :children (mapv (fn [[id msg]] ^{:key (str id)} [display-message id msg]) @msgs)])
                                  [re-com/gap :size "18px"]
                                  (condp = @view
                                    :artwork [artwork-view]
                                    :contacts [contacts-view]
                                    :expenses [expenses-view]
                                    :press [press-view]
                                    :profile [profile-view]
                                    :static-page [static-pages-view]
                                    :account [account-view]
                                    :pages [pages-view]
                                    :purchases [purchases-view]
                                    :documents [documents-view]
                                    :exhibitions [exhibitions-view]
                                    :search-results [search-results-view]
                                    [box :width "50%" :align-self :center
                                       :child [re-com/alert-box :alert-type :warning :closeable? true :body (str "Unexpected value for :view of " @view)]])]]
                     [footer]]]

       :else (show-spinner)))))
