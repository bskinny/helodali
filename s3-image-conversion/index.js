// This function is responsible for processing images uploaded to the helodali-raw-images bucket:
// - A (large) thumb is created and placed in the helodali-images bucket with the same key Value
//   as the source bucket key.
// - The references to both the original raw image and processed thumb are written
//   to the user's DynamoDB 'artwork' table.
//
// The raw image object key looks like a filename (S3 let's us think of /-delimited strings
// as unix filenames):
// facebook|10208314583117362/b1543a71-b751-11e6-af6f-f8a3047232a7/1073c8b0-ab47-11e6-8f9d-c83ff47bbdcb/284187881_VMFA_7.jpg
// Which is of the form: openid-sub / artwork-uuid / image-uuid / filename

// dependencies
var async = require('async');
var AWS = require('aws-sdk');
var sharp = require('sharp');
var util = require('util');

// constants
var MAX_WIDTH  = 480;
var MAX_HEIGHT = 480;

// get reference to S3 client
var s3 = new AWS.S3();
var dynamoDB = new AWS.DynamoDB.DocumentClient({apiVersion: '2012-08-10',
                                                endpoint: 'dynamodb.us-east-1.amazonaws.com',
                                                region: 'us-east-1'});

function isEmptyObject( obj ) {
  for ( var name in obj ) {
      return false;
  }
  return true;
}

exports.handler = function(event, context, callback) {
  // Read options from the event.
  console.log("Reading options from event:\n", util.inspect(event, {depth: 5}));
  var eventName = event.Records[0].eventName;
  var srcBucket = event.Records[0].s3.bucket.name;
  // Object key may have spaces or unicode non-ASCII characters.
  var srcKey = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, " "));
  var fileSize = event.Records[0].s3.object.size;
  var dstBucket = "helodali-images";
  var dstKey = srcKey;
  var nameComponents = srcKey.split('/');

  // Sanity check: validate that source and destination are different buckets.
  if (srcBucket == dstBucket) {
      callback("Source and destination buckets are the same.");
      return;
  }

  // If we are triggered on object removal
  if (eventName.match(/^ObjectRemoved:/i)) {
    async.waterfall([
        function removeFile(next) {
            // Remove the associated object in the bucket helodali-images
            s3.deleteObject({Bucket: dstBucket, Key: dstKey}, next)
          },
        function getUref(result, next) {
            // Find the user's uuid to later reference in the artwork table
            // console.log(nameComponents[0]);
            dynamoDB.get({Key: {'sub': nameComponents[0]},
                          TableName: 'openid',
                          AttributesToGet: ['uref']},
                          function (err, data) {
                               if (err) {
                                 next(err);
                               } else {
                                 next(null, data);
                               }
                        });
          },
        function fetchImages(data, next) {
            if (isEmptyObject(data)) {
              next('No openid item for user ' + nameComponents[0])
            }
            dynamoDB.get({Key: {'uref': data.Item.uref,
                                'uuid': nameComponents[1]},
                          TableName: 'artwork',
                          AttributesToGet: ['images', 'uref']},
                          function (err, data) {
                               if (err) {
                                 next(err);
                               } else {
                                 next(null, data);
                               }
                        });
            },
        function update(data, next) {
            if (isEmptyObject(data)) {
              next('No images for artwork ' + nameComponents[1])
            }
            var images = data.Item.images;
            var idx = images.findIndex(function (img) {
                                         return img.key == srcKey;
                                       });
            if (idx == -1) {
              next("The image was not found in the db, for key: " + srcKey)
            } else {
              var updateExpression = 'REMOVE #images[' + idx + ']';
              var params = {TableName: 'artwork',
                            Key: {'uref': data.Item.uref,
                                  'uuid': nameComponents[1]},
                            UpdateExpression: updateExpression,
                            ExpressionAttributeNames: {'#images': 'images'}};
              console.log("UpdateExpression: " + updateExpression)
              dynamoDB.update(params, next);
            }
          }
        ], function (err, result) {
               if (err) {
                   console.error('Unable to remove ' + dstKey + ' from ' + dstBucket +
                                 ' due to an error: ' + err);
                   callback(err)
               } else {
                   console.log('Successfully removed ' + dstKey +
                               ' from ' + dstBucket + ': '  + result);
                   callback(null);
               }
         }
    );
  }

  // If we are triggered on object creation
  if (eventName.match(/^ObjectCreated:/i)) {
    // Download the image from S3, transform, and upload to a different S3 bucket.
    async.waterfall([
        function download(next) {
            // Download the image from S3 into a buffer.
            s3.getObject({
                    Bucket: srcBucket,
                    Key: srcKey
                },
                next);
            },
        function transform(response, next) {
            original = sharp(response.Body);
            original
              .metadata()
              .then(function resizeIt(metadata) {
                 original
                   .resize(MAX_WIDTH, MAX_HEIGHT)
                   .max()
                   .withoutEnlargement()
                   .jpeg({"quality": 100})
                   .toBuffer(function (err, data, info) {
                       if (err) {
                         next(err);
                       } else {
                         next(null, "image/" + info.format, data, metadata);
                       }
                   });
              });
            },
        function upload(contentType, data, metadata, next) {
            // Stream the transformed image to a different S3 bucket.
            s3.putObject({
                    Bucket: dstBucket,
                    Key: dstKey,
                    Body: data,
                    ContentType: contentType
                }, function (err, info) {
                     if (err) {
                       next(err);
                     } else {
                       next(null, info, metadata);
                     }
                });
            },
        function getUref(result, metadata, next) {
            // Find the user's uuid to later reference in the artwork table
            // console.log(nameComponents[0]);
            dynamoDB.get({Key: {'sub': nameComponents[0]},
                          TableName: 'openid',
                          AttributesToGet: ['uref']},
                          function (err, data) {
                               if (err) {
                                 next(err);
                               } else {
                                 next(null, data, metadata);
                               }
                        });
            },
        function update(data, metadata, next) {
            // console.log(data)

            if (isEmptyObject(data)) {
              next('No openid item for user ' + nameComponents[0])
            }

            // Update the artwork's images attributes by appending the new image to the
            // existing list of images. The "ADD" action does this for us.
            var originalMetadata = {'format': metadata.format,
                                    'width': metadata.width,
                                    'height': metadata.height,
                                    'space': metadata.space,
                                    'size': fileSize,
                                    'density': metadata.density}
            var params = {TableName: 'artwork',
                          Key: {'uref': data.Item.uref,
                                'uuid': nameComponents[1]},
                          AttributeUpdates: {'images': {Action: 'ADD', Value: [{'key': srcKey,
                                                                                'uuid': nameComponents[2],
                                                                                'metadata': originalMetadata,
                                                                                'filename': nameComponents[3]}]}}};
            dynamoDB.update(params, next);
            }
        ], function (err, result) {
            if (err) {
                console.error('Unable to resize ' + srcBucket + '/' + srcKey +
                              ' and upload to ' + dstBucket + '/' + dstKey +
                              ' due to an error: ' + err);
                callback(err)
            } else {
                console.log('Successfully resized ' + srcBucket + '/' + srcKey +
                            ' into ' + dstBucket + '/' + dstKey + ': ' + result);
                callback(null);
            }
        }
    );
  }
};
