#!/bin/bash

## define namespace
NAMESPACE=dataroaster-argo-workflow

## define helm application name.
APP_NAME=argo-workflow

# install.
helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./;


# wait.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app=postgres,component=argo-workflow \
  --timeout=120s

kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app=workflow-controller,component=argo-workflow \
  --timeout=120s

kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app=argo-server,component=argo-workflow \
  --timeout=120s