#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

helm uninstall prom-stack --namespace prom-stack

