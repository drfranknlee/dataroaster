package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface ObjectStorageService {

    void createMinIOOperator(long clusterId, long serviceId);
    void createMinIO(String userName, long namespaceId, long serviceId, String tenantName, int servers, int volumes, int capacity);
    void expandMinIO(String userName, long namespaceId, long serviceId, int servers, int volumes, int capacity);
    void deleteMinIOTenant(String userName, long namespaceId, long serviceId);
    void deleteMinIOOperator(long clusterId, long serviceId);
    void createOzone(long namespaceId,
                     long serviceId,
                     int datanodeReplicas,
                     int datanodeStorageSize,
                     int omStorageSize,
                     int s3gStorageSize,
                     int scmStorageSize,
                     String storageClass);
    void deleteOzone(long namespaceId, long serviceId);
}
