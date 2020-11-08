#!/bin/bash

cd {{ tempDirectory }};
export KUBECONFIG={{ kubeconfig }};

if [[ $(kubectl get pods -n elastic-system -l control-plane=elastic-operator -o jsonpath={..status.phase}) != *"Running"* ]]
then
  echo "elasticsearch operator is not running...";
  # install crd, operator, sa, etc.
  kubectl apply -f https://download.elastic.co/downloads/eck/1.2.1/all-in-one.yaml;
fi

# wait for operator being run.
while [[ $(kubectl get pods -n elastic-system -l control-plane=elastic-operator -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for operator being run" && sleep 2; done

# install es cluster.
kubectl apply -f es.yaml;

# install kibana.
kubectl apply -f kibana.yaml;

