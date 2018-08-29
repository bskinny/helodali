#!/bin/sh

# Invoke with json file as only argument.

wget --header="Content-Type: application/json" \
  --post-file $1 http://localhost:10000
