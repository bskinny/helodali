(ns helodali.cv
  (:require [clj-http.client :as http]
            [cheshire.core :refer [parse-string generate-string]]
            [clojure.java.io :as io]
            [clj-pdf.core :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [helodali.db :as db]
            [helodali.common :refer [reverse-compare]]
            [slingshot.slingshot :refer [throw+ try+]]))

(defn get-year
  "Parse the year from a date string of the form yyyy-MM-dd"
  [date-string]
  (try
    (subs date-string 0 4)
    (catch Exception e (prn (str "Unable to parse year from date string: " date-string)))))

(defn year-val-formatter
  "Produce a string representation of the given [year string-value] vector."
  [year-and-val]
  [:chunk (str (:year year-and-val) " " (:val year-and-val))])

(defn exhibition-formatter
  "Produce a string representation of an exhibition. E.g.
   2019 \"Dimensionality\", Joshua Liner Gallery, New York, NY"
  [exhibition]
  [:chunk (str (get-year (:begin-date exhibition)) " \"" (:name exhibition) "\", " (:location exhibition))])

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
        solo-exhibitions (sort-by :begin-date reverse-compare (filter #(= :solo (:kind %)) exhibitions))
        group-exhibitions (sort-by :begin-date reverse-compare (filter #(= :group (:kind %)) exhibitions))
        selected-exhibitions (sort-by :begin-date reverse-compare (filter #(= :selected (:kind %)) exhibitions))
        other-exhibitions (sort-by :begin-date reverse-compare (filter #(= :other (:kind %)) exhibitions))
        contact-info (cond-> []
                       (:email profile) (conj (:email profile))
                       (:url profile) (conj (:url profile))
                       (:phone profile) (conj (:phone profile)))]
    ;; Use filterv to filter out nil elements created by (when)
    (filterv not-empty
             [{:title         (str (:name profile) " - CV")
               :subject       "Artist CV"}
              (when (:name profile)
                [:phrase (:name profile)])
              (when (not-empty contact-info)
                [:paragraph (str (s/join " | " contact-info))])
              [:spacer]
              (when (not-empty (:degrees profile))
                [[:phrase "EDUCATION"]
                 [:spacer]
                 (into [:list {:symbol ""}] (map #(year-val-formatter %) (:degrees profile)))
                 [:spacer]])
              (when (not-empty solo-exhibitions)
                [[:phrase "SOLO EXHIBITIONS"]
                 [:spacer]
                 (into [:list {:symbol ""}] (map exhibition-formatter solo-exhibitions))
                 [:spacer]])
              (when (not-empty group-exhibitions)
                [[:phrase "GROUP EXHIBITIONS"]
                 [:spacer]
                 (into [:list {:symbol ""}] (map exhibition-formatter group-exhibitions))
                 [:spacer]])
              (when (not-empty selected-exhibitions)
                [[:phrase "SELECTED EXHIBITIONS"]
                 [:spacer]
                 (into [:list {:symbol ""}] (map exhibition-formatter selected-exhibitions))
                 [:spacer]])
              (when (not-empty other-exhibitions)
                [[:phrase "EXHIBITIONS"]
                 [:spacer]
                 (into [:list {:symbol ""}] (map exhibition-formatter other-exhibitions))
                 [:spacer]])
              (when (not-empty (:awards-and-grants profile))
                [[:phrase "AWARDS/GRANTS"]
                 [:spacer]
                 (into [:list {:symbol ""}] (map #(year-val-formatter %) (:awards-and-grants profile)))
                 [:spacer]])
              (when (not-empty (:lectures-and-talks profile))
                [[:phrase "LECTURES/TALKS"]
                 [:spacer]
                 (into [:list {:symbol ""}] (map #(year-val-formatter %) (:lectures-and-talks profile)))
                 [:spacer]])
              (when (not-empty (:collections profile))
                [[:phrase "COLLECTIONS"]
                 [:spacer]
                 (into [:list {:symbol ""}] (map #(year-val-formatter %) (:collections profile)))
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

(defn generate-cv-to-file
  [uref filename]
  (let [out (java.io.ByteArrayOutputStream.)]
    (generate-cv uref out)
    (write-to-file filename (.toByteArray out))))