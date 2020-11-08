package io.spongebob.apiserver.dao.vault;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.dao.SecretDao;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.util.JsonUtils;
import io.spongebob.apiserver.kubernetes.YamlUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.properties"
})
public class VaultSecretDaoTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(VaultSecretDaoTestRunner.class);

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    @Test
    public void putSecret() throws Exception {
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("manifests/kubeconfig-test.yaml");

        Kubeconfig kubeconfig = YamlUtils.readKubeconfigYaml(inputStream);

        String path = "secret/kubeconfig/some-cluster-name/admin/kubernetes-admin";
        secretDao.writeSecret(path, kubeconfig);

        Kubeconfig ret = secretDao.readSecret(path, Kubeconfig.class);
        LOG.info("ret kubeconfig: \n{}", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), ret)));
        
        Assert.assertTrue(kubeconfig.getClusterName().equals(ret.getClusterName()));
        Assert.assertTrue(kubeconfig.getMasterUrl().equals(ret.getMasterUrl()));
        Assert.assertTrue(kubeconfig.getClusterCertData().equals(ret.getClusterCertData()));
        Assert.assertTrue(kubeconfig.getNamespace().equals(ret.getNamespace()));
        Assert.assertTrue(kubeconfig.getUser().equals(ret.getUser()));
        Assert.assertTrue(kubeconfig.getClientCertData().equals(ret.getClientCertData()));
        Assert.assertTrue(kubeconfig.getClientKeyData().equals(ret.getClientKeyData()));

        secretDao.delete(path);
    }

    @Test
    public void readSecret() throws Exception {
        String path = System.getProperty("path");

        Kubeconfig ret = secretDao.readSecret(path, Kubeconfig.class);
        LOG.info("kubeconfig yaml: \n{}", YamlUtils.getKubeconfigYaml(ret));
    }
}
