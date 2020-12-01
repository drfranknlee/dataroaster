#!/bin/bash

cd {{ tempDirectory }};
export KUBECONFIG={{ kubeconfig }};

# install metrics server.
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.4.1/components.yaml;


# patch metrics server deployment.
## add --kubelet-insecure-tls in args.
kubectl patch deploy metrics-server -n kube-system -p '{"spec": {"template": {"spec": {"containers": [{"args": ["--kubelet-insecure-tls", "--cert-dir=/tmp", "--secure-port=4443", "--kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname", "--kubelet-use-node-status-port"], "name": "metrics-server"}]}}}}'

