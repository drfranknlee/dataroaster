package io.spongebob.apiserver.dao.kubernetes;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.spongebob.apiserver.api.dao.*;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.StorageClass;
import io.spongebob.apiserver.domain.model.*;
import io.spongebob.apiserver.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.properties"
})
public class KubernetesResourceControlDaoTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(KubernetesResourceControlDaoTestRunner.class);

    @Autowired
    @Qualifier("kubernetesResourceControlDao")
    private ResourceControlDao resourceControlDao;

    @Autowired
    @Qualifier("hibernateK8sClusterDao")
    private K8sClusterDao k8sClusterDao;

    @Autowired
    @Qualifier("hibernateK8sNamespaceDao")
    private K8sNamespaceDao k8sNamespaceDao;

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    private Kubeconfig getAdminKubeconfig(K8sNamespace k8sNamespace) {
        K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
        K8sKubeconfigAdmin kubeconfigAdmin = k8sCluster.getK8sKubeconfigAdminSet().iterator().next();
        String secretPath = kubeconfigAdmin.getSecretPath();

        return secretDao.readSecret(secretPath, Kubeconfig.class);
    }

    @Test
    public void getTenantName() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        String tenantName = resourceControlDao.getTenantName(kubeconfig, namespace);
        LOG.info("tenantName: {}", tenantName);
        Assert.assertTrue(tenantName.equals("mykidong-tenant"));
    }


    @Test
    public void listStorageClasses() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        List<StorageClass> storageClasses = resourceControlDao.listStorageClasses(kubeconfig);
        LOG.info("storageClasses: \n{}", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), storageClasses)));
    }

    @Test
    public void getExternalIPForMetalLB() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        String ip = resourceControlDao.getExternalIPForMetalLB(kubeconfig);
        LOG.info("ip: {}", ip);
    }

    @Test
    public void getIngresses() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        List<Ingress> ingresses = resourceControlDao.getIngresses(kubeconfig, namespace);
        LOG.info("ingresses: \n{}", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), ingresses)));
    }

    @Test
    public void getSecret() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        Secret secret = resourceControlDao.getSecret(kubeconfig, namespace);
        Map<String, String> map = secret.getData();
        String accessKey = map.get("accesskey");
        String secretKey = map.get("secretkey");

        LOG.info("accessKey: {}, secretKey: {}", accessKey, secretKey);
        LOG.info("base64 decoded accessKey: {}, secretKey: {}", new String(Base64.getDecoder().decode(accessKey)),
                new String(Base64.getDecoder().decode(secretKey)));
    }

    @Test
    public void existsMinIOStorageClass() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        boolean exists = resourceControlDao.existsMinIOStorageClass(kubeconfig);
        LOG.info("exists: {}", exists);
    }

    @Test
    public void existsNfsStorageClass() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        boolean exists = resourceControlDao.existsNfsStorageClass(kubeconfig);
        LOG.info("exists: {}", exists);
    }

    @Test
    public void existsTenant() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        boolean exists = resourceControlDao.existsTenant(kubeconfig, namespace);
        LOG.info("exists: {}", exists);
    }

    @Test
    public void getMinIOS3Endpoint() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        String endpoint = resourceControlDao.getMinIOS3Endpoint(kubeconfig, namespace);
        LOG.info("endpoint: {}", endpoint);
    }

    @Test
    public void listPvcUsingStorageClass() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        List<PersistentVolumeClaim> persistentVolumeClaimsNfs = resourceControlDao.listPvcUsingStorageClass(kubeconfig, "nfs");
        LOG.info("persistentVolumeClaimsNfs: \n{}", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), persistentVolumeClaimsNfs)));

        List<PersistentVolumeClaim> persistentVolumeClaimsCsi = resourceControlDao.listPvcUsingStorageClass(kubeconfig, "direct.csi.min.io");
        LOG.info("persistentVolumeClaimsCsi: \n{}", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), persistentVolumeClaimsCsi)));
    }

    @Test
    public void listPods() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        List<Pod> selectedResources = resourceControlDao.listPods(kubeconfig, namespace);
        LOG.info("selectedResources: \n{}", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), selectedResources)));
    }

    @Test
    public void getOzoneS3Endpoint() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        String endpoint = resourceControlDao.getOzoneS3Endpoint(kubeconfig, namespace);
        LOG.info("endpoint: {}", endpoint);
    }


    @Test
    public void killSparkThriftServer() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        resourceControlDao.killSparkThriftServer(kubeconfig, namespace);
    }

    @Test
    public void existsSparkServiceAccountPVC() throws Exception {
        long namespaceId = 14;

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();
        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);
        boolean exists = resourceControlDao.existsSparkServiceAccountPVC(kubeconfig, namespace);
        LOG.info("exists: {}", exists);
    }
}
