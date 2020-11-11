#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

kubectl apply -n {{ namespace }} -f install.yaml;


# wait for postgresql being run
while [[ $(kubectl get pods -n {{ namespace }} -l app=postgres,component=argo-workflow -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for postgresql being run" && sleep 2; done


# wait for workflow-controller being run
while [[ $(kubectl get pods -n {{ namespace }} -l app=workflow-controller,component=argo-workflow -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for workflow-controller being run" && sleep 2; done

# wait for argo-server being run
while [[ $(kubectl get pods -n {{ namespace }} -l app=argo-server,component=argo-workflow -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for argo-server being run" && sleep 2; done


