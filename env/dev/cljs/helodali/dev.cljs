(ns ^:figwheel-hooks helodali.dev
    (:require [devtools.core :as devtools]
              [helodali.core :as core]))


(enable-console-print!)
(println "dev mode")
(devtools/install!)

(core/init)
