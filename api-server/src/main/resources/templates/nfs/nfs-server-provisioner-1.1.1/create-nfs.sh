#!/bin/sh

# install helm.
cd ~;
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash;

cd {{ tempDirectory }};

# install nfs.
helm install nfs-server . --set replicaCount=1,namespace=nfs,persistence.enabled=true,persistence.size={{ persistenceSize }}Gi;


# wait for nfs server being run.
while [[ $(kubectl get pods -n nfs -l app=nfs-server-provisioner -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for nfs server being run" && sleep 2; done
