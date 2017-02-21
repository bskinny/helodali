(ns helodali.spec
  (:require [cljs.spec :as s]))

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
(s/def ::size (s/nilable int?))
(s/def ::space (s/nilable string?))
(s/def ::width (s/nilable int?))
(s/def ::height (s/nilable int?))
(s/def ::density (s/nilable int?))
(s/def ::format (s/nilable string?))
(s/def ::signed-raw-url-expiration-time (s/nilable #(instance? goog.date.Date %)))
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
(s/def ::artwork-item (s/keys :req-un [::uuid ::title ::editing ::year ::images ::medium ::created
                                       ::type ::dimensions ::series ::status]
                              :opt-un [::description ::editions ::condition ::style ::associated-documents ::purchases
                                       ::list-price ::current-location ::expanded ::editing ::exhibition-history
                                       ::instagram-media-ref ::sync-with-instagram?]))
(s/def ::artwork (s/and                              ;; should use the :kind kw to s/map-of (not supported yet)
                   (s/map-of ::id ::artwork-item)       ;; in this map, each todo is keyed by its :id
                   #(instance? PersistentTreeMap %)))    ;; is a sorted-map (not just a map)



;; Contacts
(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/nilable (s/and string? (s/or :empty empty?
                                               :valid-email #(re-matches email-regex %)))))
(s/def ::phone (s/nilable string?))
(s/def ::role #{:person :company :dealer :agent :gallery :institution})
(s/def ::address (s/nilable string?))
(s/def ::url (s/nilable string?))
(s/def ::name (s/nilable string?))
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
(s/def ::begin-date (s/nilable ::date))
(s/def ::end-date (s/nilable ::date))
;; TODO: Do we need reception broken out or can that be left as 'notes'?
; (s/def ::begin-time ::time)
; (s/def ::end-time ::time)
; (s/def ::reception (s/keys :req-un [::title ::start-time ::end-time]))
; (s/def ::receptions (s/* ::reception))
(s/def ::exhibition (s/keys :req-un [::uuid ::name ::created]
                            :opt-un [::kind ::location ::url ::begin-date ::end-date ::notes
                                     ::images ::associated-documents ::associated-press]))

;; Document
(s/def ::document (s/keys :req-un [::uuid ::created]
                          :opt-un [::notes ::size ::processing ::filename ::last-modified ::title
                                   ::key ::signed-raw-url ::signed-raw-url-expiration-time]))

;; User's artist profile
(s/def ::photo (s/nilable string?))
(s/def ::birth-year int?)
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

;; Top-level db
(s/def ::authenticated? boolean?)
(s/def ::initialized? boolean?)
(s/def ::access-token (s/nilable string?))
(s/def ::id-token (s/nilable string?))
(s/def ::delegation-token (s/nilable (s/keys :opt-un []))) ;; From Auth0, no need to spec the contents of this map
(s/def ::delegation-token-expiration (s/nilable ::time))
(s/def ::delegation-token-retrieval-underway boolean?)
(s/def ::userinfo (s/nilable (s/keys :opt-un [::sub]))) ;; From Auth0, no need to spec the complete contents of this map
(s/def ::csrf-token (s/nilable string?))
(s/def ::single-item-uuid (s/nilable string?))
(s/def ::search-pattern (s/nilable string?))
(s/def ::view #{:show-login :artwork :contacts :exhibitions :documents :purchases :press :profile :search-results :account :static-page})
(s/def ::static-page (s/nilable #{:privacy-policy}))
(s/def ::display-type #{:contact-sheet :single-item :new-item :list :row :instagram :large-contact-sheet})
(s/def ::contacts (s/and
                     (s/map-of ::id ::contact)
                     #(instance? PersistentTreeMap %)))
(s/def ::exhibitions (s/and
                        (s/map-of ::id ::exhibition)
                        #(instance? PersistentTreeMap %)))
(s/def ::documents (s/and
                        (s/map-of ::id ::document)
                        #(instance? PersistentTreeMap %)))
(s/def ::press (s/and
                  (s/map-of ::id ::press-ref)
                  #(instance? PersistentTreeMap %)))
(s/def ::instagram-media (s/nilable (s/and
                                       (s/map-of ::id ::instagram-media-ref)
                                       #(instance? PersistentTreeMap %))))
(s/def ::messages (s/keys :opt-un [:form-error]))  ;; The keys for ::messages are mostly random
(s/def ::db (s/keys :req-un [::view ::display-type ::single-item-uuid ::artwork ::contacts ::exhibitions
                             ::press ::profile ::authenticated? ::initialized? ::access-token ::id-token
                             ::delegation-token ::delegation-token-expiration ::delegation-token-retrieval-underway
                             ::userinfo ::search-pattern ::documents]
                    :opt-un [::messages ::instagram-media]))
