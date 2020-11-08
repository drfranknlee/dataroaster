#!/bin/sh

cd {{ tempDirectory }};

kubectl apply -f ozone-deploy.yaml --kubeconfig={{ kubeconfig }};


# wait for ozone being run.
while [[ $(kubectl get pods -n {{ namespace }} -l app=ozone,component=s3g -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for ozone being run" && sleep 2; done
