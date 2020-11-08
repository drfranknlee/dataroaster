package com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sClusterDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sServicesDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.SecretDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.util.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.properties"
})
public class CsiHandlerTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(CsiHandlerTestRunner.class);

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    @Autowired
    @Qualifier("hibernateK8sServicesDao")
    private K8sServicesDao k8sServicesDao;

    @Autowired
    @Qualifier("hibernateK8sClusterDao")
    private K8sClusterDao k8sClusterDao;

    @Test
    public void createCsi() throws Exception {
        long clusterId = 10;
        long serviceId = 1;

        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        CsiHandler.craeteCsi(k8sServices, "/data/minio", 4, kubeconfigAdmin);
    }


    @Test
    public void copyFromClasspathToFileSystem() throws Exception {
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/csi/" + UUID.randomUUID().toString();
        LOG.info("tempDirectory: {}", tempDirectory);

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String version = "0.2.1";
        String csiRootPath = "/templates/csi";
        String versionedCsiDir = "direct-csi-" + version;
        String csiPath = csiRootPath + "/" + versionedCsiDir;

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(csiRootPath, versionedCsiDir, tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(csiPath, "resources", tempDirectory + "/" + "resources");

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);
    }
}
