package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.DataCatalogDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Base64;

public class HttpDataCatalogDao extends AbstractHttpClient implements DataCatalogDao {

    public HttpDataCatalogDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createDataCatalog(ConfigProps configProps,
                                   long projectId,
                                   long serviceDefId,
                                   long clusterId,
                                   String s3Bucket,
                                          String s3AccessKey,
                                   String s3SecretKey,
                                   String s3Endpoint,
                                   String storageClass,
                                   int storageSize) {

        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/data_catalog/create";

        // parameters in body.
        String content = "project_id=" + projectId;
        content += "&service_def_id=" + serviceDefId;
        content += "&cluster_id=" + clusterId;
        content += "&s3_bucket=" + s3Bucket;
        content += "&s3_access_key=" + Base64.getEncoder().encodeToString(s3AccessKey.getBytes());
        content += "&s3_secret_key=" + Base64.getEncoder().encodeToString(s3SecretKey.getBytes());
        content += "&s3_endpoint=" + s3Endpoint;
        content += "&storage_class=" + storageClass;
        content += "&storage_size=" + storageSize;

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
    public RestResponse deleteDataCatalog(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/data_catalog/delete";

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
