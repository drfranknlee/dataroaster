#!/bin/sh

cd {{ tempDirectory }};

# first, delete csr if exists.
kubectl delete csr {{ csrName }} --kubeconfig={{ kubeconfig }};

# create csr.
kubectl apply -f k8s-user-csr.yaml --kubeconfig={{ kubeconfig }};

# get csr.
kubectl get csr --kubeconfig={{ kubeconfig }};

# approve csr.
kubectl certificate approve {{ csrName }} --kubeconfig={{ kubeconfig }};

# get csr.
kubectl get csr --kubeconfig={{ kubeconfig }};

# get user cert data from csr and save it as user cert.
kubectl get csr {{ csrName }} -o jsonpath='{.status.certificate}' --kubeconfig={{ kubeconfig }} | base64 --decode > {{ user }}.crt;

# create role binding for user.
kubectl create rolebinding {{ user }}-{{ group }}-admin --namespace={{ namespace }} --clusterrole=admin --user={{ user }} --kubeconfig={{ kubeconfig }};
