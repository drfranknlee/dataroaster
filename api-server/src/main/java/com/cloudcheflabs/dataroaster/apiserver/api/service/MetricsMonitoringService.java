package com.cloudcheflabs.dataroaster.apiserver.api.service;

import java.util.List;

public interface MetricsMonitoringService {
    void create(long projectId, long serviceDefId, long clusterId, String userName, String storageClass, int storageSize);
    void delete(long serviceId, String userName);
}
