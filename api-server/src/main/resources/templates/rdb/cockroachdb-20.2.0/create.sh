#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

# create crd.
kubectl apply -n {{ namespace }} -f crdb.cockroachlabs.com_crdbclusters.yaml;

# create operator.
kubectl apply -n {{ namespace }} -f operator.yaml;


# wait for operator being run
while [[ $(kubectl get pods -n {{ namespace }} -l app=cockroach-operator -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for operator being run" && sleep 2; done


# create cockroachdb.
kubectl apply -n {{ namespace }} -f cockroachdb.yaml.yaml;


# wait for cockroachdb being run
while [[ $(kubectl get pods -n {{ namespace }} -l app.kubernetes.io/component=database,app.kubernetes.io/instance=cockroachdb,app.kubernetes.io/name=cockroachdb -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for cockroachdb being run" && sleep 2; done

