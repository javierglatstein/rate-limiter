#!/bin/bash

aws --endpoint-url=http://localhost:4566 \
    dynamodb create-table \
    --table-name local-table \
    --attribute-definitions \
	      AttributeName=PK,AttributeType=S \
	      AttributeName=SK,AttributeType=N \
    --key-schema \
	      AttributeName=PK,KeyType=HASH \
	      AttributeName=SK,KeyType=RANGE \
    --provisioned-throughput \
	      ReadCapacityUnits=100,WriteCapacityUnits=100 \
