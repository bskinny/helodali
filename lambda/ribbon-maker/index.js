//
// This function is responsible for creating a ribbon.img for a set of exhibition images. Given a s3 path
// prefix, all images under the prefix are fetched, resized, and composited into a ribbon image and put
// back on s3 with the key = prefix/ribbon.jpg

// dependencies
var AWS = require('aws-sdk');
var sharp = require('sharp');
var util = require('util');

// constants
var OVERLAY_WIDTH  = 40;
var OVERLAY_HEIGHT = 40;
var IMAGES_PER_ROW = 9;
var S3_BUCKET = "helodali-public-pages";

// reference to S3 client
var s3 = new AWS.S3();

function processPrefix(record, callback) {

    // We expect a prefix that points to an exhibition directory in s3 and which contains a 'thumbs' directory. We create
    // the ribbon from the thumbs and place it in the path pointed to by prefix.
    let prefix = record.Sns.Message;

    // Point to the /thumbs in the prefix directory
    if (!prefix.endsWith('/')) {
        prefix = prefix + '/';
    }
    thumbsPath = prefix + "thumbs/";

    s3.listObjectsV2({Bucket: S3_BUCKET, Prefix: thumbsPath}, function(err, listResponse) {
        let numImages = listResponse.Contents.length;
        console.log("Processing " + numImages + " images for prefix " + prefix);

        if (numImages == 0) {
            return;
        }

        // Determine the ribbon dimensions. We wrap the ribbon after nine images; each
        // image is 40x40 pixels. A ribbon representing 21 images will have three rows
        // with the last row containing three images.
        let numRows = Math.trunc(numImages / IMAGES_PER_ROW);
        if (numImages % IMAGES_PER_ROW > 0) { numRows = numRows + 1; };

        let ribbonWidth = OVERLAY_WIDTH * (numImages > IMAGES_PER_ROW ? IMAGES_PER_ROW : numImages);
        let ribbonHeight = OVERLAY_HEIGHT * numRows;

        let ribbon = sharp({
            create: {
                width: ribbonWidth, height: ribbonHeight, channels: 4,
                background: {r: 255, g: 255, b: 255, alpha: 128}
            }
        });

        let options = {raw: {width: ribbonWidth, height: ribbonHeight, channels: 4}};

        let s3promises = listResponse.Contents.map(img => s3.getObject({ Bucket: S3_BUCKET, Key: img.Key }).promise());

        Promise.all(s3promises).then(function(s3responses) {
            const composite = s3responses.reduce(function(acc, overlayResponse, currentIndex, origArray) {
                // Calculate the placement of the current tile in the ribbon

                return acc.then(function(data) {
                    return sharp(overlayResponse.Body).resize(OVERLAY_WIDTH, OVERLAY_HEIGHT).crop(sharp.strategy.entropy).toBuffer().then(function (overlay) {
                        var left = OVERLAY_WIDTH * (currentIndex % IMAGES_PER_ROW);
                        var top = OVERLAY_HEIGHT * Math.trunc(currentIndex / IMAGES_PER_ROW);
                        return sharp(data, options).overlayWith(overlay, {left: left, top: top}).raw().toBuffer();
                    }).catch((err) => {
                        console.log('Error adding image to ribbon: ' + err);
                      });
                }).catch((err) => {
                    console.log('Error processing images when creating ribbon: ' + err);
                  });
            }, ribbon.raw().toBuffer());

            composite.then(function(data) {
                sharp(data, options).jpeg().toBuffer(function(err, data, info) {
                    if (err) {
                        console.error("Err: " + err);
                    } else {
                        s3.putObject({Bucket: S3_BUCKET, ACL: "public-read", Key: prefix + 'ribbon.jpg',
                            Body: data, ContentType: "image/" + info.format},
                            function(err, info) {
                                if (err) {
                                    console.error("Unable to put ribbon.jpg: " + err);
                                } else {
                                    console.log("Created ribbon.jpg");
                                }
                            });
                    }
                });
            }).catch((err) => {
                console.log('Error putting ribbon to s3: ' + err);
              });
        });
    });
}

exports.handler = function(event, context, callback) {
  // event.Records is a list
  event.Records.map(processPrefix, callback);
};
