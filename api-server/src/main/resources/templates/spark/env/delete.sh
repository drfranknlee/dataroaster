#!/bin/sh

cd {{ tempDirectory }};

kubectl delete -f . --kubeconfig={{ kubeconfig }};
