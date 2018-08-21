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
var S3_BUCKET = "helodali-public-pages";

// reference to S3 client
var s3 = new AWS.S3();

function processPrefix(record, callback) {

    let prefix = record.Sns.Message;

    // Append a trailing slash onto prefix if it is not already present.
    if (!prefix.endsWith('/')) {
        prefix = prefix + '/';
    }

    s3.listObjectsV2({Bucket: S3_BUCKET, Prefix: prefix}, function(err, listResponse) {
        let numImages = listResponse.Contents.length;
        console.log("Processing " + numImages + " images for prefix " + prefix);
        let ribbon = sharp({
            create: {
                width: OVERLAY_WIDTH * numImages, height: OVERLAY_HEIGHT, channels: 4,
                background: {r: 255, g: 255, b: 255, alpha: 128}
            }
        });

        let options = {raw: {width: OVERLAY_WIDTH * numImages, height: OVERLAY_HEIGHT, channels: 4}};

        let s3promises = listResponse.Contents.map(img => s3.getObject({ Bucket: S3_BUCKET, Key: img.Key }).promise());

        Promise.all(s3promises).then(function(s3responses) {
            const composite = s3responses.reduce(function(acc, overlayResponse, currentIndex, origArray) {
                return acc.then(function(data) {
                    return sharp(overlayResponse.Body).resize(40, 40).crop(sharp.strategy.entropy).toBuffer().then(function (overlay) {
                        return sharp(data, options).overlayWith(overlay, {left: OVERLAY_WIDTH * currentIndex, top: 0}).raw().toBuffer();
                    });
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
            });
        });
    });
}

exports.handler = function(event, context, callback) {
  // event.Records is a list
  event.Records.map(processPrefix, callback);
};
