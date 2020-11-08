package com.cloudcheflabs.dataroaster.apiserver.kubernetes.client;

import io.fabric8.kubernetes.client.KubernetesClient;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;

public class KubernetesClientUtils {

    public static KubernetesClient newClient(Kubeconfig kubeconfig) {
        String masterUrl = kubeconfig.getMasterUrl();
        String clusterCertData = kubeconfig.getClusterCertData();
        String clientCertData = kubeconfig.getClientCertData();
        String clientKeyData = kubeconfig.getClientKeyData();
        return KubernetesClientManager.newClient(masterUrl, clusterCertData, clientCertData, clientKeyData);
    }
}
