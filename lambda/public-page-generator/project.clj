(defproject page-generator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.amazonaws/aws-lambda-java-core "1.2.0"]
                 [amazonica "0.3.125" :exclusions [com.amazonaws/aws-java-sdk
                                                   com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-core "1.11.333"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.333"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.333"]
                 [org.clojure/data.json "0.2.1"]]
  :min-lein-version "2.6.1"

  :source-paths ["src/clj"]

  :test-paths ["test/clj" "test/cljc"]

  :aot :all

  :profiles
    {:dbmgmt {:dependencies [[org.clojure/tools.nrepl "0.2.13"]]}}
  :omit-source true
  )

