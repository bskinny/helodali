FROM clojure:alpine

# Create an image which contains clojure, leiningen, and the helodali source.
# The source will be built with minor cache-busting adjustments.
# The associated container is simply executing the uberjar.

# NOTE: Environment variables, or java properties, are required and provided
# by the combination of .ebextensions/options.config and EB Configuration (see
# the EB application environment configuration definition in the EB Console).

RUN mkdir -p /app
WORKDIR /app
COPY project.clj /app/

# Run lein deps to cache dependencies
RUN lein deps

COPY . /app

# Perform some asset renaming for cache busting purposes
RUN ts=$(date +%s) \
    && sed -i "s/app.js/app-$ts.js/g" project.clj \
    && sed -i "s/app.js/app-$ts.js/g" resources/public/index.html

EXPOSE 3000

RUN lein with-profile webapp ring uberjar
CMD ["java", "-jar", "target/helodali.jar"]
