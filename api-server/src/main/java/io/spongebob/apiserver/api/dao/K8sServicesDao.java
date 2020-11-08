package io.spongebob.apiserver.api.dao;

import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.domain.model.K8sServices;

import java.util.List;

public interface K8sServicesDao extends Operations<K8sServices> {

    List<K8sServices> findListByType(String type);

}
