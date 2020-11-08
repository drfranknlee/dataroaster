#!/bin/sh

cd {{ tempDirectory }};

kubectl apply -f ingress-ozone.yaml --kubeconfig={{ kubeconfig }};

