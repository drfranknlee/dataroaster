#!/bin/sh

cd {{ tempDirectory }};

kubectl apply -f ingress-nginx-deploy.yaml --kubeconfig={{ kubeconfig }};


# wait for ingress nginx being run.
while [[ $(kubectl get pods -n ingress-nginx -l app.kubernetes.io/component=controller,app.kubernetes.io/name=ingress-nginx -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for ingress nginx being run" && sleep 2; done
