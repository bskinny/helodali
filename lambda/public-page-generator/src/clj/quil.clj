(ns quil
  (:gen-class)
  (:require [quil.core :as q]
            [clojure.pprint :refer [pprint]]))

(defn draw
  [images]
  ; make background white
  (q/background 255)
  (pprint "In draw")
  (let [
        ;img (q/create-image 200 20 :rgb)
        loaded-img (q/load-image "https://s3.amazonaws.com/helodali-public-pages/718158b0-69a6-11e8-9c73-094f06ce2361/images/20de7b80-6d21-11e8-84b1-7f0de084e1d9/16584003_1321767761200061_8457464623631695872_n.jpg")]
    ;resized (q/resize loaded-img 20 20)]
    ;; Write the file
    ;(.hide (.frame (q/current-graphics)))
    (reduce (fn [count img] (q/image (q/load-image img) count 0 20 20) (+ 20 count)) 0 images)
    (q/save "image.png")))

(def images
  ["https://s3.amazonaws.com/helodali-public-pages/718158b0-69a6-11e8-9c73-094f06ce2361/images/20de7b80-6d21-11e8-84b1-7f0de084e1d9/16584003_1321767761200061_8457464623631695872_n.jpg"
   "https://s3.amazonaws.com/helodali-public-pages/718158b0-69a6-11e8-9c73-094f06ce2361/images/1cca2bc0-6d21-11e8-84b1-7f0de084e1d9/29087702_571750999850826_5010848175699787776_n.jpg"
   "https://s3.amazonaws.com/helodali-public-pages/718158b0-69a6-11e8-9c73-094f06ce2361/images/1be6d0a0-6d21-11e8-84b1-7f0de084e1d9/29714455_172117150110907_7876120046836121600_n.jpg"
   "https://s3.amazonaws.com/helodali-public-pages/718158b0-69a6-11e8-9c73-094f06ce2361/images/1af25e80-6d21-11e8-84b1-7f0de084e1d9/30077339_446752859110986_3688345479457800192_n.jpg"])

(defn testing
  [images]
  (q/defsketch testing
               :renderer :opengl
               :size [200 20]
               :setup #(q/no-loop)
               :draw (partial draw images)
               :on-close #(pprint "Sketch closed")))
