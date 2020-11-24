#!/bin/bash

# install helm.
cd ~;
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash;

# add helm repo.
helm repo add jupyterhub https://jupyterhub.github.io/helm-chart/
helm repo update;

cd {{ tempDirectory }};
export KUBECONFIG={{ kubeconfig }};

# create config.
cat <<EOF > config.yaml
proxy:
  secretToken: $(openssl rand -hex 32)
singleuser:
  image:
    name: mykidong/dataroaster-jupyter
    tag: '0.9.1'
    pullPolicy: Always
#hub:
#  extraEnv:
#    OAUTH2_AUTHORIZE_URL: http://192.168.10.102:8091/authorizer/oauth/authorize
#    OAUTH2_TOKEN_URL: http://192.168.10.102:8091/authorizer/oauth/token
#    OAUTH_CALLBACK_URL: http://118.67.128.161:8888/hub/oauth_callback
#    OAUTH2_TLS_VERIFY: false
#auth:
#  type: custom
#  custom:
#    className: oauthenticator.generic.GenericOAuthenticator
#    config:
#      login_service: "dataroaster"
#      client_id: "api"
#      client_secret: "helloAuthAPI"
#      token_url: http://192.168.10.102:8091/authorizer/oauth/token
EOF


# install jupyterhub.
RELEASE=jhub
NAMESPACE={{ namespace }}
echo "NAMESPACE: $NAMESPACE";

helm upgrade --cleanup-on-fail \
--install $RELEASE jupyterhub/jupyterhub \
--namespace $NAMESPACE \
--create-namespace \
--version={{ version }} \
--set hub.db.pvc.storageClassName=direct.csi.min.io \
--set singleuser.storage.dynamic.storageClass=direct.csi.min.io \
--set singleuser.storage.capacity={{ storage }}Gi \
--values config.yaml;


# wait for jupyterhub being run.
while [[ $(kubectl get pods -n ${NAMESPACE} -l app=jupyterhub,component=hub -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for jupyterhub being run" && sleep 2; done


