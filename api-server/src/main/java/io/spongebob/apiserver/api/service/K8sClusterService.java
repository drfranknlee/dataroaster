package io.spongebob.apiserver.api.service;

import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.domain.model.K8sCluster;

public interface K8sClusterService extends Operations<K8sCluster> {

    void createCluster(String clusterName, String description, String kubeconfig);
    void updateCluster(long id, String description, String kubeconfig);
    void deleteCluster(long id);
}
