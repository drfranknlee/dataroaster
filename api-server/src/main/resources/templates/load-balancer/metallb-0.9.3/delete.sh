#!/bin/sh

cd {{ tempDirectory }};

kubectl delete secret memberlist -n metallb-system --kubeconfig={{ kubeconfig }};
kubectl delete -f https://raw.githubusercontent.com/metallb/metallb/v0.9.3/manifests/metallb.yaml --kubeconfig={{ kubeconfig }};
kubectl delete -f https://raw.githubusercontent.com/metallb/metallb/v0.9.3/manifests/namespace.yaml --kubeconfig={{ kubeconfig }};
