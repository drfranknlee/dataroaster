#!/bin/bash

set -x


## define namespace
NAMESPACE={{ redashNamespace }}

## define helm application name.
APP_NAME=redash

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./ \
--kubeconfig={{ kubeconfig }};

# wait for a while to initialize redash.
sleep 5

# wait.
kubectl wait --namespace ${NAMESPACE} \
--for=condition=ready pod \
--selector=app=redash \
--timeout=120s \
--kubeconfig={{ kubeconfig }};

# create tables.
kubectl exec -it -n ${NAMESPACE} \
$(kubectl get po -l app=redash -n ${NAMESPACE} -o jsonpath={.items[0].metadata.name}) \
-c server \
--kubeconfig={{ kubeconfig }} \
-- /app/bin/docker-entrypoint create_db;