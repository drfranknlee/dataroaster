package com.cloudcheflabs.dataroaster.apiserver.api.service;


public interface SparkService {
    void createEnv(long namespaceId);
    void deleteEnv(long namespaceId);
}
