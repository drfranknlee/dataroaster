#!/bin/sh

cd {{ tempDirectory }};

kubectl get po --kubeconfig={{ userKubeconfig }};
