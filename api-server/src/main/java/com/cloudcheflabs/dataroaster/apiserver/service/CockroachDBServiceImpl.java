package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.CockroachDBService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigAdmin;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.CockroachDBHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CockroachDBServiceImpl implements CockroachDBService {

    private static Logger LOG = LoggerFactory.getLogger(CockroachDBServiceImpl.class);

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

    public CockroachDBServiceImpl() {
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
    public void create(long namespaceId, long serviceId, int storage, int cpu, int memory, int nodes) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.RDB.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.RDB.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // add namespace.
        k8sServices.getRdbK8sNamespaceSet().add(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        // check if minio direct csi storage class exists or not.
        if(!resourceControlDao.existsMinIOStorageClass(kubeconfig)) {
            throw new RuntimeException("MinIO Direct CSI not installed, please install it first.");
        }

        ExecutorUtils.runTask(() -> {
            return CockroachDBHandler.create(k8sServices,
                    kubeconfig,
                    namespace,
                    storage,
                    cpu,
                    memory,
                    nodes);
        });
    }

    @Override
    public void delete(long namespaceId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.RDB.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.RDB.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        // remove namespace.
        k8sServices.getRdbK8sNamespaceSet().remove(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        ExecutorUtils.runTask(() -> {
            return CockroachDBHandler.delete(k8sServices,
                    kubeconfig,
                    namespace);
        });
    }
}