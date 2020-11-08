package io.spongebob.apiserver.service;

import io.fabric8.kubernetes.api.model.Secret;
import io.spongebob.apiserver.api.dao.*;
import io.spongebob.apiserver.api.service.SparkThriftServerService;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.model.K8sCluster;
import io.spongebob.apiserver.domain.model.K8sKubeconfigAdmin;
import io.spongebob.apiserver.domain.model.K8sNamespace;
import io.spongebob.apiserver.domain.model.K8sServices;
import io.spongebob.apiserver.kubernetes.ExecutorUtils;
import io.spongebob.apiserver.kubernetes.handler.SparkThriftServerHandler;
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
public class SparkThriftServerServiceImpl implements SparkThriftServerService {

    private static Logger LOG = LoggerFactory.getLogger(SparkThriftServerServiceImpl.class);

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

    public SparkThriftServerServiceImpl() {
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
    public void createSparkThriftServer(long namespaceId,
                                        long serviceId,
                                        String bucket,
                                        int executors,
                                        int executorMemory,
                                        int executorCore,
                                        int driverMemory,
                                        String s3StorageType) {

        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.SPARK_THRIFT_SERVER.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.SPARK_THRIFT_SERVER.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // add namespace.
        k8sServices.getSparkThriftServerK8sNamespaceSet().add(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        // check if spark service account and PVC exists.
        if(!resourceControlDao.existsSparkServiceAccountPVC(kubeconfig, namespace)) {
            throw new RuntimeException("Spark Service Account or PVC not created, please create it first.");
        }

        String masterUrl = kubeconfig.getMasterUrl();

        String hiveMetastoreEndpoint = "metastore." + namespace + ":9083";

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

            // get secret.
            Secret secret = resourceControlDao.getSecret(kubeconfig, namespace);
            Map<String, String> map = secret.getData();
            String ak = map.get("accesskey");
            String sk = map.get("secretkey");

            String accessKey = new String(Base64.getDecoder().decode(ak));
            String secretKey = new String(Base64.getDecoder().decode(sk));
            ExecutorUtils.runTask(() -> {
                return SparkThriftServerHandler.create(k8sServices,
                        kubeconfig,
                        masterUrl,
                        namespace,
                        endpoint,
                        bucket,
                        accessKey,
                        secretKey,
                        executors,
                        executorMemory,
                        executorCore,
                        driverMemory,
                        hiveMetastoreEndpoint);
            });
        } else if(s3StorageType.equals("ozone")) {
            // check if ozone is running.
            if(!resourceControlDao.existsOzone(kubeconfig, namespace)) {
                throw new RuntimeException("Ozone not installed, please install it first.");
            }

            String endpoint = "http://" + resourceControlDao.getOzoneS3Endpoint(kubeconfig, namespace);
            String accessKey = "any-access-key";
            String secretKey = "any-secret-key";
            ExecutorUtils.runTask(() -> {
                return SparkThriftServerHandler.create(k8sServices,
                        kubeconfig,
                        masterUrl,
                        namespace,
                        endpoint,
                        bucket,
                        accessKey,
                        secretKey,
                        executors,
                        executorMemory,
                        executorCore,
                        driverMemory,
                        hiveMetastoreEndpoint);
            });
        } else {
            throw new RuntimeException("Unsupported S3 Storage: " + s3StorageType);
        }
    }

    @Override
    public void deleteSparkThriftServer(long namespaceId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.SPARK_THRIFT_SERVER.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.SPARK_THRIFT_SERVER.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        // remove namespace.
        k8sServices.getSparkThriftServerK8sNamespaceSet().remove(k8sNamespace);
        k8sServicesDao.update(k8sServices);

        // kill spark thrift server pod.
        resourceControlDao.killSparkThriftServer(kubeconfig, namespace);
    }
}
