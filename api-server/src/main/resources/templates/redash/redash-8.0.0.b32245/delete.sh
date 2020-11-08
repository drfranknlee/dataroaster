#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

kubectl delete -f redash.yaml;

