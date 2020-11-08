package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface SparkThriftServerService {

    void createSparkThriftServer(long namespaceId,
                             long serviceId,
                             String bucket,
                             int executors,
                             int executorMemory,
                             int executorCore,
                             int driverMemory,
                             String s3StorageType);

    void deleteSparkThriftServer(long namespaceId, long serviceId);
}
