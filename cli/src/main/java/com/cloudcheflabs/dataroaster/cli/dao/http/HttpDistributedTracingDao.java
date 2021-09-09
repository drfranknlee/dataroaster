package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.DistributedTracingDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpDistributedTracingDao extends AbstractHttpClient implements DistributedTracingDao {

    public HttpDistributedTracingDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createDistributedTracing(ConfigProps configProps,
                                                 long projectId,
                                                 long serviceDefId,
                                                 long clusterId,
                                                 String storageClass,
                                                 String ingressHost,
                                                 String elasticsearchHostPort) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/distributed_tracing/create";

        // parameters in body.
        String content = "project_id=" + projectId;
        content += "&service_def_id=" + serviceDefId;
        content += "&cluster_id=" + clusterId;
        content += "&storage_class=" + storageClass;
        content += "&ingress_host=" + ingressHost;
        content += "&elasticsearch_host_port=" + elasticsearchHostPort;


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
    public RestResponse deleteDistributedTracing(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/distributed_tracing/delete";

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
