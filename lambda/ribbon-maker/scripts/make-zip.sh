#!/bin/sh

rm -rf node_modules
docker run -v "$PWD":/var/task lambci/lambda:build-nodejs8.10 npm install

# Create the zip with just index.js and node_modules at the top level.
rm -f ribbon-make.zip
mkdir zip-contents
cp index.js zip-contents
mv node_modules zip-contents
cd zip-contents
zip -r ../ribbon-maker.zip index.js node_modules
cd ..
rm -rf zip-contents
