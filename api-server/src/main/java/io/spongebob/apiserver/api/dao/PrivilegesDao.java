package io.spongebob.apiserver.api.dao;


import io.spongebob.apiserver.domain.Privileges;

/**
 * Created by mykidong on 2019-08-28.
 */
public interface PrivilegesDao {

    void putPrivileges(String accessKey, Privileges privileges);

    Privileges getPrivileges(String accessToken);
}
