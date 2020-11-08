package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigAdmin;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.ObjectStorageMinIOHandler;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.ObjectStorageOzoneHandler;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.ObjectStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class ObjectStorageServiceImpl implements ObjectStorageService {

    private static Logger LOG = LoggerFactory.getLogger(ObjectStorageServiceImpl.class);

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    @Autowired
    @Qualifier("hibernateK8sServicesDao")
    private K8sServicesDao k8sServicesDao;

    @Autowired
    @Qualifier("hibernateK8sClusterDao")
    private K8sClusterDao k8sClusterDao;

    @Autowired
    @Qualifier("hibernateK8sNamespaceDao")
    private K8sNamespaceDao k8sNamespaceDao;

    @Autowired
    @Qualifier("hibernateUsersDao")
    private UsersDao usersDao;

    @Autowired
    @Qualifier("kubernetesResourceControlDao")
    private ResourceControlDao resourceControlDao;

    @Value("${objectStorage.minio.defaultAccessKey}")
    private String minioAccessKey;

    @Value("${objectStorage.minio.defaultSecretKey}")
    private String minioSecretKey;

    public ObjectStorageServiceImpl() {
        super();
    }

    private Kubeconfig getAdminKubeconfig(K8sNamespace k8sNamespace) {
        K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
        return getAdminKubeconfig(k8sCluster);
    }

    private Kubeconfig getAdminKubeconfig(K8sCluster k8sCluster) {
        K8sKubeconfigAdmin kubeconfigAdmin = k8sCluster.getK8sKubeconfigAdminSet().iterator().next();
        String secretPath = kubeconfigAdmin.getSecretPath();

        return secretDao.readSecret(secretPath, Kubeconfig.class);
    }

    @Override
    @Transactional
    public void createMinIO(String userName, long namespaceId, long serviceId, String tenantName, int servers, int volumes, int capacity) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // add namespace.
        k8sServices.getK8sNamespaceSet().add(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        // check if minio direct csi storage class exists or not.
        if(!resourceControlDao.existsMinIOStorageClass(kubeconfig)) {
            throw new RuntimeException("MinIO Direct CSI not installed, please install it first.");
        }

        // create minio in real k8s.
        ExecutorUtils.runTask(() -> {
            return ObjectStorageMinIOHandler.createMinIO(k8sServices,
                    minioAccessKey,
                    minioSecretKey,
                    namespace,
                    tenantName,
                    servers,
                    volumes,
                    capacity,
                    kubeconfig);
        });
    }

    @Override
    @Transactional
    public void expandMinIO(String userName, long namespaceId, long serviceId, int servers, int volumes, int capacity) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // add namespace.
        k8sServices.getK8sNamespaceSet().add(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        // tenant name.
        String tenantName = resourceControlDao.getTenantName(kubeconfig, namespace);

        // expand minio in real k8s.
        ExecutorUtils.runTask(() -> {
            return ObjectStorageMinIOHandler.expandMinIO(k8sServices,
                    namespace,
                    tenantName,
                    servers,
                    volumes,
                    capacity,
                    kubeconfig);
        });
    }

    @Override
    @Transactional
    public void deleteMinIOTenant(String userName, long namespaceId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        // remove namespace.
        k8sServices.getK8sNamespaceSet().remove(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        // tenant name.
        String tenantName = resourceControlDao.getTenantName(kubeconfig, namespace);

        // delete tenant in real k8s.
        ExecutorUtils.runTask(() -> {
            return ObjectStorageMinIOHandler.deleteTenant(k8sServices,
                    namespace,
                    tenantName,
                    kubeconfig);
        });
    }

    @Override
    @Transactional
    public void createMinIOOperator(long clusterId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE_OPERATOR.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE_OPERATOR.name());
        }

        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        k8sServices.getObjectStorageOperatorK8sClusterSet().add(k8sCluster);
        k8sServicesDao.update(k8sServices);

        // delete operator in real k8s.
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);
        ExecutorUtils.runTask(() -> {
            return ObjectStorageMinIOHandler.createOperator(k8sServices, kubeconfigAdmin);
        });
    }

    @Override
    @Transactional
    public void deleteMinIOOperator(long clusterId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE_OPERATOR.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE_OPERATOR.name());
        }

        Set<K8sCluster> k8sClusterSet = k8sServices.getObjectStorageOperatorK8sClusterSet();
        for(K8sCluster k8sCluster : k8sClusterSet) {
            long id = k8sCluster.getId();
            // remove cluster.
            if(id == clusterId) {
                k8sClusterSet.remove(k8sCluster);
            }
        }
        k8sServices.setObjectStorageOperatorK8sClusterSet(k8sClusterSet);
        k8sServicesDao.update(k8sServices);

        // delete minio operator in real k8s.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        ExecutorUtils.runTask(() -> {
            return ObjectStorageMinIOHandler.deleteOperator(k8sServices, kubeconfigAdmin);
        });
    }

    @Override
    public void createOzone(long namespaceId,
                            long serviceId,
                            int datanodeReplicas,
                            int datanodeStorageSize,
                            int omStorageSize,
                            int s3gStorageSize,
                            int scmStorageSize,
                            String storageClass) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // add namespace.
        k8sServices.getK8sNamespaceSet().add(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        // check if minio direct csi storage class exists or not.
        if(!resourceControlDao.existsMinIOStorageClass(kubeconfig)) {
            throw new RuntimeException("MinIO Direct CSI not installed, please install it first.");
        }

        // expand minio in real k8s.
        ExecutorUtils.runTask(() -> {
            return ObjectStorageOzoneHandler.createOzone(k8sServices,
                    kubeconfig,
                    namespace,
                    datanodeReplicas,
                    datanodeStorageSize,
                    omStorageSize,
                    s3gStorageSize,
                    scmStorageSize,
                    storageClass);
        });

    }

    @Override
    public void deleteOzone(long namespaceId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        // remove namespace.
        k8sServices.getK8sNamespaceSet().remove(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        // delete tenant in real k8s.
        ExecutorUtils.runTask(() -> {
            return ObjectStorageOzoneHandler.deleteOzone(k8sServices, kubeconfig, namespace);
        });
    }
}
