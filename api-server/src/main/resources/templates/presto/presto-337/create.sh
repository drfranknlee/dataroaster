#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

# create secret for aws keys.
kubectl create secret generic presto-s3-keys \
--from-literal=access-key={{ accessKey }} \
--from-literal=secret-key={{ secretKey }} \
-n {{ namespace }};

# create configmap.
kubectl create configmap presto-cfg --dry-run \
--from-file=config.properties.coordinator \
--from-file=config.properties.worker \
--from-file=jvm.config \
--from-file=node.properties.template \
--from-file=hive.properties.template \
-o yaml \
-n {{ namespace }} | kubectl apply -f -

# install presto.
kubectl apply -f presto.yaml -n {{ namespace }};

# wait for presto being run.
while [[ $(kubectl get pods -n {{ namespace }} -l app=presto-coordinator -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for presto being run" && sleep 2; done


