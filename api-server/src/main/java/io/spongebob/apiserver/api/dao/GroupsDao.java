package io.spongebob.apiserver.api.dao;

import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.domain.model.Groups;

public interface GroupsDao extends Operations<Groups> {

    Groups findByGroupName(String group);
    Groups getDefaultGroup();

}
