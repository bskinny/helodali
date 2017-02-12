#!/bin/sh

. ~/env-helodali.sh

curl -F "client_id=${HD_INSTAGRAM_CLIENT_ID}" \
	   -F "client_secret=${HD_INSTAGRAM_CLIENT_SECRET}" \
		 -F 'object=user' \
		 -F 'aspect=media' \
		 -F 'verify_token=123qweasd' \
		 -F 'callback_url=https://api.helodali.com/instagram/subscription-handler' \
		 https://api.instagram.com/v1/subscriptions/
