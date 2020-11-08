#!/bin/bash

set -e

cd docker;

# create hivemetastore namespace.
kubectl create namespace hive-metastore;

# Update configmaps
kubectl create configmap metastore-cfg --dry-run --from-file=metastore-site.xml --from-file=core-site.xml -o yaml -n hive-metastore | kubectl apply -f -

# create secret for aws keys.
kubectl create secret generic my-s3-keys --from-literal=access-key='minio' --from-literal=secret-key='minio123' -n hive-metastore;
