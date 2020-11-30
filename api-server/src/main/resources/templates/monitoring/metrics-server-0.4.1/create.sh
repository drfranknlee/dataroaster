#!/bin/bash

cd {{ tempDirectory }};
export KUBECONFIG={{ kubeconfig }};

# install metrics server.
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.4.1/components.yaml;


# TODO: edit metrics server deployment.
kubectl edit deploy metrics-server -n kube-system;
## add --kubelet-insecure-tls
spec:
  containers:
  - args:
    - --kubelet-insecure-tls
...
