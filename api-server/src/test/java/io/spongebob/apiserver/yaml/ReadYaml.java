package io.spongebob.apiserver.yaml;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.kubernetes.user.CreateCsr;
import io.spongebob.apiserver.util.JsonUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ReadYaml {

    private static Logger LOG = LoggerFactory.getLogger(ReadYaml.class);

    @Test
    public void readKubeconfigAdminYaml() throws Exception {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("manifests/kubeconfig-test.yaml");
        Map<String, Object> map = yaml.load(inputStream);

        System.out.println(JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), map)));

        List<Map<String, Object>> clustersList = (List<Map<String, Object>>) map.get("clusters");
        Map<String, Object> firstClustersMap = clustersList.get(0);
        String clusterName = (String) firstClustersMap.get("name");
        LOG.info("clusterName: {}", clusterName);

        Map<String, Object> clusterMap = (Map<String, Object>) firstClustersMap.get("cluster");
        String masterUrl = (String) clusterMap.get("server");
        LOG.info("masterUrl: {}", masterUrl);

        String clusterCertData = (String) clusterMap.get("certificate-authority-data");
        LOG.info("clusterCertData: {}", clusterCertData);

        String namespace = "default";
        LOG.info("namespace: {}", namespace);

        List<Map<String, Object>> usersMap = (List<Map<String, Object>>) map.get("users");
        Map<String, Object> firstUsersMap = usersMap.get(0);
        String user = (String) firstUsersMap.get("name");
        LOG.info("user: {}", user);

        Map<String, Object> userMap = (Map<String, Object>) firstUsersMap.get("user");
        String clientCertData = (String) userMap.get("client-certificate-data");
        LOG.info("clientCertData: {}", clientCertData);

        String clientKeyData = (String) userMap.get("client-key-data");
        LOG.info("clientKeyData: {}", clientKeyData);
    }
}
