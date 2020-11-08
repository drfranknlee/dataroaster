package io.spongebob.apiserver.api.service;

import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.domain.model.K8sNamespace;

public interface K8sNamespaceService extends Operations<K8sNamespace> {
    void craeteNamespace(K8sNamespace k8sNamespace);
    void deleteNamespace(K8sNamespace k8sNamespace);
}
