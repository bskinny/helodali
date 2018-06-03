(defproject helodali "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [reagent "0.8.0"]
                 [re-frame "0.10.5"]
                 [cljs-ajax "0.7.3"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [joda-time "2.9.9"]
                 [amazonica "0.3.125" :exclusions [com.amazonaws/aws-java-sdk
                                                   com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-core "1.11.333"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.333"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.333"]
                 [com.taoensso/faraday "1.9.0"]
                 [commons-codec "1.10"]
                 [org.apache.httpcomponents/httpclient "4.5.3"]
                 [ring "1.6.3"]
                 [clj-pdf "2.2.19"]
                 [org.clojure/core.async "0.4.474"]
                 [danlentz/clj-uuid "0.1.7"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [clj-time "0.14.3"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [re-com "2.1.0"]
                 [compojure "1.6.1"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [org.clojure/core.specs.alpha "0.1.24"]
                 [yogthos/config "1.1.1"]
                 [clj-jwt "0.1.1"]
                 [buddy/buddy-core "1.5.0-SNAPSHOT"]
                 [cljsjs/aws-sdk-js "2.247.1-0"]  ;; This is a local build of aws-sdk-js
                 [ring/ring-defaults "0.3.1"]
                 [ring-middleware-format "0.7.2"]
                 [ring-logger "0.7.7"]
                 [clj-http "2.3.0"]
                 [hickory "0.7.1"]
                 [cheshire "5.8.0"]
                 [secretary "1.2.3"]
                 [slingshot "0.12.2"]
                 [venantius/accountant "0.2.3"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-ring "0.12.4"]
            [lein-asset-minifier "0.4.4"
               :exclusions [org.clojure/clojure]]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj" "src/cljc"]

  :test-paths ["test/clj" "test/cljc"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]
             :server-logfile "/Users/brianww/github/helodali-figwheel.log"
             :ring-handler helodali.handler/dev-handler}

  :war-resources-path "war-resources" ;; Used only for packaging .ebextensions at the top level of the war

  :profiles
    {:dev
       {:hooks [leiningen.cljsbuild]  ;; This adds cljsbuild when lein does an ordinary compile
        :dependencies [[binaryage/devtools "0.9.10"]]
        :plugins      [[lein-figwheel "0.5.16"]
                       [lein-doo "0.1.10"]]
        :ring {:handler helodali.handler/dev-handler}
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
               :war-exclusions []}  ;; This prevents excluding hidden files (default behavior) such as .ebextensions
        :cljsbuild
         {:builds
           {:app
             {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
              :jar true
              :compiler {:main            helodali.prod
                         :output-to       "resources/public/js/compiled/app.js" ;; This filename is changed by eb-deploy.pl for cache busting
                         :optimizations   :advanced
                         :closure-defines {goog.DEBUG false}
                         :pretty-print    false}}}}}

     :dbmgmt {:dependencies [[org.clojure/tools.nrepl "0.2.13"]]}}

    :api
    {:ring {:handler helodali.handler/api-handler
            :uberwar-name "helodali-api.war"}
     :cljsbuild {:builds []}}

  :main helodali.server

  :aot [helodali.server]

  :uberjar-name "helodali.jar")
