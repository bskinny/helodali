#!/usr/bin/env bash

export AWS_PROFILE = default

aws lambda add-permission \
	--function-name image-conversion \
	--region us-east-1 \
	--statement-id invode-helodali-image-conversion \
	--action "lambda:InvokeFunction" \
	--principal s3.amazonaws.com \
	--source-arn arn:aws:s3:::helodali-raw-images \
	--source-account 128225160927
