package io.spongebob.apiserver.api.service;

import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.domain.model.Groups;

public interface GroupsService extends Operations<Groups> {

    Groups findByGroupName(String group);
}
