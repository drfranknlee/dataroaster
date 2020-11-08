#!/bin/bash

# create secret for aws keys.
kubectl create secret generic presto-s3-keys \
--from-literal=access-key=bWluaW8= \
--from-literal=secret-key=bWluaW8xMjM= \
-n presto;

# create configmap.
kubectl create configmap presto-cfg --dry-run \
--from-file=config.properties.coordinator \
--from-file=config.properties.worker \
--from-file=jvm.config \
--from-file=node.properties.template \
--from-file=hive.properties.template \
-o yaml \
-n presto | kubectl apply -f -

# install presto.
kubectl apply -f presto.yaml -n presto;

