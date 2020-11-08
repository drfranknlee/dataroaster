package com.cloudcheflabs.dataroaster.apiserver.api.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Groups;

public interface GroupsService extends Operations<Groups> {

    Groups findByGroupName(String group);
}
