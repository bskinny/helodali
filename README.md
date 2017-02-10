# helodali

An artist inventory system with social media integration

Helodali is a SPA style webapp using [re-frame](https://github.com/Day8/re-frame) backed by a small clojure webserver and AWS DynamoDB + S3. The application is served via jetty in src/clj/server.clj and does not depend on state or anything defined locally on the serer. An access token, provided by [Auth0](https://github.com/auth0/lock), is presented by the client in every request and compared with a cached copy stored in the user's profile on DynamoDB. Amazon S3 is used to process and store images and documents for long term archival.

## Development Mode

### Run application:
Define the following environment variables before proceeding: AWS_DYNAMODB_ACCESS_KEY, AWS_DYNAMODB_SECRET_KEY and AWS_DYNAMODB_ENDPOINT.

For Instagram integration, also define HD_INSTAGRAM_CLIENT_ID, HD_INSTAGRAM_CLIENT_SECRET and HD_INSTAGRAM_REDIRECT_URI.

```
lein clean
rlwrap lein figwheel
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

### Run tests (Er... not yet):

```
lein clean
lein doo phantom test once
```

The above command assumes that you have [phantomjs](https://www.npmjs.com/package/phantomjs) installed. However, please note that [doo](https://github.com/bensu/doo) can be configured to run cljs.test in many other JS environments (chrome, ie, safari, opera, slimer, node, rhino, or nashorn).

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
