(ns public-page-generator
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.data.json :as json]
            [java-time :as jt]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [amazonica.core :refer [ex->map]]
            [amazonica.aws.dynamodbv2 :as ddb]
            [amazonica.aws.s3 :as aws3]
            [amazonica.aws.sns :as sns]
            [amazonica.aws.cloudfront :as cf]
            [clojure.pprint :refer [pprint]])
  (:import (java.net URLEncoder)))

;; These environment variables are only necessary when running the app locally
;; or outside AWS. Within AWS, we assign an IAM role to the ElasticBeanstalk
;; instance housing the application.
(def co {:access-key (or (System/getenv "AWS_ACCESS_KEY")
                         (System/getProperty "AWS_ACCESS_KEY"))
         :secret-key (or (System/getenv "AWS_SECRET_KEY")
                         (System/getProperty "AWS_SECRET_KEY"))
         :endpoint   (or (System/getenv "AWS_DYNAMODB_ENDPOINT")
                         (System/getProperty "AWS_DYNAMODB_ENDPOINT"))
         :create-ribbon-topic-arn (or (System/getenv "HD_CREATE_RIBBON_TOPIC_ARN")
                                      (System/getProperty "HD_CREATE_RIBBON_TOPIC_ARN"))})

(def pages-bucket "helodali-public-pages")
(def index-template (io/resource "index-template.html"))
(def contact-form-template (io/resource "contact-form-template.html"))
(def exhibition-template (io/resource "exhibition-template.html"))
(def artwork-template (slurp (io/resource "artwork-template.html")))
(def cv-template (io/resource "cv-template.html"))
(def hd-public-css "hd-public.css")
(def Arrows-Left-icon-png "Arrows-Left-icon.png")
(def Arrows-Right-icon-png "Arrows-Right-icon.png")
(def favicon "favicon.ico")

(def THUMBS "thumbs")
(def IMAGES "images")
(def LARGE-IMAGES "large-images")

(defn fix-date
  "Called with a list of maps, converts a date valued kw from string to java-time Instant."
  [kw l]
  (apply vector (map #(if (get % kw)
                        (assoc % kw (jt/local-date "yyyy-MM-dd" (get % kw)))
                        %)
                     l)))

(defn delete-pages
  "Delete all existing content for uref."
  [prefix]
  ;; DO NOT INVOKE delete-objects WITH AN EMPTY STRING OR NIL
  (when (not (empty? prefix))
    (let [object-maps (:object-summaries (aws3/list-objects :bucket-name pages-bucket :prefix prefix))
          ;; object-maps is a list of maps, we want a list of keys
          object-keys (reduce (fn [l object-map] (conj l (get object-map :key))) [] object-maps)]
      (when (not (empty? object-keys))
        ;; TODO: Handle edge case when more than 1000 objects need to be deleted
        (aws3/delete-objects :bucket-name pages-bucket :quiet true :keys object-keys)))))

(defn is-in-exhibition-history?
  "Given artwork item, look in exhibition-history for given exhibition-uuid"
  [exhibition-uuid artwork-item]
  (let [exhibition-history (:exhibition-history artwork-item)]
    (not (empty? (filter #(= (:ref %) exhibition-uuid) exhibition-history)))))

(defn artwork-uri
  [exhibition-page-name image-type item]
  (str exhibition-page-name "/" image-type "/" (:uuid item) "/" (get-in item [:images 0 :filename])))

(defn artwork-key
  "Generate the public-pages bucket url for the image represented by artwork-item"
  [uref exhibition-page-name image-type artwork-item]
  (str uref "/" (artwork-uri exhibition-page-name image-type artwork-item)))

(defn url-encode
  [string]
  (-> string
      (URLEncoder/encode "UTF-8")
      (.replace "+" "%20")))

(defn artwork-details-string
  [item]
  (cond-> (str (:year item))
          (:dimensions item) (str ", " (:dimensions item))
          (:medium item) (str ", " (:medium item))
          (and (not-empty (:status item)) (not= (:status item) (name :for-sale))) (str " - " (name (:status item)))))

(defn create-artwork-div
  [page-name artwork-item]
  (let [img-uri (url-encode (artwork-uri page-name IMAGES artwork-item))
        thumb-uri (url-encode (artwork-uri page-name THUMBS artwork-item))
        artwork-page-uri (str page-name "/" (:uuid artwork-item) ".html")
        dimensions-matched (if (:dimensions artwork-item)
                             (re-matches #"(?i)[^\d]*(\d+)[\"\']?\s*[xXby]+\s*(\d+)[\"\']?\s*(inches|in|feet|ft|cm|meters|m)?" (:dimensions artwork-item)))
        width (if dimensions-matched (get dimensions-matched 1 0) "24")
        height (if dimensions-matched (get dimensions-matched 2 0) "24")]
    (str "\n"
       "               <div style=\"display: flex; align-items: center; flex-flow: column nowrap; margin: 32px\">\n"
       "                   <a href=\"" artwork-page-uri "\"><picture>\n"
       "                      <source media=\"(min-width: 480px)\" srcset=\"" img-uri "\"/>\n"
       "                      <img src=\"" thumb-uri "\"/>\n"
       "                   </picture></a>\n"
       "                  <div style=\"height: 4px;\"></div>\n"
       "                  <div style=\"width: 100%; display: flex; justify-content: space-between;\">\n"
       "                     <div style=\"margin: 4px;\">\n"
       "                        <div style=\"max-width: " (- 360 (Integer/parseInt width) 10) "px;\">" (:title artwork-item) "</div>\n"
       "                        <div style=\"height: 2px;\"></div>\n"
       "                        <div id=\"" (:uuid artwork-item) "\" style=\"visibility: hidden;\" class=\"hd-subcaption\">" (artwork-details-string artwork-item) "</div>\n"
       "                     </div>\n"
         (if dimensions-matched
           (str
             "                     <div style=\"width: " width "px; height: " height "px; margin-right: 4px;\"\n"
             "                          onmouseover=\"document.getElementById('" (:uuid artwork-item) "').style.visibility='visible';\""
             "                          onmouseout=\"document.getElementById('" (:uuid artwork-item) "').style.visibility='hidden';\">\n"
             "                        <svg viewBox=\"0 0 " width " " height "\" xmlns=\"http://www.w3.org/2000/svg\">\n"
             "                           <rect x=\"0\" y=\"0\" width=\"100%\" height=\"100%\" fill=\"none\" stroke=\"black\" /></svg>\n"
             "                     </div>\n")
           (str "                     <div style=\"width: 24px;\"></div>\n"))
       "                  </div>\n"
       "               </div>")))

(defn create-artwork-page
  [context artwork-list idx item]
  (let [list-length (count artwork-list)
        prev-idx (mod (dec idx) list-length)
        next-idx (mod (inc idx) list-length)
        html (-> artwork-template
                 (s/replace "{{PREV}}" (str (:uuid (nth artwork-list prev-idx)) ".html"))
                 (s/replace "{{NEXT}}" (str (:uuid (nth artwork-list next-idx)) ".html"))
                 ;; For {{IMG}}, the template must provide the iamges/, thumbs/, or large-images/ prefix.
                 (s/replace "{{IMG}}" (url-encode (str (:uuid item) "/" (get-in item [:images 0 :filename]))))
                 (s/replace "{{DETAILS}}" (artwork-details-string item))
                 (s/replace "{{TITLE}}" (:title item))
                 (s/replace "{{USER_DISPLAY_NAME}}" (:user-display-name context))
                 (s/replace "{{EXHIBITION_PAGE_NAME}}" (:exhibition-page-name context))
                 (s/replace "{{EXHIBITION_DISPLAY_NAME}}" (:exhibition-title context)))]
    (aws3/put-object :bucket-name pages-bucket
                      :key (str (:uref context) "/" (:exhibition-page-name context) "/" (:uuid item) ".html")
                      :input-stream (io/input-stream (.getBytes html))
                      :access-control-list {:grant-permission ["AllUsers" "Read"]}
                      :metadata {:content-length (count html)
                                 :content-type "text/html"})))

(defn sort-by-keys
  "Used as a comparator to sort, comparing the string-value of keys in 'ks' between
   maps. This function is called recursively, with an empty ks list signifying equality."
  [ks reverse? m1 m2]
  (if (empty? ks)
    0
    (let [asc-desc (if reverse? (partial * -1) identity)
          k (first ks)]
      (if (or (nil? m1) (nil? m2))
        (asc-desc (compare m1 m2))
        (let [result (asc-desc (compare (get m1 k) (get m2 k)))]
          (if (not= 0 result)
            result
            (sort-by-keys (rest ks) reverse? m1 m2)))))))

(defn sort-by-date
  "Used as a comparator to sort, comparing two datetime key values and
   falling back to :created time of the item. If the input maps do not
   have :created, use sort-by-datetime-only instead"
  [k reverse? m1 m2]
  (let [before-after (if reverse? jt/after? jt/before?)]
    (if (or (nil? m1) (nil? m2))
      (compare m1 m2)
      (if (or (nil? (get m1 k)) (nil? (get m2 k)) (= (get m1 k) (get m2 k)))
        (if (or (nil? (get m1 :created)) (nil? (get m2 :created)))
          (compare (get m1 :created) (get m2 :created))
          (before-after (get m1 :created) (get m2 :created)))
        (before-after (get m1 k) (get m2 k))))))

(defn show-description
  "Create the string representation of the form 'YEAR, TITLE, LOCATION"
  [exhibition]
  (let [year (jt/year (:begin-date exhibition))]
    (str "        <div>" year ", <span style=\"font-style: italic;\">" (:name exhibition) "</span>, " (:location exhibition) "</div>\n")))

(defn create-cv-page
  [uref page-config exhibitions]
  (let [template (slurp cv-template)
        included-exhibitions (fix-date :begin-date (filter #(= (:include-in-cv %) true) exhibitions))
        sorted-exhibitions (sort (partial sort-by-date :begin-date true) included-exhibitions)
        solo-shows (filter #(= (:kind %) (name :solo)) sorted-exhibitions)
        duo-shows (filter #(= (:kind %) (name :duo)) sorted-exhibitions)
        group-shows (filter #(= (:kind %) (name :group)) sorted-exhibitions)
        html (-> template
                 (s/replace "{{USER_DISPLAY_NAME}}" (:display-name page-config))
                 (s/replace "{{SOLO_EXHIBITIONS}}" (s/join (map show-description solo-shows)))
                 (s/replace "{{DUO_EXHIBITIONS}}" (s/join (map show-description duo-shows)))
                 (s/replace "{{GROUP_EXHIBITIONS}}" (s/join (map show-description group-shows))))]
    (try
      (aws3/put-object :bucket-name pages-bucket
                       :key (str uref "/cv.html")
                       :input-stream (io/input-stream (.getBytes html))
                       :access-control-list {:grant-permission ["AllUsers" "Read"]}
                       :metadata {:content-length (count html)
                                  :content-type "text/html"})
      (catch Exception e
        (pprint (ex->map e))))))


(defn create-exhibition-page
  "Given the user uref, the public-exhibitions from the :pages tables keyed by exhibition :uuid,
   the list of all exhibition items in the db, the list of all artwork in the db, and lastly the uuid of the
   exhibition to process, create a public page representing the exhibition.
   The public-exhibitions map is referenced for statement, page-name, etc.

   Create or replace the exhibition page under <bucket-name>/uref/<page-name>.html

   This function also copies all exhibition artwork into <bucket-name>/uref/<page-name>/images/"
  [uref page-config public-exhibitions exhibitions artwork exhibition-uuid]
  (let [template (slurp exhibition-template)
        public-exhibition (get public-exhibitions exhibition-uuid)
        page-name (:page-name public-exhibition)
        exhibition (first (filter #(= (:uuid %) exhibition-uuid) exhibitions))
        title (or (:name exhibition) "")
        statement (or (:statement public-exhibition) "")
        exhibition-artwork (filter (partial is-in-exhibition-history? exhibition-uuid) artwork)
        artwork-html (s/join (map (partial create-artwork-div page-name) exhibition-artwork))
        html (-> template
                 (s/replace "{{TITLE}}" title)
                 (s/replace "{{STATEMENT}}" statement)
                 (s/replace "{{USER_DISPLAY_NAME}}" (:display-name page-config))
                 (s/replace "{{ARTWORK}}" artwork-html))]
    (doall (for [artwork-item exhibition-artwork
                 target [THUMBS IMAGES LARGE-IMAGES]]
             ;; Copy artwork to bucket to uref/<page-name>/images/artwork-uuid/filename
             (aws3/copy-object :source-bucket-name (str "helodali-" target)
                               :destination-bucket-name pages-bucket
                               :source-key (get-in artwork-item [:images 0 :key])
                               :access-control-list {:grant-permission ["AllUsers" "Read"]}
                               :destination-key (artwork-key uref page-name target artwork-item))))
    (doall (map-indexed (partial create-artwork-page {:user-display-name (:display-name page-config)
                                                      :uref uref
                                                      :exhibition-page-name page-name
                                                      :exhibition-title title}
                                 exhibition-artwork) exhibition-artwork))
    (aws3/put-object :bucket-name pages-bucket
                     :key (str uref "/" page-name ".html")
                     :input-stream (io/input-stream (.getBytes html))
                     :access-control-list {:grant-permission ["AllUsers" "Read"]}
                     :metadata {:content-length (count html)
                                :content-type "text/html"})))

(defn create-ribbon
  "Invoke the lambda which creates the ribbon image with name <bucket-name>/uref/<page-name>/ribbon.png."
  [uref public-exhibitions exhibition-uuid]
  (let [public-exhibition (get public-exhibitions exhibition-uuid)
        page-name (:page-name public-exhibition)]
    (sns/publish :topic-arn (:create-ribbon-topic-arn co)
                 :subject "make-ribbon"
                 :message (str uref "/" page-name "/"))))

(defn create-exhibition-div
  "Create the list element in the index.html representing an exhibition. The public-exhibitions
  argument is a map of public-exhibitions keyed by exhibition :uuid. The exhibitions argument
  is a list of all exhibitions defined in the db for the user. The exhibition-uuid is exhibition
  we are working on."
  [public-exhibitions exhibition odd-row?]
  (let [public-exhibition (get public-exhibitions (:uuid exhibition))
        page-name (:page-name public-exhibition)]

    (str "\n"
      "               <div style=\"display: flex; width: 100%; margin-bottom: 16px\">\n"
         (when (not odd-row?) "                 <div style=\"flex: 1 auto\"></div>\n")
      "                 <div style=\"flex: 1 0px; display: flex; flex-flow: column nowrap; align-items: "
         (if odd-row? "flex-end" "flex-start") ";\">\n"
      "                   <div class=\"hd-title-2\"><a href=\"" page-name ".html\">" (:name exhibition) "</a></div>\n"
      "                   <div class=\"ribbon\"><a href=\"" page-name ".html\"><img src=\"" page-name "/ribbon.jpg\" alt=\"" page-name "\"></a></div>\n"
      "                 </div>\n"
         (when odd-row? "                 <div style=\"flex: 1 auto\"></div>\n")
      "               </div>\n")))

(defn create-contact-form-page
  "Build the simple page which contains the contact form."
  [uref page-config]
  (let [uref (:uuid page-config)
        template (slurp contact-form-template)
        html (-> template
                 (s/replace "{{USER_DISPLAY_NAME}}" (:display-name page-config))
                 (s/replace "{{USER_UUID}}" uref)
                 (s/replace "{{USER_DESCRIPTION}}" (:description page-config)))]
    (try
      (aws3/put-object :bucket-name pages-bucket
                       :key (str uref "/contact.html")
                       :input-stream (io/input-stream (.getBytes html))
                       :access-control-list {:grant-permission ["AllUsers" "Read"]}
                       :metadata {:content-length (count html)
                                  :content-type "text/html"})
      (catch Exception e
        (pprint (ex->map e))))))

(defn create-index-page
  "Build the index page which lists the exhibitions which can be browsed. The public-exhibitions is a list keyed by exhibition
   :uuid containing :page-name, :statement, etc. defined on the piblic-pages and the exhibitions arg is a list of :exhibitions items
   pulled from the db."
  [page-config public-exhibitions exhibitions]
  (let [uref (:uuid page-config)
        template (slurp index-template)
        public-exhibitions-keyset (set (keys public-exhibitions))
        associated-exhibitions (fix-date :begin-date (filter #(contains? public-exhibitions-keyset (:uuid %)) exhibitions))
        sorted-exhibitions (sort (partial sort-by-date :begin-date true) associated-exhibitions)
        exhibitions-html (s/join (map (partial create-exhibition-div public-exhibitions) sorted-exhibitions (cycle [true false])))
        html (-> template
                 (s/replace "{{USER_DISPLAY_NAME}}" (:display-name page-config))
                 (s/replace "{{USER_DESCRIPTION}}" (:description page-config))
                 (s/replace "{{EXHIBITIONS}}" exhibitions-html))]
    (try
      (aws3/put-object :bucket-name pages-bucket
                       :key (str uref "/index.html")
                       :input-stream (io/input-stream (.getBytes html))
                       :access-control-list {:grant-permission ["AllUsers" "Read"]}
                       :metadata {:content-length (count html)
                                  :content-type "text/html"})
      (catch Exception e
        (pprint (ex->map e))))))

(defn copy-resources
  "Copy asset files from the bucket's root level to user's path."
  [uref]
  (doall (for [resource-object [(str "css/" hd-public-css)
                                (str "assets/" Arrows-Left-icon-png)
                                (str "assets/" Arrows-Right-icon-png)
                                (str "assets/" favicon)]]
           (aws3/copy-object :source-bucket-name pages-bucket :destination-bucket-name pages-bucket
                             :source-key resource-object
                             :destination-key (str uref "/" resource-object)
                             :access-control-list {:grant-permission ["AllUsers" "Read"]}))))

(defn invalidate-web-distribution
  [uref distribution-id]
  (try
    (cf/create-invalidation {:distribution-id distribution-id
                             :invalidation-batch {:paths {:items ["/*"]
                                                          :quantity 1}
                                                  :caller-reference (jt/to-millis-from-epoch (jt/instant))}})
    (catch Exception e
      (pprint (ex->map e)))))

(defn sort-artwork
  "Fetch all the user's artwork and sort according to :year and then :created"
  [uref]
  (->> (:items (ddb/query :table-name :artwork :key-conditions {:uref {:attribute-value-list [uref]
                                                                       :comparison-operator "EQ"}}))
       (sort (partial sort-by-keys [:year :created] true))))

(defn publish-site
  [pages-profile]
  (pprint (str "Publishing site for user " (:s (:uuid pages-profile))))
  (let [uref (:s (:uuid pages-profile))
        ;; Fetch the pages item from DynamoDb even though we have an image from the Lambda invocation.
        page-config (:item (ddb/get-item :table-name :pages :key {:uuid uref}))
        ;; Create of map representation of the public exhibitions keyed by :uuid values
        public-exhibitions (reduce (fn [m e] (assoc m (get e :ref) (dissoc e :ref))) {} (:public-exhibitions page-config))
        ;; Retrieve all artwork items
        artwork (sort-artwork uref)
        ;; Retrieve all exhibition items
        exhibitions (:items (ddb/query :table-name :exhibitions :key-conditions {:uref {:attribute-value-list [uref]
                                                                                        :comparison-operator "EQ"}}))]
    ;; Delete existing site
    (delete-pages uref)
    ;; Copy in css
    (copy-resources uref)
    ;; Create contact form
    (create-contact-form-page uref page-config)
    ;; Create CV page+
    (create-cv-page uref page-config exhibitions)
    ;; Create the individual exhibition pages
    (doall (map (partial create-exhibition-page uref page-config public-exhibitions exhibitions artwork) (keys public-exhibitions)))
    ;; This next step could have also been performed within the the previous function but an occasional s3 race condition surfaces (missing Key)
    (doall (map (partial create-ribbon uref public-exhibitions) (keys public-exhibitions)))
    (create-index-page page-config public-exhibitions exhibitions)
    ; If Cloudront is enabled for this site, invalidate the files in the Cloudfront cache
    (if (:cloudfront-distribution-id page-config)
      (invalidate-web-distribution uref (:cloudfront-distribution-id page-config)))))

(defn remove-site
  [profile]
  (pprint (str "Removing site for user " (:uuid profile)))
  (delete-pages (:uuid profile)))

(comment "The event passed to us looks like the following. We convert keys to lowercase keywords."
  {"Records"
   [{"eventID" "d4729e4d4986bad83965c3829db49bb3",
     "eventName" "MODIFY",
     "eventVersion" "1.1",
     "eventSource" "aws:dynamodb",
     "awsRegion" "us-east-1",
     "dynamodb"
     {"ApproximateCreationDateTime" 1.53088938E9,
      "Keys" {"uuid" {"S" "718158b0-69a6-11e8-9c73-094f06ce2361"}},
      "NewImage"
        {"uuid" {"S" "718158b0-69a6-11e8-9c73-094f06ce2361"},
         "enabled" {"BOOL" true},
         "public-exhibitions"
         {"L"
          [{"M"
            {"ref" {"S" "ddf19da6-b7d3-4c1c-89c7-7bd663d555c0"},
             "statement" {"NULL" true}}}
           {"M"
            {"ref" {"S" "2ba1cc60-6b5b-11e8-9573-094f06ce2361"},
             "statement" {"S" "Most recent."}}}]}},
      "OldImage"
        {"uuid" {"S" "718158b0-69a6-11e8-9c73-094f06ce2361"},
         "enabled" {"BOOL" false},
         "public-exhibitions"
         {"L"
          [{"M"
            {"ref" {"S" "2ba1cc60-6b5b-11e8-9573-094f06ce2361"},
             "statement" {"NULL" true}}}]}},
      "SequenceNumber" "81544500000000009444272790",
      "SizeBytes" 280,
      "StreamViewType" "NEW_AND_OLD_IMAGES"},
     "eventSourceARN"
     "arn:aws:dynamodb:us-east-1:128225160927:table/pages/stream/2018-06-22T21:31:07.588"}]})

(defn process-record
  "Look at the event's NewImage and OldImage values and:
    - Publish the site if enabled in NewImage and disabled in OldImage
    - Publish the site if enabled in NewImage and :version differs between OldImage and NewImage
    - Delete the site if disabled in NewImage and enabled in OldImage"
  [rec]
  (let [new-image (get-in rec [:dynamodb :newimage])
        old-image (get-in rec [:dynamodb :oldimage])]
    (if (or (not= (:enabled new-image) (:enabled old-image))
            (not= (:version new-image) (:version old-image)))
      (if (get-in new-image [:enabled :bool])
        (publish-site new-image)
        (remove-site new-image)))))

(defn handle-event [event]
  "Given a list of updates to the :pages table, look for changes to the :enabled field and
  publish/remove the user's public pages accordingly. In addition, look for a changed value of
  :version (uuid-valued) which implies the 'publish now' request has been made."
  ;; TODO: What processes the return value?
  (doall (map process-record (:records event))))

(defn key->keyword
  "Convert string keys to keywords but do not change the now lower case camelcase to snake-case - too difficult"
  [key-string]
  (-> key-string
      (s/lower-case)
      (keyword)))

(defn -handleRequest [this is os context]
  (let [w (io/writer os)]
    (-> (json/read (io/reader is) :key-fn key->keyword)
        (handle-event))
        ;; In case there is a need to write a response, use (json/write w))
    (.flush w)))