package io.spongebob.apiserver.api.service;


import io.spongebob.apiserver.domain.Privileges;

/**
 * Created by mykidong on 2019-08-28.
 */
public interface PrivilegesService {

    void putPrivileges(String accessKey, Privileges privileges);

    Privileges getPrivileges(String accessToken);
}
