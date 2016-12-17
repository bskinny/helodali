(ns helodali.demodata
  (:require #?(:clj  [clj-uuid :as uuid]
               :cljs [cljs-uuid-utils.core :as uuid])
            #?(:clj  [clj-time.core :refer [now days ago date-time]]
               :cljs [cljs-time.core :refer [now days ago date-time]])))

(def account
  {:uuid "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb"
   :created (-> 900 days ago)})

(def profile
  {:uuid "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb"
   :created (-> 800 days ago)
   :name "Brian Williams"
   :photo nil ;; href to image
   :birth-year 1968
   :birth-place "Charleston, South Carolina, USA"
   :currently-resides "Arlington, VA, USA"
   :email "brian@mayalane.com"
   :phone "703 629-1125"
   :url "mayalane.com"
   :degrees [{:year 2009 :val "University of British Columbia, Master of Fine Arts"}
             {:year 2005 :val "Emily Carr University, Bachelor of Fine Arts"}]
   :awards-and-grants [{:year 2011 :val "Canada Council grant"}
                       {:year 2010 :val "BC Arts Council grant"}]
   :residencies [{:year 2010 :val "Studio residency, School of Visual Arts, New York"}]
   :lectures-and-talks []
   :collections []})

(def press
  [{:uuid "123123-222-123123-0001"
    :created (-> 13 days ago)
    :title "That was wierd"
    :author-first-name "Mel"
    :author-last-name "Mourning"
    :publication "News of the Wierd"
    :publication-date (date-time 2016 4 24)
    :url "http://studiogallerydc.com"
    :volume "14"
    :page-numbers "44-45"
    :include-in-cv true
    :notes nil}
   {:uuid "123123-222-123123-0002"
    :created (-> 122 days ago)
    :title "This is better, but still not good enough for a visit"
    :publication "News Daily"
    :publication-date (date-time 2016 9 2)
    :url "whatsup.org"
    :volume "143"
    :page-numbers "344-349"
    :include-in-cv false
    :notes nil}])

(def exhibitions
  [{:uuid "123123-123123-123123"
    :created (-> 12 days ago)
    :name "PLAYTIME 10 PLUS 30"
    :begin-date (date-time 2015 4 1)
    :end-date (date-time 2015 4 24)
    :location "Studio Gallery, Washington DC"
    :url "http://studiogallerydc.com"
    :notes "Solo show"}
   {:uuid "12-123123-00000002"
    :created (-> 4 days ago)
    :name "The Lonely Drone"
    :location "Fisher Gallery, Alexandria, VA"
    :associated-press #{"123123-222-123123-0002" "123123-222-123123-0001"}
    :url "http://nova.edu"
    :notes nil}])

(def contacts
  [{:uuid "123123-234234-0000001"
    :created (-> 400 days ago)
    :name "Samra Sulaiman"
    :email "samra@g.com"
    :role :person}
   {:uuid "123123-234234-0000002"
    :created (-> 320 days ago)
    :name "Andy Coulbeck"
    :email "andy.Coulbeck@unboundid.com"
    :role :person}
   {:uuid "123123-234234-0000003"
    :created (-> 10 days ago)
    :name "Adah Rose Bitterbaum"
    :email "adahrose@gmail.com"
    :address "Kensington, MD"
    :role :dealer}
   {:uuid "123123-234234-0000004"
    :created (-> 4 days ago)
     :name "Raena and Jamie Bishop"
     :email "r@g.com"
     :address "Brooklyn, NY"
     :role :person}
   {:uuid "123123-234234-0000005"
    :created (-> 7 days ago)
     :name "Alaine Simone"
     :email "alaine@me.com"
     :role :agent}
   {:uuid "123123-234234-0000006"
    :created (-> 201 days ago)
     :name "Marriott Corp."
     :role :company}
   {:uuid "123123-234234-0000007"
    :created (-> 120 days ago)
     :name "Marshall Ralls"
     :email "marsha@me.com"
     :role :agent}
   {:uuid "123123-234234-0000008"
    :created (-> 120 days ago)
     :name "David Ely"
     :phone "512 555-1212"
     :role :person}
   {:uuid "123123-234234-0000009"
    :created (-> 111 days ago)
     :name "Glenn"
     :email "glenn@me2.com"
     :role :person}
   {:uuid "123123-234234-0000010"
    :created (-> 72 days ago)
     :name "Willie Lin"
     :email "akisoc@yahoo.com"
     :role :person}
   {:uuid "123123-234234-0000011"
    :created (-> 490 days ago)
     :name "Elizabeth Hermes"
     :email "elizabeth@me.com"
     :role :person}
   {:uuid "123123-234234-0000012"
    :created (-> 13 days ago)
     :name "Mary Welch Higgins"
     :email "mart.welch.higgins@somethinglong.com"
     :role :person}])


(def artwork
  [{:description "This was named after the passing of controversial DC figure Marion Barry. It was also named SPOT for the PLAYTIME 10 PLUS 30 show at Stuio Gallery in 2015. This is a long description, testing the user interface. Real-time machine stenography is a code translation system that lets users enter words and syllables by pressing multiple keys simultaneously in a chord, which is then instantly translated into English text. This makes steno the fastest and most accurate text entry method currently available. It is also more ergonomic than QWERTY.",
    :series false,
    :images [{:filename "MarionsPass-24x24.tif",
              :uuid "5dda7498-4852-4ec9-a9dd-f6c5c532bc0d",
              :key "facebook|10208314583117362/70437ca0-baa5-11e6-9803-f8a3047232a7/5dda7498-4852-4ec9-a9dd-f6c5c532bc0d/MarionsPass-24x24.tif",}]
    :medium "oil on panel",
    :expenses 150,
    :list-price 1200,
    :type :painting,
    :created (date-time 2015 8 27),
    :dimensions "12 x 14 x 1 inches",
    :title "Marion's Pass",
    :style #{:abstract :political :biomorphic :conceptual},
    :year 2015,
    :status :sold,
    :condition nil,
    :exhibition-history [{:images [],
                          :ref "123123-123123-123123",
                          :notes "This is Carolee's favorite\n",}]
    :uref "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb",
    :purchases [{:total-commission-percent 0,
                 :date (date-time 2016 4 3),
                 :dealer nil,
                 :agent "123123-234234-0000003",
                 :notes "Carolee\n",
                 :donated false,
                 :commissioned false,
                 :buyer "123123-234234-0000004",
                 :location nil,
                 :price 200,
                 :on-public-display false,}]
    :uuid "70437ca0-baa5-11e6-9803-f8a3047232a7"}
   {:description "Figure of carol reclining",
    :series false,
    :images
    [{:filename "Cetus-2013.jpg",
      :uuid "0b6d637c-f668-48fa-aaf4-c2ae4242fe36",
      :key "facebook|10208314583117362/70437ca1-baa5-11e6-9803-f8a3047232a7/0b6d637c-f668-48fa-aaf4-c2ae4242fe36/Cetus-2013.jpg"}],
    :medium "oil on panel",
    :expenses 250,
    :list-price 4000,
    :type :painting,
    :created (-> 300 days ago),
    :dimensions "48 x 48 x 1 inches",
    :title "Cetus",
    :style #{:abstract},
    :year 2014,
    :status :sold,
    :condition nil,
    :uref "1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb",
    :purchases
    [{:date (date-time 2016 8 13),
      :on-public-display true,
      :agent "123123-234234-0000007",
      :price 4000,
      :location "Washington, DC",
      :total-commission-percent 40,
      :buyer "123123-234234-0000006"}],
    :uuid "70437ca1-baa5-11e6-9803-f8a3047232a7"}])
  
