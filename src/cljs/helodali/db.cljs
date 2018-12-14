(ns helodali.db
  (:require [cljs-time.core :refer [now year days plus date-time]]
            [helodali.misc :refer [into-sorted-map generate-uuid]]))

(defn default-year-val-map
  []
  {:year (year (now))
   :val nil})

;; The UI Defaults for some of the editable fields (useful when creating multiple items)
(def ui-defaults {:artwork-defaults {:type :mixed-media :dimensions nil :medium nil}})

(defn default-exhibition
  []
  {:uuid (generate-uuid)
   :created (now)
   :name nil ;; Name or Title of exhibition
   :kind :solo ;; can be :solo :group :duo or :other
   :location nil  ;; The gallery or location
   :url nil ;; optional link to gallery or exhibition website
   :notes nil ;; A user provided paragraph
   :begin-date (now) ;; Today's date
   :end-date (plus (now) (days 7)) ;; Next week's date
   :include-in-cv false
   :associated-documents #{}
   :associated-press #{}
   ; :associated-press #{uuid1 uuid2}
   :images []}) ;; One or more images

(defn default-contact
  []
  {:uuid (generate-uuid)
   :created (now)
   :name nil ;; Can be a person, gallery or institution
   :email nil
   :phone nil
   :url nil
   :address nil
   :instagram nil
   :facebook nil
   :role :person ;; Can also be :agent :company :dealer :gallery :institution
   :notes nil
   :associated-documents #{}
   :editing false
   :expanded false})

(defn default-expense
  []
  {:uuid (generate-uuid)
   :created (now)
   :date (now)
   :expense-type :materials
   :notes nil
   :price 0
   :editing false
   :expanded false})

(defn default-document
  []
  {:uuid (generate-uuid)
   :title nil
   :key nil
   :signed-raw-url nil
   :signed-raw-url-expiration-time nil
   :processing false
   :created (now)
   :filename nil
   :size 0
   :notes nil
   :editing false
   :expanded false})

(defn default-press
  []
  {:uuid (generate-uuid)
   :created (now)
   :title nil
   :author-first-name nil
   :author-last-name nil
   :publication nil
   :url nil
   :volume nil
   :publication-date nil
   :page-numbers nil
   :include-in-cv true
   :associated-documents #{}
   :notes nil
   :editing false})

(defn default-exhibition-history
  "Return an exhibition-history map with defaults"
  []
  {:ref nil
   :images []
   :notes nil})

(defn default-public-exhibition
  "Return an public-exhibitions map"
  []
  {:ref nil
   :statement nil
   :page-name nil})

(defn default-purchase
  "Return a purchase with date popluated with today"
  []
  {:buyer nil
   :date (now)
   :donated false
   :commissioned false
   :price 0
   :agent nil
   :dealer nil
   :total-commission-percent 0
   :location nil
   :on-public-display false
   :notes nil})

(defn default-artwork
  "Define an empty artwork item with default field values provided in the 'defaults' map."
  []
  {:uuid (generate-uuid)
   :uref nil ;; The user's uuid, part of the DB primary key
   :created (now) ;; time this item was created in helodali
   :title nil
   :series false ;; Whether the piece is singular or has editions (photo prints) or incarnations (installations)
   :images [] ;; One or more high resolution images (optional)
   :style #{} ;; Enumeration of styles taken from wpadc.org. E.g. :abstract :assemblage. Full list at bottom.
   :type :mixed-media ;; Single-valued enumeration describing category of piece, such as painting, sculpture. See 'media' map defined at bottom.
   :year (year (now)) ;; year piece was created
   :medium nil ;; free-form and provided by user. Examples include "oil on panel"
   :dimensions nil ;; User provided. Can be "h x w x d inches" or "variable" or "21 minutes"
   :editions 0 ;; Useful for prints
   :status :for-sale ;; Can be :private :destroyed :not-for-sale :sold :for-sale
   :condition nil ;; User provided string
   :description nil ;; User provided paragraph
   :expenses 0 ;; A number representing cost. No units are attached

   ;; The piece can be exhibited multiple times
   :exhibition-history []
   ; :exhibition-history [{:ref nil ;; Reference to the exhibition
   ;                       :notes nil
   ;                       :images []}]

   ;; A 'series' piece can have multiple buyers
   :purchases []
   ; :purchases [{:buyer nil ;; <uuid> Used to link to a Contact in the database
   ;              :date nil ;; Down to the day clj(s)-time LocalDate instance
   ;              :donated false
   ;              :commissioned false ;; Whether this piece was commissioned by the buyer
   ;              :collection false
   ;              :price 0 ;; float valued
   ;              :agent nil ;; <uuid> If an art consultant is involved in the sale
   ;              :dealer nil ;; <uuid> The dealer or gallery taking commission on the sale (splitting with agent if one involved)
   ;              :total-commission-percent 0 ;; The total commission taken by agent and dealer
   ;              :location nil ;; Optional city name or address where art is located, possibly on public display
   ;              :on-public-display false
   ;              :notes nil}]
   :list-price 0
   :current-location nil ; If not sold, where the item is currently located: This could be a person's name or "Studio", whatever the user decides.

   ;; Social Media
   :instagram-media-ref nil
   :sync-with-instagram false

   ; :facebook {:posted "<date>" :description nil :comments [{:user "@username" :comment nil}] :likes 0}

   ;; Square
   ; :square {:category :artwork}

   ;; The following are housekeeping attributes for the web client
   :expanded false
   :editing false})


(def default-db
  {:artwork (sorted-map)
   :refresh-aws-creds? false
   :documents (sorted-map)
   :profile {}
   :account {}
   :exhibitions (sorted-map)
   :contacts (sorted-map)
   :expenses (sorted-map)
   :press (sorted-map)
   :instagram-media nil ;; This should be nil-valued at time of login
   :ui-defaults ui-defaults
   :view :artwork
   :static-page nil
   :display-type :contact-sheet ;; Can be :new-item :contact-sheet :row :list :single-item :instagram
   :single-item-uuid nil ;; Used to defined which entity is being viewed in detail
   :messages {}
   :search-pattern nil
   :sit-and-spin false ;; A directive to show the spinner on the main page
   :initialized? false
   :authenticated? false
   :aws-creds nil
   :access-token nil
   :id-token nil
   :do-cognito-logout? false
   :aws-s3 nil; ;; Accesses multiple S3 buckets
   :csrf-token nil
   :userinfo nil ;; The userinfo map returned by Cognito
   :pages {:enabled false :editing false :public-exhibitions []}
   :sort-keys {:artwork [:year false]  ;; true/false for forward/reverse sorting
               :contacts [:name true]
               :expenses [:date false]
               :referred-artwork [:year false]
               :exhibitions [:name true]
               :documents [:title true]
               :purchases [:date false]
               :press [:publication-date false]
               :search-results [:item-type false]}})

(defn defaults-for-type
  [db type]
  (let [defaults (condp = type
                    :artwork (default-artwork)
                    :documents (default-document)
                    :exhibitions (default-exhibition)
                    :contacts (default-contact)
                    :expenses (default-expense)
                    :press (default-press)
                    {})]
     (assoc defaults :uuid (generate-uuid))))

(defn ui-defaults-for-type
  [db type]
  (condp = type
    :artwork (get-in db [:ui-defaults :artwork-defaults])
    {}))

(defn default-view-for-type
  [type]
  (condp = type
     :artwork :contact-sheet
     :list))

