package io.spongebob.apiserver.api.service;

import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.domain.StorageClass;
import io.spongebob.apiserver.domain.model.K8sServices;

import java.util.List;

public interface K8sServicesService extends Operations<K8sServices> {

    List<K8sServices> findListByType(String type);
    List<K8sServices> listObjectStorageByNamespace(long namespaceId);
    List<StorageClass> listStorageClasses(long clusterId);
}
