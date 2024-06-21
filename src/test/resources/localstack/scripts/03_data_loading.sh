#!/bin/bash

aws --endpoint-url=http://localhost:4566 \
    dynamodb put-item \
    --table-name local-table \
    --item '{"PK": {"S": "local-bucket"},"SK": {"N": "1"}, "refilled_at":{ "S":"2024-01-01T00:00:00Z"}, "tokens": { "N": "1"}}'

aws --endpoint-url=http://localhost:4566 \
    dynamodb put-item \
    --table-name local-table \
    --item '{"PK": {"S": "local-bucket"},"SK": {"N": "2"}, "refilled_at":{ "S":"2024-01-01T00:00:00Z"}, "tokens": { "N": "1"}}'

aws --endpoint-url=http://localhost:4566 \
    dynamodb put-item \
    --table-name local-table \
    --item '{"PK": {"S": "local-bucket"},"SK": {"N": "3"}, "refilled_at":{ "S":"2024-01-01T00:00:00Z"}, "tokens": { "N": "1"}}'

aws --endpoint-url=http://localhost:4566 \
    dynamodb put-item \
    --table-name local-table \
    --item '{"PK": {"S": "local-bucket"},"SK": {"N": "4"}, "refilled_at":{ "S":"2024-01-01T00:00:00Z"}, "tokens": { "N": "1"}}'

aws --endpoint-url=http://localhost:4566 \
    dynamodb put-item \
    --table-name local-table \
    --item '{"PK": {"S": "local-bucket"},"SK": {"N": "5"}, "refilled_at":{ "S":"2024-01-01T00:00:00Z"}, "tokens": { "N": "1"}}'

aws --endpoint-url=http://localhost:4566 \
    dynamodb put-item \
    --table-name local-table \
    --item '{"PK": {"S": "local-bucket"},"SK": {"N": "6"}, "refilled_at":{ "S":"2024-01-01T00:00:00Z"}, "tokens": { "N": "1"}}'
