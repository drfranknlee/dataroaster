package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface ArgoService {

    void createArgo(long namespaceId,
                    long serviceId,
                    String bucket,
                    String s3Endpoint,
                    boolean insecure,
                    String accessKey,
                    String secretKey);

    void deleteArgo(long namespaceId, long serviceId);
}
