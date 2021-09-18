package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.PrivateRegistryDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Base64;

public class HttpPrivateRegistryDao extends AbstractHttpClient implements PrivateRegistryDao {

    public HttpPrivateRegistryDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createPrivateRegistry(ConfigProps configProps, long projectId, long serviceDefId, long clusterId, String coreHost, String notaryHost, String storageClass, int registryStorageSize, int chartmuseumStorageSize, int jobserviceStorageSize, int databaseStorageSize, int redisStorageSize, int trivyStorageSize, String s3Bucket, String s3AccessKey, String s3SecretKey, String s3Endpoint) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/private_registry/create";

        // parameters in body.
        String content = "project_id=" + projectId;
        content += "&service_def_id=" + serviceDefId;
        content += "&cluster_id=" + clusterId;
        content += "&core_host=" + coreHost;
        content += "&notary_host=" + notaryHost;
        content += "&storage_class=" + storageClass;
        content += "&registry_storage_size=" + registryStorageSize;
        content += "&chartmuseum_storage_size=" + chartmuseumStorageSize;
        content += "&jobservice_storage_size=" + jobserviceStorageSize;
        content += "&database_storage_size=" + databaseStorageSize;
        content += "&redis_storage_size=" + redisStorageSize;
        content += "&trivy_storage_size=" + trivyStorageSize;
        content += "&s3_bucket=" + s3Bucket;
        content += "&s3_access_key=" + Base64.getEncoder().encodeToString(s3AccessKey.getBytes());
        content += "&s3_secret_key=" + Base64.getEncoder().encodeToString(s3SecretKey.getBytes());
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
    public RestResponse deletePrivateRegistry(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/private_registry/delete";

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
