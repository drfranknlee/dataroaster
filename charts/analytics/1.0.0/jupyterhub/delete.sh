#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

RELEASE=jhub
NAMESPACE={{ namespace }}
helm uninstall $RELEASE --namespace $NAMESPACE;

