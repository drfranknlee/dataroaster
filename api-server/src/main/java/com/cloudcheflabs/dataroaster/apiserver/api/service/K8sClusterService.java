package com.cloudcheflabs.dataroaster.apiserver.api.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;

public interface K8sClusterService extends Operations<K8sCluster> {

    void createCluster(String clusterName, String description, String kubeconfig);
    void updateCluster(long id, String description, String kubeconfig);
    void deleteCluster(long id);
}
