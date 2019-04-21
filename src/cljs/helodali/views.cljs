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
  [h-box :width "100%" :class "header" :height "100px" :gap "40px" :align :center :justify :center
            :children [[re-com/hyperlink-href :class "uppercase" :style {:color :black}
                          :label "Contact" :href "mailto:support@helodali.com"]
                       [hyperlink :class "uppercase" :label "privacy" :style {:color :black}
                          :on-click #(dispatch [:display-static-html :privacy-policy])]]])

(defn show-spinner
  []
  [v-box :gap "0px" :width "100%" :height "100%" :margin "0"
         :align :center :justify :center ;:style {:border "dashed 1px red"}
     :children [[re-com/throbber :size :large]]])

(def cognito-base-url "https://helodali.auth.us-east-1.amazoncognito.com")
(def cognito-client-id "2j61fm7lap9t771afkndvc9uof")
(def origin (.-origin (.-location js/document)))
(def authorize-url (str cognito-base-url "/oauth2/authorize?redirect_uri=" origin
                        "/login&response_type=code&client_id=" cognito-client-id "&state=" (rand)
                        "&scope=openid%20email%20profile"))

;; on-click event handler which performs login flow
(defn authorize-fn
  "Kick off Oauth2 authorization with or without an IdP (valid Idp values are :Google or :Facebook)"
  ([]
   #(set! (.. js/window -location -href) authorize-url))
  ([idp]
   #(set! (.. js/window -location -href) (str authorize-url "&identity_provider=" (name idp)))))

;; on-click event handler which triggers signup flow
(def do-register
  #(set! (.. js/window -location -href)
         (str cognito-base-url "/signup?redirect_uri=" origin
              "/login&response_type=code&client_id=" cognito-client-id "&state=" (rand)
              "&scope=openid%20email%20profile")))

(defn- our-title
  "Display the HELODALI top-left title and link to either the landing page or login depending on context."
  [view]
  (let [back-to-landing? (r/atom (not= view :landing))
        style {:padding-top "10px" :color "rgb(110, 90, 90)" :text-decoration "none"}]
    (if @back-to-landing?
       [hyperlink :class "level1" :label "helodali" :style style :on-click #(dispatch [:back-to-landing-page])]
       [label :class "level1" :label "helodali" :style style])))

;; Use the following panel if it is desired to display login buttons for external identity providers Google and FB. This is a shortcut
;; approach to the current "sign in" link. Unfortunately, a shortcut may not be easily available for native user login.
(defn- login-panel
  []
  (let [social-login-box [v-box :gap "10px" :width "100%" :height "100%" :margin "0" :align :stretch :justify :start
                           :children
                            [[:button {:name "googleSignIn" :on-click (authorize-fn :Google) :class "btn google-button"}
                                      [:span>svg {:class "social-logo" :viewBox "0 0 256 262" :xmlns "http://www.w3.org/2000/svg"
                                                  :preserveAspectRatio "xMidYMid"}
                                        [:path {:d "M255.878 133.451c0-10.734-.871-18.567-2.756-26.69H130.55v48.448h71.947c-1.45 12.04-9.283
                                                   30.172-26.69 42.356l-.244 1.622 38.755 30.023 2.685.268c24.659-22.774 38.875-56.282 38.875-96.027"
                                                :fill "#4285F4"}]
                                        [:path {:d "M130.55 261.1c35.248 0 64.839-11.605 86.453-31.622l-41.196-31.913c-11.024 7.688-25.82 13.055-45.257
                                                   13.055-34.523 0-63.824-22.773-74.269-54.25l-1.531.13-40.298 31.187-.527 1.465C35.393 231.798 79.49 261.1 130.55 261.1"
                                                :fill "#34A853"}]
                                        [:path {:d "M56.281 156.37c-2.756-8.123-4.351-16.827-4.351-25.82 0-8.994 1.595-17.697 4.206-25.82l-.073-1.73L15.26
                                                   71.312l-1.335.635C5.077 89.644 0 109.517 0 130.55s5.077 40.905 13.925 58.602l42.356-32.782"
                                                :fill "#FBBC05"}]
                                        [:path {:d "M130.55 50.479c24.514 0 41.05 10.589 50.479 19.438l36.844-35.974C195.245 12.91 165.798 0 130.55 0 79.49
                                                   0 35.393 29.301 13.925 71.947l42.211 32.783c10.59-31.477 39.891-54.251 74.414-54.251"
                                                :fill "#EA4335"}]]
                                      [:span "Continue with Google"]]
                             [:button {:name "facebookSignIn" :on-click (authorize-fn :Facebook) :class "btn facebook-button"}
                                      [:span>svg {:class "social-logo" :viewBox "0 0 216 216" :xmlns "http://www.w3.org/2000/svg" :color "#ffffff"}
                                       [:path {:d "M204.1 0H11.9C5.3 0 0 5.3 0 11.9v192.2c0 6.6 5.3 11.9 11.9 11.9h103.5v-83.6H87.2V99.8h28.1v-24c0-27.9
                                                   17-43.1 41.9-43.1 11.9 0 22.2.9 25.2 1.3v29.2h-17.3c-13.5 0-16.2 6.4-16.2 15.9v20.8h32.3l-4.2 32.6h-28V216h55c6.6 0
                                                   11.9-5.3 11.9-11.9V11.9C216 5.3 210.7 0 204.1 0z"
                                               :fill "#ffffff"}]]
                                      [:span "Continue with Facebook"]]]]
        native-login-box [v-box :gap "10px" :width "100%" :height "100%" :margin "0" :align :center :justify :center ;:style {:border "dashed 1px red"}
                          :children []]]
    [h-box :align :center :justify :center :gap "10px"
      :children [social-login-box native-login-box]]))

(defn- login-button
  []
  ;; We are not checking the state value of the authorize request on the return visit.
  (let [style {:padding-top "10px" :color "rgb(110, 90, 90)" :font-weight "300" :text-decoration "none"}]
    [h-box :align :center :justify :center :gap "10px"
      :children [[hyperlink :class "level3" :label "sign in" :style style :on-click (authorize-fn)]
                 [md-icon-button :md-icon-name "zmdi zmdi-brush" :style style :on-click (authorize-fn)]
                 [hyperlink :class "level3" :label "register" :style style :on-click do-register]]]))

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
