aws lambda invoke \
 --invocation-type Event \
 --function-name image-conversion \
 --region us-east-1 \
 --payload file://test-event.txt \
 outputfile.txt
