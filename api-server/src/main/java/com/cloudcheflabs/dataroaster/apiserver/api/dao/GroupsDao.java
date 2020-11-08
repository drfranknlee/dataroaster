package com.cloudcheflabs.dataroaster.apiserver.api.dao;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Groups;

public interface GroupsDao extends Operations<Groups> {

    Groups findByGroupName(String group);
    Groups getDefaultGroup();

}
