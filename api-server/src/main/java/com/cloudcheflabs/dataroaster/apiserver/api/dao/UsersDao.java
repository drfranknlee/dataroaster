package com.cloudcheflabs.dataroaster.apiserver.api.dao;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.Groups;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;

import java.util.List;

public interface UsersDao extends Operations<Users> {

    Users findByUserName(String userName);

    List<Users> findAllByGroup(Groups groups);

}
