#!/bin/sh

# Run the docker container locally with 'eb local'

# Ensure that the eb cli has an environment defined with 'eb use <env>'

. ~/env-helodali.sh

eb local run --port 9500 --envvars "AWS_ACCESS_KEY=$AWS_ACCESS_KEY,AWS_SECRET_KEY=$AWS_SECRET_KEY,AWS_DYNAMODB_ENDPOINT=$AWS_DYNAMODB_ENDPOINT,HD_INSTAGRAM_CLIENT_ID=HD_INSTAGRAM_CLIENT_ID,HD_INSTAGRAM_CLIENT_SECRET=$HD_INSTAGRAM_CLIENT_SECRET,HD_INSTAGRAM_REDIRECT_URI=$HD_INSTAGRAM_REDIRECT_URI,HD_COGNITO_CLIENT_ID=$HD_COGNITO_CLIENT_ID,HD_COGNITO_CLIENT_SECRET=$HD_COGNITO_CLIENT_SECRET,HD_COGNITO_REDIRECT_URI=$HD_COGNITO_REDIRECT_URI"


