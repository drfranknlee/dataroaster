#!/bin/sh

cd {{ tempDirectory }};

kubectl delete -f ozone-deploy.yaml --kubeconfig={{ kubeconfig }};