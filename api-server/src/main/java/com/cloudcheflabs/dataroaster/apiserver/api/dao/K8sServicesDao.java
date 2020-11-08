package com.cloudcheflabs.dataroaster.apiserver.api.dao;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;

import java.util.List;

public interface K8sServicesDao extends Operations<K8sServices> {

    List<K8sServices> findListByType(String type);

}
