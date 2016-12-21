#!/bin/sh

cp index.js zip-contents
cd zip-contents
zip -r ../image-conversion.zip index.js node_modules
cd ..

