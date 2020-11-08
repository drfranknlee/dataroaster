package io.spongebob.apiserver.api.service;


public interface CertManagerService {
    void createCertManager(long serviceId, long clusterId);
    void deleteCertManager(long serviceId, long clusterId);
}
