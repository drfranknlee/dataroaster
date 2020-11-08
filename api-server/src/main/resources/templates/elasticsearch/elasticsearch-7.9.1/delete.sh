#!/bin/bash

cd {{ tempDirectory }};
export KUBECONFIG={{ kubeconfig }};

# delete kibana.
kubectl delete -f kibana.yaml;

# delete es cluster.
kubectl delete -f es.yaml;

