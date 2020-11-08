package com.cloudcheflabs.dataroaster.apiserver.api.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;

public interface K8sNamespaceService extends Operations<K8sNamespace> {
    void craeteNamespace(K8sNamespace k8sNamespace);
    void deleteNamespace(K8sNamespace k8sNamespace);
}
