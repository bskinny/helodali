(defproject public-pages-generator "1.0.1"
  :description "AWS Lambda function to generate public pages"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.4"]
                 [com.amazonaws/aws-lambda-java-core "1.2.0"]
                 [amazonica "0.3.134" :exclusions [com.amazonaws/aws-java-sdk
                                                   com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-core "1.11.561"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.561"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.561"]
                 [org.clojure/data.json "0.2.1"]
                 [clojure.java-time "1.4.3"]
                 [com.amazonaws/aws-java-sdk-sns "1.11.561"]
                 [com.amazonaws/aws-java-sdk-cloudfront "1.11.561"]]
  :min-lein-version "2.6.1"

  :source-paths ["src/clj"]

  :test-paths ["test/clj" "test/cljc"]

  :aot :all

  :profiles
    {:dbmgmt {:dependencies [[org.clojure/tools.nrepl "0.2.13"]]}}
  :omit-source true)
