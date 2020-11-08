package io.spongebob.apiserver.api.dao;


import io.spongebob.apiserver.domain.AuthorizerResponse;

/**
 * Created by mykidong on 2019-08-28.
 */
public interface AuthorizerDao {

    AuthorizerResponse login(String userName, String password);

    AuthorizerResponse getPrivileges(String accessToken);

    AuthorizerResponse removeToken(String accessToken, String refreshToken);

}
