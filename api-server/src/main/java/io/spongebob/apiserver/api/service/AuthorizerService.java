package io.spongebob.apiserver.api.service;


import io.spongebob.apiserver.domain.AuthorizerResponse;
import io.spongebob.apiserver.domain.Token;

/**
 * Created by mykidong on 2019-08-28.
 */
public interface AuthorizerService {

    Token login(String userName, String password);
    AuthorizerResponse logout(String accessToken, String refreshToken);

}
