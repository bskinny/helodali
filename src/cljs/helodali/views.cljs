(ns helodali.views
    (:require [helodali.misc :refer [expired?]]
              [helodali.routes :refer [route route-profile route-search route-static-html]]
              [helodali.views.artwork :refer [artwork-view]]
              [helodali.views.contacts :refer [contacts-view]]
              [helodali.views.press :refer [press-view]]
              [helodali.views.documents :refer [documents-view]]
              [helodali.views.profile :refer [profile-view]]
              [helodali.views.purchases :refer [purchases-view]]
              [helodali.views.exhibitions :refer [exhibitions-view]]
              [helodali.views.search-results :refer [search-results-view]]
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
                           [label :label "Account" :on-click #(select-fn :account)]
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
                                                 :on-click #(route helodali.routes/view {:type (name :artwork)})]
                                 [re-com/popover-anchor-wrapper :showing? showing-account-popover? :position :below-right
                                   :anchor   [md-icon-button :md-icon-name "zmdi zmdi-account-o" :size :larger
                                                    :on-click #(reset! showing-account-popover? true)]
                                   :popover  [account-popover-body showing-account-popover?]]
                                 [re-com/popover-anchor-wrapper :showing? showing-more? :position :below-center
                                   :anchor   [md-icon-button :md-icon-name "zmdi zmdi-more" :size :larger
                                              ; label :label "more" :style {:cursor "pointer"}
                                                    :on-click #(reset! showing-more? true)]
                                   :popover  [more-popover-body showing-more?]]]]]])))

(defn static-page
  []
  (let [page (subscribe [:app-key :static-page])]
    [:iframe {:src (str "/static/" @page) :frameBorder "0" :scrolling "yes" :height "800px"}]))

(defn footer
  []
  (let [showing-privacy? (r/atom false)]
    [h-box :width "100%" :class "header" :height "100px" :gap "40px" :align :center :justify :center
              :children [[re-com/hyperlink-href :class "uppercase"
                            :label "Contact" :href "mailto:support@helodali.com"]
                         [hyperlink :class "uppercase" :label "privacy"
                            :on-click #(route-static-html "privacy.html")]]]))
(defn show-spinner
  []
  [v-box :gap "0px" :width "100%" :height "100%" :margin "0"
         :align :center :justify :center ;:style {:border "dashed 1px red"}
     :children [[re-com/throbber :size :large]]])

(defn show-login-page
  [lock]
  [v-box :gap "0px" :width "100%" :height "100%" :margin "0"
         :align :center :justify :center ;:style {:border "dashed 1px red"}
     :children [[md-icon-button :md-icon-name "zmdi zmdi-run" :size :larger
                     :on-click #(.show lock)]]])

(defn- our-title [] [re-com/title :level :level1 :label "helodali"])

(defn- login-button
  [lock]
  [md-icon-button :md-icon-name "zmdi zmdi-brush" :size :larger
       :on-click #(.show lock (clj->js {:initialScreen "login"
                                        :rememberLastLogin true}))])

(defn- handle-authenticated
  "Auth0 has authenticated the user. The auth-result contains accessToken, idToken and idTokenPayload"
  [auth-result]
  (pprint (str "auth-result: " (js->clj auth-result)))
  (let [result (js->clj auth-result)]
    (dispatch [:authenticated true (get result "accessToken") (get result "idToken")])))

(defn main-panel []
 (let [msg (subscribe [:app-key :message])
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
                          :children [(our-title) (login-button lock)]]
                       [gap :size "1px"]
                       ; [v-box
                       ;   :children [(our-title) (login-button lock)]]
                       [footer]]]

         (and (not @authenticated?) (empty? @access-token) (= @view :static-page))
         ;; Display static html with login header
         [v-box :gap "20px" :width "100%" :height "100%" :margin "0" ;:class "login-page"
                :justify :between ;:style {:border "dashed 1px red"}
            :children [[h-box :align :center :justify :around :children [(our-title) (login-button lock)]]
                       ; [v-box :align :start :justify :start :style {:border "dashed 1px red"}] ;:child [static-page]]
                       [v-box :margin "40px" :max-width "800px" :align-self :center :align :start :justify :start :children [[re-com/title :level :level2 :label "How We Collect and Use Information"] [re-com/title :level :level3 :label "We collect the following types of information about you"] [gap :size "14px"] [label :class "bold" :label "Information you provide us directly"] [gap :size "14px"] [:p "We allow, but do not require, you to provide biographical information sufficient for the creation of an artist CV, defined in the\nartist profile. This information includes email, phone and year of birth; it is not shared with any third party.\nThe images and description of artwork are not shared with any third party unless consent is provided by you, such as when\nyou post an artwork to social media from Helodali."] [gap :size "14px"] [label :class "bold" :label "Information we may receive from third parties"] [gap :size "14px"] [:p "Helodali uses third party identity providers to allow you to login with credentials maintained elsewhere (e.g. Facebook, Google, etc.). We ask these\nidentity providers to provide us with your email and name. This information is only used by Helodali to communicate with you and is not shared\nwith a third party. If using the Helodali feature which integrates with Facebook and Instagram to pull images of artwork, we ask for your\nconsent and limit the data gathering to just images and associated &quot;like&quot; counts. Helodali does not look for or use lists of friends or followers."] [gap :size "14px"] [label :class "bold" :label "Analytics and Cookies"] [gap :size "14px"] [:p "At this time, Helodali does not collect usage data using third party tools such as Google Analytics. Helodali makes minimal use of HTTP Cookies.\nA session key is placed in the cookie which contains no personal information."] [gap :size "14px"] [label :class "bold" :label "Server Log file information"] [gap :size "14px"] [:p "Log file information is automatically reported by your browser or mobile device each time you access the service. When you\nuse our service, our servers automatically record certain this information. These server logs may include anonymous information\nsuch as your web request, IP address, browser type, referring / exit pages and URLs, number of clicks and how you interact\nwith links on the service, domain names, landing pages, pages viewed, and other such information. Helodali does not use methods which\nallow sensitive data to be written in access logs, such as URL based parameter passing of identity information."] [gap :size "14px"] [label :class "bold" :label "Tracking and Location"] [gap :size "14px"] [:p "Helodali does not employ any tracking mechanism nor does it collect location data from web or mobile clients."] [gap :size "14px"] [label :class "bold" :label "Commercial and marketing communications"] [gap :size "14px"] [:p "We use your email address to communicate directly with you. We may send you service-related emails\n(e.g., account verification, purchase and billing confirmations and reminders, changes/updates to features of the service,\ntechnical and security notices)."] [gap :size "14px"] [re-com/title :level :level3 :label "Sharing of Your Information"] [gap :size "14px"] [:p "Helodali does not in any case rent or sell personally identifiable information."] [gap :size "14px"] [label :class "bold" :label "Instances where we are required to share your information"] [gap :size "14px"] [:p "Helodali will disclose your information where required to do so by law or subpoena or if we reasonably\n believe that such action is necessary to (a) comply with the law and the reasonable requests of law enforcement;\n  (b) to enforce our Terms of Use or to protect the security, quality or integrity of our service; and/or\n  (c) to exercise or protect the rights, property, or personal safety of Helodali, our Users, or others."] [gap :size "14px"] [re-com/title :level :level3 :label "How We Store and Protect Your Information"] [gap :size "14px"] [:p "Your information and images are stored and processed securely in the United States on Amazon Web services cloud systems only."] [gap :size "14px"] [label :class "bold" :label "Keeping your information safe"] [gap :size "14px"] [:p "Helodali uses industry standard safeguards to preserve the integrity and\nsecurity of all user information. You are responsible for maintaining the secrecy of the identity used to access Helodali."] [gap :size "14px"] [label :class "bold" :label "Compromise of information"] [gap :size "14px"] [:p "In the event that any information under our control is compromised as a result of a breach of security, Helodali will take reasonable steps\n to investigate the situation and where appropriate, notify those individuals whose information may have been compromised and take other steps, in accordance with any applicable laws and regulations."] [gap :size "14px"] [re-com/title :level :level3 :label "Your Choices about Your Information"] [gap :size "14px"] [:p "You control your account information and settings: You may update your account information and email-communication preferences at any time by logging in to your account and changing your profile settings. You can also stop receiving promotional email communications from us by modifying your account preferences.\nAs noted above, you may not opt out of service-related communications (e.g., account verification, purchase and billing confirmations and reminders, changes/updates to features of the service, technical and security notices). If you have any questions about reviewing or modifying your account information, you can contact us directly at support@helodali.com."] [gap :size "14px"] [label :class "bold" :label "How long we keep your private profile information"] [gap :size "14px"] [:p "Following termination of your User account, Helodali may retain your private profile information for a commercially reasonable time for backup, archival, or audit purposes.\nFor the avoidance of doubt, any information that you choose to make public on the service may not be removable."] [gap :size "14px"] [re-com/title :level :level3 :label "Links to Other Websites and services"] [gap :size "14px"] [:p "We are not responsible for the practices employed by websites or services linked to or from the service,\nincluding the information or content contained therein. Please remember that when you use a link to go\n from Helodali to another website, our Privacy Policy does not apply to third-party websites or services.\n Your browsing and interaction on any third-party website or service, including those that have a link or\n advertisement on our website, are subject to that third party’s own rules and policies. In addition,\n you agree that we are not responsible and we do not control over any third-parties that you authorize to\n access your User Content. If you are using a third-party website or service (like Facebook, Google groups, or\n an IRC chatroom) and you allow such a third-party access to you User Content you do so at your own risk.\n This Privacy Policy does not apply to information we collect by other means (including offline) or from\n other sources other than through the service."]]]
                       [footer]]]


         (and @authenticated? @initialized?)
         ;; Finally, display the app
         [v-box :gap "0px" :width "100%" :margin "0" :justify :between ; :style {:border "dashed 1px red"}
            :children [[v-box
                         :children [[header]
                                    (if (not (empty? @msg))
                                      [box :width "50%" :align-self :center
                                         :child [re-com/alert-box :alert-type :warning :closeable? true :body @msg
                                                    :on-close #(dispatch [:set-local-item-val [:message] ""])]])
                                    [re-com/gap :size "18px"]
                                    (condp = @view
                                      :artwork [artwork-view]
                                      :contacts [contacts-view]
                                      :press [press-view]
                                      :profile [profile-view]
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
