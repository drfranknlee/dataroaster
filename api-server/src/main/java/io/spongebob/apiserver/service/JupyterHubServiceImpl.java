package io.spongebob.apiserver.service;

import io.spongebob.apiserver.api.dao.*;
import io.spongebob.apiserver.api.service.JupyterHubService;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.model.K8sCluster;
import io.spongebob.apiserver.domain.model.K8sKubeconfigAdmin;
import io.spongebob.apiserver.domain.model.K8sNamespace;
import io.spongebob.apiserver.domain.model.K8sServices;
import io.spongebob.apiserver.kubernetes.ExecutorUtils;
import io.spongebob.apiserver.kubernetes.handler.JupyterHubHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JupyterHubServiceImpl implements JupyterHubService {

    private static Logger LOG = LoggerFactory.getLogger(JupyterHubServiceImpl.class);

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

    public JupyterHubServiceImpl() {
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
    public void createJupyterHub(long namespaceId,
                                 long serviceId,
                                 int storage) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.JUPYTERHUB.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.JUPYTERHUB.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // add namespace.
        k8sServices.getJupyterHubK8sNamespaceSet().add(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        ExecutorUtils.runTask(() -> {
            return JupyterHubHandler.create(k8sServices,
                    kubeconfig,
                    namespace,
                    storage);
        });
    }

    @Override
    public void deleteJupyterHub(long namespaceId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.JUPYTERHUB.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.JUPYTERHUB.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        // remove namespace.
        k8sServices.getJupyterHubK8sNamespaceSet().remove(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        ExecutorUtils.runTask(() -> {
            return JupyterHubHandler.delete(k8sServices,
                    kubeconfig,
                    namespace);
        });
    }
}
