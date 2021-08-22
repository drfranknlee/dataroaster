package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.cli.api.dao.PodLogMonitoringDao;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpPodLogMonitoringDao extends AbstractHttpClient implements PodLogMonitoringDao {

    public HttpPodLogMonitoringDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createPodLogMonitoring(ConfigProps configProps, long projectId, long serviceDefId, long clusterId, String elasticsearchHosts) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/pod_log_monitoring/create";

        // parameters in body.
        String content = "project_id=" + projectId + "&service_def_id=" + serviceDefId + "&cluster_id=" + clusterId + "&elasticsearch_hosts=" + elasticsearchHosts;

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
    public RestResponse deletePodLogMonitoring(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/pod_log_monitoring/delete";

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
