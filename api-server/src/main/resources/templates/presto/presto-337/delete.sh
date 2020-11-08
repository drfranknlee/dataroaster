#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

kubectl delete -f presto.yaml;
kubectl delete configmap presto-cfg -n {{ namespace }};
kubectl delete secret presto-s3-keys -n {{ namespace }};

