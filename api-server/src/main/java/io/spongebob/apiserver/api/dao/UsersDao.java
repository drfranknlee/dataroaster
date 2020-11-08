package io.spongebob.apiserver.api.dao;

import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.domain.model.Groups;
import io.spongebob.apiserver.domain.model.Users;

import java.util.List;

public interface UsersDao extends Operations<Users> {

    Users findByUserName(String userName);

    List<Users> findAllByGroup(Groups groups);

}
