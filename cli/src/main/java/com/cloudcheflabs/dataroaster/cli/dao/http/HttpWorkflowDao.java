package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.WorkflowDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpWorkflowDao extends AbstractHttpClient implements WorkflowDao {

    public HttpWorkflowDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createWorkflow(ConfigProps configProps,
                                       long projectId,
                                       long serviceDefId,
                                       long clusterId,
                                       String storageClass,
                                       int storageSize,
                                       String s3Bucket,
                                       String s3AccessKey,
                                       String s3SecretKey,
                                       String s3Endpoint) {

        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/workflow/create";

        // parameters in body.
        String content = "project_id=" + projectId;
        content += "&service_def_id=" + serviceDefId;
        content += "&cluster_id=" + clusterId;
        content += "&storage_class=" + storageClass;
        content += "&storage_size=" + storageSize;
        content += "&s3_bucket=" + s3Bucket;
        content += "&s3_access_key=" + s3AccessKey;
        content += "&s3_secret_key=" + s3SecretKey;
        content += "&s3_endpoint=" + s3Endpoint;

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
    public RestResponse deleteWorkflow(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/workflow/delete";

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
