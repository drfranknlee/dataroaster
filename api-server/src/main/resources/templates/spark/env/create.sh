#!/bin/sh

cd {{ tempDirectory }};

kubectl apply -f . --kubeconfig={{ kubeconfig }};

