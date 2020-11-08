#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

# delete kafka and zookeeper.
kubectl delete -f kafka-persistent.yaml -n {{ namespace }};

# delete operator.
kubectl delete -f operator.yaml -n {{ namespace }};



