package com.cloudcheflabs.dataroaster.apiserver.api.service;


public interface MonitoringService {
    void createPrometheusStack(long serviceId, long clusterId, String storageClass, int storageSize);
    void deletePrometheusStack(long serviceId, long clusterId);
    void createMetricsServer(long serviceId, long clusterId);
    void deleteMetricsServer(long serviceId, long clusterId);
}
