package io.spongebob.apiserver.api.service;


public interface CsiService {
    void createCsi(long serviceId, long clusterId, String dataRootPath, int dataDirCount);
    void deleteCsi(long serviceId, long clusterId);
}
