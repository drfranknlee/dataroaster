package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface KafkaService {

    void createKafka(long namespaceId,
                          long serviceId,
                          int kafkaStorage,
                          int zookeeperStorage);

    void deleteKafka(long namespaceId, long serviceId);
}
