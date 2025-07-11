#!/bin/bash

set -x

echo "S3バケットの作成を開始します..."
awslocal s3 mb s3://profile-images
awslocal s3 ls
