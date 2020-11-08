#!/bin/sh

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

## Expanding a Tenant.
kubectl minio tenant volume add \
--name {{ tenantName }} \
--servers {{ servers }} \
--volumes {{ volumes }} \
--capacity {{ capacity }}Gi \
--namespace {{ namespace }} \
--storage-class=direct.csi.min.io;