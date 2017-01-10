(ns helodali.prod
    (:require [helodali.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init)
