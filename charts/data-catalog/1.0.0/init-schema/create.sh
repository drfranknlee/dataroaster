#!/bin/bash

set -x


## define namespace
NAMESPACE=dataroaster-hivemetastore

## define helm application name.
APP_NAME=hivemetastore-init-schema

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./;