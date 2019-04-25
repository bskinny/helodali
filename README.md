# helodali

An artist image inventory system with Instagram integration

![Helodali Screenshot](https://raw.githubusercontent.com/bskinny/helodali/master/resources/doc/images/helodali-screenshot.png)

Helodali is a SPA style webapp using [re-frame](https://github.com/Day8/re-frame) backed by server-side clojure, AWS Elastic Beanstalk, 
DynamoDB, S3, Cognito, and Lambda. The server is deployed using a single docker container with load balancing and auto-scaling managed by 
Elastic Beanstalk.

Artwork images are uploaded directly or imported from an Instagram feed and annotated according to exhibitions, purchases, etc.
Basic revenue and expense is tracked as well as artist CV information. See [REQUIREMENTS](docs/REQUIREMENTS.md) for more detail. 

#### Users and Authentication
Users of the application are assigned identities by AWS Cognito based on native account creation with Amazon or 
external identity provider authentication with Google or Facebook. The Oauth2 `authorization_code` grant type is used with the helodali
server component performing the request for an access token on behalf of the user. The token is stored in a DynamoDB session table and also
returned to the client for resubmission on future requests. Any authenticated request from a user must include the access token which
is compared by the server against the session table.

When registering users natively or through Google or Facebook, only email and name values are requested and stored in the helodali 
database. This information is later available upon every user sign-in via the [OIDC](https://openid.net/specs/openid-connect-core-1_0.html) 
ID Token which is acquired by the helodali server from Cognito. In this token is also the `sub` claim which defines the subject identifier that
is stored in the helodali database as the user's `uuid` value. **Helodali does not request or desire write or post permission to 
services such as Google, Facebook, or Instagram. Facebook is only used for authentication purposes.**


#### AWS S3 Access
The helodali clojurescript application communicates directly with AWS S3 when fetching or storing images and documents.
Because images and documents are sensitive information they are stored in private S3 buckets and are under user-level access control
based on the pathname of the files (S3 keys). For example, the bucket helodali-images contains the key 
_8bcb5043-1571-46db-b944-0c7845e08d4b/1041b850...a2b/1041b8...3fa2b/some-image.jpg_
which has a top-level basename of _8bcb5043-1571-46db-b944-0c7845e08d4b_. This value is the user's unique identifier found in the
OIDC sub claim and the key element to enforcing user-level access control. The IAM policy definition resource reference is
`"arn:aws:s3:::/${cognito-identity.amazonaws.com:sub}/*"`. See the _Cognito_HelodaliIdentityPoolAuth_Role_ role defined in IAM.

A drawback of image privacy is the need to continually refresh the secure URLs provided by S3. The default one-hour timeout of the URLs is
excessive for the purposes of this application.

#### Image Upload and Resizing
As mentioned above, the web application uploads and deletes images directly against S3, specifically the _helodali-raw-images_ bucket.
The events trigger the [s3-image-conversion](lambda/s3-image-conversion/README.md) AWS Lambda function which makes corresponding changes to the user's DynamoDB artwork item. An upload results in 
the Lambda function creating mutiple smaller, web-friendly, versions of the image and storing them in separate S3 buckets.

#### Instagram Integration
The ability to let the helodali user import images from Instagram is a convenience in managing the inventory. Instagram integration 
is performed on the helodali server using the Oauth2 `authentication_code` flow, redirecting the user's browser to Instagram for authentication 
and approval, returning an authorization code to the helodali server. The server will then exchange the code for an Instagram access 
token which is stored in the `accounts` database table for reuse. 

#### Website Creation
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

#### Local builds of aws-sdk-js dependency
Until the [issue](https://github.com/cljsjs/packages/issues/1619) with the externs file is addressed, there is a local copy of aws-sdk-js installed
using the following process in a [branch](https://github.com/bskinny/packages/tree/aws-sdk-js-update) of cljsjs/packages:
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
  "constructor": function () {},
  "createClients": function () {},
  "expiryWindow": function () {},
  "get": function () {},
  "getCredentialsForIdentity": function () {},
  "getCredentialsFromSTS": function () {},
  "getId": function () {},
  "getPromise": function () {},
  "getStorage": function () {},
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
2. Update the version number if necessary in aws-sdk-js/build.boot
3. `boot package install target`

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