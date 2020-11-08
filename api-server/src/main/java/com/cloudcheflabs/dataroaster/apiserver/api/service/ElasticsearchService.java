package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface ElasticsearchService {

    void createElasticsearch(long namespaceId,
                             long serviceId,
                             String esClusterName,
                             int masterStorage,
                             int dataNodes,
                             int dataStorage,
                             int clientNodes,
                             int clientStorage);

    void deleteElasticsearch(long namespaceId, long serviceId);
}
