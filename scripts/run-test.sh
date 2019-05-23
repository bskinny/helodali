#!/bin/sh

# Spin up a local DynamoDB via docker container, run the tests, and remove the container

# Connection configuration for dynamodb-local
export AWS_ACCESS_KEY=12345678901234567890
export AWS_SECRET_KEY=1234567890123456789012345678901234567890
export AWS_DYNAMODB_ENDPOINT=http://localhost:8000

docker run -d -p 8000:8000 --name "localddb" amazon/dynamodb-local
lein test
docker stop localddb
docker rm localddb
