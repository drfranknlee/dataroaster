package io.spongebob.apiserver.api.dao;

import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.domain.model.K8sCluster;

public interface K8sClusterDao extends Operations<K8sCluster> {

    K8sCluster findByName(String clusterName);

}
