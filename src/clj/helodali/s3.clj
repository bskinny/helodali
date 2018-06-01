(ns helodali.s3
  (:require [amazonica.aws.s3 :as aws3]
            [clojure.pprint :refer [pprint]]
            [slingshot.slingshot :refer [throw+ try+]]))

;; These environment variables are only necessary when running the app locally
;; or outside AWS. Within AWS, we assign an IAM role to the ElasticBeanstalk
;; instance housing the application.
(def co
  {:access-key (or (System/getenv "AWS_ACCESS_KEY")
                   (System/getProperty "AWS_ACCESS_KEY"))
   :secret-key (or (System/getenv "AWS_SECRET_KEY")
                   (System/getProperty "AWS_SECRET_KEY"))})


(defn list-objects
  "Return the first batch of matches (up to 1000 keys). See the loop/recur approach in
  delete-objects, below, if iterating through batches is desirable.

  Returns a map which resembles the following (if more than max-keys is found):
      :truncated? true
      :bucket-name \"helodali-raw-images\"
      :max-keys 1000
      :object-summaries [{:key \"2018-03-22-21-58-21-897AF78F0BEC8E30\"
                          :size 437
                          :bucket-name \"helodali-raw-images\"
                          :etag \"4f35ad55ddc8738104ee7beb5c29baeb\"
                          :storage-class \"STANDARD\"
                          :last-modified #object[org.joda.time.DateTime
                                                 0x6677a3d
                                                 \"2018-03-22T17:58:22.000-04:00\"]}
                         ...
      :common-prefixes []
      :next-continuation-token \"15EwLkXctI7FgGF...hIwhn0SE9Pq4FS7AO4\"
      :key-count 1000
      :prefix \"2018\"
  "
  ([bucket]
   (list-objects bucket nil))
  ([bucket prefix]
   (aws3/list-objects-v2 co
     {:bucket-name bucket
      :prefix prefix})))  ; optional

(defn put-object
  [bucket key input-stream]
  (aws3/put-object co bucket key input-stream {}))

(defn copy-object
  [from-bucket from-key to-bucket to-key]
  (aws3/copy-object co from-bucket from-key to-bucket to-key))

(defn delete-objects
  "Delete objects represented by vector of keys"
  [bucket keys]
  (aws3/delete-objects co {:bucket-name bucket :quiet? true :keys keys}))


(defn delete-objects-by-prefix
  "Delete the objects in a S3 bucket for all matching prefix. Be extra careful as a nil prefix will
   delete the entire bucket. Hence the nil prefix check."
  [bucket prefix]
  (if (or (empty? prefix) (nil? prefix))
    (throw+ {:type :gaurding-delete-objects-without-prefix})
    (loop [options {:bucket-name bucket
                    :prefix prefix}]
      (let [results (aws3/list-objects-v2 co options)]
        (when (and (:object-summaries results) (not (empty? (:object-summaries results))))
          (aws3/delete-objects co {:bucket-name bucket
                                   :quiet? true
                                   :keys (vec (map #(get %1 :key) (:object-summaries results)))})
          (if (:truncated? results)
            (recur (assoc options :next-continuation-token (:next-continuation-token results)))))))))