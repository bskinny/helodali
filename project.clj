(defproject helodali "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.439"]
                 [org.clojure/tools.logging "0.4.1"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"]
                 [cljs-ajax "0.8.0"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [joda-time "2.10.1"]
                 [amazonica "0.3.134" :exclusions [com.amazonaws/aws-java-sdk
                                                   com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-core "1.11.452"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.452"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.452"]
                 [com.taoensso/faraday "1.9.0"]
                 [commons-codec "1.11"]
                 [org.apache.httpcomponents/httpclient "4.5.6"]
                 [ring "1.7.1"]
                 [clj-pdf "2.2.33"]
                 [org.clojure/core.async "0.4.474"]
                 [danlentz/clj-uuid "0.1.7"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [clj-time "0.15.1"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [re-com "2.4.0"]
                 [compojure "1.6.1"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [org.clojure/core.specs.alpha "0.1.24"]
                 [yogthos/config "1.1.1"]
                 [clj-jwt "0.1.1"]
                 [buddy/buddy-core "1.5.0"]
                 [cljsjs/aws-sdk-js "2.394.0-0"]  ;; This is a local build of aws-sdk-js for now (see README).
                 [ring/ring-defaults "0.3.2"]
                 [ring-middleware-format "0.7.2"]
                 [ring-logger "0.7.8"]
                 [clj-http "2.3.0"]
                 [hickory "0.7.1"]
                 [cheshire "5.8.1"]
                 [clj-commons/secretary "1.2.4"]
                 [slingshot "0.12.2"]
                 [venantius/accountant "0.2.4"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-ring "0.12.4"]
            [lein-asset-minifier "0.4.5"
               :exclusions [org.clojure/clojure]]]

  :min-lein-version "2.7.1"

  :source-paths ["src/clj" "src/cljc"]

  :test-paths ["test/clj" "test/cljc"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]
             :server-logfile "/Users/brianww/gitlab/helodali-figwheel.log"
             :ring-handler helodali.handler/dev-handler}

  :war-resources-path "war-resources" ;; Used only for packaging .ebextensions at the top level of the war

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]}

  :profiles
    {:dbmgmt {:dependencies [[org.clojure/tools.nrepl "0.2.13"]]}

     :dev
       {:dependencies [[binaryage/devtools "0.9.10"]]
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
       {:ring {:handler helodali.handler/handler
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
                         :pretty-print    false}}}}}}

    :api {:ring {:handler helodali.handler/api-handler
                 :uberwar-name "helodali-api.war"}
          :cljsbuild {:builds []}}

  :main helodali.server

  :aot [helodali.server]

  :uberjar-name "helodali.jar")
