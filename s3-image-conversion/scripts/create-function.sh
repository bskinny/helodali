aws lambda create-function \
	--region us-east-1 \
	--function-name image-conversion \
	--zip-file fileb://image-conversion.zip \
	--role arn:aws:iam::128225160927:role/helodali-image-processing \
	--handler index.handler \
	--runtime nodejs4.3 \
	--timeout 10 \
	--memory-size 1536

	#--profile admin \
