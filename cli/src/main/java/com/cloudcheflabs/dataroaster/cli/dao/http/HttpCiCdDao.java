package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.CiCdDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpCiCdDao extends AbstractHttpClient implements CiCdDao {

    public HttpCiCdDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createCiCd(ConfigProps configProps, long projectId, long serviceDefId, long clusterId, String argocdIngressHost, String jenkinsIngressHost, String storageClass) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/ci_cd/create";

        // parameters in body.
        String content = "project_id=" + projectId;
        content += "&service_def_id=" + serviceDefId;
        content += "&cluster_id=" + clusterId;
        content += "&argocd_ingress_host=" + argocdIngressHost;
        content += "&jenkins_ingress_host=" + jenkinsIngressHost;
        content += "&storage_class=" + storageClass;

        RequestBody body = RequestBody.create(mediaType, content);
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .post(body)
                    .build();

            return ResponseHandler.doCall(client, request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RestResponse deleteCiCd(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/ci_cd/delete";

        // parameters in body.
        String content = "service_id=" + serviceId;

        RequestBody body = RequestBody.create(mediaType, content);
        try {
            Request request = new Request.Builder()
                    .url(urlPath)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .delete(body)
                    .build();

            return ResponseHandler.doCall(client, request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
