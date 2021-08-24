package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.LoginDao;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.*;

import java.io.IOException;

public class HttpLoginDao extends AbstractHttpClient implements LoginDao {

    public HttpLoginDao(OkHttpClient client) {
        super(client);
    }


    @Override
    public RestResponse login(String user, String password, String serverUrl) {
        String urlPath = serverUrl + "/api/auth/login";

        // parameters in body.
        String content = "username=" + user + "&password=" + password;

        RequestBody body = RequestBody.create(mediaType, content);
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .post(body)
                    .build();

            return ResponseHandler.doCall(client, request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}