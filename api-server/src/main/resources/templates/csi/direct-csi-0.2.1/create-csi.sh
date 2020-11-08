#!/bin/sh

cd {{ tempDirectory }};

# install direct csi.
DIRECT_CSI_DRIVES=data{1...{{ dataDirCount }}} DIRECT_CSI_DRIVES_DIR={{ dataRootPath }} kubectl apply -k . --kubeconfig={{ kubeconfig }};

# wait for csi minio being run.
while [[ $(kubectl get pods -n minio -l app=direct-csi-min-io -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for csi minio being run" && sleep 2; done

# wait for csi controller being run.
while [[ $(kubectl get pods -n minio -l app=direct-csi-controller-min-io -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for csi controller being run" && sleep 2; done


