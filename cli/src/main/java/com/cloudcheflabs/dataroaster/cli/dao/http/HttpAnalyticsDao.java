package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.AnalyticsDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpAnalyticsDao extends AbstractHttpClient implements AnalyticsDao {

    public HttpAnalyticsDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createAnalytics(ConfigProps configProps,
                                        long projectId,
                                        long serviceDefId,
                                        long clusterId,
                                        String jupyterhubGithubClientId,
                                        String jupyterhubGithubClientSecret,
                                        String jupyterhubIngressHost,
                                        String storageClass,
                                        int jupyterhubStorageSize,
                                        int redashStorageSize) {

        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/analytics/create";

        // parameters in body.
        String content = "project_id=" + projectId;
        content += "&service_def_id=" + serviceDefId;
        content += "&cluster_id=" + clusterId;
        content += "&jupyterhub_github_client_id=" + jupyterhubGithubClientId;
        content += "&jupyterhub_github_client_secret=" + jupyterhubGithubClientSecret;
        content += "&jupyterhub_ingress_host=" + jupyterhubIngressHost;
        content += "&storage_class=" + storageClass;
        content += "&jupyterhub_storage_size=" + jupyterhubStorageSize;
        content += "&redash_storage_size=" + redashStorageSize;

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
    public RestResponse deleteAnalytics(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/analytics/delete";

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
