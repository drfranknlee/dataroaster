package com.cloudcheflabs.dataroaster.apiserver.api.service;


public interface CsiService {
    void createCsi(long serviceId, long clusterId, String dataRootPath, int dataDirCount);
    void deleteCsi(long serviceId, long clusterId);
    void createMayastor(long serviceId, long clusterId, String workerNodes, String disks);
    void deleteMayastor(long serviceId, long clusterId);
}
