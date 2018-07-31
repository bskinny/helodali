(ns helodali.spec
  (:require [cljs.spec.alpha :as s]
            [cljs.pprint :refer [pprint]]))

;; clojure.spec definitions of the application db.

;; Common types
(s/def ::id int?)
(s/def ::uuid string?)
(s/def ::title (s/nilable string?))
(s/def ::editing boolean?)
(s/def ::expanded boolean?)
(s/def ::description (s/nilable string?))
(s/def ::notes (s/nilable string?))
(s/def ::date #(instance? goog.date.Date %)) ;; a DateTime, down to the day
(s/def ::time #(instance? goog.date.Date %))  ;; A DateTime
(s/def ::created #(instance? goog.date.Date %))  ;; A DateTime
(s/def ::last-modified #(instance? goog.date.Date %))  ;; A DateTime, non-nil value otherwise undefined
(s/def ::include-in-cv boolean?)
(s/def ::ref (s/nilable ::uuid)) ;; Used to reference an item, certain situations require allowing nil
(s/def ::associated-documents (s/nilable (s/* ::uuid)))
(s/def ::associated-press (s/nilable (s/* ::uuid)))
(s/def ::processing (s/nilable boolean?))
(s/def ::name string?)

;; Instagram items
(s/def ::instagram-id (s/nilable string?))
(s/def ::artwork-uuid (s/nilable ::uuid))
(s/def ::caption (s/nilable string?))
(s/def ::likes int?)
(s/def ::media-type (s/nilable keyword?))
(s/def ::image-url (s/nilable string?))
(s/def ::thumb-url (s/nilable string?))
(s/def ::instagram-media-ref (s/nilable (s/keys :opt-un [::instagram-id ::artwork-uuid ::caption ::likes ::media-type
                                                         ::image-url ::thumb-url ::created ::processing])))

;; Artwork
(s/def ::year (s/and int? #(> % 1000)))
(s/def ::key (s/nilable string?))
(s/def ::filename (s/nilable string?))
(s/def ::signed-thumb-url (s/nilable string?))
(s/def ::signed-thumb-url-expiration-time (s/nilable #(instance? goog.date.Date %)))
(s/def ::signed-raw-url (s/nilable string?))
(s/def ::signed-raw-url-expiration-time (s/nilable #(instance? goog.date.Date %)))
(s/def ::size (s/nilable int?))
(s/def ::space (s/nilable string?))
(s/def ::width (s/nilable int?))
(s/def ::height (s/nilable int?))
(s/def ::density (s/nilable int?))
(s/def ::format (s/nilable string?))
(s/def ::metadata (s/keys :opt-un [::size ::space ::height ::width ::density ::format]))
(s/def ::image (s/keys :req-un [::uuid]
                       :opt-un [::key ::filename ::processing ::signed-thumb-url ::signed-thumb-url-expiration-time
                                ::signed-raw-url ::signed-raw-url-expiration-time ::metadata]))
(s/def ::images (s/coll-of ::image))
(s/def ::medium (s/nilable string?))
(s/def ::dimensions (s/nilable string?))
(s/def ::series boolean?)
(s/def ::editions int?)
(s/def ::condition (s/nilable string?))
(s/def ::exhibition-history-item (s/keys :opt-un [::ref ::notes ::images]))
(s/def ::exhibition-history (s/* ::exhibition-history-item))
(s/def ::price float?)
(s/def ::donated boolean?)
(s/def ::commissioned boolean?)
(s/def ::collection boolean?)
(s/def ::buyer (s/nilable ::uuid))
(s/def ::agent (s/nilable ::uuid))
(s/def ::dealer (s/nilable ::uuid))
(s/def ::total-commission-percent (s/and int? #(>= % 0) #(<= % 100)))
(s/def ::location (s/nilable string?))
(s/def ::on-public-display boolean?)
(s/def ::purchase (s/keys :req-un [::price]
                          :opt-un [::buyer ;; uuid of the contact, nil for unknown buyer (.e.g bought through dealer)
                                   ::donated ::commissioned ::collection ::agent ::dealer ::date
                                   ::total-commission-percent ::location ::on-public-display ::notes]))
(s/def ::purchases (s/* ::purchase))
(s/def ::status #{:sold :private :destroyed :not-for-sale :for-sale})
(s/def ::type #{:architecture :books :collage :computer :digital :drawings
                :film-video :installation :mixed-media :mural :painting :performance
                :photography :prints :sculpture :wall-relief :works-on-paper})
(s/def ::styles #{:abstract :allegorical :architecture :assemblage :autobiographical :biomorphic :cartoonesque :color-field
                  :conceptual :constructed :decorative :didactic :documentary :domestic-family :environmental :erotic :expressionistic
                  :fantasy :feminist :figurative :functional :futuristic :gender-sexuality :geometric :hard-edge :humanist :humorous
                  :illusionistic :impressionistic :interactive :ironic :kinetic :kitsch :landscape :light-reflective :linear :literary
                  :lyrical :minimal :narrative :nudes :optical :painterly :political :popular-imagery :portraits :primitivistic :process-oriented
                  :psychological :religious :representational :romantic :serial :shaped-format :sociological :spiritual :still-life :surreal
                  :symbolic :technological :trompe-loeil :urban})
(s/def ::style (s/* ::styles))
(s/def ::list-price int?)
(s/def ::current-location (s/nilable string?))
(s/def ::sync-with-instagram boolean?)
(s/def ::artwork-item (s/keys :req-un [::uuid ::title ::year ::images ::medium ::created
                                       ::type ::dimensions ::series ::status]
                              :opt-un [::description ::editions ::condition ::style ::associated-documents ::purchases
                                       ::list-price ::current-location ::expanded ::editing ::exhibition-history
                                       ::instagram-media-ref ::sync-with-instagram]))
(s/def ::artwork (s/every-kv ::id ::artwork-item))
(s/def ::artwork-defaults (s/keys :req-un [::type ::dimensions ::medium]))


;; Contacts
(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/nilable (s/and string? (s/or :empty empty?
                                               :valid-email #(re-matches email-regex %)))))
(s/def ::phone (s/nilable string?))
(s/def ::role #{:person :company :dealer :agent :gallery :institution})
(s/def ::address (s/nilable string?))
(s/def ::url (s/nilable string?))
(s/def ::instagram (s/nilable string?))
(s/def ::facebook (s/nilable string?))
(s/def ::contact (s/keys :req-un [::uuid ::name ::role ::created]
                         :opt-un [::email ::phone ::url ::address ::notes ::instagram ::facebook
                                  ::associated-documents]))

;; Press
;; Can be used as input to CV like so:
;; Coupland, Douglas: “Why I Love This Artwork”, Canadian Art Magazine, vol. 12, February 2011, p. 55-60
(s/def ::author-first-name (s/nilable string?))
(s/def ::author-last-name (s/nilable string?))
(s/def ::publication (s/nilable string?))
(s/def ::volume (s/nilable string?))
(s/def ::page-numbers (s/nilable string?))  ;; e.g. 55-60
(s/def ::publication-date (s/nilable ::date))
(s/def ::press-ref (s/keys :req-un [::uuid]
                           :opt-un [::title ::author-first-name ::author-last-name ::publication ::url ::volume
                                    ::publication-date ::page-numbers ::include-in-cv ::notes ::associated-documents ::notes]))

;; Exhibitions
(s/def ::kind #{:solo :group :duo :other})
(s/def ::begin-date ::date)
(s/def ::end-date (s/nilable ::date))
;; TODO: Do we need reception broken out or can that be left as 'notes'?
; (s/def ::begin-time ::time)
; (s/def ::end-time ::time)
; (s/def ::reception (s/keys :req-un [::title ::start-time ::end-time]))
; (s/def ::receptions (s/* ::reception))
(s/def ::exhibition (s/keys :req-un [::uuid ::name ::created ::begin-date]
                            :opt-un [::kind ::location ::url ::end-date ::notes
                                     ::images ::associated-documents ::associated-press]))

;; Document
(s/def ::document (s/keys :req-un [::uuid ::created]
                          :opt-un [::notes ::size ::processing ::filename ::last-modified ::title
                                   ::key ::signed-raw-url ::signed-raw-url-expiration-time]))

;; Public Pages
(s/def ::enabled boolean?)
(s/def ::version ::uuid)
(s/def ::display-name (s/and string? #(not-empty %)))
(s/def ::page-name (s/and string? #(not-empty %)))
(s/def ::public-exhibition-item (s/keys :req-un [::ref ::page-name]
                                        :opt-un [::notes]))
(s/def ::public-exhibitions (s/* ::public-exhibition-item))
;(s/def ::pages (s/nilable (s/keys :req-un [::enabled]
;                                  :opt-un [::display-name ::public-exhibitions ::editing ::description])))

;; This multi-method approach allows us to define the :pages spec differently based on
;; the value of [:pages :enabled].
(defmulti pages-multi :enabled)
(defmethod pages-multi true [_]
  (s/keys :req-un [::enabled ::display-name]
          :opt-un [::processing ::version ::description ::editing ::public-exhibitions]))
(defmethod pages-multi false [_]
  (s/keys :req-un [::enabled]
          :opt-un [::processing ::version ::display-name ::description ::editing ::public-exhibitions]))
(s/def ::pages (s/nilable (s/multi-spec pages-multi :enabled)))

;(s/def :hd/enabled boolean?)
;(s/def :hd/description string?)
;(s/def :hd/display-name string?)

;(defmulti pages-multi :enabled)
;(defmethod pages-multi true [_]
;  (s/keys :req-un [::enabled ::display-name]
;          :opt-un [::description ::editing ::public-exhibitions]))
;(defmethod pages-multi false [_]
;  (s/keys :req-un [::enabled]
;          :opt-un [::display-name ::description ::editing ::public-exhibitions]))
;(s/def ::test-pages (s/nilable (s/multi-spec pages-multi :enabled)))

;; User's artist profile
(s/def ::photo (s/nilable string?))
(s/def ::birth-year (s/and int? #(> % 1000)))
(s/def ::birth-place (s/nilable string?))
(s/def ::currently-resides (s/nilable string?))
(s/def ::val (s/nilable string?))
(s/def ::year-and-string (s/keys :opt-un [::year ::val]))
(s/def ::degrees (s/* ::year-and-string))
(s/def ::awards-and-grants (s/* ::year-and-string))
(s/def ::residencies (s/* ::year-and-string))
(s/def ::lectures-and-talks (s/* ::year-and-string))
(s/def ::collections (s/* string?))
(s/def ::profile (s/nilable (s/keys :opt-un [::uuid ::name ::birth-year ::birth-place ::currently-resides ::email ::phone
                                             ::url ::degrees ::awards-and-grants ::residencies ::lectures-and-talks
                                             ::collections ::photo])))

;; User's account - mostly read-only information
(s/def ::bio (s/nilable string?))
(s/def ::full_name (s/nilable string?))
(s/def ::profile_picture (s/nilable string?))
(s/def ::website (s/nilable string?))
(s/def ::username (s/nilable string?))
(s/def ::is_business boolean?)
(s/def ::instagram-user (s/keys :opt-un [::bio ::is_business ::full_name ::profile_picture ::website ::username]))
(s/def ::account (s/nilable (s/keys :opt-un [::created ::instagram-user ::uuid])))

;; AWS Credentials for access S3
(s/def ::accessKeyId (s/nilable string?))
(s/def ::secretAccessKey (s/nilable string?))
(s/def ::sessionToken (s/nilable string?))

;; Top-level db
(s/def ::authenticated? boolean?)
(s/def ::aws-creds (s/nilable (s/keys :req-un [::accessKeyId ::secretAccessKey ::sessionToken])))
(s/def ::initialized? boolean?)
(s/def ::refresh-aws-creds? boolean?)
(s/def ::do-cognito-logout? boolean?)
(s/def ::access-token (s/nilable string?))
(s/def ::id-token (s/nilable string?))
(s/def ::userinfo (s/nilable (s/keys :opt-un [::sub]))) ;; From Cognito, no need to spec the complete contents of this map
(s/def ::csrf-token (s/nilable string?))
(s/def ::single-item-uuid (s/nilable string?))
(s/def ::search-pattern (s/nilable string?))
(s/def ::view #{:show-login :artwork :contacts :exhibitions :documents :purchases :press :profile :search-results :account :pages :static-page})
(s/def ::static-page (s/nilable #{:privacy-policy}))
(s/def ::display-type #{:contact-sheet :single-item :new-item :list :row :instagram})
(s/def ::contacts (s/every-kv ::id ::contact))
(s/def ::exhibitions (s/every-kv ::id ::exhibition))
(s/def ::documents (s/every-kv ::id ::document))
(s/def ::press (s/every-kv ::id ::press-ref))
(s/def ::instagram-media (s/nilable (s/every-kv ::id ::instagram-media-ref)))
(s/def ::ui-defaults (s/nilable (s/keys :req-un [::artwork-defaults])))
(s/def ::messages (s/keys))  ;; The keys for ::messages are mostly random
(s/def ::db (s/keys :req-un [::view ::display-type ::single-item-uuid ::artwork ::contacts ::exhibitions ::ui-defaults
                             ::press ::profile ::authenticated? ::initialized? ::access-token ::id-token
                             ::userinfo ::search-pattern ::documents ::aws-creds ::refresh-aws-creds? ::account]
                    :opt-un [::messages ::instagram-media ::do-cognito-logout? ::pages]))

(defn spec-for-type
  "Map the 'type' of item to a spec keyword"
  [type]
  (condp = type
    :artwork ::artwork-item
    :exhibitions ::exhibition
    :contacts ::contact
    :documents ::document
    :press ::press-ref
    (keyword "helodali.spec" type)))

(defn invalid?
  "Check the given item (val) against it's spec. The incoming 'type' will be of the top-level keyword
  key into app-db, e.g. :artwork or :contacts. We need to translate this key to the proper spec keyword,
  e.g. :contacts -> ::contact."
  [type val]
  (let [spec (spec-for-type type)]
    (s/explain spec val)
    (s/invalid? (s/conform spec val))))