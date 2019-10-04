(ns helodali.cv
  (:require [clj-http.client :as http]
            [cheshire.core :refer [parse-string generate-string]]
            [clojure.java.io :as io]
            [clj-pdf.core :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [helodali.db :as db]
            [slingshot.slingshot :refer [throw+ try+]]))

(defn get-year
  "Parse the year from a date string of the form yyyy-MM-dd"
  [date-string]
  (try
    (subs date-string 0 4)
    (catch Exception e (prn (str "Unable to parse year from date string: " date-string)))))

;; TODO: This is in cljc
(def reverse-compare #(compare %2 %1))

(defn generate-pdf
  "Generate the vector representation to feed clj-pdf. The user's associated item
   in the :profiles table has most of the information we need with the only remaining
   data coming from :exhibitions items which have include-in-cv set to true.

   For solo and group show separation, we will consider any exhibition in the database
   which is not :solo to be a group show (this means :duo, :group, and :other).

   Here are the relevant fields of the profile item:
   :name :birth-year :birth-place :currently-resides :email :phone :url
   :degrees :awards-and-grants :residencies :lectures-and-talks :collections"
  [uref]
  (let [profile (db/get-item-by-uref :profiles uref)
        ;; Get solo and group exhibitions with include-in-cv set to true
        exhibitions (filter :include-in-cv (db/query-by-uref :exhibitions uref))
        solo (sort-by :begin-date reverse-compare (filter #(= :solo (:kind %)) exhibitions))
        group (sort-by :begin-date reverse-compare (filter #(not= :solo (:kind %)) exhibitions))]
    (pprint exhibitions)
    ;; Use filterv to filter out nil elements created by (when)
    (filterv not-empty
             [{:title         (str (:name profile) " - CV")
               :subject       "Artist CV"}
              []
              (when (not-empty (:degrees profile))
                [[:phrase "EDUCATION"]
                 [:spacer]
                 (into [:list {:symbol ""}]
                       (map (fn [degree] [:chunk (str (:year degree) " " (:val degree))]) (:degrees profile)))
                 [:spacer]])
              (when (not-empty solo)
                [[:phrase "SOLO EXHIBITIONS"]
                 [:spacer]
                 (into [:list {:symbol ""}]
                       (map (fn [exhibition] [:chunk (str (get-year (:begin-date exhibition)) " " (:name exhibition))]) solo))
                 [:spacer]])])))

(defn generate-cv
  [uref ^java.io.ByteArrayOutputStream out]
  (pdf
    (generate-pdf uref)
    out))

(defn write-to-file
  [filename bytes]
  (with-open [os (io/output-stream filename)]
    (.write os bytes)))

