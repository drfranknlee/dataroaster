#!/bin/sh

cd {{ tempDirectory }};

# install metal LB.
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.9.3/manifests/namespace.yaml --kubeconfig={{ kubeconfig }};
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.9.3/manifests/metallb.yaml --kubeconfig={{ kubeconfig }};

# create secret.
kubectl create secret generic memberlist --from-literal=secretkey="$(openssl rand -base64 128)" -n metallb-system --kubeconfig={{ kubeconfig }};

# create configmap.
kubectl apply -f config.yaml --kubeconfig={{ kubeconfig }};