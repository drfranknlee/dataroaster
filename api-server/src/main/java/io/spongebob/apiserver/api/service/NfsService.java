package io.spongebob.apiserver.api.service;


public interface NfsService {
    void createNfs(long serviceId, long clusterId, int persistenceSize);
    void deleteNfs(long serviceId, long clusterId);
}
