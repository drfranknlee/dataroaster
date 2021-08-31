package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface WorkflowService {

    void create(long projectId,
                long serviceDefId,
                long clusterId,
                String userName,
                String storageClass,
                int storageSize,
                String s3Bucket,
                String s3AccessKey,
                String s3SecretKey,
                String s3Endpoint);
    void delete(long serviceId, String userName);
}
