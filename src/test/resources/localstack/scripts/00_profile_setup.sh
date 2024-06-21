#!/bin/bash
echo "########### Creating profile ###########"
aws --endpoint-url=http://localhost:4566 configure set aws_access_key_id aws-local-key --profile=default
aws --endpoint-url=http://localhost:4566 configure set aws_secret_access_key aws-local-secret-key --profile=default
aws --endpoint-url=http://localhost:4566 configure set region us-east-1 --profile=default
