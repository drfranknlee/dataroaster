#!/bin/bash

cd {{ tempDirectory }};
export KUBECONFIG={{ kubeconfig }};


# install crd, cluster role and binding.
kubectl apply -f kafka-init.yaml -n {{ namespace }};

# install operator.
kubectl apply -f operator.yaml -n {{ namespace }};

# wait for operator being run.
while [[ $(kubectl get pods -n {{ namespace }} -l name=strimzi-cluster-operator -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for operator being run" && sleep 2; done

# install kafka and zookeeper.
kubectl apply -f kafka-persistent.yaml -n {{ namespace }};


# wait for kafka being run.
while [[ $(kubectl get pods -n {{ namespace }} -l app.kubernetes.io/managed-by=strimzi-cluster-operator,app.kubernetes.io/name=kafka -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for kafka being run" && sleep 2; done
