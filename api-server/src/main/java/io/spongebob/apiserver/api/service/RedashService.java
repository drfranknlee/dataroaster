package io.spongebob.apiserver.api.service;

public interface RedashService {

    void createRedash(long namespaceId,
                     long serviceId,
                     int storage);

    void deleteRedash(long namespaceId, long serviceId);
}
