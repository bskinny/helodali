# helodali

An artist inventory system with Instagram integration

![Helodali Screenshot](https://raw.githubusercontent.com/bskinny/helodali/master/resources/doc/images/helodali-screenshot.png)

Helodali is a SPA style webapp using [re-frame](https://github.com/Day8/re-frame) backed by server-side clojure, AWS DynamoDB, S3, and Lambda.
The application is served via jetty in src/clj/server.clj and depends on a session cookie at time of login and a client-provided access
token afterwards. The access token, provided by Amazon Cognito, is presented by the client in every request and compared with a cached 
copy stored in DynamoDB. Amazon Lambda and S3 are used to process and store images and documents.

Helodali can also produce an artist website, as seen [here](http://mayalane.com). The generation of the website is handled by
multiple AWS Lambda functions: public-page-generator, ribbon-maker, and contact-form.

### Development Mode

Define the following environment variables before proceeding (the INSTAGRAM values are optional):

```
export AWS_ACCESS_KEY=...
export AWS_SECRET_KEY=...
export AWS_DYNAMODB_ENDPOINT=dynamodb.us-east-1.amazonaws.com

export HD_INSTAGRAM_CLIENT_ID=...
export HD_INSTAGRAM_CLIENT_SECRET=..
export HD_INSTAGRAM_REDIRECT_URI=http://localhost:9500/instagram/oauth/callback

export HD_COGNITO_CLIENT_ID=...
export HD_COGNITO_CLIENT_SECRET=...
export HD_COGNITO_REDIRECT_URI=http://localhost:9500/login

export HD_CREATE_RIBBON_TOPIC_ARN=<arn for hd-create-ribbon only needed for website deployment>
```

Once the environment is defined, you can run figwheel in one of two ways. With lein:

```
lein clean
rlwrap lein figwheel
```

Or with the clojure command line tool:
```
clojure -m figwheel.main --build dev --repl
```

Wait a bit, then browse to [http://localhost:9500](http://localhost:9500).


## Production Builds

The helodali webapp can be built with the webapp profile like so:
```
lein clean
lein with-profile webapp ring uberwar
```

And then deployed with:

```
scripts/eb-deploy.pl
```