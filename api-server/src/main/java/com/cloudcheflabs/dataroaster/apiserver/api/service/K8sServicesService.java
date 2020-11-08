package com.cloudcheflabs.dataroaster.apiserver.api.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.StorageClass;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;

import java.util.List;

public interface K8sServicesService extends Operations<K8sServices> {

    List<K8sServices> findListByType(String type);
    List<K8sServices> listObjectStorageByNamespace(long namespaceId);
    List<StorageClass> listStorageClasses(long clusterId);
}
