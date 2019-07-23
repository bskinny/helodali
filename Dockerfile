FROM clojure:alpine

# Create an image which contains clojure, leiningen, and the helodali source.
# The source will be built with minor cache-busting adjustments.
# The associated container is simply executing the uberjar.

# NOTE: Environment variables, or java properties, are required and provided
# by the combination of .ebextensions/options.config and EB Configuration (see
# the EB application environment configuration definition in the EB Console).

# Install tini to act as an a init process (pid 1) to allow jstack/jmap of the
# java process. This can also be done with docker run --init but passing
# docker run command line args through to elastic beanstalk does not seem
# possible at this time (07/21/19). See also the ENTRYPOINT below.
RUN apk add --no-cache tini

RUN mkdir -p /app
WORKDIR /app
COPY project.clj /app/

# Run lein deps to cache dependencies
RUN lein deps

COPY . /app

# Perform some asset renaming for cache busting purposes
RUN ts=$(date +%s) \
    && sed -i "s/app.js/app-$ts.js/g" project.clj \
    && sed -i "s/app.js/app-$ts.js/g" resources/public/index.html \
    && mv resources/public/css/helodali.css resources/public/css/helodali-$ts.css \
    && sed -i "s/helodali.css/helodali-$ts.css/g" resources/public/index.html \
    && sed -i "s/helodali.css/helodali-$ts.css/g" resources/public/static/privacy.html

EXPOSE 3000

RUN lein with-profile webapp ring uberjar
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-Xms1g", "-Xmx1g", "-XX:NewSize=256m", "-XX:MaxNewSize=256m", "-verbose:gc", \
     "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps", "-jar", "target/helodali.jar"]
