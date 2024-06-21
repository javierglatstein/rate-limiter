#!/bin/bash

aws --endpoint-url=http://localhost:4566 \
    sqs create-queue \
    --queue-name local-scheduled-messages

aws --endpoint-url=http://localhost:4566 \
    sqs create-queue \
    --queue-name local-processed-messages
