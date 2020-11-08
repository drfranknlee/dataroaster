package com.cloudcheflabs.dataroaster.apiserver.api.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;

import java.util.List;

public interface UsersService extends Operations<Users> {

    void signUp(Users users);
    Users findByUserName(String userName);
    List<Users> findUsersByGroupName(String groupName);
    void changePassword(String userName, String newPassword);
    void changeRole(String userName, String role);
    void joinGroup(List<Long> userIdList, long groupId);
    void leaveGroup(List<Long> userIdList, long groupId);
    void creatCerts(long clusterId, long groupId, long namespaceId, List<Long> userIdList);

}
