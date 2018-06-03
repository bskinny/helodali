(ns helodali.cv
  (:require [clj-http.client :as http]
            [cheshire.core :refer [parse-string generate-string]]
            [clojure.java.io :as io]
            [clj-pdf.core :refer :all]
            [clojure.pprint :refer [pprint]]
            [slingshot.slingshot :refer [throw+ try+]]))
(pdf
  [{}
   [:list {:roman true}
          [:chunk {:style :bold} "a bold item"]
          "another item"
          "yet another item"]
   [:phrase "some text"]
   [:phrase "some more text"]
   [:paragraph "yet more text"]]
  "doc.pdf")
