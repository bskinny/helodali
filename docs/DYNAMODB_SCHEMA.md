
### The DynamoDB Schema 

The following describe the tables in use by the application. 

The tables which store items with a user/item one-to-one relationship have a single hash key with typically the
user's `uuid` being the key. Tables with multiple items per user, such as artwork, exhibitions, etc., have a hash
and range key pair which tell DynamoDB how to group the items. In this scenario, the hash key is the user's uuid 
stored in the `uref` attribute and the range key is the unique `uuid` attribute value representing the artwork/exhibition/etc. item.

The __accounts__ table is keyed on the user's uuid value stored in the `uuid` attribute and is referenced mostly just
at time of user login.
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableName": "accounts",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uuid"
            }
        ]
    }
}
```

The __artwork__ table is keyed by user uuid, `uref`, and item `uuid`. This table is read frequently and will have the most
growth, hence the higher read capacity.

```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "uref",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 4,
            "ReadCapacityUnits": 20
        },
        "TableName": "artwork",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uref"
            },
            {
                "KeyType": "RANGE",
                "AttributeName": "uuid"
            }
        ]
    }
}
```

The __contacts__ table is similar to the artwork table though not as frequently accessed.
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "uref",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableName": "contacts",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uref"
            },
            {
                "KeyType": "RANGE",
                "AttributeName": "uuid"
            }
        ]
    }
}
```

The __documents__ table is similar to the artwork table but again has less frequent access.
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "uref",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableName": "documents",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uref"
            },
            {
                "KeyType": "RANGE",
                "AttributeName": "uuid"
            }
        ]
    }
}
```

The __exhibitions__ table is similar to the artwork table but has less frequent access.
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "uref",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableName": "exhibitions",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uref"
            },
            {
                "KeyType": "RANGE",
                "AttributeName": "uuid"
            }
        ]
    }
}
```

The __expenses__ table is similar to the artwork table but has less frequent access.
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "uref",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableName": "expenses",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uref"
            },
            {
                "KeyType": "RANGE",
                "AttributeName": "uuid"
            }
        ]
    }
}
```

The __openid__ table stores the information needed to connect a user's identity elements: the user's Oauth2
Identity Provider's `sub` value, the _Cognito_ `identity-id`, and lastly the internally defined uuid value
for the user (typically referred to as `uref`).

This table is primarily keyed on `sub` value. In order to search by identity-id, there is a global secondary index defined.

There is also a stream defined for capturing an audit trail of table activity in CloudWatch.
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "identity-id",
                "AttributeType": "S"
            },
            {
                "AttributeName": "sub",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uref",
                "AttributeType": "S"
            }
        ],
        "GlobalSecondaryIndexes": [
            {
                "IndexName": "identity-id-uref-index",
                "Projection": {
                    "ProjectionType": "KEYS_ONLY"
                },
                "ProvisionedThroughput": {
                    "WriteCapacityUnits": 2,
                    "ReadCapacityUnits": 4
                },
                "IndexStatus": "ACTIVE",
                "KeySchema": [
                    {
                        "KeyType": "HASH",
                        "AttributeName": "identity-id"
                    },
                    {
                        "KeyType": "RANGE",
                        "AttributeName": "uref"
                    }
                ],
                "IndexArn": "arn:aws:dynamodb:us-east-1:<account-number>:table/openid/index/identity-id-uref-index",
                "ItemCount": 2
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableName": "openid",
        "StreamSpecification": {
            "StreamViewType": "NEW_AND_OLD_IMAGES",
            "StreamEnabled": true
        },
        "LatestStreamLabel": "2019-09-27T19:37:25.949",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "sub"
            }
        ],
        "LatestStreamArn": "arn:aws:dynamodb:us-east-1:<account-number>:table/openid/stream/2019-09-27T19:37:25.949"
    }
}
```

The __pages__ table is keyed only on user uuid and activity on the table triggers a Lambda function. Hence the
stream definition.
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableName": "pages",
        "StreamSpecification": {
            "StreamViewType": "NEW_AND_OLD_IMAGES",
            "StreamEnabled": true
        },
        "LatestStreamLabel": "2018-06-22T21:31:07.588",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uuid"
            }
        ],
        "LatestStreamArn": "arn:aws:dynamodb:us-east-1:<account-number>:table/pages/stream/2018-06-22T21:31:07.588"
    }
}
```

The __groupings__ table is similar to the exhibitions table.
```json
{
    "Table": {
        "TableArn": "arn:aws:dynamodb:us-east-1:128225160927:table/groupings",
        "AttributeDefinitions": [
            {
                "AttributeName": "uref",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "NumberOfDecreasesToday": 0,
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableSizeBytes": 0,
        "TableName": "groupings",
        "TableStatus": "ACTIVE",
        "TableId": "30efb896-f6ef-4feb-9f84-915cbf1a5cb7",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uref"
            },
            {
                "KeyType": "RANGE",
                "AttributeName": "uuid"
            }
        ],
        "ItemCount": 0,
        "CreationDateTime": 1583684938.057
    }
}
```

The __press__ table is similar to the artwork table but with less volume and read activity expected.
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "uref",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableName": "press",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uref"
            },
            {
                "KeyType": "RANGE",
                "AttributeName": "uuid"
            }
        ]
    }
}
```

The __profiles__ table defines the user's artist profile and is keyed only by user uuid.
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 2
        },
        "TableName": "profiles",
        "StreamSpecification": {
            "StreamViewType": "NEW_AND_OLD_IMAGES",
            "StreamEnabled": true
        },
        "LatestStreamLabel": "2018-06-14T02:34:23.897",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uuid"
            }
        ],
        "LatestStreamArn": "arn:aws:dynamodb:us-east-1:<account-number>:table/profiles/stream/2018-06-14T02:34:23.897"
    }
}
```

The __sessions__ table is referenced on every request to verify the access token present
in each request. It is also written to as often as tokens expire, which is once an hour. Note the global
secondary index for searching by `access-token` value. 
```json
{
    "Table": {
        "AttributeDefinitions": [
            {
                "AttributeName": "token",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uref",
                "AttributeType": "S"
            },
            {
                "AttributeName": "uuid",
                "AttributeType": "S"
            }
        ],
        "GlobalSecondaryIndexes": [
            {
                "IndexName": "uref-token-index",
                "Projection": {
                    "ProjectionType": "ALL"
                },
                "ProvisionedThroughput": {
                    "WriteCapacityUnits": 2,
                    "ReadCapacityUnits": 8
                },
                "KeySchema": [
                    {
                        "KeyType": "HASH",
                        "AttributeName": "uref"
                    },
                    {
                        "KeyType": "RANGE",
                        "AttributeName": "token"
                    }
                ],
                "IndexArn": "arn:aws:dynamodb:us-east-1:<account-number>:table/sessions/index/uref-token-index"            }
        ],
        "ProvisionedThroughput": {
            "WriteCapacityUnits": 2,
            "ReadCapacityUnits": 4
        },
        "TableName": "sessions",
        "KeySchema": [
            {
                "KeyType": "HASH",
                "AttributeName": "uuid"
            }
        ]
    }
}
```

The __sessions__ table also contains a time-to-live attribute. Below is 
the `aws dynamodb describe-time-to-live` command output.
```json
{
    "TimeToLiveDescription": {
        "AttributeName": "expire-at",
        "TimeToLiveStatus": "ENABLED"
    }
}
```
