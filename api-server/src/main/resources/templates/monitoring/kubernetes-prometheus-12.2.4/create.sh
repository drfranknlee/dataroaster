#!/bin/bash

# install helm.
cd ~;
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash;

# add helm repo.
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts;
helm repo add stable https://charts.helm.sh/stable;
helm repo update;

cd {{ tempDirectory }};
export KUBECONFIG={{ kubeconfig }};

# create config.
cat > config.yaml <<'EOF'
alertmanager:
  alertmanagerSpec:
    storage:
      volumeClaimTemplate:
        spec:
          storageClassName: {{ storageClass }}
          accessModes: ["ReadWriteOnce"]
          resources:
            requests:
              storage: {{ storageSize }}Gi
prometheus:
  prometheusSpec:
    storageSpec:
      volumeClaimTemplate:
        spec:
          storageClassName: {{ storageClass }}
          accessModes: ["ReadWriteOnce"]
          resources:
            requests:
              storage: {{ storageSize }}Gi
EOF


echo "config yaml: "
cat config.yaml

# install prom stack.
helm install \
prom-stack \
prometheus-community/kube-prometheus-stack \
--version 12.2.4 \
--create-namespace \
--namespace prom-stack \
--values config.yaml;

echo "sleep 5s...";
sleep 5

## patch grafana service to set the type of LoadBalancer.
kubectl patch svc prom-stack-grafana -n prom-stack -p '{"spec": {"type": "LoadBalancer"}}';

