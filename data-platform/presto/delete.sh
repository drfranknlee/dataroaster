#!/bin/bash

kubectl delete -f presto.yaml;
kubectl delete configmap presto-cfg -n presto;
kubectl delete secret presto-s3-keys -n presto;

