package com.cloudcheflabs.dataroaster.util;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.test.TestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudcheflabs.dataroaster.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.encrypt.KeyEncryptorProcessExecutor;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Properties;

public class PropertiesUtilsTestRun extends TestBase {

    @Test
    public void readProp() throws Exception {
        Properties prop = PropertiesUtils.readPropertiesFromFileSystem("/home/pcp/pod-runner.properties");
        LOG.info("encryption key: {}", prop.getProperty("encryption.key"));
    }

    @Test
    public void readKubeconfig() throws Exception {
        Properties prop = PropertiesUtils.readPropertiesFromFileSystem("/home/pcp/pod-runner.properties");
        String encryptedKey = prop.getProperty("encryption.key");
        String encryptedKubeconfig = prop.getProperty("admin.kubeconfig");

        String encryptionKey = KeyEncryptorProcessExecutor.doExec("/home/pcp/dataroaster/encyptor/enc", encryptedKey, "false");
        String adminKubeconfig = EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, encryptedKubeconfig);
        Kubeconfig kubeconfig = YamlUtils.readKubeconfigYaml(new ByteArrayInputStream(adminKubeconfig.getBytes()));
        LOG.info(JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), kubeconfig)));
    }
}
