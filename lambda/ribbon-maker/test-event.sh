#!/bin/sh

wget --header="Content-Type: application/json" \
  --post-file test-event.json \
  http://localhost:10000
