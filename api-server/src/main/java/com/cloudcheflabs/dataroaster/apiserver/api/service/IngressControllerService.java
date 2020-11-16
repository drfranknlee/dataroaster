package com.cloudcheflabs.dataroaster.apiserver.api.service;

import io.fabric8.kubernetes.api.model.extensions.Ingress;

import java.util.List;

public interface IngressControllerService {

    void createIngressNginx(long clusterId, long serviceId, int port);
    void deleteIngressNginx(long clusterId, long serviceId);
    void createMetalLB(long clusterId, long serviceId, String fromIp, String toIp);
    void deleteMetalLB(long clusterId, long serviceId);
    void createMinIOIngress(long namespaceId, long serviceId, String host);
    void deleteMinIOIngress(long namespaceId, long serviceId);
    void createOzoneIngress(long namespaceId, long serviceId, String host);
    void deleteOzoneIngress(long namespaceId, long serviceId);
    List<Ingress> getIngresses(long namespaceId);
}
