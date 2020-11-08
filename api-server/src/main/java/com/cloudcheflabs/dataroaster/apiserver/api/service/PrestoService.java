package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface PrestoService {

    void createPresto(long namespaceId,
                     long serviceId,
                     int serverMaxMemory,
                     int cpu,
                     int tempDataStorage,
                     int dataStorage,
                     int workers,
                     String s3StorageType);

    void deletePresto(long namespaceId, long serviceId);
}
