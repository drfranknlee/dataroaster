package com.cloudcheflabs.dataroaster.apiserver.api.dao;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;

public interface K8sClusterDao extends Operations<K8sCluster> {

    K8sCluster findByName(String clusterName);

}
