FROM clojure:alpine

RUN mkdir -p /app
WORKDIR /app
COPY project.clj /app/
RUN lein deps
COPY . /app

# Perform some asset renaming for cache busting purposes
RUN ts=$(date +%s) \
    && sed -i "s/app.js/app-$ts.js/g" project.clj \
    && sed -i "s/app.js/app-$ts.js/g" resources/public/index.html

EXPOSE 3000

RUN lein with-profile webapp ring uberjar
CMD ["java", "-jar", "target/helodali.jar"]
