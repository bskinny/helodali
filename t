

============= i-0111355a4891555ec - /aws/elasticbeanstalk/uatest/var/log/eb-activity.log ==============

[i-0111355a4891555ec] [2019-04-19T21:16:59.044Z] INFO  [3023]  - [Initialization/PreInitStage0/PreInitHook/03upstart-deamons.sh] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:16:59.044Z] INFO  [3023]  - [Initialization/PreInitStage0/PreInitHook/04nginx.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:16:59.277Z] INFO  [3023]  - [Initialization/PreInitStage0/PreInitHook/04nginx.sh] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:16:59.278Z] INFO  [3023]  - [Initialization/PreInitStage0/PreInitHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/preinit.
[i-0111355a4891555ec] [2019-04-19T21:16:59.278Z] INFO  [3023]  - [Initialization/PreInitStage0] : Completed activity. Result:
  Initialization - Command CMD-PreInit stage 0 completed
[i-0111355a4891555ec] [2019-04-19T21:16:59.278Z] INFO  [3023]  - [Initialization/AddonsAfter] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:16:59.278Z] INFO  [3023]  - [Initialization/AddonsAfter] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:16:59.278Z] INFO  [3023]  - [Initialization] : Completed activity. Result:
  Initialization - Command CMD-PreInit succeeded
[i-0111355a4891555ec] [2019-04-19T21:17:22.263Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:22.264Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsBefore] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:22.264Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsBefore/ConfigCWLAgent] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:22.264Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsBefore/ConfigCWLAgent/10-config.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:22.424Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsBefore/ConfigCWLAgent/10-config.sh] : Completed activity. Result:
  Log streaming option setting is not specified, ignore cloudwatch logs setup.
  
  Disabled log streaming.
[i-0111355a4891555ec] [2019-04-19T21:17:22.424Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsBefore/ConfigCWLAgent] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/addons/logstreaming/hooks/config.
[i-0111355a4891555ec] [2019-04-19T21:17:22.424Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsBefore] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:17:22.878Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:22.878Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/HealthdLogRotation] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:22.884Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/HealthdLogRotation] : Completed activity. Result:
  ["/etc/cron.hourly/cron.logrotate.elasticbeanstalk.healthd.conf"]
[i-0111355a4891555ec] [2019-04-19T21:17:22.884Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/HealthdHTTPDLogging] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:22.886Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/HealthdHTTPDLogging] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:17:22.886Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/HealthdNginxLogging] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:22.886Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/HealthdNginxLogging] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:17:22.886Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/EbExtensionPreBuild] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:23.535Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/EbExtensionPreBuild/Infra-EmbeddedPreBuild] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:23.535Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/EbExtensionPreBuild/Infra-EmbeddedPreBuild] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:17:23.553Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/EbExtensionPreBuild] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:17:23.553Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:23.555Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook/00clean_dir.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:32.453Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook/00clean_dir.sh] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:17:32.453Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook/01unzip.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:32.800Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook/01unzip.sh] : Completed activity. Result:
  Archive:  /opt/elasticbeanstalk/deploy/appsource/source_bundle
  bd9908e3a98c41b66407d354f5efb3601a3c0ecd
    inflating: /var/app/current/.dockerignore  
    inflating: /var/app/current/.gitignore  
    inflating: /var/app/current/Dockerfile  
    inflating: /var/app/current/LICENSE  
    inflating: /var/app/current/README.md  
    inflating: /var/app/current/deps.edn  
    inflating: /var/app/current/dev.cljs.edn  
     creating: /var/app/current/env/
     creating: /var/app/current/env/dev/
     creating: /var/app/current/env/dev/cljs/
     creating: /var/app/current/env/dev/cljs/helodali/
    inflating: /var/app/current/env/dev/cljs/helodali/dev.cljs  
     creating: /var/app/current/env/prod/
     creating: /var/app/current/env/prod/cljs/
     creating: /var/app/current/env/prod/cljs/helodali/
    inflating: /var/app/current/env/prod/cljs/helodali/prod.cljs  
    inflating: /var/app/current/figwheel-main.edn  
     creating: /var/app/current/lambda/
     creating: /var/app/current/lambda/contact-form/
    inflating: /var/app/current/lambda/contact-form/contact-form.yaml  
    inflating: /var/app/current/lambda/contact-form/index.js  
     creating: /var/app/current/lambda/public-page-generator/
   extracting: /var/app/current/lambda/public-page-generator/README.md  
    inflating: /var/app/current/lambda/public-page-generator/project.clj  
    inflating: /var/app/current/lambda/public-page-generator/public-pages-generator.yaml  
     creating: /var/app/current/lambda/public-page-generator/resources/
    inflating: /var/app/current/lambda/public-page-generator/resources/README  
    inflating: /var/app/current/lambda/public-page-generator/resources/artwork-template.html  
     creating: /var/app/current/lambda/public-page-generator/resources/assets/
   extracting: /var/app/current/lambda/public-page-generator/resources/assets/Arrows-Left-icon.png  
   extracting: /var/app/current/lambda/public-page-generator/resources/assets/Arrows-Right-icon.png  
    inflating: /var/app/current/lambda/public-page-generator/resources/assets/favicon.ico  
    inflating: /var/app/current/lambda/public-page-generator/resources/contact-form-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/cv-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/exhibition-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/hd-public.css  
    inflating: /var/app/current/lambda/public-page-generator/resources/index-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/index.html  
     creating: /var/app/current/lambda/public-page-generator/scripts/
    inflating: /var/app/current/lambda/public-page-generator/scripts/update-function-code.pl  
     creating: /var/app/current/lambda/public-page-generator/src/
     creating: /var/app/current/lambda/public-page-generator/src/clj/
    inflating: /var/app/current/lambda/public-page-generator/src/clj/public_page_generator.clj  
     creating: /var/app/current/lambda/public-page-generator/test/
     creating: /var/app/current/lambda/public-page-generator/test/clj/
    inflating: /var/app/current/lambda/public-page-generator/test/clj/public_page_generator_test.clj  
     creating: /var/app/current/lambda/ribbon-maker/
    inflating: /var/app/current/lambda/ribbon-maker/index.js  
    inflating: /var/app/current/lambda/ribbon-maker/package-lock.json  
    inflating: /var/app/current/lambda/ribbon-maker/package.json  
    inflating: /var/app/current/lambda/ribbon-maker/ribbon-maker.yaml  
   extracting: /var/app/current/lambda/ribbon-maker/ribbon.png  
     creating: /var/app/current/lambda/ribbon-maker/scripts/
    inflating: /var/app/current/lambda/ribbon-maker/scripts/make-zip.sh  
    inflating: /var/app/current/lambda/ribbon-maker/scripts/update-function-code.pl  
    inflating: /var/app/current/lambda/ribbon-maker/test-event.json  
    inflating: /var/app/current/lambda/ribbon-maker/test-event.sh  
     creating: /var/app/current/lambda/s3-image-conversion/
    inflating: /var/app/current/lambda/s3-image-conversion/README.md  
    inflating: /var/app/current/lambda/s3-image-conversion/image-conversion.yaml  
    inflating: /var/app/current/lambda/s3-image-conversion/index.js  
    inflating: /var/app/current/lambda/s3-image-conversion/package-lock.json  
    inflating: /var/app/current/lambda/s3-image-conversion/package.json  
     creating: /var/app/current/lambda/s3-image-conversion/scripts/
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/add-permission.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/create-function.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/make-zip.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/update-function-code.pl  
    inflating: /var/app/current/lambda/s3-image-conversion/test-create-event.json  
    inflating: /var/app/current/lambda/s3-image-conversion/test-event.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/test-remove-event.json  
    inflating: /var/app/current/lambda/s3-image-conversion/webstorm-bespoken-config.png  
    inflating: /var/app/current/project.clj  
     creating: /var/app/current/resources/
    inflating: /var/app/current/resources/comodo-trust.jks  
     creating: /var/app/current/resources/doc/
     creating: /var/app/current/resources/doc/images/
    inflating: /var/app/current/resources/doc/images/helodali-screenshot.png  
     creating: /var/app/current/resources/public/
     creating: /var/app/current/resources/public/css/
    inflating: /var/app/current/resources/public/css/helodali.css  
    inflating: /var/app/current/resources/public/favicon.ico  
     creating: /var/app/current/resources/public/image-assets/
    inflating: /var/app/current/resources/public/image-assets/Veronica-snow.jpg  
    inflating: /var/app/current/resources/public/image-assets/ajax-loader.gif  
    inflating: /var/app/current/resources/public/image-assets/file-cabinet.png  
    inflating: /var/app/current/resources/public/image-assets/file-question.png  
    inflating: /var/app/current/resources/public/image-assets/file-text.png  
    inflating: /var/app/current/resources/public/image-assets/hd-bg-1.jpg  
    inflating: /var/app/current/resources/public/image-assets/logo.png  
    inflating: /var/app/current/resources/public/image-assets/thumb-stub.png  
    inflating: /var/app/current/resources/public/index.html  
     creating: /var/app/current/resources/public/static/
    inflating: /var/app/current/resources/public/static/privacy.html  
     creating: /var/app/current/resources/public/vendor/
     creating: /var/app/current/resources/public/vendor/css/
   extracting: /var/app/current/resources/public/vendor/css/chosen-sprite.png  
    inflating: /var/app/current/resources/public/vendor/css/chosen-sprite@2x.png  
    inflating: /var/app/current/resources/public/vendor/css/material-design-color-palette.css  
    inflating: /var/app/current/resources/public/vendor/css/material-design-color-palette.min.css  
    inflating: /var/app/current/resources/public/vendor/css/material-design-iconic-font.min.css  
    inflating: /var/app/current/resources/public/vendor/css/re-com.css  
     creating: /var/app/current/resources/public/vendor/fonts/
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.eot  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.svg  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.ttf  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.woff  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.woff2  
     creating: /var/app/current/resources/storage-shed/
     creating: /var/app/current/resources/storage-shed/icon/
   extracting: /var/app/current/resources/storage-shed/icon/favicon.png  
    inflating: /var/app/current/resources/storage-shed/icon/paint-brush-icons.txt  
    inflating: /var/app/current/resources/storage-shed/icon/paint-brush-icons.zip  
     creating: /var/app/current/scripts/
    inflating: /var/app/current/scripts/convert-html-to-hiccup.clj  
    inflating: /var/app/current/scripts/create-instagram-subscription.sh  
    inflating: /var/app/current/scripts/eb-deploy.pl  
     creating: /var/app/current/src/
     creating: /var/app/current/src/clj/
     creating: /var/app/current/src/clj/helodali/
    inflating: /var/app/current/src/clj/helodali/cognito.clj  
   extracting: /var/app/current/src/clj/helodali/core.clj  
    inflating: /var/app/current/src/clj/helodali/cv.clj  
    inflating: /var/app/current/src/clj/helodali/db.clj  
    inflating: /var/app/current/src/clj/helodali/handler.clj  
    inflating: /var/app/current/src/clj/helodali/instagram.clj  
    inflating: /var/app/current/src/clj/helodali/s3.clj  
    inflating: /var/app/current/src/clj/helodali/server.clj  
     creating: /var/app/current/src/cljc/
     creating: /var/app/current/src/cljc/helodali/
    inflating: /var/app/current/src/cljc/helodali/common.cljc  
    inflating: /var/app/current/src/cljc/helodali/types.cljc  
     creating: /var/app/current/src/cljs/
     creating: /var/app/current/src/cljs/helodali/
   extracting: /var/app/current/src/cljs/helodali/config.cljs  
    inflating: /var/app/current/src/cljs/helodali/core.cljs  
    inflating: /var/app/current/src/cljs/helodali/db.cljs  
    inflating: /var/app/current/src/cljs/helodali/events.cljs  
    inflating: /var/app/current/src/cljs/helodali/misc.cljs  
    inflating: /var/app/current/src/cljs/helodali/routes.cljs  
    inflating: /var/app/current/src/cljs/helodali/spec.cljs  
    inflating: /var/app/current/src/cljs/helodali/subs.cljs  
    inflating: /var/app/current/src/cljs/helodali/views.cljs  
     creating: /var/app/current/src/cljs/helodali/views/
    inflating: /var/app/current/src/cljs/helodali/views/account.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/artwork.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/contacts.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/documents.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/exhibitions.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/expenses.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/pages.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/press.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/profile.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/purchases.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/referred_artwork.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/search_results.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/static_pages.cljs  
     creating: /var/app/current/test/
     creating: /var/app/current/test/cljs/
     creating: /var/app/current/test/cljs/helodali/
    inflating: /var/app/current/test/cljs/helodali/core_test.cljs  
    inflating: /var/app/current/test/cljs/helodali/runner.cljs  
     creating: /var/app/current/war-resources/
     creating: /var/app/current/war-resources/.ebextensions/
     creating: /var/app/current/war-resources/.ebextensions/httpd/
     creating: /var/app/current/war-resources/.ebextensions/httpd/conf.d/
    inflating: /var/app/current/war-resources/.ebextensions/httpd/conf.d/elasticbeanstalk.conf  
[i-0111355a4891555ec] [2019-04-19T21:17:32.800Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook/02loopback-check.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:17:32.915Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook/02loopback-check.sh] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:17:32.915Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook/03build.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:43.324Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook/03build.sh] : Completed activity. Result:
  cat: Dockerrun.aws.json: No such file or directory
  cat: Dockerrun.aws.json: No such file or directory
  cat: Dockerrun.aws.json: No such file or directory
  alpine: Pulling from library/clojure
  bdf0201b3a05: Pulling fs layer
  9e12771959ad: Pulling fs layer
  c4efe34cda6e: Pulling fs layer
  9bb5aca2da8c: Pulling fs layer
  6b46af58bbe4: Pulling fs layer
  9e4373f522e9: Pulling fs layer
  9bb5aca2da8c: Waiting
  6b46af58bbe4: Waiting
  9e4373f522e9: Waiting
  9e12771959ad: Verifying Checksum
  9e12771959ad: Download complete
  bdf0201b3a05: Verifying Checksum
  bdf0201b3a05: Download complete
  bdf0201b3a05: Pull complete
  9bb5aca2da8c: Verifying Checksum
  9bb5aca2da8c: Download complete
  6b46af58bbe4: Verifying Checksum
  6b46af58bbe4: Download complete
  9e12771959ad: Pull complete
  9e4373f522e9: Verifying Checksum
  9e4373f522e9: Download complete
  c4efe34cda6e: Verifying Checksum
  c4efe34cda6e: Download complete
  c4efe34cda6e: Pull complete
  9bb5aca2da8c: Pull complete
  6b46af58bbe4: Pull complete
  9e4373f522e9: Pull complete
  Digest: sha256:053c0cd70e38f1bf72fe239ede4e8b6634ccb7ca1b4af8976c9e50ff4e5762fd
  Status: Downloaded newer image for clojure:alpine
  Successfully pulled clojure:alpine
  Sending build context to Docker daemon  3.918MB
  Step 1/9 : FROM clojure:alpine
   ---> c8ae7fa7ee1f
  Step 2/9 : RUN mkdir -p /app
   ---> Running in dd94e6619fa8
  Removing intermediate container dd94e6619fa8
   ---> 2cc095583ca8
  Step 3/9 : WORKDIR /app
   ---> Running in 77f9780da99a
  Removing intermediate container 77f9780da99a
   ---> dedec6aca7ed
  Step 4/9 : COPY project.clj /app/
   ---> 09f471643878
  Step 5/9 : RUN lein deps
   ---> Running in df53464f0f73
  [91mRetrieving lein-cljsbuild/lein-cljsbuild/1.1.7/lein-cljsbuild-1.1.7.pom from clojars
  [0m[91mRetrieving fs/fs/1.1.2/fs-1.1.2.pom from clojars
  [0m[91mRetrieving org/clojure/clojure/1.3.0/clojure-1.3.0.pom from central
  [0m[91mRetrieving org/sonatype/oss/oss-parent/5/oss-parent-5.pom from central
  [0m[91mRetrieving org/apache/commons/commons-compress/1.3/commons-compress-1.3.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/22/commons-parent-22.pom from central
  [0m[91mRetrieving org/apache/apache/9/apache-9.pom from central
  [0m[91mRetrieving lein-ring/lein-ring/0.12.5/lein-ring-0.12.5.pom from clojars
  [0m[91mRetrieving org/clojure/core.unify/0.5.7/core.unify-0.5.7.pom from central
  [0m[91mRetrieving org/clojure/pom.contrib/0.1.2/pom.contrib-0.1.2.pom from central
  [0m[91mRetrieving org/sonatype/oss/oss-parent/7/oss-parent-7.pom from central
  [0m[91mRetrieving org/clojure/clojure/1.4.0/clojure-1.4.0.pom from central
  [0m[91mRetrieving org/clojure/data.xml/0.0.8/data.xml-0.0.8.pom from central
  [0m[91mRetrieving leinjacker/leinjacker/0.4.2/leinjacker-0.4.2.pom from clojars
  [0m[91mRetrieving org/clojure/core.contracts/0.0.1/core.contracts-0.0.1.pom from central
  [0m[91mRetrieving org/clojure/pom.contrib/0.0.26/pom.contrib-0.0.26.pom from central
  [0m[91mRetrieving lein-asset-minifier/lein-asset-minifier/0.4.5/lein-asset-minifier-0.4.5.pom from clojars
  [0m[91mRetrieving asset-minifier/asset-minifier/0.2.6/asset-minifier-0.2.6.pom from clojars
  [0m[91mRetrieving clojure-future-spec/clojure-future-spec/1.9.0-alpha17/clojure-future-spec-1.9.0-alpha17.pom from clojars
  [0m[91mRetrieving org/clojure/test.check/0.9.0/test.check-0.9.0.pom from central
  [0m[91mRetrieving com/yahoo/platform/yui/yuicompressor/2.4.8/yuicompressor-2.4.8.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler/v20160208/closure-compiler-v20160208.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20160208/closure-compiler-parent-v20160208.pom from central
  [0m[91mRetrieving org/sonatype/oss/oss-parent/9/oss-parent-9.pom from central
  [0m[91mRetrieving clj-html-compressor/clj-html-compressor/0.1.1/clj-html-compressor-0.1.1.pom from clojars
  [0m[91mRetrieving com/googlecode/htmlcompressor/htmlcompressor/1.5.2/htmlcompressor-1.5.2.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler/r1043/closure-compiler-r1043.pom from central
  [0m[91mRetrieving args4j/args4j/2.0.12/args4j-2.0.12.pom from central
  [0m[91mRetrieving args4j/args4j-site/2.0.12/args4j-site-2.0.12.pom from central
  [0m[91mRetrieving com/google/guava/guava/r08/guava-r08.pom from central
  [0m[91mRetrieving com/google/google/5/google-5.pom from central
  [0m[91mRetrieving com/google/protobuf/protobuf-java/2.3.0/protobuf-java-2.3.0.pom from central
  [0m[91mRetrieving com/google/google/3/google-3.pom from central
  [0m[91mRetrieving org/json/json/20090211/json-20090211.pom from central
  [0m[91mRetrieving org/apache/ant/ant/1.8.1/ant-1.8.1.pom from central
  [0m[91mRetrieving org/apache/ant/ant-parent/1.8.1/ant-parent-1.8.1.pom from central
  [0m[91mRetrieving org/apache/ant/ant-launcher/1.8.1/ant-launcher-1.8.1.pom from central
  [0m[91mRetrieving com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.pom from central
  [0m[91mRetrieving junit/junit/4.8.2/junit-4.8.2.pom from central
  [0m[91mRetrieving com/yahoo/platform/yui/yuicompressor/2.4.6/yuicompressor-2.4.6.pom from central
  [0m[91mRetrieving rhino/js/1.6R7/js-1.6R7.pom from central
  [0m[91mRetrieving commons-io/commons-io/2.6/commons-io-2.6.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/42/commons-parent-42.pom from central
  [0m[91mRetrieving org/apache/apache/18/apache-18.pom from central
  [0m[91mRetrieving org/clojure/core.async/0.3.465/core.async-0.3.465.pom from central
  [0m[91mRetrieving org/clojure/tools.analyzer.jvm/0.7.0/tools.analyzer.jvm-0.7.0.pom from central
  [0m[91mRetrieving org/clojure/pom.contrib/0.2.0/pom.contrib-0.2.0.pom from central
  [0m[91mRetrieving org/clojure/tools.analyzer/0.6.9/tools.analyzer-0.6.9.pom from central
  [0m[91mRetrieving org/clojure/core.memoize/0.5.9/core.memoize-0.5.9.pom from central
  [0m[91mRetrieving org/clojure/core.cache/0.6.5/core.cache-0.6.5.pom from central
  [0m[91mRetrieving org/clojure/data.priority-map/0.0.7/data.priority-map-0.0.7.pom from central
  [0m[91mRetrieving org/ow2/asm/asm-all/4.2/asm-all-4.2.pom from central
  [0m[91mRetrieving org/ow2/asm/asm-parent/4.2/asm-parent-4.2.pom from central
  [0m[91mRetrieving org/ow2/ow2/1.3/ow2-1.3.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/1.0.0-beta4/tools.reader-1.0.0-beta4.pom from central
  [0m[91mRetrieving org/clojure/clojure/1.3.0/clojure-1.3.0.jar from central
  [0m[91mRetrieving org/apache/commons/commons-compress/1.3/commons-compress-1.3.jar from central
  [0m[91mRetrieving org/clojure/core.unify/0.5.7/core.unify-0.5.7.jar from central
  [0m[91mRetrieving org/clojure/data.xml/0.0.8/data.xml-0.0.8.jar from central
  [0m[91mRetrieving org/clojure/core.contracts/0.0.1/core.contracts-0.0.1.jar from central
  [0m[91mRetrieving org/clojure/test.check/0.9.0/test.check-0.9.0.jar from central
  [0m[91mRetrieving com/yahoo/platform/yui/yuicompressor/2.4.8/yuicompressor-2.4.8.jar from central
  [0m[91mRetrieving com/google/javascript/closure-compiler/v20160208/closure-compiler-v20160208.jar from central
  [0m[91mRetrieving com/googlecode/htmlcompressor/htmlcompressor/1.5.2/htmlcompressor-1.5.2.jar from central
  [0m[91mRetrieving commons-io/commons-io/2.6/commons-io-2.6.jar from central
  [0m[91mRetrieving org/clojure/core.async/0.3.465/core.async-0.3.465.jar from central
  [0m[91mRetrieving org/clojure/tools.analyzer.jvm/0.7.0/tools.analyzer.jvm-0.7.0.jar from central
  [0m[91mRetrieving org/clojure/tools.analyzer/0.6.9/tools.analyzer-0.6.9.jar from central
  [0m[91mRetrieving org/clojure/core.memoize/0.5.9/core.memoize-0.5.9.jar from central
  [0m[91mRetrieving org/clojure/core.cache/0.6.5/core.cache-0.6.5.jar from central
  [0m[91mRetrieving org/clojure/data.priority-map/0.0.7/data.priority-map-0.0.7.jar from central
  [0m[91mRetrieving org/ow2/asm/asm-all/4.2/asm-all-4.2.jar from central
  [0m[91mRetrieving org/clojure/tools.reader/1.0.0-beta4/tools.reader-1.0.0-beta4.jar from central
  [0m[91mRetrieving lein-cljsbuild/lein-cljsbuild/1.1.7/lein-cljsbuild-1.1.7.jar from clojars
  [0m[91mRetrieving fs/fs/1.1.2/fs-1.1.2.jar from clojars
  [0m[91mRetrieving leinjacker/leinjacker/0.4.2/leinjacker-0.4.2.jar from clojars
  [0m[91mRetrieving clojure-future-spec/clojure-future-spec/1.9.0-alpha17/clojure-future-spec-1.9.0-alpha17.jar from clojars
  [0m[91mRetrieving lein-ring/lein-ring/0.12.5/lein-ring-0.12.5.jar from clojars
  [0m[91mRetrieving asset-minifier/asset-minifier/0.2.6/asset-minifier-0.2.6.jar from clojars
  [0m[91mRetrieving clj-html-compressor/clj-html-compressor/0.1.1/clj-html-compressor-0.1.1.jar from clojars
  [0m[91mRetrieving lein-asset-minifier/lein-asset-minifier/0.4.5/lein-asset-minifier-0.4.5.jar from clojars
  [0m[91mRetrieving lein-figwheel/lein-figwheel/0.5.16/lein-figwheel-0.5.16.pom from clojars
  [0m[91mRetrieving simple-lein-profile-merge/simple-lein-profile-merge/0.1.4/simple-lein-profile-merge-0.1.4.pom from clojars
  [0m[91mRetrieving org/clojure/clojure/1.6.0/clojure-1.6.0.pom from central
  [0m[91mRetrieving lein-doo/lein-doo/0.1.10/lein-doo-0.1.10.pom from clojars
  [0m[91mRetrieving org/clojure/clojure/1.7.0/clojure-1.7.0.pom from central
  [0m[91mRetrieving doo/doo/0.1.10/doo-0.1.10.pom from clojars
  [0m[91mRetrieving org/clojure/clojure/1.9.0/clojure-1.9.0.pom from central
  [0m[91mRetrieving org/clojure/spec.alpha/0.1.143/spec.alpha-0.1.143.pom from central
  [0m[91mRetrieving org/clojure/core.specs.alpha/0.1.24/core.specs.alpha-0.1.24.pom from central
  [0m[91mRetrieving org/clojure/data.json/0.2.6/data.json-0.2.6.pom from central
  [0m[91mRetrieving karma-reporter/karma-reporter/2.1.2/karma-reporter-2.1.2.pom from clojars
  [0m[91mRetrieving org/clojure/clojure/1.8.0/clojure-1.8.0.pom from central
  [0m[91mRetrieving org/clojure/clojurescript/1.9.229/clojurescript-1.9.229.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-unshaded/v20160315/closure-compiler-unshaded-v20160315.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-main/v20160315/closure-compiler-main-v20160315.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20160315/closure-compiler-parent-v20160315.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20160315/closure-compiler-externs-v20160315.pom from central
  [0m[91mRetrieving args4j/args4j/2.0.26/args4j-2.0.26.pom from central
  [0m[91mRetrieving args4j/args4j-site/2.0.26/args4j-site-2.0.26.pom from central
  [0m[91mRetrieving org/kohsuke/pom/6/pom-6.pom from central
  [0m[91mRetrieving com/google/guava/guava/19.0/guava-19.0.pom from central
  [0m[91mRetrieving com/google/guava/guava-parent/19.0/guava-parent-19.0.pom from central
  [0m[91mRetrieving com/google/protobuf/protobuf-java/2.5.0/protobuf-java-2.5.0.pom from central
  [0m[91mRetrieving com/google/google/1/google-1.pom from central
  [0m[91mRetrieving com/google/code/gson/gson/2.2.4/gson-2.2.4.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20160609-f42b4a24/google-closure-library-0.0-20160609-f42b4a24.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20160609-f42b4a24/google-closure-library-third-party-0.0-20160609-f42b4a24.pom from central
  [0m[91mRetrieving org/mozilla/rhino/1.7R5/rhino-1.7R5.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/1.0.0-beta3/tools.reader-1.0.0-beta3.pom from central
  [0m[91mRetrieving org/clojure/clojure/1.5.1/clojure-1.5.1.pom from central
  [0m[91mRetrieving fipp/fipp/0.6.7/fipp-0.6.7.pom from clojars
  [0m[91mRetrieving org/clojure/core.rrb-vector/0.0.11/core.rrb-vector-0.0.11.pom from central
  [0m[91mRetrieving meta-merge/meta-merge/1.0.0/meta-merge-1.0.0.pom from clojars
  [0m[91mRetrieving org/clojure/data.json/0.2.6/data.json-0.2.6.jar from central
  [0m[91mRetrieving org/clojure/clojure/1.7.0/clojure-1.7.0.jar from central
  [0m[91mRetrieving org/clojure/clojurescript/1.9.229/clojurescript-1.9.229.jar from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-unshaded/v20160315/closure-compiler-unshaded-v20160315.jar from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20160315/closure-compiler-externs-v20160315.jar from central
  [0m[91mRetrieving args4j/args4j/2.0.26/args4j-2.0.26.jar from central
  [0m[91mRetrieving com/google/guava/guava/19.0/guava-19.0.jar from central
  [0m[91mRetrieving com/google/protobuf/protobuf-java/2.5.0/protobuf-java-2.5.0.jar from central
  [0m[91mRetrieving com/google/code/gson/gson/2.2.4/gson-2.2.4.jar from central
  [0m[91mRetrieving com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20160609-f42b4a24/google-closure-library-0.0-20160609-f42b4a24.jar from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20160609-f42b4a24/google-closure-library-third-party-0.0-20160609-f42b4a24.jar from central
  [0m[91mRetrieving org/mozilla/rhino/1.7R5/rhino-1.7R5.jar from central
  [0m[91mRetrieving org/clojure/core.rrb-vector/0.0.11/core.rrb-vector-0.0.11.jar from central
  [0m[91mRetrieving simple-lein-profile-merge/simple-lein-profile-merge/0.1.4/simple-lein-profile-merge-0.1.4.jar from clojars
  [0m[91mRetrieving lein-doo/lein-doo/0.1.10/lein-doo-0.1.10.jar from clojars
  [0m[91mRetrieving doo/doo/0.1.10/doo-0.1.10.jar from clojars
  [0m[91mRetrieving karma-reporter/karma-reporter/2.1.2/karma-reporter-2.1.2.jar from clojars
  [0m[91mRetrieving meta-merge/meta-merge/1.0.0/meta-merge-1.0.0.jar from clojars
  [0m[91mRetrieving fipp/fipp/0.6.7/fipp-0.6.7.jar from clojars
  [0m[91mRetrieving lein-figwheel/lein-figwheel/0.5.16/lein-figwheel-0.5.16.jar from clojars
  [0m[91mRetrieving org/clojure/clojurescript/1.10.439/clojurescript-1.10.439.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-unshaded/v20180805/closure-compiler-unshaded-v20180805.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-main/v20180805/closure-compiler-main-v20180805.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20180805/closure-compiler-parent-v20180805.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20180805/closure-compiler-externs-v20180805.pom from central
  [0m[91mRetrieving com/google/errorprone/error_prone_annotations/2.0.18/error_prone_annotations-2.0.18.pom from central
  [0m[91mRetrieving com/google/errorprone/error_prone_parent/2.0.18/error_prone_parent-2.0.18.pom from central
  [0m[91mRetrieving com/google/guava/guava/25.1-jre/guava-25.1-jre.pom from central
  [0m[91mRetrieving com/google/guava/guava-parent/25.1-jre/guava-parent-25.1-jre.pom from central
  [0m[91mRetrieving com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.pom from central
  [0m[91mRetrieving org/checkerframework/checker-qual/2.0.0/checker-qual-2.0.0.pom from central
  [0m[91mRetrieving com/google/errorprone/error_prone_annotations/2.1.3/error_prone_annotations-2.1.3.pom from central
  [0m[91mRetrieving com/google/errorprone/error_prone_parent/2.1.3/error_prone_parent-2.1.3.pom from central
  [0m[91mRetrieving com/google/j2objc/j2objc-annotations/1.1/j2objc-annotations-1.1.pom from central
  [0m[91mRetrieving org/codehaus/mojo/animal-sniffer-annotations/1.14/animal-sniffer-annotations-1.14.pom from central
  [0m[91mRetrieving org/codehaus/mojo/animal-sniffer-parent/1.14/animal-sniffer-parent-1.14.pom from central
  [0m[91mRetrieving org/codehaus/mojo/mojo-parent/34/mojo-parent-34.pom from central
  [0m[91mRetrieving org/codehaus/codehaus-parent/4/codehaus-parent-4.pom from central
  [0m[91mRetrieving com/google/protobuf/protobuf-java/3.0.2/protobuf-java-3.0.2.pom from central
  [0m[91mRetrieving com/google/protobuf/protobuf-parent/3.0.2/protobuf-parent-3.0.2.pom from central
  [0m[91mRetrieving com/google/code/gson/gson/2.7/gson-2.7.pom from central
  [0m[91mRetrieving com/google/code/gson/gson-parent/2.7/gson-parent-2.7.pom from central
  [0m[91mRetrieving com/google/code/findbugs/jsr305/3.0.1/jsr305-3.0.1.pom from central
  [0m[91mRetrieving com/google/jsinterop/jsinterop-annotations/1.0.0/jsinterop-annotations-1.0.0.pom from central
  [0m[91mRetrieving com/google/jsinterop/jsinterop/1.0.0/jsinterop-1.0.0.pom from central
  [0m[91mRetrieving org/sonatype/oss/oss-parent/4/oss-parent-4.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20170809-b9c14c6b/google-closure-library-0.0-20170809-b9c14c6b.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20170809-b9c14c6b/google-closure-library-third-party-0.0-20170809-b9c14c6b.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/1.3.0/tools.reader-1.3.0.pom from central
  [0m[91mRetrieving com/cognitect/transit-clj/0.8.309/transit-clj-0.8.309.pom from central
  [0m[91mRetrieving com/cognitect/transit-java/0.8.332/transit-java-0.8.332.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-core/2.8.7/jackson-core-2.8.7.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-parent/2.8/jackson-parent-2.8.pom from central
  [0m[91mRetrieving com/fasterxml/oss-parent/27/oss-parent-27.pom from central
  [0m[91mRetrieving org/msgpack/msgpack/0.6.12/msgpack-0.6.12.pom from central
  [0m[91mRetrieving com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.pom from central
  [0m[91mRetrieving org/javassist/javassist/3.18.1-GA/javassist-3.18.1-GA.pom from central
  [0m[91mRetrieving commons-codec/commons-codec/1.10/commons-codec-1.10.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/35/commons-parent-35.pom from central
  [0m[91mRetrieving org/apache/apache/15/apache-15.pom from central
  [0m[91mRetrieving org/clojure/tools.logging/0.4.1/tools.logging-0.4.1.pom from central
  [0m[91mRetrieving reagent/reagent/0.8.1/reagent-0.8.1.pom from clojars
  [0m[91mRetrieving cljsjs/react/16.3.2-0/react-16.3.2-0.pom from clojars
  [0m[91mRetrieving cljsjs/react-dom/16.3.2-0/react-dom-16.3.2-0.pom from clojars
  [0m[91mRetrieving cljsjs/react-dom-server/16.3.2-0/react-dom-server-16.3.2-0.pom from clojars
  [0m[91mRetrieving cljsjs/create-react-class/15.6.3-0/create-react-class-15.6.3-0.pom from clojars
  [0m[91mRetrieving cljsjs/react/15.6.1-2/react-15.6.1-2.pom from clojars
  [0m[91mRetrieving re-frame/re-frame/0.10.6/re-frame-0.10.6.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/1.9.908/clojurescript-1.9.908.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-unshaded/v20170806/closure-compiler-unshaded-v20170806.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-main/v20170806/closure-compiler-main-v20170806.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20170806/closure-compiler-parent-v20170806.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20170806/closure-compiler-externs-v20170806.pom from central
  [0m[91mRetrieving args4j/args4j/2.33/args4j-2.33.pom from central
  [0m[91mRetrieving args4j/args4j-site/2.33/args4j-site-2.33.pom from central
  [0m[91mRetrieving org/kohsuke/pom/14/pom-14.pom from central
  [0m[91mRetrieving com/google/guava/guava/20.0/guava-20.0.pom from central
  [0m[91mRetrieving com/google/guava/guava-parent/20.0/guava-parent-20.0.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/1.0.5/tools.reader-1.0.5.pom from central
  [0m[91mRetrieving reagent/reagent/0.7.0/reagent-0.7.0.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/1.9.655/clojurescript-1.9.655.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-unshaded/v20170521/closure-compiler-unshaded-v20170521.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-main/v20170521/closure-compiler-main-v20170521.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20170521/closure-compiler-parent-v20170521.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20170521/closure-compiler-externs-v20170521.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20170519-fa0499ef/google-closure-library-0.0-20170519-fa0499ef.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20170519-fa0499ef/google-closure-library-third-party-0.0-20170519-fa0499ef.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/1.0.0/tools.reader-1.0.0.pom from central
  [0m[91mRetrieving cljsjs/react-dom/15.5.4-0/react-dom-15.5.4-0.pom from clojars
  [0m[91mRetrieving cljsjs/react/15.5.4-0/react-15.5.4-0.pom from clojars
  [0m[91mRetrieving cljsjs/react-dom-server/15.5.4-0/react-dom-server-15.5.4-0.pom from clojars
  [0m[91mRetrieving cljsjs/create-react-class/15.5.3-0/create-react-class-15.5.3-0.pom from clojars
  [0m[91mRetrieving net/cgrand/macrovich/0.2.0/macrovich-0.2.0.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/1.9.293/clojurescript-1.9.293.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-unshaded/v20160911/closure-compiler-unshaded-v20160911.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-main/v20160911/closure-compiler-main-v20160911.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20160911/closure-compiler-parent-v20160911.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20160911/closure-compiler-externs-v20160911.pom from central
  [0m[91mRetrieving org/clojure/tools.logging/0.3.1/tools.logging-0.3.1.pom from central
  [0m[91mRetrieving cljs-ajax/cljs-ajax/0.8.0/cljs-ajax-0.8.0.pom from clojars
  [0m[91mRetrieving cheshire/cheshire/5.7.1/cheshire-5.7.1.pom from clojars
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-core/2.8.6/jackson-core-2.8.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformat-smile/2.8.6/jackson-dataformat-smile-2.8.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformats-binary/2.8.6/jackson-dataformats-binary-2.8.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformat-cbor/2.8.6/jackson-dataformat-cbor-2.8.6.pom from central
  [0m[91mRetrieving tigris/tigris/0.1.1/tigris-0.1.1.pom from clojars
  [0m[91mRetrieving com/cognitect/transit-cljs/0.8.256/transit-cljs-0.8.256.pom from central
  [0m[91mRetrieving com/cognitect/transit-js/0.8.846/transit-js-0.8.846.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpasyncclient/4.1.3/httpasyncclient-4.1.3.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-asyncclient/4.1.3/httpcomponents-asyncclient-4.1.3.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/project/7/project-7.pom from central
  [0m[91mRetrieving org/apache/apache/13/apache-13.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcore/4.4.6/httpcore-4.4.6.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-core/4.4.6/httpcomponents-core-4.4.6.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcore-nio/4.4.6/httpcore-nio-4.4.6.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpclient/4.5.3/httpclient-4.5.3.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-client/4.5.3/httpcomponents-client-4.5.3.pom from central
  [0m[91mRetrieving commons-logging/commons-logging/1.2/commons-logging-1.2.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/34/commons-parent-34.pom from central
  [0m[91mRetrieving commons-codec/commons-codec/1.9/commons-codec-1.9.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/32/commons-parent-32.pom from central
  [0m[91mRetrieving day8/re-frame/http-fx/0.1.6/http-fx-0.1.6.pom from clojars
  [0m[91mRetrieving re-frame/re-frame/0.8.0/re-frame-0.8.0.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/1.9.89/clojurescript-1.9.89.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler/v20160315/closure-compiler-v20160315.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/1.0.0-beta1/tools.reader-1.0.0-beta1.pom from central
  [0m[91mRetrieving reagent/reagent/0.6.0-rc/reagent-0.6.0-rc.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/1.8.51/clojurescript-1.8.51.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20151016-61277aea/google-closure-library-0.0-20151016-61277aea.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20151016-61277aea/google-closure-library-third-party-0.0-20151016-61277aea.pom from central
  [0m[91mRetrieving cljsjs/react-dom/15.1.0-0/react-dom-15.1.0-0.pom from clojars
  [0m[91mRetrieving cljsjs/react/15.1.0-0/react-15.1.0-0.pom from clojars
  [0m[91mRetrieving cljsjs/react-dom-server/15.1.0-0/react-dom-server-15.1.0-0.pom from clojars
  [0m[91mRetrieving cljs-ajax/cljs-ajax/0.7.3/cljs-ajax-0.7.3.pom from clojars
  [0m[91mRetrieving com/cognitect/transit-clj/0.8.300/transit-clj-0.8.300.pom from central
  [0m[91mRetrieving com/cognitect/transit-java/0.8.324/transit-java-0.8.324.pom from central
  [0m[91mRetrieving com/cognitect/transit-cljs/0.8.243/transit-cljs-0.8.243.pom from central
  [0m[91mRetrieving net/colourcoding/poppea/0.2.1/poppea-0.2.1.pom from clojars
  [0m[91mRetrieving org/clojure/clojure/1.5.0/clojure-1.5.0.pom from central
  [0m[91mRetrieving joda-time/joda-time/2.10.1/joda-time-2.10.1.pom from central
  [0m[91mRetrieving amazonica/amazonica/0.3.134/amazonica-0.3.134.pom from clojars
  [0m[91mRetrieving org/clojure/algo.generic/0.1.2/algo.generic-0.1.2.pom from central
  [0m[91mRetrieving com/amazonaws/dynamodb-streams-kinesis-adapter/1.2.1/dynamodb-streams-kinesis-adapter-1.2.1.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-databind/2.6.6/jackson-databind-2.6.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-parent/2.6.2/jackson-parent-2.6.2.pom from central
  [0m[91mRetrieving com/fasterxml/oss-parent/24/oss-parent-24.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-annotations/2.6.0/jackson-annotations-2.6.0.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-parent/2.6.1/jackson-parent-2.6.1.pom from central
  [0m[91mRetrieving com/fasterxml/oss-parent/23/oss-parent-23.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-core/2.6.6/jackson-core-2.6.6.pom from central
  [0m[91mRetrieving commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/28/commons-parent-28.pom from central
  [0m[91mRetrieving joda-time/joda-time/2.9.6/joda-time-2.9.6.pom from central
  [0m[91mRetrieving robert/hooke/1.3.0/hooke-1.3.0.pom from clojars
  [0m[91mRetrieving com/taoensso/nippy/2.12.2/nippy-2.12.2.pom from clojars
  [0m[91mRetrieving org/clojure/tools.reader/0.10.0/tools.reader-0.10.0.pom from central
  [0m[91mRetrieving com/taoensso/encore/2.68.0/encore-2.68.0.pom from clojars
  [0m[91mRetrieving com/taoensso/truss/1.3.4/truss-1.3.4.pom from clojars
  [0m[91mRetrieving org/iq80/snappy/snappy/0.4/snappy-0.4.pom from central
  [0m[91mRetrieving org/tukaani/xz/1.5/xz-1.5.pom from central
  [0m[91mRetrieving net/jpountz/lz4/lz4/1.3/lz4-1.3.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-core/1.11.452/aws-java-sdk-core-1.11.452.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-pom/1.11.452/aws-java-sdk-pom-1.11.452.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpclient/4.5.5/httpclient-4.5.5.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-client/4.5.5/httpcomponents-client-4.5.5.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-parent/10/httpcomponents-parent-10.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcore/4.4.9/httpcore-4.4.9.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-core/4.4.9/httpcomponents-core-4.4.9.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-parent/9/httpcomponents-parent-9.pom from central
  [0m[91mRetrieving software/amazon/ion/ion-java/1.0.2/ion-java-1.0.2.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-databind/2.6.7.1/jackson-databind-2.6.7.1.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-core/2.6.7/jackson-core-2.6.7.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformat-cbor/2.6.7/jackson-dataformat-cbor-2.6.7.pom from central
  [0m[91mRetrieving joda-time/joda-time/2.8.1/joda-time-2.8.1.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-dynamodb/1.11.452/aws-java-sdk-dynamodb-1.11.452.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-s3/1.11.452/aws-java-sdk-s3-1.11.452.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-kms/1.11.452/aws-java-sdk-kms-1.11.452.pom from central
  [0m[91mRetrieving com/amazonaws/jmespath-java/1.11.452/jmespath-java-1.11.452.pom from central
  [0m[91mRetrieving com/taoensso/faraday/1.9.0/faraday-1.9.0.pom from clojars
  [0m[91mRetrieving com/taoensso/encore/2.67.2/encore-2.67.2.pom from clojars
  [0m[91mRetrieving com/taoensso/truss/1.3.3/truss-1.3.3.pom from clojars
  [0m[91mRetrieving com/taoensso/nippy/2.12.0/nippy-2.12.0.pom from clojars
  [0m[91mRetrieving com/taoensso/encore/2.67.1/encore-2.67.1.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.9.4/joda-time-2.9.4.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-dynamodb/1.10.49/aws-java-sdk-dynamodb-1.10.49.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-pom/1.10.49/aws-java-sdk-pom-1.10.49.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-s3/1.10.49/aws-java-sdk-s3-1.10.49.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-kms/1.10.49/aws-java-sdk-kms-1.10.49.pom from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-core/1.10.49/aws-java-sdk-core-1.10.49.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpclient/4.3.6/httpclient-4.3.6.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-client/4.3.6/httpcomponents-client-4.3.6.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcore/4.3.3/httpcore-4.3.3.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-core/4.3.3/httpcomponents-core-4.3.3.pom from central
  [0m[91mRetrieving commons-codec/commons-codec/1.6/commons-codec-1.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-databind/2.5.3/jackson-databind-2.5.3.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-parent/2.5.1/jackson-parent-2.5.1.pom from central
  [0m[91mRetrieving com/fasterxml/oss-parent/19/oss-parent-19.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-annotations/2.5.0/jackson-annotations-2.5.0.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-parent/2.5/jackson-parent-2.5.pom from central
  [0m[91mRetrieving com/fasterxml/oss-parent/18/oss-parent-18.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-core/2.5.3/jackson-core-2.5.3.pom from central
  [0m[91mRetrieving commons-codec/commons-codec/1.11/commons-codec-1.11.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpclient/4.5.6/httpclient-4.5.6.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-client/4.5.6/httpcomponents-client-4.5.6.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcore/4.4.10/httpcore-4.4.10.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-core/4.4.10/httpcomponents-core-4.4.10.pom from central
  [0m[91mRetrieving ring/ring/1.7.1/ring-1.7.1.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.7.1/ring-core-1.7.1.pom from clojars
  [0m[91mRetrieving ring/ring-codec/1.1.1/ring-codec-1.1.1.pom from clojars
  [0m[91mRetrieving commons-fileupload/commons-fileupload/1.3.3/commons-fileupload-1.3.3.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/41/commons-parent-41.pom from central
  [0m[91mRetrieving commons-io/commons-io/2.2/commons-io-2.2.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/24/commons-parent-24.pom from central
  [0m[91mRetrieving clj-time/clj-time/0.14.3/clj-time-0.14.3.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.9.9/joda-time-2.9.9.pom from central
  [0m[91mRetrieving crypto-random/crypto-random/1.2.0/crypto-random-1.2.0.pom from clojars
  [0m[91mRetrieving org/clojure/clojure/1.2.1/clojure-1.2.1.pom from central
  [0m[91mRetrieving crypto-equality/crypto-equality/1.0.0/crypto-equality-1.0.0.pom from clojars
  [0m[91mRetrieving ring/ring-devel/1.7.1/ring-devel-1.7.1.pom from clojars
  [0m[91mRetrieving hiccup/hiccup/1.0.5/hiccup-1.0.5.pom from clojars
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.8/clj-stacktrace-0.2.8.pom from clojars
  [0m[91mRetrieving ns-tracker/ns-tracker/0.3.1/ns-tracker-0.3.1.pom from clojars
  [0m[91mRetrieving org/clojure/tools.namespace/0.2.11/tools.namespace-0.2.11.pom from central
  [0m[91mRetrieving org/clojure/java.classpath/0.2.3/java.classpath-0.2.3.pom from central
  [0m[91mRetrieving ring/ring-jetty-adapter/1.7.1/ring-jetty-adapter-1.7.1.pom from clojars
  [0m[91mRetrieving ring/ring-servlet/1.7.1/ring-servlet-1.7.1.pom from clojars
  [0m[91mRetrieving org/eclipse/jetty/jetty-server/9.4.12.v20180830/jetty-server-9.4.12.v20180830.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-project/9.4.12.v20180830/jetty-project-9.4.12.v20180830.pom from central
  [0m[91mRetrieving javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.pom from central
  [0m[91mRetrieving net/java/jvnet-parent/3/jvnet-parent-3.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-http/9.4.12.v20180830/jetty-http-9.4.12.v20180830.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-util/9.4.12.v20180830/jetty-util-9.4.12.v20180830.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-io/9.4.12.v20180830/jetty-io-9.4.12.v20180830.pom from central
  [0m[91mRetrieving clj-pdf/clj-pdf/2.2.33/clj-pdf-2.2.33.pom from clojars
  [0m[91mRetrieving org/jfree/jfreechart/1.0.19/jfreechart-1.0.19.pom from central
  [0m[91mRetrieving org/jfree/jcommon/1.0.23/jcommon-1.0.23.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-bridge/1.8/batik-bridge-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-anim/1.8/batik-anim-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-awt-util/1.8/batik-awt-util-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-util/1.8/batik-util-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-css/1.8/batik-css-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-ext/1.8/batik-ext-1.8.pom from central
  [0m[91mRetrieving xml-apis/xml-apis/1.3.04/xml-apis-1.3.04.pom from central
  [0m[91mRetrieving org/apache/apache/3/apache-3.pom from central
  [0m[91mRetrieving xml-apis/xml-apis-ext/1.3.04/xml-apis-ext-1.3.04.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-dom/1.8/batik-dom-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-xml/1.8/batik-xml-1.8.pom from central
  [0m[91mRetrieving xalan/xalan/2.7.0/xalan-2.7.0.pom from central
  [0m[91mRetrieving xml-apis/xml-apis/2.0.2/xml-apis-2.0.2.pom from central
  [0m[91mRetrieving xml-apis/xml-apis/1.0.b2/xml-apis-1.0.b2.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-parser/1.8/batik-parser-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-svg-dom/1.8/batik-svg-dom-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-gvt/1.8/batik-gvt-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-script/1.8/batik-script-1.8.pom from central
  [0m[91mRetrieving org/apache/xmlgraphics/xmlgraphics-commons/2.1/xmlgraphics-commons-2.1.pom from central
  [0m[91mRetrieving org/apache/apache/7/apache-7.pom from central
  [0m[91mRetrieving commons-io/commons-io/1.3.1/commons-io-1.3.1.pom from central
  [0m[91mRetrieving commons-logging/commons-logging/1.0.4/commons-logging-1.0.4.pom from central
  [0m[91mRetrieving org/clojure/core.async/0.4.474/core.async-0.4.474.pom from central
  [0m[91mRetrieving danlentz/clj-uuid/0.1.7/clj-uuid-0.1.7.pom from clojars
  [0m[91mRetrieving primitive-math/primitive-math/0.1.4/primitive-math-0.1.4.pom from clojars
  [0m[91mRetrieving com/lucasbradstreet/cljs-uuid-utils/1.0.2/cljs-uuid-utils-1.0.2.pom from clojars
  [0m[91mRetrieving clj-time/clj-time/0.15.1/clj-time-0.15.1.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.10/joda-time-2.10.pom from central
  [0m[91mRetrieving com/andrewmcveigh/cljs-time/0.5.2/cljs-time-0.5.2.pom from clojars
  [0m[91mRetrieving re-com/re-com/2.4.0/re-com-2.4.0.pom from clojars
  [0m[91mRetrieving compojure/compojure/1.6.1/compojure-1.6.1.pom from clojars
  [0m[91mRetrieving org/clojure/tools.macro/0.1.5/tools.macro-0.1.5.pom from central
  [0m[91mRetrieving clout/clout/2.2.1/clout-2.2.1.pom from clojars
  [0m[91mRetrieving instaparse/instaparse/1.4.8/instaparse-1.4.8.pom from clojars
  [0m[91mRetrieving medley/medley/1.0.0/medley-1.0.0.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.6.3/ring-core-1.6.3.pom from clojars
  [0m[91mRetrieving ring/ring-codec/1.0.1/ring-codec-1.0.1.pom from clojars
  [0m[91mRetrieving commons-io/commons-io/2.5/commons-io-2.5.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/39/commons-parent-39.pom from central
  [0m[91mRetrieving org/apache/apache/16/apache-16.pom from central
  [0m[91mRetrieving clj-time/clj-time/0.11.0/clj-time-0.11.0.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.8.2/joda-time-2.8.2.pom from central
  [0m[91mRetrieving ring/ring-codec/1.1.0/ring-codec-1.1.0.pom from clojars
  [0m[91mRetrieving yogthos/config/1.1.1/config-1.1.1.pom from clojars
  [0m[91mRetrieving org/clojure/tools.logging/0.4.0/tools.logging-0.4.0.pom from central
  [0m[91mRetrieving clj-jwt/clj-jwt/0.1.1/clj-jwt-0.1.1.pom from clojars
  [0m[91mRetrieving org/clojure/data.codec/0.1.0/data.codec-0.1.0.pom from central
  [0m[91mRetrieving org/clojure/pom.contrib/0.0.25/pom.contrib-0.0.25.pom from central
  [0m[91mRetrieving org/bouncycastle/bcpkix-jdk15on/1.52/bcpkix-jdk15on-1.52.pom from central
  [0m[91mRetrieving org/bouncycastle/bcprov-jdk15on/1.52/bcprov-jdk15on-1.52.pom from central
  [0m[91mRetrieving buddy/buddy-core/1.5.0/buddy-core-1.5.0.pom from clojars
  [0m[91mRetrieving cheshire/cheshire/5.8.0/cheshire-5.8.0.pom from clojars
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-core/2.9.0/jackson-core-2.9.0.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-parent/2.9.0/jackson-parent-2.9.0.pom from central
  [0m[91mRetrieving com/fasterxml/oss-parent/28/oss-parent-28.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformat-smile/2.9.0/jackson-dataformat-smile-2.9.0.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformats-binary/2.9.0/jackson-dataformats-binary-2.9.0.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-bom/2.9.0/jackson-bom-2.9.0.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformat-cbor/2.9.0/jackson-dataformat-cbor-2.9.0.pom from central
  [0m[91mRetrieving net/i2p/crypto/eddsa/0.3.0/eddsa-0.3.0.pom from central
  [0m[91mRetrieving org/bouncycastle/bcprov-jdk15on/1.59/bcprov-jdk15on-1.59.pom from central
  [0m[91mRetrieving org/bouncycastle/bcpkix-jdk15on/1.59/bcpkix-jdk15on-1.59.pom from central
  [0m[91mRetrieving org/clojars/bskinny/aws-sdk-js/2.394.0-1/aws-sdk-js-2.394.0-1.pom from clojars
  [0m[91mRetrieving ring/ring-defaults/0.3.2/ring-defaults-0.3.2.pom from clojars
  [0m[91mRetrieving ring/ring-ssl/0.3.0/ring-ssl-0.3.0.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.6.0/ring-core-1.6.0.pom from clojars
  [0m[91mRetrieving commons-fileupload/commons-fileupload/1.3.2/commons-fileupload-1.3.2.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/40/commons-parent-40.pom from central
  [0m[91mRetrieving org/apache/apache/17/apache-17.pom from central
  [0m[91mRetrieving ring/ring-headers/0.3.0/ring-headers-0.3.0.pom from clojars
  [0m[91mRetrieving ring/ring-anti-forgery/1.3.0/ring-anti-forgery-1.3.0.pom from clojars
  [0m[91mRetrieving ring-middleware-format/ring-middleware-format/0.7.4/ring-middleware-format-0.7.4.pom from clojars
  [0m[91mRetrieving org/clojure/core.memoize/0.7.1/core.memoize-0.7.1.pom from central
  [0m[91mRetrieving org/clojure/core.cache/0.7.1/core.cache-0.7.1.pom from central
  [0m[91mRetrieving cheshire/cheshire/5.8.1/cheshire-5.8.1.pom from clojars
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-core/2.9.6/jackson-core-2.9.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-base/2.9.6/jackson-base-2.9.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-bom/2.9.6/jackson-bom-2.9.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/jackson-parent/2.9.1.1/jackson-parent-2.9.1.1.pom from central
  [0m[91mRetrieving com/fasterxml/oss-parent/33/oss-parent-33.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformat-smile/2.9.6/jackson-dataformat-smile-2.9.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformats-binary/2.9.6/jackson-dataformats-binary-2.9.6.pom from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformat-cbor/2.9.6/jackson-dataformat-cbor-2.9.6.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/1.3.2/tools.reader-1.3.2.pom from central
  [0m[91mRetrieving clj-commons/clj-yaml/0.6.0/clj-yaml-0.6.0.pom from clojars
  [0m[91mRetrieving org/yaml/snakeyaml/1.23/snakeyaml-1.23.pom from central
  [0m[91mRetrieving org/flatland/ordered/1.5.7/ordered-1.5.7.pom from clojars
  [0m[91mRetrieving org/flatland/useful/0.11.6/useful-0.11.6.pom from clojars
  [0m[91mRetrieving org/clojure/tools.macro/0.1.1/tools.macro-0.1.1.pom from central
  [0m[91mRetrieving org/clojure/pom.contrib/0.0.20/pom.contrib-0.0.20.pom from central
  [0m[91mRetrieving org/clojure/clojure/1.3.0-alpha5/clojure-1.3.0-alpha5.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/0.7.2/tools.reader-0.7.2.pom from central
  [0m[91mRetrieving clojure-msgpack/clojure-msgpack/1.2.1/clojure-msgpack-1.2.1.pom from clojars
  [0m[91mRetrieving com/cognitect/transit-clj/0.8.313/transit-clj-0.8.313.pom from central
  [0m[91mRetrieving com/cognitect/transit-java/0.8.337/transit-java-0.8.337.pom from central
  [0m[91mRetrieving javax/xml/bind/jaxb-api/2.3.0/jaxb-api-2.3.0.pom from central
  [0m[91mRetrieving javax/xml/bind/jaxb-api-parent/2.3.0/jaxb-api-parent-2.3.0.pom from central
  [0m[91mRetrieving net/java/jvnet-parent/5/jvnet-parent-5.pom from central
  [0m[91mRetrieving ring-logger/ring-logger/0.7.8/ring-logger-0.7.8.pom from clojars
  [0m[91mRetrieving org/clojars/pjlegato/clansi/1.3.0/clansi-1.3.0.pom from clojars
  [0m[91mRetrieving clj-http/clj-http/2.3.0/clj-http-2.3.0.pom from clojars
  [0m[91mRetrieving org/apache/httpcomponents/httpcore/4.4.5/httpcore-4.4.5.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-core/4.4.5/httpcomponents-core-4.4.5.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpclient/4.5.2/httpclient-4.5.2.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-client/4.5.2/httpcomponents-client-4.5.2.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcore/4.4.4/httpcore-4.4.4.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcomponents-core/4.4.4/httpcomponents-core-4.4.4.pom from central
  [0m[91mRetrieving org/apache/httpcomponents/httpmime/4.5.2/httpmime-4.5.2.pom from central
  [0m[91mRetrieving slingshot/slingshot/0.12.2/slingshot-0.12.2.pom from clojars
  [0m[91mRetrieving potemkin/potemkin/0.4.3/potemkin-0.4.3.pom from clojars
  [0m[91mRetrieving clj-tuple/clj-tuple/0.2.2/clj-tuple-0.2.2.pom from clojars
  [0m[91mRetrieving riddley/riddley/0.1.12/riddley-0.1.12.pom from clojars
  [0m[91mRetrieving hickory/hickory/0.7.1/hickory-0.7.1.pom from clojars
  [0m[91mRetrieving org/jsoup/jsoup/1.9.2/jsoup-1.9.2.pom from central
  [0m[91mRetrieving viebel/codox-klipse-theme/0.0.1/codox-klipse-theme-0.0.1.pom from clojars
  [0m[91mRetrieving quoin/quoin/0.1.2/quoin-0.1.2.pom from clojars
  [0m[91mRetrieving clj-commons/secretary/1.2.4/secretary-1.2.4.pom from clojars
  [0m[91mRetrieving com/cemerick/clojurescript.test/0.2.3-SNAPSHOT/clojurescript.test-0.2.3-20140317.141743-3.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/0.0-2014/clojurescript-0.0-2014.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler/v20130603/closure-compiler-v20130603.pom from central
  [0m[91mRetrieving args4j/args4j/2.0.16/args4j-2.0.16.pom from central
  [0m[91mRetrieving args4j/args4j-site/2.0.16/args4j-site-2.0.16.pom from central
  [0m[91mRetrieving com/google/guava/guava/14.0.1/guava-14.0.1.pom from central
  [0m[91mRetrieving com/google/guava/guava-parent/14.0.1/guava-parent-14.0.1.pom from central
  [0m[91mRetrieving com/google/protobuf/protobuf-java/2.4.1/protobuf-java-2.4.1.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20130212-95c19e7f0f5f/google-closure-library-0.0-20130212-95c19e7f0f5f.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20130212-95c19e7f0f5f/google-closure-library-third-party-0.0-20130212-95c19e7f0f5f.pom from central
  [0m[91mRetrieving org/clojure/data.json/0.2.3/data.json-0.2.3.pom from central
  [0m[91mRetrieving org/mozilla/rhino/1.7R4/rhino-1.7R4.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/0.7.10/tools.reader-0.7.10.pom from central
  [0m[91mRetrieving venantius/accountant/0.2.4/accountant-0.2.4.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/1.7.48/clojurescript-1.7.48.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler/v20150729/closure-compiler-v20150729.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20150729/closure-compiler-parent-v20150729.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20150729/closure-compiler-externs-v20150729.pom from central
  [0m[91mRetrieving com/google/guava/guava/18.0/guava-18.0.pom from central
  [0m[91mRetrieving com/google/guava/guava-parent/18.0/guava-parent-18.0.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20150805-acd8b553/google-closure-library-0.0-20150805-acd8b553.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20150805-acd8b553/google-closure-library-third-party-0.0-20150805-acd8b553.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/0.10.0-alpha3/tools.reader-0.10.0-alpha3.pom from central
  [0m[91mRetrieving binaryage/devtools/0.9.10/devtools-0.9.10.pom from clojars
  [0m[91mRetrieving binaryage/env-config/0.2.2/env-config-0.2.2.pom from clojars
  [0m[91mRetrieving com/google/errorprone/error_prone_annotations/2.0.18/error_prone_annotations-2.0.18.jar from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20180805/closure-compiler-externs-v20180805.jar from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-unshaded/v20180805/closure-compiler-unshaded-v20180805.jar from central
  [0m[91mRetrieving org/clojure/clojurescript/1.10.439/clojurescript-1.10.439.jar from central
  [0m[91mRetrieving com/google/guava/guava/25.1-jre/guava-25.1-jre.jar from central
  [0m[91mRetrieving org/checkerframework/checker-qual/2.0.0/checker-qual-2.0.0.jar from central
  [0m[91mRetrieving com/google/j2objc/j2objc-annotations/1.1/j2objc-annotations-1.1.jar from central
  [0m[91mRetrieving org/codehaus/mojo/animal-sniffer-annotations/1.14/animal-sniffer-annotations-1.14.jar from central
  [0m[91mRetrieving com/google/protobuf/protobuf-java/3.0.2/protobuf-java-3.0.2.jar from central
  [0m[91mRetrieving com/google/code/gson/gson/2.7/gson-2.7.jar from central
  [0m[91mRetrieving com/google/code/findbugs/jsr305/3.0.1/jsr305-3.0.1.jar from central
  [0m[91mRetrieving com/google/jsinterop/jsinterop-annotations/1.0.0/jsinterop-annotations-1.0.0.jar from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20170809-b9c14c6b/google-closure-library-0.0-20170809-b9c14c6b.jar from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20170809-b9c14c6b/google-closure-library-third-party-0.0-20170809-b9c14c6b.jar from central
  [0m[91mRetrieving org/clojure/tools.reader/1.3.0/tools.reader-1.3.0.jar from central
  [0m[91mRetrieving com/cognitect/transit-clj/0.8.309/transit-clj-0.8.309.jar from central
  [0m[91mRetrieving com/cognitect/transit-java/0.8.332/transit-java-0.8.332.jar from central
  [0m[91mRetrieving org/msgpack/msgpack/0.6.12/msgpack-0.6.12.jar from central
  [0m[91mRetrieving com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar from central
  [0m[91mRetrieving org/javassist/javassist/3.18.1-GA/javassist-3.18.1-GA.jar from central
  [0m[91mRetrieving org/clojure/tools.logging/0.4.1/tools.logging-0.4.1.jar from central
  [0m[91mRetrieving com/cognitect/transit-cljs/0.8.256/transit-cljs-0.8.256.jar from central
  [0m[91mRetrieving org/apache/httpcomponents/httpasyncclient/4.1.3/httpasyncclient-4.1.3.jar from central
  [0m[91mRetrieving com/cognitect/transit-js/0.8.846/transit-js-0.8.846.jar from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcore-nio/4.4.6/httpcore-nio-4.4.6.jar from central
  [0m[91mRetrieving org/apache/httpcomponents/httpcore/4.4.6/httpcore-4.4.6.jar from central
  [0m[91mRetrieving joda-time/joda-time/2.10.1/joda-time-2.10.1.jar from central
  [0m[91mRetrieving org/clojure/algo.generic/0.1.2/algo.generic-0.1.2.jar from central
  [0m[91mRetrieving com/amazonaws/dynamodb-streams-kinesis-adapter/1.2.1/dynamodb-streams-kinesis-adapter-1.2.1.jar from central
  [0m[91mRetrieving org/iq80/snappy/snappy/0.4/snappy-0.4.jar from central
  [0m[91mRetrieving org/tukaani/xz/1.5/xz-1.5.jar from central
  [0m[91mRetrieving net/jpountz/lz4/lz4/1.3/lz4-1.3.jar from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-core/1.11.452/aws-java-sdk-core-1.11.452.jar from central
  [0m[91mRetrieving commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar from central
  [0m[91mRetrieving software/amazon/ion/ion-java/1.0.2/ion-java-1.0.2.jar from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-databind/2.6.7.1/jackson-databind-2.6.7.1.jar from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-annotations/2.6.0/jackson-annotations-2.6.0.jar from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformat-cbor/2.6.7/jackson-dataformat-cbor-2.6.7.jar from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-dynamodb/1.11.452/aws-java-sdk-dynamodb-1.11.452.jar from central
  [0m[91mRetrieving com/amazonaws/jmespath-java/1.11.452/jmespath-java-1.11.452.jar from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-s3/1.11.452/aws-java-sdk-s3-1.11.452.jar from central
  [0m[91mRetrieving com/amazonaws/aws-java-sdk-kms/1.11.452/aws-java-sdk-kms-1.11.452.jar from central
  [0m[91mRetrieving commons-codec/commons-codec/1.11/commons-codec-1.11.jar from central
  [0m[91mRetrieving org/apache/httpcomponents/httpclient/4.5.6/httpclient-4.5.6.jar from central
  [0m[91mRetrieving commons-fileupload/commons-fileupload/1.3.3/commons-fileupload-1.3.3.jar from central
  [0m[91mRetrieving org/clojure/tools.namespace/0.2.11/tools.namespace-0.2.11.jar from central
  [0m[91mRetrieving org/clojure/java.classpath/0.2.3/java.classpath-0.2.3.jar from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-server/9.4.12.v20180830/jetty-server-9.4.12.v20180830.jar from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-http/9.4.12.v20180830/jetty-http-9.4.12.v20180830.jar from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-util/9.4.12.v20180830/jetty-util-9.4.12.v20180830.jar from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-io/9.4.12.v20180830/jetty-io-9.4.12.v20180830.jar from central
  [0m[91mRetrieving org/jfree/jcommon/1.0.23/jcommon-1.0.23.jar from central
  [0m[91mRetrieving org/jfree/jfreechart/1.0.19/jfreechart-1.0.19.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-bridge/1.8/batik-bridge-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-awt-util/1.8/batik-awt-util-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-css/1.8/batik-css-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-dom/1.8/batik-dom-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-ext/1.8/batik-ext-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-gvt/1.8/batik-gvt-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-parser/1.8/batik-parser-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-script/1.8/batik-script-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-svg-dom/1.8/batik-svg-dom-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-util/1.8/batik-util-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-xml/1.8/batik-xml-1.8.jar from central
  [0m[91mRetrieving xalan/xalan/2.7.0/xalan-2.7.0.jar from central
  [0m[91mRetrieving xml-apis/xml-apis/1.3.04/xml-apis-1.3.04.jar from central
  [0m[91mRetrieving xml-apis/xml-apis-ext/1.3.04/xml-apis-ext-1.3.04.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/batik-anim/1.8/batik-anim-1.8.jar from central
  [0m[91mRetrieving org/apache/xmlgraphics/xmlgraphics-commons/2.1/xmlgraphics-commons-2.1.jar from central
  [0m[91mRetrieving org/clojure/core.async/0.4.474/core.async-0.4.474.jar from central
  [0m[91mRetrieving org/clojure/tools.macro/0.1.5/tools.macro-0.1.5.jar from central
  [0m[91mRetrieving org/clojure/core.specs.alpha/0.1.24/core.specs.alpha-0.1.24.jar from central
  [0m[91mRetrieving org/clojure/data.codec/0.1.0/data.codec-0.1.0.jar from central
  [0m[91mRetrieving org/bouncycastle/bcpkix-jdk15on/1.52/bcpkix-jdk15on-1.52.jar from central
  [0m[91mRetrieving net/i2p/crypto/eddsa/0.3.0/eddsa-0.3.0.jar from central
  [0m[91mRetrieving org/bouncycastle/bcprov-jdk15on/1.59/bcprov-jdk15on-1.59.jar from central
  [0m[91mRetrieving javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar from central
  [0m[91mRetrieving org/clojure/core.memoize/0.7.1/core.memoize-0.7.1.jar from central
  [0m[91mRetrieving org/clojure/core.cache/0.7.1/core.cache-0.7.1.jar from central
  [0m[91mRetrieving org/yaml/snakeyaml/1.23/snakeyaml-1.23.jar from central
  [0m[91mRetrieving org/apache/httpcomponents/httpmime/4.5.2/httpmime-4.5.2.jar from central
  [0m[91mRetrieving commons-io/commons-io/2.5/commons-io-2.5.jar from central
  [0m[91mRetrieving org/jsoup/jsoup/1.9.2/jsoup-1.9.2.jar from central
  [0m[91mRetrieving com/fasterxml/jackson/core/jackson-core/2.9.6/jackson-core-2.9.6.jar from central
  [0m[91mRetrieving com/fasterxml/jackson/dataformat/jackson-dataformat-smile/2.9.6/jackson-dataformat-smile-2.9.6.jar from central
  [0m[91mRetrieving reagent/reagent/0.8.1/reagent-0.8.1.jar from clojars
  [0m[91mRetrieving cljsjs/react-dom/16.3.2-0/react-dom-16.3.2-0.jar from clojars
  [0m[91mRetrieving cljsjs/react-dom-server/16.3.2-0/react-dom-server-16.3.2-0.jar from clojars
  [0m[91mRetrieving cljsjs/create-react-class/15.6.3-0/create-react-class-15.6.3-0.jar from clojars
  [0m[91mRetrieving net/cgrand/macrovich/0.2.0/macrovich-0.2.0.jar from clojars
  [0m[91mRetrieving re-frame/re-frame/0.10.6/re-frame-0.10.6.jar from clojars
  [0m[91mRetrieving robert/hooke/1.3.0/hooke-1.3.0.jar from clojars
  [0m[91mRetrieving com/taoensso/nippy/2.12.2/nippy-2.12.2.jar from clojars
  [0m[91mRetrieving cljsjs/react/16.3.2-0/react-16.3.2-0.jar from clojars
  [0m[91mRetrieving cljs-ajax/cljs-ajax/0.8.0/cljs-ajax-0.8.0.jar from clojars
  [0m[91mRetrieving amazonica/amazonica/0.3.134/amazonica-0.3.134.jar from clojars
  [0m[91mRetrieving day8/re-frame/http-fx/0.1.6/http-fx-0.1.6.jar from clojars
  [0m[91mRetrieving com/taoensso/faraday/1.9.0/faraday-1.9.0.jar from clojars
  [0m[91mRetrieving ring/ring/1.7.1/ring-1.7.1.jar from clojars
  [0m[91mRetrieving crypto-random/crypto-random/1.2.0/crypto-random-1.2.0.jar from clojars
  [0m[91mRetrieving ring/ring-devel/1.7.1/ring-devel-1.7.1.jar from clojars
  [0m[91mRetrieving hiccup/hiccup/1.0.5/hiccup-1.0.5.jar from clojars
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.8/clj-stacktrace-0.2.8.jar from clojars
  [0m[91mRetrieving ns-tracker/ns-tracker/0.3.1/ns-tracker-0.3.1.jar from clojars
  [0m[91mRetrieving ring/ring-jetty-adapter/1.7.1/ring-jetty-adapter-1.7.1.jar from clojars
  [0m[91mRetrieving ring/ring-core/1.7.1/ring-core-1.7.1.jar from clojars
  [0m[91mRetrieving ring/ring-servlet/1.7.1/ring-servlet-1.7.1.jar from clojars
  [0m[91mRetrieving com/taoensso/encore/2.67.2/encore-2.67.2.jar from clojars
  [0m[91mRetrieving com/taoensso/truss/1.3.3/truss-1.3.3.jar from clojars
  [0m[91mRetrieving clj-pdf/clj-pdf/2.2.33/clj-pdf-2.2.33.jar from clojars
  [0m[91mRetrieving danlentz/clj-uuid/0.1.7/clj-uuid-0.1.7.jar from clojars
  [0m[91mRetrieving primitive-math/primitive-math/0.1.4/primitive-math-0.1.4.jar from clojars
  [0m[91mRetrieving com/lucasbradstreet/cljs-uuid-utils/1.0.2/cljs-uuid-utils-1.0.2.jar from clojars
  [0m[91mRetrieving clj-time/clj-time/0.15.1/clj-time-0.15.1.jar from clojars
  [0m[91mRetrieving com/andrewmcveigh/cljs-time/0.5.2/cljs-time-0.5.2.jar from clojars
  [0m[91mRetrieving compojure/compojure/1.6.1/compojure-1.6.1.jar from clojars
  [0m[91mRetrieving clout/clout/2.2.1/clout-2.2.1.jar from clojars
  [0m[91mRetrieving instaparse/instaparse/1.4.8/instaparse-1.4.8.jar from clojars
  [0m[91mRetrieving medley/medley/1.0.0/medley-1.0.0.jar from clojars
  [0m[91mRetrieving ring/ring-codec/1.1.0/ring-codec-1.1.0.jar from clojars
  [0m[91mRetrieving clj-jwt/clj-jwt/0.1.1/clj-jwt-0.1.1.jar from clojars
  [0m[91mRetrieving crypto-equality/crypto-equality/1.0.0/crypto-equality-1.0.0.jar from clojars
  [0m[91mRetrieving buddy/buddy-core/1.5.0/buddy-core-1.5.0.jar from clojars
  [0m[91mRetrieving re-com/re-com/2.4.0/re-com-2.4.0.jar from clojars
  [0m[91mRetrieving yogthos/config/1.1.1/config-1.1.1.jar from clojars
  [0m[91mRetrieving ring/ring-ssl/0.3.0/ring-ssl-0.3.0.jar from clojars
  [0m[91mRetrieving ring/ring-headers/0.3.0/ring-headers-0.3.0.jar from clojars
  [0m[91mRetrieving ring/ring-anti-forgery/1.3.0/ring-anti-forgery-1.3.0.jar from clojars
  [0m[91mRetrieving org/clojars/bskinny/aws-sdk-js/2.394.0-1/aws-sdk-js-2.394.0-1.jar from clojars
  [0m[91mRetrieving clj-commons/clj-yaml/0.6.0/clj-yaml-0.6.0.jar from clojars
  [0m[91mRetrieving ring/ring-defaults/0.3.2/ring-defaults-0.3.2.jar from clojars
  [0m[91mRetrieving ring-middleware-format/ring-middleware-format/0.7.4/ring-middleware-format-0.7.4.jar from clojars
  [0m[91mRetrieving org/flatland/useful/0.11.6/useful-0.11.6.jar from clojars
  [0m[91mRetrieving org/flatland/ordered/1.5.7/ordered-1.5.7.jar from clojars
  [0m[91mRetrieving org/clojars/pjlegato/clansi/1.3.0/clansi-1.3.0.jar from clojars
  [0m[91mRetrieving clj-http/clj-http/2.3.0/clj-http-2.3.0.jar from clojars
  [0m[91mRetrieving potemkin/potemkin/0.4.3/potemkin-0.4.3.jar from clojars
  [0m[91mRetrieving clj-tuple/clj-tuple/0.2.2/clj-tuple-0.2.2.jar from clojars
  [0m[91mRetrieving riddley/riddley/0.1.12/riddley-0.1.12.jar from clojars
  [0m[91mRetrieving hickory/hickory/0.7.1/hickory-0.7.1.jar from clojars
  [0m[91mRetrieving viebel/codox-klipse-theme/0.0.1/codox-klipse-theme-0.0.1.jar from clojars
  [0m[91mRetrieving quoin/quoin/0.1.2/quoin-0.1.2.jar from clojars
  [0m[91mRetrieving tigris/tigris/0.1.1/tigris-0.1.1.jar from clojars
  [0m[91mRetrieving cheshire/cheshire/5.8.1/cheshire-5.8.1.jar from clojars
  [0m[91mRetrieving clojure-msgpack/clojure-msgpack/1.2.1/clojure-msgpack-1.2.1.jar from clojars
  [0m[91mRetrieving com/cemerick/clojurescript.test/0.2.3-SNAPSHOT/clojurescript.test-0.2.3-20140317.141743-3.jar from clojars
  [0m[91mRetrieving slingshot/slingshot/0.12.2/slingshot-0.12.2.jar from clojars
  [0m[91mRetrieving binaryage/devtools/0.9.10/devtools-0.9.10.jar from clojars
  [0m[91mRetrieving binaryage/env-config/0.2.2/env-config-0.2.2.jar from clojars
  [0m[91mRetrieving venantius/accountant/0.2.4/accountant-0.2.4.jar from clojars
  [0m[91mRetrieving ring-logger/ring-logger/0.7.8/ring-logger-0.7.8.jar from clojars
  [0m[91mRetrieving clj-commons/secretary/1.2.4/secretary-1.2.4.jar from clojars
  [0mRemoving intermediate container df53464f0f73
   ---> b682af240237
  Step 6/9 : COPY . /app
   ---> 1dff65cb5aac
  Step 7/9 : EXPOSE 3000
   ---> Running in f116f963a811
  Removing intermediate container f116f963a811
   ---> fabb85d7afb1
  Step 8/9 : RUN lein with-profile webapp ring uberjar
   ---> Running in 041d0139bdfb
  [91mRetrieving ring-server/ring-server/0.5.0/ring-server-0.5.0.pom from clojars
  [0m[91mRetrieving ring/ring/1.3.2/ring-1.3.2.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.3.2/ring-core-1.3.2.pom from clojars
  [0m[91mRetrieving org/clojure/tools.reader/0.8.1/tools.reader-0.8.1.pom from central
  [0m[91mRetrieving ring/ring-codec/1.0.0/ring-codec-1.0.0.pom from clojars
  [0m[91mRetrieving commons-io/commons-io/2.4/commons-io-2.4.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/25/commons-parent-25.pom from central
  [0m[91mRetrieving commons-fileupload/commons-fileupload/1.3/commons-fileupload-1.3.pom from central
  [0m[91mRetrieving clj-time/clj-time/0.6.0/clj-time-0.6.0.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.2/joda-time-2.2.pom from central
  [0m[91mRetrieving ring/ring-devel/1.3.2/ring-devel-1.3.2.pom from clojars
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.7/clj-stacktrace-0.2.7.pom from clojars
  [0m[91mRetrieving ns-tracker/ns-tracker/0.2.2/ns-tracker-0.2.2.pom from clojars
  [0m[91mRetrieving org/clojure/tools.namespace/0.2.4/tools.namespace-0.2.4.pom from central
  [0m[91mRetrieving org/clojure/java.classpath/0.2.2/java.classpath-0.2.2.pom from central
  [0m[91mRetrieving ring/ring-jetty-adapter/1.3.2/ring-jetty-adapter-1.3.2.pom from clojars
  [0m[91mRetrieving ring/ring-servlet/1.3.2/ring-servlet-1.3.2.pom from clojars
  [0m[91mRetrieving org/eclipse/jetty/jetty-server/7.6.13.v20130916/jetty-server-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-project/7.6.13.v20130916/jetty-project-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-parent/20/jetty-parent-20.pom from central
  [0m[91mRetrieving org/eclipse/jetty/orbit/javax.servlet/2.5.0.v201103041518/javax.servlet-2.5.0.v201103041518.pom from central
  [0m[91mRetrieving org/eclipse/jetty/orbit/jetty-orbit/1/jetty-orbit-1.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-parent/18/jetty-parent-18.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-continuation/7.6.13.v20130916/jetty-continuation-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-http/7.6.13.v20130916/jetty-http-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-io/7.6.13.v20130916/jetty-io-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-util/7.6.13.v20130916/jetty-util-7.6.13.v20130916.pom from central
  [0m[91mRetrieving ring-refresh/ring-refresh/0.1.2/ring-refresh-0.1.2.pom from clojars
  [0m[91mRetrieving watchtower/watchtower/0.1.1/watchtower-0.1.1.pom from clojars
  [0m[91mRetrieving compojure/compojure/1.1.5/compojure-1.1.5.pom from clojars
  [0m[91mRetrieving org/clojure/core.incubator/0.1.0/core.incubator-0.1.0.pom from central
  [0m[91mRetrieving org/clojure/tools.macro/0.1.0/tools.macro-0.1.0.pom from central
  [0m[91mRetrieving clout/clout/1.0.1/clout-1.0.1.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.1.7/ring-core-1.1.7.pom from clojars
  [0m[91mRetrieving commons-io/commons-io/2.1/commons-io-2.1.pom from central
  [0m[91mRetrieving commons-fileupload/commons-fileupload/1.2.1/commons-fileupload-1.2.1.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/7/commons-parent-7.pom from central
  [0m[91mRetrieving org/apache/apache/4/apache-4.pom from central
  [0m[91mRetrieving javax/servlet/servlet-api/2.5/servlet-api-2.5.pom from central
  [0m[91mRetrieving clj-time/clj-time/0.3.7/clj-time-0.3.7.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.0/joda-time-2.0.pom from central
  [0m[91mRetrieving ring/ring/1.6.1/ring-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.6.1/ring-core-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-devel/1.6.1/ring-devel-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-jetty-adapter/1.6.1/ring-jetty-adapter-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-servlet/1.6.1/ring-servlet-1.6.1.pom from clojars
  [0m[91mRetrieving org/eclipse/jetty/jetty-server/9.2.21.v20170120/jetty-server-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-project/9.2.21.v20170120/jetty-project-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-parent/23/jetty-parent-23.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-http/9.2.21.v20170120/jetty-http-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-util/9.2.21.v20170120/jetty-util-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-io/9.2.21.v20170120/jetty-io-9.2.21.v20170120.pom from central
  [0m[91mRetrieving watchtower/watchtower/0.1.1/watchtower-0.1.1.jar from clojars
  [0m[91mRetrieving ring-server/ring-server/0.5.0/ring-server-0.5.0.jar from clojars
  [0m[91mRetrieving ring-refresh/ring-refresh/0.1.2/ring-refresh-0.1.2.jar from clojars
  [0m[91mCompiling helodali.server
  [0m[91m2019-04-19 21:19:07.387:INFO::main: Logging initialized @36118ms to org.eclipse.jetty.util.log.StdErrLog[0m[91m
  [0m[91mCompiling helodali.handler.main
  [0mCompiling ClojureScript...
  [91mRetrieving cljsbuild/cljsbuild/1.1.7/cljsbuild-1.1.7.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/0.0-3211/clojurescript-0.0-3211.pom from central
  [0m[91mRetrieving org/clojure/clojure/1.7.0-beta1/clojure-1.7.0-beta1.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler/v20150126/closure-compiler-v20150126.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20150126/closure-compiler-parent-v20150126.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20150126/closure-compiler-externs-v20150126.pom from central
  [0m[91mRetrieving com/google/truth/truth/0.24/truth-0.24.pom from central
  [0m[91mRetrieving com/google/truth/truth-parent/0.24/truth-parent-0.24.pom from central
  [0m[91mRetrieving com/google/guava/guava/17.0/guava-17.0.pom from central
  [0m[91mRetrieving com/google/guava/guava-parent/17.0/guava-parent-17.0.pom from central
  [0m[91mRetrieving junit/junit/4.10/junit-4.10.pom from central
  [0m[91mRetrieving org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.pom from central
  [0m[91mRetrieving org/hamcrest/hamcrest-parent/1.1/hamcrest-parent-1.1.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20140718-946a7d39/google-closure-library-0.0-20140718-946a7d39.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20140718-946a7d39/google-closure-library-third-party-0.0-20140718-946a7d39.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/0.9.1/tools.reader-0.9.1.pom from central
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.5/clj-stacktrace-0.2.5.pom from clojars
  [0m[91mRetrieving cljsbuild/cljsbuild/1.1.7/cljsbuild-1.1.7.jar from clojars
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.5/clj-stacktrace-0.2.5.jar from clojars
  [0mCompiling ["resources/public/js/compiled/app.js"] from ["src/cljs" "src/cljc" "env/prod/cljs"]...
  [32mSuccessfully compiled ["resources/public/js/compiled/app.js"] in 100.402 seconds.[0m
  Compiling ClojureScript...
  Created /app/target/helodali-0.1.0-SNAPSHOT.jar
  Created /app/target/helodali.jar
  Removing intermediate container 041d0139bdfb
   ---> 0bb98cf711dd
  Step 9/9 : CMD ["java", "-jar", "target/helodali.jar"]
   ---> Running in dc462d46b0a0
  Removing intermediate container dc462d46b0a0
   ---> e9d0c35da1ce
  Successfully built e9d0c35da1ce
  Successfully tagged aws_beanstalk/staging-app:latest
  Successfully built aws_beanstalk/staging-app
[i-0111355a4891555ec] [2019-04-19T21:21:43.325Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/AppDeployPreHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/appdeploy/pre.
[i-0111355a4891555ec] [2019-04-19T21:21:43.326Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/EbExtensionPostBuild] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:43.887Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/EbExtensionPostBuild/Infra-EmbeddedPostBuild] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:43.887Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/EbExtensionPostBuild/Infra-EmbeddedPostBuild] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:21:43.904Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/EbExtensionPostBuild] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:21:43.904Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/InfraCleanEbExtension] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:43.905Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0/InfraCleanEbExtension] : Completed activity. Result:
  Cleaned ebextensions subdirectories from /tmp.
[i-0111355a4891555ec] [2019-04-19T21:21:43.906Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage0] : Completed activity. Result:
  Application deployment - Command CMD-Startup stage 0 completed
[i-0111355a4891555ec] [2019-04-19T21:21:43.906Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:43.906Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployEnactHook] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:43.908Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployEnactHook/00run.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:50.435Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployEnactHook/00run.sh] : Completed activity. Result:
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  2cc8bcb1707edfa90a3b807e38229ba1850a0b452089f655cc60e9098436cf16
[i-0111355a4891555ec] [2019-04-19T21:21:50.435Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployEnactHook/01flip.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:53.335Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployEnactHook/01flip.sh] : Completed activity. Result:
  nginx: [warn] duplicate MIME type "text/html" in /etc/nginx/sites-enabled/elasticbeanstalk-nginx-docker-proxy.conf:11
  Stopping nginx: [FAILED]
  Starting nginx: nginx: [warn] duplicate MIME type "text/html" in /etc/nginx/sites-enabled/elasticbeanstalk-nginx-docker-proxy.conf:11
  [  OK  ]
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  /opt/elasticbeanstalk/hooks/common.sh: line 98: [: 1: unary operator expected
  iptables: Saving firewall rules to /etc/sysconfig/iptables: [  OK  ]
  Making STAGING app container current...
  Untagged: aws_beanstalk/staging-app:latest
  eb-docker start/running, process 5602
  Docker container 2cc8bcb1707e is running aws_beanstalk/current-app.
[i-0111355a4891555ec] [2019-04-19T21:21:53.335Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployEnactHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/appdeploy/enact.
[i-0111355a4891555ec] [2019-04-19T21:21:53.335Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployPostHook] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:53.338Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployPostHook/00_clean_imgs.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:55.160Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployPostHook/00_clean_imgs.sh] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:21:55.160Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployPostHook/01_monitor_pids.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:55.709Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployPostHook/01_monitor_pids.sh] : Completed activity. Result:
  ++ /opt/elasticbeanstalk/bin/get-config container -k proxy_server
  + EB_CONFIG_PROXY_SERVER=nginx
  + '[' nginx = none ']'
  + /opt/elasticbeanstalk/bin/healthd-track-pidfile --proxy nginx
  + /opt/elasticbeanstalk/bin/healthd-track-pidfile --name application --location /var/run/docker.pid
[i-0111355a4891555ec] [2019-04-19T21:21:55.709Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployPostHook/02_container_logging.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:56.048Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployPostHook/02_container_logging.sh] : Completed activity. Result:
  ++ docker ps --no-trunc -q
  + log_pattern='/var/lib/docker/containers/2cc8bcb1707edfa90a3b807e38229ba1850a0b452089f655cc60e9098436cf16/*.log'
  + /opt/elasticbeanstalk/bin/log-conf -n docker-container '-l/var/lib/docker/containers/2cc8bcb1707edfa90a3b807e38229ba1850a0b452089f655cc60e9098436cf16/*.log' -f /opt/elasticbeanstalk/containerfiles/support/docker.logrotate.conf -t rotatelogs
[i-0111355a4891555ec] [2019-04-19T21:21:56.048Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/AppDeployPostHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/appdeploy/post.
[i-0111355a4891555ec] [2019-04-19T21:21:56.048Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/PostInitHook] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:56.054Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1/PostInitHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/postinit.
[i-0111355a4891555ec] [2019-04-19T21:21:56.054Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/StartupStage1] : Completed activity. Result:
  Application deployment - Command CMD-Startup stage 1 completed
[i-0111355a4891555ec] [2019-04-19T21:21:56.054Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsAfter] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:56.054Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsAfter/ConfigLogRotation] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:56.059Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsAfter/ConfigLogRotation/10-config.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-19T21:21:56.237Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsAfter/ConfigLogRotation/10-config.sh] : Completed activity. Result:
  Disabled forced hourly log rotation.
[i-0111355a4891555ec] [2019-04-19T21:21:56.237Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsAfter/ConfigLogRotation] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/addons/logpublish/hooks/config.
[i-0111355a4891555ec] [2019-04-19T21:21:56.237Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1/AddonsAfter] : Completed activity.
[i-0111355a4891555ec] [2019-04-19T21:21:56.237Z] INFO  [3633]  - [Application deployment app-bd99-190419_171518@1] : Completed activity. Result:
  Application deployment - Command CMD-Startup succeeded
[i-0111355a4891555ec] [2019-04-20T00:42:12.750Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:12.750Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsBefore] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:12.750Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsBefore/ConfigCWLAgent] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:12.750Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsBefore/ConfigCWLAgent/10-config.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:15.145Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsBefore/ConfigCWLAgent/10-config.sh] : Completed activity. Result:
  Starting awslogs: [  OK  ]
  Enabled log streaming.
[i-0111355a4891555ec] [2019-04-20T00:42:15.145Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsBefore/ConfigCWLAgent] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/addons/logstreaming/hooks/config.
[i-0111355a4891555ec] [2019-04-20T00:42:15.145Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsBefore] : Completed activity.
[i-0111355a4891555ec] [2019-04-20T00:42:15.145Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage0] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:15.145Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage0/ConfigDeployPreHook] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:15.146Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage0/ConfigDeployPreHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/configdeploy/pre.
[i-0111355a4891555ec] [2019-04-20T00:42:15.146Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage0] : Completed activity. Result:
  Configuration update - Command CMD-ConfigDeploy stage 0 completed
[i-0111355a4891555ec] [2019-04-20T00:42:15.146Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:15.146Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployEnactHook] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:15.148Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployEnactHook/00run.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:21.923Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployEnactHook/00run.sh] : Completed activity. Result:
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  c03aa0004affcbd0b7733d7af8942302005a0cdfa0d2590e9798e9b2aa9be2aa
[i-0111355a4891555ec] [2019-04-20T00:42:21.923Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployEnactHook/01flip.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:26.435Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployEnactHook/01flip.sh] : Completed activity. Result:
  nginx: [warn] duplicate MIME type "text/html" in /etc/nginx/sites-enabled/elasticbeanstalk-nginx-docker-proxy.conf:11
  Stopping nginx: [  OK  ]
  Starting nginx: nginx: [warn] duplicate MIME type "text/html" in /etc/nginx/sites-enabled/elasticbeanstalk-nginx-docker-proxy.conf:11
  [  OK  ]
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  /opt/elasticbeanstalk/hooks/common.sh: line 98: [: 1: unary operator expected
  iptables: Saving firewall rules to /etc/sysconfig/iptables: [  OK  ]
  Stopping current app container: 2cc8bcb1707e...
  eb-docker stop/waiting
  2cc8bcb1707e
  Making STAGING app container current...
  Untagged: aws_beanstalk/staging-app:latest
  eb-docker start/running, process 12670
  Docker container c03aa0004aff is running aws_beanstalk/current-app.
[i-0111355a4891555ec] [2019-04-20T00:42:26.435Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployEnactHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/configdeploy/enact.
[i-0111355a4891555ec] [2019-04-20T00:42:26.435Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployPostHook] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:26.436Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployPostHook/01_monitor_pids.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:27.222Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployPostHook/01_monitor_pids.sh] : Completed activity. Result:
  ++ /opt/elasticbeanstalk/bin/get-config container -k proxy_server
  + EB_CONFIG_PROXY_SERVER=nginx
  + '[' nginx = none ']'
  + /opt/elasticbeanstalk/bin/healthd-track-pidfile --proxy nginx
  + /opt/elasticbeanstalk/bin/healthd-track-pidfile --name application --location /var/run/docker.pid
[i-0111355a4891555ec] [2019-04-20T00:42:27.222Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployPostHook/02_container_logging.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:27.609Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployPostHook/02_container_logging.sh] : Completed activity. Result:
  + /opt/elasticbeanstalk/hooks/appdeploy/post/02_container_logging.sh
  ++ docker ps --no-trunc -q
  + log_pattern='/var/lib/docker/containers/c03aa0004affcbd0b7733d7af8942302005a0cdfa0d2590e9798e9b2aa9be2aa/*.log'
  + /opt/elasticbeanstalk/bin/log-conf -n docker-container '-l/var/lib/docker/containers/c03aa0004affcbd0b7733d7af8942302005a0cdfa0d2590e9798e9b2aa9be2aa/*.log' -f /opt/elasticbeanstalk/containerfiles/support/docker.logrotate.conf -t rotatelogs
[i-0111355a4891555ec] [2019-04-20T00:42:27.609Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1/ConfigDeployPostHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/configdeploy/post.
[i-0111355a4891555ec] [2019-04-20T00:42:27.609Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/ConfigDeployStage1] : Completed activity. Result:
  Application restart - Command CMD-ConfigDeploy stage 1 completed
[i-0111355a4891555ec] [2019-04-20T00:42:27.610Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsAfter] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:27.610Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsAfter/ConfigLogRotation] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:27.612Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsAfter/ConfigLogRotation/10-config.sh] : Starting activity...
[i-0111355a4891555ec] [2019-04-20T00:42:27.874Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsAfter/ConfigLogRotation/10-config.sh] : Completed activity. Result:
  Disabled forced hourly log rotation.
[i-0111355a4891555ec] [2019-04-20T00:42:27.874Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsAfter/ConfigLogRotation] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/addons/logpublish/hooks/config.
[i-0111355a4891555ec] [2019-04-20T00:42:27.875Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1/AddonsAfter] : Completed activity.
[i-0111355a4891555ec] [2019-04-20T00:42:27.875Z] INFO  [12092] - [Configuration update app-bd99-190419_171518@1] : Completed activity. Result:
  Configuration update - Command CMD-ConfigDeploy succeeded

============= i-0a437d75a06f1448f - /aws/elasticbeanstalk/uatest/var/log/eb-activity.log ==============

[i-0a437d75a06f1448f] [2019-04-21T18:25:53.278Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/EbExtensionPreBuild] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:25:53.679Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/EbExtensionPreBuild/Infra-EmbeddedPreBuild] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:25:53.679Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/EbExtensionPreBuild/Infra-EmbeddedPreBuild] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T18:25:53.695Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/EbExtensionPreBuild] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T18:25:53.696Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:25:53.696Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook/00clean_dir.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:25:54.490Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook/00clean_dir.sh] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T18:25:54.490Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook/01unzip.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:25:54.811Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook/01unzip.sh] : Completed activity. Result:
  Archive:  /opt/elasticbeanstalk/deploy/appsource/source_bundle
  cfea05072d96405d439486981e6f5473dc2af768
    inflating: /var/app/current/.dockerignore  
     creating: /var/app/current/.ebextensions/
    inflating: /var/app/current/.ebextensions/options.config  
    inflating: /var/app/current/.gitignore  
    inflating: /var/app/current/Dockerfile  
    inflating: /var/app/current/LICENSE  
    inflating: /var/app/current/README.md  
    inflating: /var/app/current/deps.edn  
    inflating: /var/app/current/dev.cljs.edn  
     creating: /var/app/current/env/
     creating: /var/app/current/env/dev/
     creating: /var/app/current/env/dev/cljs/
     creating: /var/app/current/env/dev/cljs/helodali/
    inflating: /var/app/current/env/dev/cljs/helodali/dev.cljs  
     creating: /var/app/current/env/prod/
     creating: /var/app/current/env/prod/cljs/
     creating: /var/app/current/env/prod/cljs/helodali/
    inflating: /var/app/current/env/prod/cljs/helodali/prod.cljs  
    inflating: /var/app/current/figwheel-main.edn  
     creating: /var/app/current/lambda/
     creating: /var/app/current/lambda/contact-form/
    inflating: /var/app/current/lambda/contact-form/contact-form.yaml  
    inflating: /var/app/current/lambda/contact-form/index.js  
     creating: /var/app/current/lambda/public-page-generator/
   extracting: /var/app/current/lambda/public-page-generator/README.md  
    inflating: /var/app/current/lambda/public-page-generator/project.clj  
    inflating: /var/app/current/lambda/public-page-generator/public-pages-generator.yaml  
     creating: /var/app/current/lambda/public-page-generator/resources/
    inflating: /var/app/current/lambda/public-page-generator/resources/README  
    inflating: /var/app/current/lambda/public-page-generator/resources/artwork-template.html  
     creating: /var/app/current/lambda/public-page-generator/resources/assets/
   extracting: /var/app/current/lambda/public-page-generator/resources/assets/Arrows-Left-icon.png  
   extracting: /var/app/current/lambda/public-page-generator/resources/assets/Arrows-Right-icon.png  
    inflating: /var/app/current/lambda/public-page-generator/resources/assets/favicon.ico  
    inflating: /var/app/current/lambda/public-page-generator/resources/contact-form-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/cv-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/exhibition-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/hd-public.css  
    inflating: /var/app/current/lambda/public-page-generator/resources/index-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/index.html  
     creating: /var/app/current/lambda/public-page-generator/scripts/
    inflating: /var/app/current/lambda/public-page-generator/scripts/update-function-code.pl  
     creating: /var/app/current/lambda/public-page-generator/src/
     creating: /var/app/current/lambda/public-page-generator/src/clj/
    inflating: /var/app/current/lambda/public-page-generator/src/clj/public_page_generator.clj  
     creating: /var/app/current/lambda/public-page-generator/test/
     creating: /var/app/current/lambda/public-page-generator/test/clj/
    inflating: /var/app/current/lambda/public-page-generator/test/clj/public_page_generator_test.clj  
     creating: /var/app/current/lambda/ribbon-maker/
    inflating: /var/app/current/lambda/ribbon-maker/index.js  
    inflating: /var/app/current/lambda/ribbon-maker/package-lock.json  
    inflating: /var/app/current/lambda/ribbon-maker/package.json  
    inflating: /var/app/current/lambda/ribbon-maker/ribbon-maker.yaml  
   extracting: /var/app/current/lambda/ribbon-maker/ribbon.png  
     creating: /var/app/current/lambda/ribbon-maker/scripts/
    inflating: /var/app/current/lambda/ribbon-maker/scripts/make-zip.sh  
    inflating: /var/app/current/lambda/ribbon-maker/scripts/update-function-code.pl  
    inflating: /var/app/current/lambda/ribbon-maker/test-event.json  
    inflating: /var/app/current/lambda/ribbon-maker/test-event.sh  
     creating: /var/app/current/lambda/s3-image-conversion/
    inflating: /var/app/current/lambda/s3-image-conversion/README.md  
    inflating: /var/app/current/lambda/s3-image-conversion/image-conversion.yaml  
    inflating: /var/app/current/lambda/s3-image-conversion/index.js  
    inflating: /var/app/current/lambda/s3-image-conversion/package-lock.json  
    inflating: /var/app/current/lambda/s3-image-conversion/package.json  
     creating: /var/app/current/lambda/s3-image-conversion/scripts/
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/add-permission.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/create-function.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/make-zip.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/update-function-code.pl  
    inflating: /var/app/current/lambda/s3-image-conversion/test-create-event.json  
    inflating: /var/app/current/lambda/s3-image-conversion/test-event.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/test-remove-event.json  
    inflating: /var/app/current/lambda/s3-image-conversion/webstorm-bespoken-config.png  
    inflating: /var/app/current/project.clj  
     creating: /var/app/current/resources/
    inflating: /var/app/current/resources/comodo-trust.jks  
     creating: /var/app/current/resources/doc/
     creating: /var/app/current/resources/doc/images/
    inflating: /var/app/current/resources/doc/images/helodali-screenshot.png  
     creating: /var/app/current/resources/public/
     creating: /var/app/current/resources/public/css/
    inflating: /var/app/current/resources/public/css/helodali.css  
    inflating: /var/app/current/resources/public/favicon.ico  
     creating: /var/app/current/resources/public/image-assets/
    inflating: /var/app/current/resources/public/image-assets/Veronica-snow.jpg  
    inflating: /var/app/current/resources/public/image-assets/ajax-loader.gif  
    inflating: /var/app/current/resources/public/image-assets/file-cabinet.png  
    inflating: /var/app/current/resources/public/image-assets/file-question.png  
    inflating: /var/app/current/resources/public/image-assets/file-text.png  
    inflating: /var/app/current/resources/public/image-assets/hd-bg-1.jpg  
    inflating: /var/app/current/resources/public/image-assets/logo.png  
    inflating: /var/app/current/resources/public/image-assets/thumb-stub.png  
    inflating: /var/app/current/resources/public/index.html  
     creating: /var/app/current/resources/public/static/
    inflating: /var/app/current/resources/public/static/privacy.html  
     creating: /var/app/current/resources/public/vendor/
     creating: /var/app/current/resources/public/vendor/css/
   extracting: /var/app/current/resources/public/vendor/css/chosen-sprite.png  
    inflating: /var/app/current/resources/public/vendor/css/chosen-sprite@2x.png  
    inflating: /var/app/current/resources/public/vendor/css/material-design-color-palette.css  
    inflating: /var/app/current/resources/public/vendor/css/material-design-color-palette.min.css  
    inflating: /var/app/current/resources/public/vendor/css/material-design-iconic-font.min.css  
    inflating: /var/app/current/resources/public/vendor/css/re-com.css  
     creating: /var/app/current/resources/public/vendor/fonts/
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.eot  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.svg  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.ttf  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.woff  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.woff2  
     creating: /var/app/current/resources/storage-shed/
     creating: /var/app/current/resources/storage-shed/icon/
   extracting: /var/app/current/resources/storage-shed/icon/favicon.png  
    inflating: /var/app/current/resources/storage-shed/icon/paint-brush-icons.txt  
    inflating: /var/app/current/resources/storage-shed/icon/paint-brush-icons.zip  
     creating: /var/app/current/scripts/
    inflating: /var/app/current/scripts/.gitignore  
    inflating: /var/app/current/scripts/convert-html-to-hiccup.clj  
    inflating: /var/app/current/scripts/create-instagram-subscription.sh  
    inflating: /var/app/current/scripts/eb-deploy-war.pl  
    inflating: /var/app/current/scripts/eb-deploy.pl  
     creating: /var/app/current/src/
     creating: /var/app/current/src/clj/
     creating: /var/app/current/src/clj/helodali/
    inflating: /var/app/current/src/clj/helodali/cognito.clj  
   extracting: /var/app/current/src/clj/helodali/core.clj  
    inflating: /var/app/current/src/clj/helodali/cv.clj  
    inflating: /var/app/current/src/clj/helodali/db.clj  
    inflating: /var/app/current/src/clj/helodali/handler.clj  
    inflating: /var/app/current/src/clj/helodali/instagram.clj  
    inflating: /var/app/current/src/clj/helodali/s3.clj  
    inflating: /var/app/current/src/clj/helodali/server.clj  
     creating: /var/app/current/src/cljc/
     creating: /var/app/current/src/cljc/helodali/
    inflating: /var/app/current/src/cljc/helodali/common.cljc  
    inflating: /var/app/current/src/cljc/helodali/types.cljc  
     creating: /var/app/current/src/cljs/
     creating: /var/app/current/src/cljs/helodali/
   extracting: /var/app/current/src/cljs/helodali/config.cljs  
    inflating: /var/app/current/src/cljs/helodali/core.cljs  
    inflating: /var/app/current/src/cljs/helodali/db.cljs  
    inflating: /var/app/current/src/cljs/helodali/events.cljs  
    inflating: /var/app/current/src/cljs/helodali/misc.cljs  
    inflating: /var/app/current/src/cljs/helodali/routes.cljs  
    inflating: /var/app/current/src/cljs/helodali/spec.cljs  
    inflating: /var/app/current/src/cljs/helodali/subs.cljs  
    inflating: /var/app/current/src/cljs/helodali/views.cljs  
     creating: /var/app/current/src/cljs/helodali/views/
    inflating: /var/app/current/src/cljs/helodali/views/account.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/artwork.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/contacts.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/documents.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/exhibitions.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/expenses.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/pages.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/press.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/profile.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/purchases.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/referred_artwork.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/search_results.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/static_pages.cljs  
     creating: /var/app/current/test/
     creating: /var/app/current/test/cljs/
     creating: /var/app/current/test/cljs/helodali/
    inflating: /var/app/current/test/cljs/helodali/core_test.cljs  
    inflating: /var/app/current/test/cljs/helodali/runner.cljs  
     creating: /var/app/current/war-resources/
     creating: /var/app/current/war-resources/.ebextensions/
     creating: /var/app/current/war-resources/.ebextensions/httpd/
     creating: /var/app/current/war-resources/.ebextensions/httpd/conf.d/
    inflating: /var/app/current/war-resources/.ebextensions/httpd/conf.d/elasticbeanstalk.conf  
[i-0a437d75a06f1448f] [2019-04-21T18:25:54.811Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook/02loopback-check.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:25:54.896Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook/02loopback-check.sh] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T18:25:54.896Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook/03build.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:18.754Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook/03build.sh] : Completed activity. Result:
  cat: Dockerrun.aws.json: No such file or directory
  cat: Dockerrun.aws.json: No such file or directory
  cat: Dockerrun.aws.json: No such file or directory
  alpine: Pulling from library/clojure
  Digest: sha256:053c0cd70e38f1bf72fe239ede4e8b6634ccb7ca1b4af8976c9e50ff4e5762fd
  Status: Image is up to date for clojure:alpine
  Successfully pulled clojure:alpine
  Sending build context to Docker daemon  3.922MB
  Step 1/10 : FROM clojure:alpine
   ---> c8ae7fa7ee1f
  Step 2/10 : RUN mkdir -p /app
   ---> Using cache
   ---> 9756f761c555
  Step 3/10 : WORKDIR /app
   ---> Using cache
   ---> 20802c5512c2
  Step 4/10 : COPY project.clj /app/
   ---> Using cache
   ---> 864ea8aa98e3
  Step 5/10 : RUN lein deps
   ---> Using cache
   ---> dd05505d7aec
  Step 6/10 : COPY . /app
   ---> a0504c5887fa
  Step 7/10 : RUN ts=$(date +%s)     && sed -i "s/app.js/app-$ts.js/g" project.clj     && sed -i "s/app.js/app-$ts.js/g" resources/public/index.html
   ---> Running in f713a7f7821f
  Removing intermediate container f713a7f7821f
   ---> 9ef42db29c79
  Step 8/10 : EXPOSE 3000
   ---> Running in 3a7cdef0b436
  Removing intermediate container 3a7cdef0b436
   ---> ec5f89c7448a
  Step 9/10 : RUN lein with-profile webapp ring uberjar
   ---> Running in 14c76f2eacdf
  [91mRetrieving ring-server/ring-server/0.5.0/ring-server-0.5.0.pom from clojars
  [0m[91mRetrieving ring/ring/1.3.2/ring-1.3.2.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.3.2/ring-core-1.3.2.pom from clojars
  [0m[91mRetrieving org/clojure/tools.reader/0.8.1/tools.reader-0.8.1.pom from central
  [0m[91mRetrieving ring/ring-codec/1.0.0/ring-codec-1.0.0.pom from clojars
  [0m[91mRetrieving commons-io/commons-io/2.4/commons-io-2.4.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/25/commons-parent-25.pom from central
  [0m[91mRetrieving commons-fileupload/commons-fileupload/1.3/commons-fileupload-1.3.pom from central
  [0m[91mRetrieving clj-time/clj-time/0.6.0/clj-time-0.6.0.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.2/joda-time-2.2.pom from central
  [0m[91mRetrieving ring/ring-devel/1.3.2/ring-devel-1.3.2.pom from clojars
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.7/clj-stacktrace-0.2.7.pom from clojars
  [0m[91mRetrieving ns-tracker/ns-tracker/0.2.2/ns-tracker-0.2.2.pom from clojars
  [0m[91mRetrieving org/clojure/tools.namespace/0.2.4/tools.namespace-0.2.4.pom from central
  [0m[91mRetrieving org/clojure/java.classpath/0.2.2/java.classpath-0.2.2.pom from central
  [0m[91mRetrieving ring/ring-jetty-adapter/1.3.2/ring-jetty-adapter-1.3.2.pom from clojars
  [0m[91mRetrieving ring/ring-servlet/1.3.2/ring-servlet-1.3.2.pom from clojars
  [0m[91mRetrieving org/eclipse/jetty/jetty-server/7.6.13.v20130916/jetty-server-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-project/7.6.13.v20130916/jetty-project-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-parent/20/jetty-parent-20.pom from central
  [0m[91mRetrieving org/eclipse/jetty/orbit/javax.servlet/2.5.0.v201103041518/javax.servlet-2.5.0.v201103041518.pom from central
  [0m[91mRetrieving org/eclipse/jetty/orbit/jetty-orbit/1/jetty-orbit-1.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-parent/18/jetty-parent-18.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-continuation/7.6.13.v20130916/jetty-continuation-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-http/7.6.13.v20130916/jetty-http-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-io/7.6.13.v20130916/jetty-io-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-util/7.6.13.v20130916/jetty-util-7.6.13.v20130916.pom from central
  [0m[91mRetrieving ring-refresh/ring-refresh/0.1.2/ring-refresh-0.1.2.pom from clojars
  [0m[91mRetrieving watchtower/watchtower/0.1.1/watchtower-0.1.1.pom from clojars
  [0m[91mRetrieving compojure/compojure/1.1.5/compojure-1.1.5.pom from clojars
  [0m[91mRetrieving org/clojure/core.incubator/0.1.0/core.incubator-0.1.0.pom from central
  [0m[91mRetrieving org/clojure/tools.macro/0.1.0/tools.macro-0.1.0.pom from central
  [0m[91mRetrieving clout/clout/1.0.1/clout-1.0.1.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.1.7/ring-core-1.1.7.pom from clojars
  [0m[91mRetrieving commons-io/commons-io/2.1/commons-io-2.1.pom from central
  [0m[91mRetrieving commons-fileupload/commons-fileupload/1.2.1/commons-fileupload-1.2.1.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/7/commons-parent-7.pom from central
  [0m[91mRetrieving org/apache/apache/4/apache-4.pom from central
  [0m[91mRetrieving javax/servlet/servlet-api/2.5/servlet-api-2.5.pom from central
  [0m[91mRetrieving clj-time/clj-time/0.3.7/clj-time-0.3.7.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.0/joda-time-2.0.pom from central
  [0m[91mRetrieving ring/ring/1.6.1/ring-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.6.1/ring-core-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-devel/1.6.1/ring-devel-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-jetty-adapter/1.6.1/ring-jetty-adapter-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-servlet/1.6.1/ring-servlet-1.6.1.pom from clojars
  [0m[91mRetrieving org/eclipse/jetty/jetty-server/9.2.21.v20170120/jetty-server-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-project/9.2.21.v20170120/jetty-project-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-parent/23/jetty-parent-23.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-http/9.2.21.v20170120/jetty-http-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-util/9.2.21.v20170120/jetty-util-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-io/9.2.21.v20170120/jetty-io-9.2.21.v20170120.pom from central
  [0m[91mRetrieving watchtower/watchtower/0.1.1/watchtower-0.1.1.jar from clojars
  [0m[91mRetrieving ring-server/ring-server/0.5.0/ring-server-0.5.0.jar from clojars
  [0m[91mRetrieving ring-refresh/ring-refresh/0.1.2/ring-refresh-0.1.2.jar from clojars
  [0m[91mCompiling helodali.server
  [0m[91m2019-04-21 18:26:51.654:INFO::main: Logging initialized @36792ms to org.eclipse.jetty.util.log.StdErrLog[0m[91m
  [0m[91mCompiling helodali.handler.main
  [0mCompiling ClojureScript...
  [91mRetrieving cljsbuild/cljsbuild/1.1.7/cljsbuild-1.1.7.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/0.0-3211/clojurescript-0.0-3211.pom from central
  [0m[91mRetrieving org/clojure/clojure/1.7.0-beta1/clojure-1.7.0-beta1.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler/v20150126/closure-compiler-v20150126.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20150126/closure-compiler-parent-v20150126.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20150126/closure-compiler-externs-v20150126.pom from central
  [0m[91mRetrieving com/google/truth/truth/0.24/truth-0.24.pom from central
  [0m[91mRetrieving com/google/truth/truth-parent/0.24/truth-parent-0.24.pom from central
  [0m[91mRetrieving com/google/guava/guava/17.0/guava-17.0.pom from central
  [0m[91mRetrieving com/google/guava/guava-parent/17.0/guava-parent-17.0.pom from central
  [0m[91mRetrieving junit/junit/4.10/junit-4.10.pom from central
  [0m[91mRetrieving org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.pom from central
  [0m[91mRetrieving org/hamcrest/hamcrest-parent/1.1/hamcrest-parent-1.1.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20140718-946a7d39/google-closure-library-0.0-20140718-946a7d39.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20140718-946a7d39/google-closure-library-third-party-0.0-20140718-946a7d39.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/0.9.1/tools.reader-0.9.1.pom from central
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.5/clj-stacktrace-0.2.5.pom from clojars
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.5/clj-stacktrace-0.2.5.jar from clojars
  [0m[91mRetrieving cljsbuild/cljsbuild/1.1.7/cljsbuild-1.1.7.jar from clojars
  [0mCompiling ["resources/public/js/compiled/app-1555871157.js"] from ["src/cljs" "src/cljc" "env/prod/cljs"]...
  [32mSuccessfully compiled ["resources/public/js/compiled/app-1555871157.js"] in 94.86 seconds.[0m
  Compiling ClojureScript...
  Created /app/target/helodali-0.1.0-SNAPSHOT.jar
  Created /app/target/helodali.jar
  Removing intermediate container 14c76f2eacdf
   ---> 52816c4efc63
  Step 10/10 : CMD ["java", "-jar", "target/helodali.jar"]
   ---> Running in ea7f8b371d7e
  Removing intermediate container ea7f8b371d7e
   ---> e315cd36e2a0
  Successfully built e315cd36e2a0
  Successfully tagged aws_beanstalk/staging-app:latest
  Successfully built aws_beanstalk/staging-app
[i-0a437d75a06f1448f] [2019-04-21T18:29:18.755Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/AppDeployPreHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/appdeploy/pre.
[i-0a437d75a06f1448f] [2019-04-21T18:29:18.755Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/EbExtensionPostBuild] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:19.105Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/EbExtensionPostBuild/Infra-EmbeddedPostBuild] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:19.106Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/EbExtensionPostBuild/Infra-EmbeddedPostBuild] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T18:29:19.122Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/EbExtensionPostBuild] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T18:29:19.122Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/InfraCleanEbextension] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:19.123Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0/InfraCleanEbextension] : Completed activity. Result:
  Cleaned ebextensions subdirectories from /tmp.
[i-0a437d75a06f1448f] [2019-04-21T18:29:19.123Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage0] : Completed activity. Result:
  Application update - Command CMD-AppDeploy stage 0 completed
[i-0a437d75a06f1448f] [2019-04-21T18:29:19.123Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:19.123Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployEnactHook] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:19.123Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployEnactHook/00run.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:25.595Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployEnactHook/00run.sh] : Completed activity. Result:
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  1e0f93ed3d94029375ae7d2e72becbef5ee5575fa162ebc685ea8c99d5ffbd74
[i-0a437d75a06f1448f] [2019-04-21T18:29:25.596Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployEnactHook/01flip.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:30.403Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployEnactHook/01flip.sh] : Completed activity. Result:
  nginx: [warn] duplicate MIME type "text/html" in /etc/nginx/sites-enabled/elasticbeanstalk-nginx-docker-proxy.conf:11
  Stopping nginx: [  OK  ]
  Starting nginx: nginx: [warn] duplicate MIME type "text/html" in /etc/nginx/sites-enabled/elasticbeanstalk-nginx-docker-proxy.conf:11
  [  OK  ]
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  /opt/elasticbeanstalk/hooks/common.sh: line 98: [: 1: unary operator expected
  iptables: Saving firewall rules to /etc/sysconfig/iptables: [  OK  ]
  Stopping current app container: 0a3c6943f7e1...
  eb-docker stop/waiting
  0a3c6943f7e1
  Untagged: aws_beanstalk/current-app:latest
  Deleted: sha256:57dd2678d0484da53af955ba51842eed6ede02288647d0eb03819397ac528534
  Deleted: sha256:32647fadbf806bf8762a7f8350a9ec2ab1bef0650be01ee5d7b0a4ebdf20a7c0
  Deleted: sha256:1f2b3a5edf3e25d5df6cf75a7a9241003a9efc43246b4e817102ac95e9e114f3
  Deleted: sha256:a952ebcafedf1b757908db9b3282577c610192e880c7b3a266f837f183698b6b
  Deleted: sha256:ec753b8181f7128057f43c1370294b9643eacfdcd638f7e736f198e94356659c
  Deleted: sha256:ac873e6e94eb409c4a596792077b33843e462d9f815e869a8d8de30acab832a6
  Making STAGING app container current...
  Untagged: aws_beanstalk/staging-app:latest
  eb-docker start/running, process 1691
  Docker container 1e0f93ed3d94 is running aws_beanstalk/current-app.
[i-0a437d75a06f1448f] [2019-04-21T18:29:30.403Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployEnactHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/appdeploy/enact.
[i-0a437d75a06f1448f] [2019-04-21T18:29:30.403Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployPostHook] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:30.403Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployPostHook/00_clean_imgs.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:32.275Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployPostHook/00_clean_imgs.sh] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T18:29:32.275Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployPostHook/01_monitor_pids.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:32.846Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployPostHook/01_monitor_pids.sh] : Completed activity. Result:
  ++ /opt/elasticbeanstalk/bin/get-config container -k proxy_server
  + EB_CONFIG_PROXY_SERVER=nginx
  + '[' nginx = none ']'
  + /opt/elasticbeanstalk/bin/healthd-track-pidfile --proxy nginx
  + /opt/elasticbeanstalk/bin/healthd-track-pidfile --name application --location /var/run/docker.pid
[i-0a437d75a06f1448f] [2019-04-21T18:29:32.846Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployPostHook/02_container_logging.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.192Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployPostHook/02_container_logging.sh] : Completed activity. Result:
  ++ docker ps --no-trunc -q
  + log_pattern='/var/lib/docker/containers/1e0f93ed3d94029375ae7d2e72becbef5ee5575fa162ebc685ea8c99d5ffbd74/*.log'
  + /opt/elasticbeanstalk/bin/log-conf -n docker-container '-l/var/lib/docker/containers/1e0f93ed3d94029375ae7d2e72becbef5ee5575fa162ebc685ea8c99d5ffbd74/*.log' -f /opt/elasticbeanstalk/containerfiles/support/docker.logrotate.conf -t rotatelogs
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.192Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1/AppDeployPostHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/appdeploy/post.
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.192Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AppDeployStage1] : Completed activity. Result:
  Application version switch - Command CMD-AppDeploy stage 1 completed
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.192Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AddonsAfter] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.192Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AddonsAfter/ConfigLogRotation] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.192Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AddonsAfter/ConfigLogRotation/10-config.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.366Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AddonsAfter/ConfigLogRotation/10-config.sh] : Completed activity. Result:
  Disabled forced hourly log rotation.
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.366Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AddonsAfter/ConfigLogRotation] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/addons/logpublish/hooks/config.
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.366Z] INFO  [32472] - [Application update app-cfea-190421_142541@21/AddonsAfter] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T18:29:33.366Z] INFO  [32472] - [Application update app-cfea-190421_142541@21] : Completed activity. Result:
  Application update - Command CMD-AppDeploy succeeded
[i-0a437d75a06f1448f] [2019-04-21T19:30:49.907Z] INFO  [4096]  - [Application update app-1259-190421_153004@22] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:49.907Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsBefore] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:49.907Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsBefore/ConfigCWLAgent] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:49.907Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsBefore/ConfigCWLAgent/10-config.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:52.407Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsBefore/ConfigCWLAgent/10-config.sh] : Completed activity. Result:
  Log stream for log group /aws/elasticbeanstalk/uatest/var/log/nginx/error.log already exists. No need to create it.
  Log stream for log group /aws/elasticbeanstalk/uatest/var/log/nginx/access.log already exists. No need to create it.
  Log stream for log group /aws/elasticbeanstalk/uatest/var/log/docker-events.log already exists. No need to create it.
  Log stream for log group /aws/elasticbeanstalk/uatest/var/log/docker already exists. No need to create it.
  Log stream for log group /aws/elasticbeanstalk/uatest/var/log/eb-docker/containers/eb-current-app/stdouterr.log already exists. No need to create it.
  Log stream for log group /aws/elasticbeanstalk/uatest/var/log/eb-activity.log already exists. No need to create it.
  stopping awslogs
  Stopping awslogs: [  OK  ]
  Starting awslogs: [  OK  ]
  Enabled log streaming.
[i-0a437d75a06f1448f] [2019-04-21T19:30:52.407Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsBefore/ConfigCWLAgent] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/addons/logstreaming/hooks/config.
[i-0a437d75a06f1448f] [2019-04-21T19:30:52.407Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsBefore] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:30:52.855Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:52.855Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/DownloadSourceBundle] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:53.232Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/DownloadSourceBundle] : Completed activity. Result:
  [2019-04-21T19:30:52.965Z] INFO  [4196]  : Application version will be saved to /opt/elasticbeanstalk/deploy/appsource.
  [2019-04-21T19:30:52.965Z] INFO  [4196]  : Using manifest cache with deployment ID 22 and serial 23.
  [2019-04-21T19:30:52.965Z] INFO  [4196]  : Attempting to download application source bundle to: '/opt/elasticbeanstalk/deploy/appsource/source_bundle'.
  [2019-04-21T19:30:52.965Z] INFO  [4196]  : Using computed s3 key.
  [2019-04-21T19:30:53.093Z] INFO  [4196]  : Downloading from bucket 'elasticbeanstalk-us-east-1-128225160927' with key 'resources/environments/e-pywcf8c8tx/_runtime/_versions/helodali-test/app-1259-190421_153004' and version '' to '/opt/elasticbeanstalk/deploy/appsource/source_bundle'.
  [2019-04-21T19:30:53.155Z] INFO  [4196]  : Size: 2453835, ETag: "a647741ecce07c835f2916c689b78477", Metadata: {"environmentid"=>"e-pywcf8c8tx", "requestid"=>"19a5f3a7-f067-479a-8999-add1843af4a0"}.
  [2019-04-21T19:30:53.218Z] INFO  [4196]  : Downloaded size: 2453835.
  [2019-04-21T19:30:53.218Z] INFO  [4196]  : Successfully downloaded to '/opt/elasticbeanstalk/deploy/appsource/source_bundle'.
[i-0a437d75a06f1448f] [2019-04-21T19:30:53.233Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPreBuild] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:53.639Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPreBuild/Infra-EmbeddedPreBuild] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:53.642Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPreBuild/Infra-EmbeddedPreBuild/prebuild_0_helodali_test] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:53.642Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPreBuild/Infra-EmbeddedPreBuild/prebuild_0_helodali_test] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:30:53.642Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPreBuild/Infra-EmbeddedPreBuild] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:30:53.660Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPreBuild] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:30:53.660Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:53.660Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook/00clean_dir.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:54.485Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook/00clean_dir.sh] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:30:54.485Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook/01unzip.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:54.810Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook/01unzip.sh] : Completed activity. Result:
  Archive:  /opt/elasticbeanstalk/deploy/appsource/source_bundle
  1259b28e04d1e6658d394106c232754f7b0c602c
    inflating: /var/app/current/.dockerignore  
     creating: /var/app/current/.ebextensions/
    inflating: /var/app/current/.ebextensions/https-redirect-docker-sc.config  
    inflating: /var/app/current/.ebextensions/options.config  
    inflating: /var/app/current/.gitignore  
    inflating: /var/app/current/Dockerfile  
    inflating: /var/app/current/LICENSE  
    inflating: /var/app/current/README.md  
    inflating: /var/app/current/deps.edn  
    inflating: /var/app/current/dev.cljs.edn  
     creating: /var/app/current/env/
     creating: /var/app/current/env/dev/
     creating: /var/app/current/env/dev/cljs/
     creating: /var/app/current/env/dev/cljs/helodali/
    inflating: /var/app/current/env/dev/cljs/helodali/dev.cljs  
     creating: /var/app/current/env/prod/
     creating: /var/app/current/env/prod/cljs/
     creating: /var/app/current/env/prod/cljs/helodali/
    inflating: /var/app/current/env/prod/cljs/helodali/prod.cljs  
    inflating: /var/app/current/figwheel-main.edn  
     creating: /var/app/current/lambda/
     creating: /var/app/current/lambda/contact-form/
    inflating: /var/app/current/lambda/contact-form/contact-form.yaml  
    inflating: /var/app/current/lambda/contact-form/index.js  
     creating: /var/app/current/lambda/public-page-generator/
   extracting: /var/app/current/lambda/public-page-generator/README.md  
    inflating: /var/app/current/lambda/public-page-generator/project.clj  
    inflating: /var/app/current/lambda/public-page-generator/public-pages-generator.yaml  
     creating: /var/app/current/lambda/public-page-generator/resources/
    inflating: /var/app/current/lambda/public-page-generator/resources/README  
    inflating: /var/app/current/lambda/public-page-generator/resources/artwork-template.html  
     creating: /var/app/current/lambda/public-page-generator/resources/assets/
   extracting: /var/app/current/lambda/public-page-generator/resources/assets/Arrows-Left-icon.png  
   extracting: /var/app/current/lambda/public-page-generator/resources/assets/Arrows-Right-icon.png  
    inflating: /var/app/current/lambda/public-page-generator/resources/assets/favicon.ico  
    inflating: /var/app/current/lambda/public-page-generator/resources/contact-form-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/cv-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/exhibition-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/hd-public.css  
    inflating: /var/app/current/lambda/public-page-generator/resources/index-template.html  
    inflating: /var/app/current/lambda/public-page-generator/resources/index.html  
     creating: /var/app/current/lambda/public-page-generator/scripts/
    inflating: /var/app/current/lambda/public-page-generator/scripts/update-function-code.pl  
     creating: /var/app/current/lambda/public-page-generator/src/
     creating: /var/app/current/lambda/public-page-generator/src/clj/
    inflating: /var/app/current/lambda/public-page-generator/src/clj/public_page_generator.clj  
     creating: /var/app/current/lambda/public-page-generator/test/
     creating: /var/app/current/lambda/public-page-generator/test/clj/
    inflating: /var/app/current/lambda/public-page-generator/test/clj/public_page_generator_test.clj  
     creating: /var/app/current/lambda/ribbon-maker/
    inflating: /var/app/current/lambda/ribbon-maker/index.js  
    inflating: /var/app/current/lambda/ribbon-maker/package-lock.json  
    inflating: /var/app/current/lambda/ribbon-maker/package.json  
    inflating: /var/app/current/lambda/ribbon-maker/ribbon-maker.yaml  
   extracting: /var/app/current/lambda/ribbon-maker/ribbon.png  
     creating: /var/app/current/lambda/ribbon-maker/scripts/
    inflating: /var/app/current/lambda/ribbon-maker/scripts/make-zip.sh  
    inflating: /var/app/current/lambda/ribbon-maker/scripts/update-function-code.pl  
    inflating: /var/app/current/lambda/ribbon-maker/test-event.json  
    inflating: /var/app/current/lambda/ribbon-maker/test-event.sh  
     creating: /var/app/current/lambda/s3-image-conversion/
    inflating: /var/app/current/lambda/s3-image-conversion/README.md  
    inflating: /var/app/current/lambda/s3-image-conversion/image-conversion.yaml  
    inflating: /var/app/current/lambda/s3-image-conversion/index.js  
    inflating: /var/app/current/lambda/s3-image-conversion/package-lock.json  
    inflating: /var/app/current/lambda/s3-image-conversion/package.json  
     creating: /var/app/current/lambda/s3-image-conversion/scripts/
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/add-permission.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/create-function.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/make-zip.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/scripts/update-function-code.pl  
    inflating: /var/app/current/lambda/s3-image-conversion/test-create-event.json  
    inflating: /var/app/current/lambda/s3-image-conversion/test-event.sh  
    inflating: /var/app/current/lambda/s3-image-conversion/test-remove-event.json  
    inflating: /var/app/current/lambda/s3-image-conversion/webstorm-bespoken-config.png  
    inflating: /var/app/current/project.clj  
     creating: /var/app/current/resources/
    inflating: /var/app/current/resources/comodo-trust.jks  
     creating: /var/app/current/resources/doc/
     creating: /var/app/current/resources/doc/images/
    inflating: /var/app/current/resources/doc/images/helodali-screenshot.png  
     creating: /var/app/current/resources/public/
     creating: /var/app/current/resources/public/css/
    inflating: /var/app/current/resources/public/css/helodali.css  
    inflating: /var/app/current/resources/public/favicon.ico  
     creating: /var/app/current/resources/public/image-assets/
    inflating: /var/app/current/resources/public/image-assets/Veronica-snow.jpg  
    inflating: /var/app/current/resources/public/image-assets/ajax-loader.gif  
    inflating: /var/app/current/resources/public/image-assets/file-cabinet.png  
    inflating: /var/app/current/resources/public/image-assets/file-question.png  
    inflating: /var/app/current/resources/public/image-assets/file-text.png  
    inflating: /var/app/current/resources/public/image-assets/hd-bg-1.jpg  
    inflating: /var/app/current/resources/public/image-assets/logo.png  
    inflating: /var/app/current/resources/public/image-assets/thumb-stub.png  
    inflating: /var/app/current/resources/public/index.html  
     creating: /var/app/current/resources/public/static/
    inflating: /var/app/current/resources/public/static/privacy.html  
     creating: /var/app/current/resources/public/vendor/
     creating: /var/app/current/resources/public/vendor/css/
   extracting: /var/app/current/resources/public/vendor/css/chosen-sprite.png  
    inflating: /var/app/current/resources/public/vendor/css/chosen-sprite@2x.png  
    inflating: /var/app/current/resources/public/vendor/css/material-design-color-palette.css  
    inflating: /var/app/current/resources/public/vendor/css/material-design-color-palette.min.css  
    inflating: /var/app/current/resources/public/vendor/css/material-design-iconic-font.min.css  
    inflating: /var/app/current/resources/public/vendor/css/re-com.css  
     creating: /var/app/current/resources/public/vendor/fonts/
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.eot  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.svg  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.ttf  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.woff  
    inflating: /var/app/current/resources/public/vendor/fonts/Material-Design-Iconic-Font.woff2  
     creating: /var/app/current/resources/storage-shed/
     creating: /var/app/current/resources/storage-shed/icon/
   extracting: /var/app/current/resources/storage-shed/icon/favicon.png  
    inflating: /var/app/current/resources/storage-shed/icon/paint-brush-icons.txt  
    inflating: /var/app/current/resources/storage-shed/icon/paint-brush-icons.zip  
     creating: /var/app/current/scripts/
    inflating: /var/app/current/scripts/convert-html-to-hiccup.clj  
    inflating: /var/app/current/scripts/create-instagram-subscription.sh  
    inflating: /var/app/current/scripts/eb-deploy-war.pl  
    inflating: /var/app/current/scripts/eb-deploy.pl  
     creating: /var/app/current/src/
     creating: /var/app/current/src/clj/
     creating: /var/app/current/src/clj/helodali/
    inflating: /var/app/current/src/clj/helodali/cognito.clj  
   extracting: /var/app/current/src/clj/helodali/core.clj  
    inflating: /var/app/current/src/clj/helodali/cv.clj  
    inflating: /var/app/current/src/clj/helodali/db.clj  
    inflating: /var/app/current/src/clj/helodali/handler.clj  
    inflating: /var/app/current/src/clj/helodali/instagram.clj  
    inflating: /var/app/current/src/clj/helodali/s3.clj  
    inflating: /var/app/current/src/clj/helodali/server.clj  
     creating: /var/app/current/src/cljc/
     creating: /var/app/current/src/cljc/helodali/
    inflating: /var/app/current/src/cljc/helodali/common.cljc  
    inflating: /var/app/current/src/cljc/helodali/types.cljc  
     creating: /var/app/current/src/cljs/
     creating: /var/app/current/src/cljs/helodali/
   extracting: /var/app/current/src/cljs/helodali/config.cljs  
    inflating: /var/app/current/src/cljs/helodali/core.cljs  
    inflating: /var/app/current/src/cljs/helodali/db.cljs  
    inflating: /var/app/current/src/cljs/helodali/events.cljs  
    inflating: /var/app/current/src/cljs/helodali/misc.cljs  
    inflating: /var/app/current/src/cljs/helodali/routes.cljs  
    inflating: /var/app/current/src/cljs/helodali/spec.cljs  
    inflating: /var/app/current/src/cljs/helodali/subs.cljs  
    inflating: /var/app/current/src/cljs/helodali/views.cljs  
     creating: /var/app/current/src/cljs/helodali/views/
    inflating: /var/app/current/src/cljs/helodali/views/account.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/artwork.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/contacts.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/documents.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/exhibitions.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/expenses.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/pages.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/press.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/profile.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/purchases.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/referred_artwork.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/search_results.cljs  
    inflating: /var/app/current/src/cljs/helodali/views/static_pages.cljs  
     creating: /var/app/current/test/
     creating: /var/app/current/test/cljs/
     creating: /var/app/current/test/cljs/helodali/
    inflating: /var/app/current/test/cljs/helodali/core_test.cljs  
    inflating: /var/app/current/test/cljs/helodali/runner.cljs  
     creating: /var/app/current/war-resources/
     creating: /var/app/current/war-resources/.ebextensions/
     creating: /var/app/current/war-resources/.ebextensions/httpd/
     creating: /var/app/current/war-resources/.ebextensions/httpd/conf.d/
    inflating: /var/app/current/war-resources/.ebextensions/httpd/conf.d/elasticbeanstalk.conf  
[i-0a437d75a06f1448f] [2019-04-21T19:30:54.810Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook/02loopback-check.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:30:54.899Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook/02loopback-check.sh] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:30:54.899Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook/03build.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:22.648Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook/03build.sh] : Completed activity. Result:
  cat: Dockerrun.aws.json: No such file or directory
  cat: Dockerrun.aws.json: No such file or directory
  cat: Dockerrun.aws.json: No such file or directory
  alpine: Pulling from library/clojure
  Digest: sha256:053c0cd70e38f1bf72fe239ede4e8b6634ccb7ca1b4af8976c9e50ff4e5762fd
  Status: Image is up to date for clojure:alpine
  Successfully pulled clojure:alpine
  Sending build context to Docker daemon  3.926MB
  Step 1/10 : FROM clojure:alpine
   ---> c8ae7fa7ee1f
  Step 2/10 : RUN mkdir -p /app
   ---> Using cache
   ---> 9756f761c555
  Step 3/10 : WORKDIR /app
   ---> Using cache
   ---> 20802c5512c2
  Step 4/10 : COPY project.clj /app/
   ---> Using cache
   ---> 864ea8aa98e3
  Step 5/10 : RUN lein deps
   ---> Using cache
   ---> dd05505d7aec
  Step 6/10 : COPY . /app
   ---> 98f75ccd5f4e
  Step 7/10 : RUN ts=$(date +%s)     && sed -i "s/app.js/app-$ts.js/g" project.clj     && sed -i "s/app.js/app-$ts.js/g" resources/public/index.html
   ---> Running in e9f53a667306
  Removing intermediate container e9f53a667306
   ---> f7c8114c42f0
  Step 8/10 : EXPOSE 3000
   ---> Running in 134b9de6fbc6
  Removing intermediate container 134b9de6fbc6
   ---> 9b403ce332a2
  Step 9/10 : RUN lein with-profile webapp ring uberjar
   ---> Running in 089bf203223b
  [91mRetrieving ring-server/ring-server/0.5.0/ring-server-0.5.0.pom from clojars
  [0m[91mRetrieving ring/ring/1.3.2/ring-1.3.2.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.3.2/ring-core-1.3.2.pom from clojars
  [0m[91mRetrieving org/clojure/tools.reader/0.8.1/tools.reader-0.8.1.pom from central
  [0m[91mRetrieving ring/ring-codec/1.0.0/ring-codec-1.0.0.pom from clojars
  [0m[91mRetrieving commons-io/commons-io/2.4/commons-io-2.4.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/25/commons-parent-25.pom from central
  [0m[91mRetrieving commons-fileupload/commons-fileupload/1.3/commons-fileupload-1.3.pom from central
  [0m[91mRetrieving clj-time/clj-time/0.6.0/clj-time-0.6.0.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.2/joda-time-2.2.pom from central
  [0m[91mRetrieving ring/ring-devel/1.3.2/ring-devel-1.3.2.pom from clojars
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.7/clj-stacktrace-0.2.7.pom from clojars
  [0m[91mRetrieving ns-tracker/ns-tracker/0.2.2/ns-tracker-0.2.2.pom from clojars
  [0m[91mRetrieving org/clojure/tools.namespace/0.2.4/tools.namespace-0.2.4.pom from central
  [0m[91mRetrieving org/clojure/java.classpath/0.2.2/java.classpath-0.2.2.pom from central
  [0m[91mRetrieving ring/ring-jetty-adapter/1.3.2/ring-jetty-adapter-1.3.2.pom from clojars
  [0m[91mRetrieving ring/ring-servlet/1.3.2/ring-servlet-1.3.2.pom from clojars
  [0m[91mRetrieving org/eclipse/jetty/jetty-server/7.6.13.v20130916/jetty-server-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-project/7.6.13.v20130916/jetty-project-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-parent/20/jetty-parent-20.pom from central
  [0m[91mRetrieving org/eclipse/jetty/orbit/javax.servlet/2.5.0.v201103041518/javax.servlet-2.5.0.v201103041518.pom from central
  [0m[91mRetrieving org/eclipse/jetty/orbit/jetty-orbit/1/jetty-orbit-1.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-parent/18/jetty-parent-18.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-continuation/7.6.13.v20130916/jetty-continuation-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-http/7.6.13.v20130916/jetty-http-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-io/7.6.13.v20130916/jetty-io-7.6.13.v20130916.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-util/7.6.13.v20130916/jetty-util-7.6.13.v20130916.pom from central
  [0m[91mRetrieving ring-refresh/ring-refresh/0.1.2/ring-refresh-0.1.2.pom from clojars
  [0m[91mRetrieving watchtower/watchtower/0.1.1/watchtower-0.1.1.pom from clojars
  [0m[91mRetrieving compojure/compojure/1.1.5/compojure-1.1.5.pom from clojars
  [0m[91mRetrieving org/clojure/core.incubator/0.1.0/core.incubator-0.1.0.pom from central
  [0m[91mRetrieving org/clojure/tools.macro/0.1.0/tools.macro-0.1.0.pom from central
  [0m[91mRetrieving clout/clout/1.0.1/clout-1.0.1.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.1.7/ring-core-1.1.7.pom from clojars
  [0m[91mRetrieving commons-io/commons-io/2.1/commons-io-2.1.pom from central
  [0m[91mRetrieving commons-fileupload/commons-fileupload/1.2.1/commons-fileupload-1.2.1.pom from central
  [0m[91mRetrieving org/apache/commons/commons-parent/7/commons-parent-7.pom from central
  [0m[91mRetrieving org/apache/apache/4/apache-4.pom from central
  [0m[91mRetrieving javax/servlet/servlet-api/2.5/servlet-api-2.5.pom from central
  [0m[91mRetrieving clj-time/clj-time/0.3.7/clj-time-0.3.7.pom from clojars
  [0m[91mRetrieving joda-time/joda-time/2.0/joda-time-2.0.pom from central
  [0m[91mRetrieving ring/ring/1.6.1/ring-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-core/1.6.1/ring-core-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-devel/1.6.1/ring-devel-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-jetty-adapter/1.6.1/ring-jetty-adapter-1.6.1.pom from clojars
  [0m[91mRetrieving ring/ring-servlet/1.6.1/ring-servlet-1.6.1.pom from clojars
  [0m[91mRetrieving org/eclipse/jetty/jetty-server/9.2.21.v20170120/jetty-server-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-project/9.2.21.v20170120/jetty-project-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-parent/23/jetty-parent-23.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-http/9.2.21.v20170120/jetty-http-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-util/9.2.21.v20170120/jetty-util-9.2.21.v20170120.pom from central
  [0m[91mRetrieving org/eclipse/jetty/jetty-io/9.2.21.v20170120/jetty-io-9.2.21.v20170120.pom from central
  [0m[91mRetrieving watchtower/watchtower/0.1.1/watchtower-0.1.1.jar from clojars
  [0m[91mRetrieving ring-refresh/ring-refresh/0.1.2/ring-refresh-0.1.2.jar from clojars
  [0m[91mRetrieving ring-server/ring-server/0.5.0/ring-server-0.5.0.jar from clojars
  [0m[91mCompiling helodali.server
  [0m[91m2019-04-21 19:31:54.781:INFO::main: Logging initialized @36741ms to org.eclipse.jetty.util.log.StdErrLog[0m[91m
  [0m[91mCompiling helodali.handler.main
  [0mCompiling ClojureScript...
  [91mRetrieving cljsbuild/cljsbuild/1.1.7/cljsbuild-1.1.7.pom from clojars
  [0m[91mRetrieving org/clojure/clojurescript/0.0-3211/clojurescript-0.0-3211.pom from central
  [0m[91mRetrieving org/clojure/clojure/1.7.0-beta1/clojure-1.7.0-beta1.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler/v20150126/closure-compiler-v20150126.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-parent/v20150126/closure-compiler-parent-v20150126.pom from central
  [0m[91mRetrieving com/google/javascript/closure-compiler-externs/v20150126/closure-compiler-externs-v20150126.pom from central
  [0m[91mRetrieving com/google/truth/truth/0.24/truth-0.24.pom from central
  [0m[91mRetrieving com/google/truth/truth-parent/0.24/truth-parent-0.24.pom from central
  [0m[91mRetrieving com/google/guava/guava/17.0/guava-17.0.pom from central
  [0m[91mRetrieving com/google/guava/guava-parent/17.0/guava-parent-17.0.pom from central
  [0m[91mRetrieving junit/junit/4.10/junit-4.10.pom from central
  [0m[91mRetrieving org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.pom from central
  [0m[91mRetrieving org/hamcrest/hamcrest-parent/1.1/hamcrest-parent-1.1.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library/0.0-20140718-946a7d39/google-closure-library-0.0-20140718-946a7d39.pom from central
  [0m[91mRetrieving org/clojure/google-closure-library-third-party/0.0-20140718-946a7d39/google-closure-library-third-party-0.0-20140718-946a7d39.pom from central
  [0m[91mRetrieving org/clojure/tools.reader/0.9.1/tools.reader-0.9.1.pom from central
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.5/clj-stacktrace-0.2.5.pom from clojars
  [0m[91mRetrieving clj-stacktrace/clj-stacktrace/0.2.5/clj-stacktrace-0.2.5.jar from clojars
  [0m[91mRetrieving cljsbuild/cljsbuild/1.1.7/cljsbuild-1.1.7.jar from clojars
  [0mCompiling ["resources/public/js/compiled/app-1555875057.js"] from ["src/cljs" "src/cljc" "env/prod/cljs"]...
  [32mSuccessfully compiled ["resources/public/js/compiled/app-1555875057.js"] in 95.61 seconds.[0m
  Compiling ClojureScript...
  Created /app/target/helodali-0.1.0-SNAPSHOT.jar
  Created /app/target/helodali.jar
  Removing intermediate container 089bf203223b
   ---> aeb6c1c5b267
  Step 10/10 : CMD ["java", "-jar", "target/helodali.jar"]
   ---> Running in 66093ef90669
  Removing intermediate container 66093ef90669
   ---> da21f77afe78
  Successfully built da21f77afe78
  Successfully tagged aws_beanstalk/staging-app:latest
  Successfully built aws_beanstalk/staging-app
[i-0a437d75a06f1448f] [2019-04-21T19:34:22.648Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/AppDeployPreHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/appdeploy/pre.
[i-0a437d75a06f1448f] [2019-04-21T19:34:22.648Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPostBuild] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:23.050Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPostBuild/Infra-EmbeddedPostBuild] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:23.050Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPostBuild/Infra-EmbeddedPostBuild] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:34:23.066Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/EbExtensionPostBuild] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:34:23.066Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/InfraCleanEbextension] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:23.066Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0/InfraCleanEbextension] : Completed activity. Result:
  Cleaned ebextensions subdirectories from /tmp.
[i-0a437d75a06f1448f] [2019-04-21T19:34:23.066Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage0] : Completed activity. Result:
  Application update - Command CMD-AppDeploy stage 0 completed
[i-0a437d75a06f1448f] [2019-04-21T19:34:23.067Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:23.067Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployEnactHook] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:23.067Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployEnactHook/00run.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:29.541Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployEnactHook/00run.sh] : Completed activity. Result:
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  bbb6d9a61506bf0c70d21079e654e4399f92a878a34637ebbbaed3a514c01f8f
[i-0a437d75a06f1448f] [2019-04-21T19:34:29.541Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployEnactHook/01flip.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:34.342Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployEnactHook/01flip.sh] : Completed activity. Result:
  nginx: [warn] duplicate MIME type "text/html" in /etc/nginx/sites-enabled/elasticbeanstalk-nginx-docker-proxy.conf:11
  Stopping nginx: [  OK  ]
  Starting nginx: nginx: [warn] duplicate MIME type "text/html" in /etc/nginx/sites-enabled/elasticbeanstalk-nginx-docker-proxy.conf:11
  [  OK  ]
  cat: /var/app/current/Dockerrun.aws.json: No such file or directory
  /opt/elasticbeanstalk/hooks/common.sh: line 98: [: 1: unary operator expected
  iptables: Saving firewall rules to /etc/sysconfig/iptables: [  OK  ]
  Stopping current app container: 1e0f93ed3d94...
  eb-docker stop/waiting
  1e0f93ed3d94
  Untagged: aws_beanstalk/current-app:latest
  Deleted: sha256:e315cd36e2a0643a6574ab82fcf56c513db04f14d73b1516a4936d070291fa34
  Deleted: sha256:52816c4efc635cc6296eb514edf7c31b676295f5834f07f2d5b1759d47836f60
  Deleted: sha256:31b6eb74bf3fab6dc28943e1d75182df4bda2c4492be8832f284210a242ef613
  Deleted: sha256:ec5f89c7448aedfc8809060a76586a8c0dcf1cd8ef9745821897d3e1036c1276
  Deleted: sha256:9ef42db29c79bdc2e83f89246b56ca1e7d48f1715baf030c0a522f0eff6d8307
  Deleted: sha256:474089142dffc61e03e035a91c2b2e917c857dfc8ff9a57955be4e8a5c852e46
  Deleted: sha256:a0504c5887fade3935b656c121809138e8c2fe998784d22e449d1ec0eccf2334
  Deleted: sha256:aefbee168a4c236e3c35bcfce75ec11b7e25363a627ad0550207f0f579e4ae9f
  Making STAGING app container current...
  Untagged: aws_beanstalk/staging-app:latest
  eb-docker start/running, process 5769
  Docker container bbb6d9a61506 is running aws_beanstalk/current-app.
[i-0a437d75a06f1448f] [2019-04-21T19:34:34.342Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployEnactHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/appdeploy/enact.
[i-0a437d75a06f1448f] [2019-04-21T19:34:34.342Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployPostHook] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:34.342Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployPostHook/00_clean_imgs.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:36.212Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployPostHook/00_clean_imgs.sh] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:34:36.212Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployPostHook/01_monitor_pids.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:36.782Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployPostHook/01_monitor_pids.sh] : Completed activity. Result:
  ++ /opt/elasticbeanstalk/bin/get-config container -k proxy_server
  + EB_CONFIG_PROXY_SERVER=nginx
  + '[' nginx = none ']'
  + /opt/elasticbeanstalk/bin/healthd-track-pidfile --proxy nginx
  + /opt/elasticbeanstalk/bin/healthd-track-pidfile --name application --location /var/run/docker.pid
[i-0a437d75a06f1448f] [2019-04-21T19:34:36.782Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployPostHook/02_container_logging.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.147Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployPostHook/02_container_logging.sh] : Completed activity. Result:
  ++ docker ps --no-trunc -q
  + log_pattern='/var/lib/docker/containers/bbb6d9a61506bf0c70d21079e654e4399f92a878a34637ebbbaed3a514c01f8f/*.log'
  + /opt/elasticbeanstalk/bin/log-conf -n docker-container '-l/var/lib/docker/containers/bbb6d9a61506bf0c70d21079e654e4399f92a878a34637ebbbaed3a514c01f8f/*.log' -f /opt/elasticbeanstalk/containerfiles/support/docker.logrotate.conf -t rotatelogs
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.147Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1/AppDeployPostHook] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/hooks/appdeploy/post.
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.147Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AppDeployStage1] : Completed activity. Result:
  Application version switch - Command CMD-AppDeploy stage 1 completed
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.147Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsAfter] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.147Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsAfter/ConfigLogRotation] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.148Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsAfter/ConfigLogRotation/10-config.sh] : Starting activity...
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.340Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsAfter/ConfigLogRotation/10-config.sh] : Completed activity. Result:
  Disabled forced hourly log rotation.
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.340Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsAfter/ConfigLogRotation] : Completed activity. Result:
  Successfully execute hooks in directory /opt/elasticbeanstalk/addons/logpublish/hooks/config.
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.340Z] INFO  [4096]  - [Application update app-1259-190421_153004@22/AddonsAfter] : Completed activity.
[i-0a437d75a06f1448f] [2019-04-21T19:34:37.340Z] INFO  [4096]  - [Application update app-1259-190421_153004@22] : Completed activity. Result:
  Application update - Command CMD-AppDeploy succeeded