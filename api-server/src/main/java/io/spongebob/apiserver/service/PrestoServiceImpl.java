package io.spongebob.apiserver.service;

import io.fabric8.kubernetes.api.model.Secret;
import io.spongebob.apiserver.api.dao.*;
import io.spongebob.apiserver.api.service.PrestoService;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.model.K8sCluster;
import io.spongebob.apiserver.domain.model.K8sKubeconfigAdmin;
import io.spongebob.apiserver.domain.model.K8sNamespace;
import io.spongebob.apiserver.domain.model.K8sServices;
import io.spongebob.apiserver.kubernetes.ExecutorUtils;
import io.spongebob.apiserver.kubernetes.handler.PrestoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Map;

@Service
@Transactional
public class PrestoServiceImpl implements PrestoService {

    private static Logger LOG = LoggerFactory.getLogger(PrestoServiceImpl.class);

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

    public PrestoServiceImpl() {
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
    public void createPresto(long namespaceId,
                             long serviceId,
                             int serverMaxMemory,
                             int cpu,
                             int tempDataStorage,
                             int dataStorage,
                             int workers,
                             String s3StorageType) {

        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.PRESTO.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.PRESTO.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // add namespace.
        k8sServices.getPrestoK8sNamespaceSet().add(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        String hiveMetastoreEndpoint = "metastore." + namespace + ":9083";

        String prestoCoordinatorEndpoint = "presto:8080";

        // check if hive metastore is installed or not.
        if(!resourceControlDao.existsHiveMetastore(kubeconfig, namespace)) {
            throw new RuntimeException("Hive Metastore not installed, please install it first.");
        }

        if(s3StorageType.equals("minio")) {
            // check if minio tenant exists in this namespae or not.
            if(!resourceControlDao.existsTenant(kubeconfig, namespace)) {
                throw new RuntimeException("MinIO Tenant not installed in this namespace, please install it first.");
            }

            // check if minio ingress is installed or not.
            String host = resourceControlDao.getMinIOIngressHost(kubeconfig, namespace);
            if (host == null) {
                throw new RuntimeException("MinIO Ingress Service not installed, please install it first.");
            }

            // minio s3 endpoint.
            String s3Endpoint = "https://" + host;
            boolean s3SSLEnabled = true;

            // get secret.
            Secret secret = resourceControlDao.getSecret(kubeconfig, namespace);
            Map<String, String> map = secret.getData();
            String ak = map.get("accesskey");
            String sk = map.get("secretkey");

            String accessKey = new String(Base64.getDecoder().decode(ak));
            String secretKey = new String(Base64.getDecoder().decode(sk));
            ExecutorUtils.runTask(() -> {
                return PrestoHandler.create(k8sServices,
                        kubeconfig,
                        namespace,
                        accessKey,
                        secretKey,
                        serverMaxMemory,
                        cpu,
                        tempDataStorage,
                        dataStorage,
                        s3Endpoint,
                        s3SSLEnabled,
                        workers,
                        hiveMetastoreEndpoint,
                        prestoCoordinatorEndpoint);
            });
        } else if(s3StorageType.equals("ozone")) {
            // check if ozone is running.
            if(!resourceControlDao.existsOzone(kubeconfig, namespace)) {
                throw new RuntimeException("Ozone not installed, please install it first.");
            }

            String s3Endpoint = "http://s3g-service." + namespace + ":9898";

            boolean s3SSLEnabled = false;
            String accessKey = "any-access-key";
            String secretKey = "any-secret-key";
            ExecutorUtils.runTask(() -> {
                return PrestoHandler.create(k8sServices,
                        kubeconfig,
                        namespace,
                        accessKey,
                        secretKey,
                        serverMaxMemory,
                        cpu,
                        tempDataStorage,
                        dataStorage,
                        s3Endpoint,
                        s3SSLEnabled,
                        workers,
                        hiveMetastoreEndpoint,
                        prestoCoordinatorEndpoint);
            });
        } else {
            throw new RuntimeException("Unsupported S3 Storage: " + s3StorageType);
        }
    }

    @Override
    public void deletePresto(long namespaceId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.PRESTO.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.PRESTO.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        // remove namespace.
        k8sServices.getPrestoK8sNamespaceSet().remove(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        ExecutorUtils.runTask(() -> {
            return PrestoHandler.delete(k8sServices,
                                        kubeconfig,
                                        namespace);
        });
    }
}
