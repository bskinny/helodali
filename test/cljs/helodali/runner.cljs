(ns helodali.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [helodali.core-test]))

(doo-tests 'helodali.core-test)
