package io.spongebob.apiserver.api.service;

public interface HiveMetastoreService {

    void createHiveMetastore(long namespaceId,
                             long serviceId,
                             String bucket,
                             int storageSize,
                             String s3StorageType);
    void deleteHiveMetastore(long namespaceId, long serviceId);
}
