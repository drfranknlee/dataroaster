package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface CockroachDBService {

    void create(long namespaceId,
                long serviceId,
                int storage,
                int cpu,
                int memory,
                int nodes);

    void delete(long namespaceId, long serviceId);
}
