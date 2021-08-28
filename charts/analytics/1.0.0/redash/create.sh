#!/bin/bash

set -x


## define namespace
NAMESPACE=dataroaster-redash

## define helm application name.
APP_NAME=redash

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./;

# wait for a while to initialize redash.
sleep 20

# create tables.
kubectl exec -it -n ${NAMESPACE} \
$(kubectl get po -l app=redash -n ${NAMESPACE} -o jsonpath={.items[0].metadata.name}) \
-c server -- /app/bin/docker-entrypoint create_db;