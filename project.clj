(defproject helodali "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
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
                 [org.clojars.bskinny/aws-sdk-js "2.394.0-1"]  ;; This is a local build of aws-sdk-js for now (see README).
                 [ring/ring-defaults "0.3.2"]
                 [ring-middleware-format "0.7.4"]
                 [ring-logger "0.7.8"]
                 [clj-http "2.3.0"]
                 [hickory "0.7.1"]
                 [cheshire "5.8.1"]
                 [clj-commons/secretary "1.2.4"]
                 [slingshot "0.12.2"]
                 [venantius/accountant "0.2.4"]]

  :plugins [[lein-cljsbuild "1.1.7"]

            ;;lein-tools-deps is needed to avoid duplicate dependency definition between lein and deps.edn. As of v0.4.5
            ;; it is not working.
            ;[lein-tools-deps "0.4.5"]

            [lein-ring "0.12.5"]
            [lein-asset-minifier "0.4.5"
               :exclusions [org.clojure/clojure]]]

  ;; The following configuration is for the lein-tools-deps plugin (commented out above). As of version 0.4.5 it still
  ;; does not work for this project.
  ;:middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  ;:lein-tools-deps/config {:config-files [:project]}
  ;:clojure-executables ["/usr/local/bin/clojure"]

  :min-lein-version "2.9.1"

  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target" "test/js"]

  :profiles
    {;; The dbmgmt profile is used from a Cursive repl to interact with DynamoDB
     :dbmgmt {:dependencies [[org.clojure/tools.nrepl "0.2.13"]]}

     :test {:resource-paths ["test-resources"]}

     :webapp    ;; Do not name this profile :uberjar
       {:prep-tasks ["compile" ["cljsbuild" "once"]]
        :ring {:handler helodali.handler/handler
               :open-browser? false
               :jar-exclusions []}  ;; This prevents excluding hidden files (default behavior) such as .ebextensions
        :cljsbuild
         {:builds
           {:app
             {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
              :jar true
              :compiler {:main            helodali.prod
                         :output-to       "resources/public/js/compiled/app.js" ;; This filename is changed by the Dockerfile for cache busting
                         :optimizations   :advanced
                         :closure-defines {goog.DEBUG false}
                         :pretty-print    false}}}}}}

  :main helodali.server

  :aot [helodali.server]

  :uberjar-name "helodali.jar")
