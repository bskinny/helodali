
### The aws-sdk-js Dependency

The `aws-sdk-js` dependency from cljsjs requires a fix to the extern generation process. The following is a manual definition 
of the externs used by helodali, for S3 and Cognito, and the process to package them. The customization is defined in
a forked [branch](https://github.com/bskinny/packages/tree/aws-sdk-js-update) of cljsjs/packages.

1. Define the minimal externs for S3 and Cognito services like so in aws-sdk-js/resources/cljsjs/aws-sdk-js/common/aws-sdk-js.ext.js:
```
/**********************************************************************
 * Minimal externs for AWS S3 and Cognito
 **********************************************************************/
var AWS = {
  "S3": {
    "getObject": function () {},
    "putObject": function () {},
    "copyObject": function () {},
    "deleteObjects": function () {},
    "getSignedUrl": function () {}
  },
  "config": {
    "region": function () {},
    "credentials": function () {}
  }
};
AWS.CognitoIdentityCredentials.prototype = {
  "cacheId": function () {},
  "clearCachedId": function () {},
  "clearIdOnNotAuthorized": function () {},
  "coalesceRefresh": function () {},
  "constructor": function () {},
  "createClients": function () {},
  "expiryWindow": function () {},
  "get": function () {},
  "getCredentialsForIdentity": function () {},
  "getCredentialsFromSTS": function () {},
  "getId": function () {},
  "getPromise": function () {},
  "getStorage": function () {},
  "load": function () {},
  "loadCachedId": function () {},
  "loadCredentials": function () {},
  "localStorageKey": function () {},
  "needsRefresh": function () {},
  "refresh": function () {},
  "refreshPromise": function () {},
  "setStorage": function () {},
  "storage": function () {}
};
```

1. Updated build.boot
```clojure
(set-env!
  :resource-paths #{"resources"}
  :dependencies '[[cljsjs/boot-cljsjs "0.10.4" :scope "test"]])

(require '[cljsjs.boot-cljsjs.packaging :refer :all])

;; Example Build and Deploy to Clojars:
;; boot package target
;; boot push --repo clojars --file target/aws-sdk-js-2.527.0-1.jar

(def +lib-version+ "2.527.0")
(def +version+ (str +lib-version+ "-1"))

;; The clojars username and password values will be pulled from .lein/credentials (see .boot/profile.boot)
(set-env! :repositories [["clojars" {:url "https://clojars.org/repo/"}]])

(task-options!
  pom  {:project 'org.clojars.bskinny/aws-sdk-js
                    ;;:project     'cljsjs/aws-sdk-js
        :version     +version+
        :description "AWS Browser SDK"
        :url         "https://github.com/aws/aws-sdk-js"
        :license     {"MIT" "https://opensource.org/licenses/MIT"}
        :scm         {:url "https://github.com/cljsjs/packages"}})

(deftask package []
  (comp
   (download :url (format "https://github.com/aws/aws-sdk-js/archive/v%s.zip" +lib-version+)
             :unzip true)

   (sift :move {#"^aws-sdk-js-.*/dist/aws-sdk.js"  "org.clojars.bskinny/aws-sdk-js/development/aws-sdk-js.inc.js"
                #"^aws-sdk-js-.*/dist/aws-sdk.min.js"  "org.clojars.bskinny/aws-sdk-js/production/aws-sdk-js.min.inc.js"})

   (sift :include #{#"^org.clojars.bskinny"})

   (deps-cljs :name "cljsjs.aws-sdk-js")
   (pom)
   (jar)
   (validate-checksums)))
```
1. Update the externs. You can generate a new (humongous) ext.js using this [generator](http://jmmk.github.io/javascript-externs-generator) with the 
input of https://sdk.amazonaws.com/js/aws-sdk-2.421.0.min.js (version number corrected) and then parse out the needed externs
1. Update the version number in aws-sdk-js/build.boot, e.g. _2.527.0-1_.
1. Comment out the (set-env! :repositories ...) in `build.boot` - FIXME: This needs to be present for the later invocation of `boot push`
1. `boot package target`
1. Configure authentication to clojars
1. `boot push --repo clojars --file target/aws-sdk-js-2.394.0-1.jar`