# helodali

An artist inventory system with social media integration

Helodali is a SPA style webapp using [re-frame](https://github.com/Day8/re-frame) backed by server-side clojure, AWS DynamoDB, and S3.
The application is served via jetty in src/clj/server.clj and depends on a session cookie at time of login only where the cookie points
into a session store on the server. Otherwise, the application does not depend on anything defined locally on the server.
An access token,provided by Amazon Cognito, is presented by the client in every request and compared with a cached copy stored in DynamoDB.
Amazon S3 is used to process and store images and documents for long term archival.

## Development Mode

### Run application:
Define the following environment variables before proceeding: AWS_ACCESS_KEY, AWS_SECRET_KEY and AWS_DYNAMODB_ENDPOINT.

For Instagram integration, also define HD_INSTAGRAM_CLIENT_ID, HD_INSTAGRAM_CLIENT_SECRET and HD_INSTAGRAM_REDIRECT_URI.

```
lein clean
rlwrap lein figwheel
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).


## Production Builds

The helodali webapp can be built with the webapp profile like so:
```
lein clean
lein with-profile webapp ring uberwar
```

Similarly, the helodali API, used by Instagram subscriptions, is built with:
```
lein clean
lein with-profile api ring uberwar
```
