package com.cloudcheflabs.dataroaster.cli.dao.http;

import com.cloudcheflabs.dataroaster.cli.api.dao.QueryEngineDao;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class HttpQueryEngineDao extends AbstractHttpClient implements QueryEngineDao {

    public HttpQueryEngineDao(OkHttpClient client) {
        super(client);
    }

    @Override
    public RestResponse createQueryEngine(ConfigProps configProps,
                                          long projectId,
                                          long serviceDefId,
                                          long clusterId,
                                          String s3Bucket,
                                          String s3AccessKey,
                                          String s3SecretKey,
                                          String s3Endpoint,
                                          String sparkThriftServerStorageClass,
                                          int sparkThriftServerExecutors,
                                          int sparkThriftServerExecutorMemory,
                                          int sparkThriftServerExecutorCores,
                                          int sparkThriftServerDriverMemory,
                                          int trinoWorkers,
                                          int trinoServerMaxMemory,
                                          int trinoCores,
                                          int trinoTempDataStorage,
                                          int trinoDataStorage,
                                          String trinoStorageClass) {

        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/query_engine/create";

        // parameters in body.
        String content = "project_id=" + projectId;
        content += "&service_def_id=" + serviceDefId;
        content += "&cluster_id=" + clusterId;
        content += "&s3_bucket=" + s3Bucket;
        content += "&s3_access_key=" + s3AccessKey;
        content += "&s3_secret_key=" + s3SecretKey;
        content += "&s3_endpoint=" + s3Endpoint;
        content += "&spark_thrift_server_storage_class=" + sparkThriftServerStorageClass;
        content += "&spark_thrift_server_executors=" + sparkThriftServerExecutors;
        content += "&spark_thrift_server_executor_memory=" + sparkThriftServerExecutorMemory;
        content += "&spark_thrift_server_executor_cores=" + sparkThriftServerExecutorCores;
        content += "&spark_thrift_server_driver_memory=" + sparkThriftServerDriverMemory;
        content += "&trino_workers=" + trinoWorkers;
        content += "&trino_server_max_memory=" + trinoServerMaxMemory;
        content += "&trino_cores=" + trinoCores;
        content += "&trino_temp_data_storage=" + trinoTempDataStorage;
        content += "&trino_data_storage=" + trinoDataStorage;
        content += "&trino_storage_class=" + trinoStorageClass;

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
    public RestResponse deleteQueryEngine(ConfigProps configProps, long serviceId) {
        String serverUrl = configProps.getServer();
        String accessToken = configProps.getAccessToken();

        String urlPath = serverUrl + "/api/apis/query_engine/delete";

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
