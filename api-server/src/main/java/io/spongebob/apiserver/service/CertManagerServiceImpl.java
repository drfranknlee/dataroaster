package io.spongebob.apiserver.service;

import io.spongebob.apiserver.api.dao.*;
import io.spongebob.apiserver.api.service.CertManagerService;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.model.K8sCluster;
import io.spongebob.apiserver.domain.model.K8sKubeconfigAdmin;
import io.spongebob.apiserver.domain.model.K8sNamespace;
import io.spongebob.apiserver.domain.model.K8sServices;
import io.spongebob.apiserver.kubernetes.ExecutorUtils;
import io.spongebob.apiserver.kubernetes.handler.CertManagerHandler;
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
public class CertManagerServiceImpl implements CertManagerService {

    private static Logger LOG = LoggerFactory.getLogger(CertManagerServiceImpl.class);

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

    public CertManagerServiceImpl() {
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
    public void createCertManager(long serviceId, long clusterId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check the service type.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.CERT_MANAGER.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.CERT_MANAGER.name());
        }

        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        k8sServices.getCertManagerK8sClusterSet().add(k8sCluster);
        k8sServicesDao.update(k8sServices);

        // create in real k8s.
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        // check if ingress controller nginx instaled or not.
        if(!resourceControlDao.existsIngressControllerNginx(kubeconfigAdmin)) {
            throw new RuntimeException("Ingress Controller NGINX not installed, please install it first.");
        }

        ExecutorUtils.runTask(() -> {
            return CertManagerHandler.create(k8sServices, kubeconfigAdmin);
        });
    }

    @Override
    public void deleteCertManager(long serviceId, long clusterId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check the service type.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.CERT_MANAGER.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.CERT_MANAGER.name());
        }

        Set<K8sCluster> k8sClusterSet = k8sServices.getCertManagerK8sClusterSet();
        for(K8sCluster k8sCluster : k8sClusterSet) {
            long id = k8sCluster.getId();
            // remove cluster.
            if(id == clusterId) {
                k8sClusterSet.remove(k8sCluster);
            }
        }
        k8sServices.setCertManagerK8sClusterSet(k8sClusterSet);
        k8sServicesDao.update(k8sServices);

        // delete in real k8s.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        ExecutorUtils.runTask(() -> {
            return CertManagerHandler.delete(k8sServices, kubeconfigAdmin);
        });
    }
}
