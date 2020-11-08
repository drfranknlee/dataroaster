package io.spongebob.apiserver.api.service;

public interface ResourceMonitorService {
    String monitorResource(long clusterId, String namespace, String type);
}
