package com.cloudcheflabs.dataroaster.kubernetes.client;

import com.cloudcheflabs.dataroaster.domain.Kubeconfig;
import io.fabric8.kubernetes.client.KubernetesClient;


public class KubernetesClientUtils {

    public static KubernetesClient newClient(Kubeconfig kubeconfig) {
        String masterUrl = kubeconfig.getMasterUrl();
        String clusterCertData = kubeconfig.getClusterCertData();
        String clientCertData = kubeconfig.getClientCertData();
        String clientKeyData = kubeconfig.getClientKeyData();
        return KubernetesClientManager.newClient(masterUrl, clusterCertData, clientCertData, clientKeyData);
    }
}
