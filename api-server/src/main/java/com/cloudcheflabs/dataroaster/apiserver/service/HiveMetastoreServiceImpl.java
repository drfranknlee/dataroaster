package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigAdmin;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.HiveMetastoreHandler;
import io.fabric8.kubernetes.api.model.Secret;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.HiveMetastoreService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
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
public class HiveMetastoreServiceImpl implements HiveMetastoreService {

    private static Logger LOG = LoggerFactory.getLogger(HiveMetastoreServiceImpl.class);

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

    public HiveMetastoreServiceImpl() {
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
    public void createHiveMetastore(long namespaceId, long serviceId, String bucket, int storageSize, String s3StorageType) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.HIVE_METASTORE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.HIVE_METASTORE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // add namespace.
        k8sServices.getHiveMetastoreK8sNamespaceSet().add(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        String clusterName = kubeconfig.getClusterName();

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
            String endpoint = "https://" + host;
            String storageClass = "direct.csi.min.io";

            // get secret.
            Secret secret = resourceControlDao.getSecret(kubeconfig, namespace);
            Map<String, String> map = secret.getData();
            String ak = map.get("accesskey");
            String sk = map.get("secretkey");

            String accessKey = new String(Base64.getDecoder().decode(ak));
            String secretKey = new String(Base64.getDecoder().decode(sk));
            ExecutorUtils.runTask(() -> {
                return HiveMetastoreHandler.create(k8sServices,
                        kubeconfig,
                        namespace,
                        clusterName,
                        bucket,
                        accessKey,
                        secretKey,
                        endpoint,
                        storageSize,
                        storageClass);
            });
        } else if(s3StorageType.equals("ozone")) {
            // check if ozone is running.
            if(!resourceControlDao.existsOzone(kubeconfig, namespace)) {
                throw new RuntimeException("Ozone not installed, please install it first.");
            }

            String endpoint = "http://" + resourceControlDao.getOzoneS3Endpoint(kubeconfig, namespace);
            String storageClass = "direct.csi.min.io";
            String accessKey = "any-access-key";
            String secretKey = "any-secret-key";
            ExecutorUtils.runTask(() -> {
                return HiveMetastoreHandler.create(k8sServices,
                        kubeconfig,
                        namespace,
                        clusterName,
                        bucket,
                        accessKey,
                        secretKey,
                        endpoint,
                        storageSize,
                        storageClass);
            });
        } else {
            throw new RuntimeException("Unsupported S3 Storage: " + s3StorageType);
        }
    }

    @Override
    public void deleteHiveMetastore(long namespaceId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.HIVE_METASTORE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.HIVE_METASTORE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        // remove namespace.
        k8sServices.getHiveMetastoreK8sNamespaceSet().remove(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        String clusterName = kubeconfig.getClusterName();

        // just set some values, it does not matter.
        String bucket = "any-bucket";
        String accessKey = "any-access-key";
        String secretKey = "any-secret-key";
        String endpoint = "any-endpoint";
        int storageSize = 0;
        String storageClass = "any-storage-class";

        // delete in real k8s.
        ExecutorUtils.runTask(() -> {
            return HiveMetastoreHandler.delete(k8sServices,
                    kubeconfig,
                    namespace,
                    clusterName,
                    bucket,
                    accessKey,
                    secretKey,
                    endpoint,
                    storageSize,
                    storageClass);
        });
    }
}
