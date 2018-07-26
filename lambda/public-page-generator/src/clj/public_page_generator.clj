(ns public-page-generator
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.data.json :as json]
            [java-time :as jt]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [amazonica.aws.dynamodbv2 :as ddb]
            [amazonica.aws.s3 :as aws3]
            [amazonica.aws.sns :as sns]
            [clojure.pprint :refer [pprint]]))

;; These environment variables are only necessary when running the app locally
;; or outside AWS. Within AWS, we assign an IAM role to the ElasticBeanstalk
;; instance housing the application.
(def co {:access-key (or (System/getenv "AWS_ACCESS_KEY")
                         (System/getProperty "AWS_ACCESS_KEY"))
         :secret-key (or (System/getenv "AWS_SECRET_KEY")
                         (System/getProperty "AWS_SECRET_KEY"))
         :endpoint   (or (System/getenv "AWS_DYNAMODB_ENDPOINT")
                         (System/getProperty "AWS_DYNAMODB_ENDPOINT"))})

(def pages-bucket "helodali-public-pages")
(def index-template (io/resource "index-template.html"))
(def exhibition-template (io/resource "exhibition-template.html"))

(defn delete-pages
  [prefix]
  ;; DO NOT INVOKE delete-objects WITH AN EMPTY STRING OR NIL
  (when (not (empty? prefix))
    (let [object-maps (:object-summaries (aws3/list-objects co :bucket-name pages-bucket :prefix prefix))
          ;; object-maps is a list of maps, we want a list of keys
          object-keys (reduce (fn [l object-map] (conj l (get object-map :key))) [] object-maps)]
      (when (not (empty? object-keys))
        ;; TODO: Handle edge case when more than 1000 objects need to be deleted
        (aws3/delete-objects co :bucket-name pages-bucket :quiet true :keys object-keys)))))

(defn is-in-exhibition-history?
  "Given artwork item, look in exhibition-history for given exhibition-uuid"
  [exhibition-uuid artwork-item]
  (let [exhibition-history (:exhibition-history artwork-item)]
    (not (empty? (filter #(= (:ref %) exhibition-uuid) exhibition-history)))))

(defn artwork-url
  "Generate the public-pages bucket url for the image represented by artwork-item"
  [uref artwork-item]
  (str uref "/images/" (:uuid artwork-item) "/" (get-in artwork-item [:images 0 :filename])))

(defn create-artwork-div
  [uref artwork-item]
  (str "\n"
       "       <div style=\"align-items: center; flex-flow: column nowrap; flex: 0 0 auto; justify-content: center; width: 240px; height: 100%; margin-bottom: 20px\">\n"
       "         <div style=\"flex-flow: inherit; flex: 0 0 auto; max-width: 240px; max-height: 240px;\">\n"
       "           <img src=\"https://" pages-bucket ".s3.amazonaws.com/" (artwork-url uref artwork-item) "\" class=\"fit-cover\" width=\"240px\" height=\"240px\">\n"
       "         </div>\n"
       "         <div style=\"flex: 0 0 auto; height: 8px;\"></div>\n"
       "         <div style=\"flex-flow: column nowrap; flex: 0 0 auto; margin-left: 4px; margin-right: 4px; justify-content: flex-start; align-items: flex-start;\">\n"
       "           <span class=\"hd-caption\">" (:title artwork-item) "</span>\n"
       "           <div style=\"flex: 0 0 auto; height: 2px;\"></div>\n"
       "           <span class=\"hd-subcaption\">" (:year artwork-item) "</span>\n"
       "         </div>\n"
       "       </div>"))

(defn create-exhibition-page
  "Given the user uref, the public-exhibitions from the :pages tables, the list of all exhibition items
   in the db, the list of all artwork in the db, and lastly the uuid of the exhibition to process, create
   a public page representing the exhibition. The public-exhibitions map is referenced for notes or other
   public information.

   Create or replace the exhibition page under <bucket-name>/uref/exhibition-name.html"
  [uref public-exhibitions exhibitions artwork exhibition-uuid]
  (let [template (slurp exhibition-template)
        ;; TODO: Use :page-name defined in public-exhibition to name the html page
        public-exhibition (get public-exhibitions exhibition-uuid)
        exhibition (first (filter #(= (:uuid %) exhibition-uuid) exhibitions))
        title (or (:name exhibition) "")
        notes (or (:notes public-exhibition) "")
        exhibition-artwork (filter (partial is-in-exhibition-history? exhibition-uuid) artwork)
        artwork-html (s/join (map (partial create-artwork-div uref) exhibition-artwork))
        html (-> template
                 (s/replace "{{TITLE}}" title)
                 (s/replace "{{NOTES}}" notes)
                 (s/replace "{{ARTWORK}}" artwork-html))]
    (pprint (str "ARTWORK HTML: " artwork-html))
    (doall (for [artwork-item exhibition-artwork]
             ;; Copy artwork to bucket to uref/images/artwork-uuid/filename
             (aws3/copy-object co :source-bucket-name "helodali-images"
                               :destination-bucket-name pages-bucket
                               :source-key (get-in artwork-item [:images 0 :key])
                               :access-control-list {:grant-permission ["AllUsers" "Read"]}
                               :destination-key (artwork-url uref artwork-item))))
    (sns/publish co :topic-arn "arn:aws:sns:us-east-1:676820690883:my-topic"
                    :subject "test"
                    :message (str))
    (aws3/put-object co :bucket-name pages-bucket
                     :key (str uref "/" exhibition-uuid ".html")
                     :input-stream (io/input-stream (.getBytes html))
                     :access-control-list {:grant-permission ["AllUsers" "Read"]}
                     :metadata {:content-length (count html)
                                :content-type "text/html"})))

(defn sort-by-end-date
  "Used as a comparator to sort, comparing two datetime key values and
   falling back to :created time of the item. If the input maps do not
   have :created, use sort-by-datetime-only instead"
  [k reverse? m1 m2]
  ; (pprint (str "Sort-by-datetime: " k " of " m1 " " m2))
  (let [before-after (if reverse? jt/after? jt/before?)]
    (if (or (nil? m1) (nil? m2))
      (compare m1 m2)
      (if (or (nil? (get m1 k)) (nil? (get m2 k)) (= (get m1 k) (get m2 k)))
        (if (or (nil? (get m1 :created)) (nil? (get m2 :created)))
          (compare (get m1 :created) (get m2 :created))
          (before-after (get m1 :created) (get m2 :created)))
        (before-after (get m1 k) (get m2 k))))))

(defn create-exhibition-div
  [public-exhibitions exhibition]
  (str "\n<div class=\"hd-exhibition-title\"><a href=\"" (:page-name public-exhibitions) "\" data-ix=\"body-fade-out-on-click\">"
          (:title exhibition) "</a></div>"))

(defn create-index-page
  "Build the index page which lists the exhibitions which can be browsed. The public-exhibitions arg defines the
   :uuid and :notes defined on the piblic-pages item and the exhibitions arg is a list of :exhibitions items
   pulled from the db."
  [page-config public-exhibitions exhibitions]
  (let [uref (:uuid page-config)
        template (slurp index-template)
        public-exhibitions-set (set (keys public-exhibitions))
        sorted-exhibitions (sort sort-by-end-date (filter #(contains? public-exhibitions-set (:uuid %)) exhibitions))
        exhibitions-html (s/join (map (partial create-exhibition-div public-exhibitions) sorted-exhibitions))]))

(defn publish-site
  [pages-profile]
  (pprint (str "Publishing site for user " (:uuid pages-profile)))
  (let [uref (:uuid pages-profile)
        ;; Fetch the pages item from DynamoDb even though we have an image from the Lambda invocation.
        page-config (:item (ddb/get-item co :table-name :pages :key {:uuid (:uuid pages-profile)}))
        ;; Create of map representation of the public exhibitions keyed by :uuid values
        public-exhibitions (reduce (fn [m e] (assoc m (get e :ref) (dissoc e :ref))) {} (:public-exhibitions page-config))
        ;; Retrieve all artwork items
        artwork (:items (ddb/query co :table-name :artwork :key-conditions {:uref {:attribute-value-list [uref]
                                                                                   :comparison-operator "EQ"}}))
        ;; Retrieve all exhibition items
        exhibitions (:items (ddb/query co :table-name :exhibitions :key-conditions {:uref {:attribute-value-list [uref]
                                                                                           :comparison-operator "EQ"}}))]
    ;; Delete existing site
    (delete-pages uref)
    ;; Create the individual exhibition pages
    (doall (map (partial create-exhibition-page uref public-exhibitions exhibitions artwork) (keys public-exhibitions)))))

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
             "notes" {"NULL" true}}}
           {"M"
            {"ref" {"S" "2ba1cc60-6b5b-11e8-9573-094f06ce2361"},
             "notes" {"S" "Most recent."}}}]}},
      "OldImage"
        {"uuid" {"S" "718158b0-69a6-11e8-9c73-094f06ce2361"},
         "enabled" {"BOOL" false},
         "public-exhibitions"
         {"L"
          [{"M"
            {"ref" {"S" "2ba1cc60-6b5b-11e8-9573-094f06ce2361"},
             "notes" {"NULL" true}}}]}},
      "SequenceNumber" "81544500000000009444272790",
      "SizeBytes" 280,
      "StreamViewType" "NEW_AND_OLD_IMAGES"},
     "eventSourceARN"
     "arn:aws:dynamodb:us-east-1:128225160927:table/pages/stream/2018-06-22T21:31:07.588"}]})

(defn process-record
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
  (pprint event)
  (map process-record (:records event)))

(defn key->keyword
  "Convert string keys to keywords but do not change the now lower case camelcase to snake-case - too difficult"
  [key-string]
  (-> key-string
      (s/lower-case)
      (keyword)))

(defn -handleRequest [this is os context]
  (let [w (io/writer os)]
    (-> (json/read (io/reader is) :key-fn key->keyword)
        (handle-event)
        (json/write w))
    (.flush w)))