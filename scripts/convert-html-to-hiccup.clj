(ns scripts.convert-html-to-hiccup
  (:require [hickory.core :as h]
            [hickory.select :as s]
            [hickory.convert :as convert]
            [clojure.string :as string]
            [clojure.pprint :refer [pprint]]))

(defn- convert-to-re-com
  "convert given hiccup vector to a re-com component."
  [in]
  (if (vector? in)
    (let [tag (first in)
          arg (if (map? (second in))
                (first (rest (rest in)))
                (first (rest in)))
          arg (if (empty? arg) arg (clojure.string/trim arg))]
      (condp = tag
        :h1 [:title :level :level1 :label arg]
        :h2 [:title :level :level2 :label arg]
        :h3 [:title :level :level3 :label arg]
        :h4 [:title :level :level4 :label arg]
        :b [:label :class "bold" :label arg]
        :br [:gap :size "14px"]
        [tag arg]))
    [:label :label in]))

(defn convert-single-div
  "Convert the div to a re-com v-box which can be pasted into clojurscript source."
  [filename]
  (let [contents (slurp filename)
        parsed (h/parse contents)
        hic (->> (h/as-hickory parsed)
                 (s/select (s/class "re-com"))
                 first
                 convert/hickory-to-hiccup
                 (filter #(not (and (string? %) (clojure.string/blank? %)))))
        children (if (map? (second hic))
                   (rest (rest hic))
                   (rest hic))
        re-com [:v-box :children (apply vector (map convert-to-re-com children))]]
    (-> re-com
       str
       (clojure.string/replace #"\[:title " "[re-com/title ")
       (clojure.string/replace #"\[:gap " "[gap ")
       (clojure.string/replace #"\[:label " "[label ")
       (clojure.string/replace #"\[:v-box " "[v-box :margin \"40px\" :max-width \"800px\" :align-self :center :align :start :justify :start "))))

; (spit "privacy.hiccup" (convert-single-div "resources/public/static/privacy.html"))
