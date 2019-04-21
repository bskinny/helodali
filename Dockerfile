FROM clojure:alpine

RUN mkdir -p /app
WORKDIR /app
COPY project.clj /app/
RUN lein deps
COPY . /app

EXPOSE 3000

RUN lein with-profile webapp ring uberjar
CMD ["java", "-jar", "target/helodali.jar"]
