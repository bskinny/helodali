(defproject helodali "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [reagent "0.6.1"]
                 [re-frame "0.9.2"]
                 [cljs-ajax "0.5.8"]
                 [day8.re-frame/http-fx "0.1.3"]
                 [com.taoensso/faraday "1.9.0"]
                 [org.clojure/core.async "0.3.441"]
                 [danlentz/clj-uuid "0.1.6"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [clj-time "0.13.0"]
                 [com.andrewmcveigh/cljs-time "0.5.0-alpha2"]
                 [re-com "2.0.0"]
                 [compojure "1.5.1"]
                 [yogthos/config "0.8"]
                 [cljsjs/auth0-lock "10.8.1-0"]
                 [cljsjs/auth0 "7.0.4-0"]
                 [cljsjs/aws-sdk-js "2.2.41-4"]
                 [org.clojars.bskinny/clj-aws-s3 "0.3.11" :exclusions [joda-time]]
                 [ring/ring-defaults "0.2.1"]
                 [ring-middleware-format "0.7.0"]
                 [ring "1.5.0"]
                 [ring-logger "0.7.7"]
                 [clj-http "2.3.0"]
                 [hickory "0.7.0"]
                 [cheshire "5.6.3"]
                 [secretary "1.2.3"]
                 [slingshot "0.12.2"]
                 [venantius/accountant "0.1.7"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-ring "0.11.0"]
            [lein-asset-minifier "0.3.1"
               :exclusions [org.clojure/clojure]]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj" "src/cljc"]

  :test-paths ["test/clj" "test/cljc"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler helodali.handler/dev-handler}

  :war-resources-path "war-resources" ;; Used only for packaging .ebextensions at the top level of the war

  :profiles
    {:dev
       {:hooks [leiningen.cljsbuild]  ;; This adds cljsbuild when lein does an ordinary compile
        :dependencies [[binaryage/devtools "0.8.3"]]
        :plugins      [[lein-figwheel "0.5.8"]
                       [lein-doo "0.1.7"]]
        :cljsbuild
          {:builds
            {:app
              {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
               :figwheel     {:on-jsload "helodali.core/mount-root"}
               :compiler     {:main                 helodali.dev
                              :output-to            "resources/public/js/compiled/app.js"
                              :output-dir           "resources/public/js/compiled/out"
                              :asset-path           "js/compiled/out"
                              :source-map-timestamp true
                              :preloads             [devtools.preload]
                              :external-config      {:devtools/config {:features-to-install :all}}}}}}}

     :webapp    ;; Do not name this profile :uberjar or lein ring uberwar will not work
       {:hooks [leiningen.cljsbuild]  ;; This adds cljsbuild when lein does an ordinary compile
        :ring {:handler helodali.handler/handler
               :uberwar-name "helodali.war"
               :war-exclusions []}  ;; This prevents excluding hidden files (default behavior) - we need .ebextensions
        :cljsbuild
         {:builds
           {:app
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :jar true
             :compiler {:main            helodali.prod
                        :output-to       "resources/public/js/compiled/app.js"
                        :optimizations   :advanced
                        :closure-defines {goog.DEBUG false}
                        :pretty-print    false}}}}}

     :api
       {:ring {:handler helodali.handler/api-handler
               :uberwar-name "helodali-api.war"}
        :cljsbuild {:builds []}}}

  :main helodali.server

  :aot [helodali.server]

  :uberjar-name "helodali.jar")
