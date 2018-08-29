'use strict';
console.log('Loading function');
const AWS = require('aws-sdk');
const sesClient = new AWS.SES();
var dynamoDB = new AWS.DynamoDB.DocumentClient({apiVersion: '2012-08-10',
                                                endpoint: 'dynamodb.us-east-1.amazonaws.com',
                                                region: 'us-east-1'});

/**
 * Lambda to process HTTP POST for contact form with the following body
 * {
      "email": <contact-email, optional>,
      "recip": <helodali openid sub of user>,
      "message": <contact-message>
    }
 *
 */
 
function isEmptyObject( obj ) {
  for ( var name in obj ) {
      return false;
  }
  return true;
}

exports.handler = (event, context, callback) => {
    console.log('Received event:', JSON.stringify(event, null, 2));
    
    var response = {
        statusCode: 200,
        headers: {
          "Access-Control-Allow-Origin" : "*", // Required for CORS support to work
          "Access-Control-Allow-Credentials" : true // Required for cookies, authorization headers with HTTPS
        }
    };
    
    let formData = JSON.parse(event.body);
    
    // ensure there a message to send
    if (formData.message == null || formData.message.length == 0) {
        response.statusCode = 400;
        callback(null, response);
        return;
    }
    
    // resolve the helodali uuid to an email address that must be already validated in AWS SES
    dynamoDB.get({Key: {'uuid': formData.recip},
                  TableName: 'profiles',
                  AttributesToGet: ['email']}).promise()
    // send the message              
    .then(result => {
        if (isEmptyObject(result)) {
              throw new Error('No openid item for user ' + formData.recip);
        }
        let recipient = result.Item.email;
        
         // create the params for submission to SES
        let params = getParams(formData.message, recipient);
    
        // if an email address is provided in request, use it as the replyTo
        if (formData.email != null && formData.email.length > 0) {
            params.ReplyToAddresses = [ formData.email ];
        }
        
        return sesClient.sendEmail(params).promise();
    })
    .then(result => {
        console.log(result);
        callback(null, response);
    })
    .catch(err => {
        console.log(err);
        response.statusCode = 500;
        callback(null, response);
    })
};

function getParams(messageText, recipient) {
    return {
        Destination: { ToAddresses: [ recipient ] },
        Message: {
            Body: {
                Text: { Data: messageText }
            },
            Subject: { Data: 'A message from your website' }
        },
        Source: recipient
    };
}