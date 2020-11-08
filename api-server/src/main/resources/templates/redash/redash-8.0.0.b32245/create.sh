#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

# create redash.
kubectl apply -f redash.yaml;

# wait for redash being run
while [[ $(kubectl get pods -n {{ namespace }} -l app=redash -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for redash being run" && sleep 2; done


# create tables.
kubectl exec -it -n {{ namespace }} \
$(kubectl get po -l app=redash -n {{ namespace }} -o jsonpath={.items[0].metadata.name}) \
-c server /app/bin/docker-entrypoint create_db;