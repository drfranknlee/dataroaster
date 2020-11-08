package io.spongebob.apiserver.kubernetes.handler;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.kubernetes.YamlUtils;
import io.spongebob.apiserver.kubernetes.client.KubernetesClientUtils;
import io.spongebob.apiserver.util.FileUtils;
import io.spongebob.apiserver.util.ProcessExecutor;
import io.spongebob.apiserver.util.StringUtils;
import io.spongebob.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserCertsHandler {

    private static Logger LOG = LoggerFactory.getLogger(UserCertsHandler.class);

    public static Kubeconfig craeteCerts(String user, String group, String namespace, Kubeconfig kubeconfigAdmin) {
        String generateCsrShFileName = "generate-csr.sh";
        String keyFileName = user + "-k8s.key";
        String csrFileName = user + "-k8s.csr";
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/csr/" + user + "-" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // generate-csr.sh template.
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("keyFileName", keyFileName);
        kv.put("csrFileName", csrFileName);
        kv.put("user", user);
        kv.put("group", group);

        String generateCsrShString = TemplateUtils.replace("/templates/k8s-user-account/" + generateCsrShFileName, true, kv);
        LOG.debug("csrString: {}", generateCsrShString);

        // create csr shell file.
        String generateCsrShFilePath = tempDirectory + "/" + generateCsrShFileName;
        FileUtils.stringToFile(generateCsrShString, generateCsrShFilePath, true);

        // run csr shell.
        ProcessExecutor.doExec(generateCsrShFilePath);

        // read key file.
        String keyFilePath = tempDirectory + "/" + keyFileName;
        String keyString = FileUtils.fileToString(keyFilePath, false);
        // encode key string as base64.
        String clientKeyData = StringUtils.base64Encode(keyString);
        LOG.debug("clientKeyData: {}", clientKeyData);

        // read csr file.
        String csrFilePath = tempDirectory + "/" + csrFileName;
        String csrString = FileUtils.fileToString(csrFilePath, false);
        // encode csr string as base64.
        String csrData = StringUtils.base64Encode(csrString);
        LOG.debug("csrData: {}", csrData);

        // k8s-user-csr.yaml template.
        String csrName = user + "-" + group + "-" + namespace + "-k8s-csr";
        String k8sUserCsrYamlFileName = "k8s-user-csr.yaml";

        kv = new HashMap<>();
        kv.put("csrName", csrName);
        kv.put("csrData", csrData);
        String k8sUserCsrYamlString = TemplateUtils.replace("/templates/k8s-user-account/" + k8sUserCsrYamlFileName, true, kv);
        LOG.debug("k8sUserCsrYamlString: \n{}", k8sUserCsrYamlString);

        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfigAdmin);

        // write kubeconfig to temp dir.
        String kubeconfig = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfigAdmin);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfig, false);

        // write csr manifest to temp dir.
        String k8sUserCsrYamlFilePath = tempDirectory + "/" + k8sUserCsrYamlFileName;
        FileUtils.stringToFile(k8sUserCsrYamlString, k8sUserCsrYamlFilePath, false);

        // read kubectl-csr.sh.
        String kubectlShFileName = "kubectl-csr.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", kubeconfig);
        kv.put("namespace", namespace);
        kv.put("csrName", csrName);
        kv.put("user", user);
        kv.put("group", group);

        String kubectlShString = TemplateUtils.replace("/templates/k8s-user-account/" + kubectlShFileName, true, kv);

        // write kubectl exec sh.
        FileUtils.stringToFile(kubectlShString, tempDirectory + "/" + kubectlShFileName, true);

        // run kubectl exec shell.
        ProcessExecutor.doExec(tempDirectory + "/" + kubectlShFileName);

        String userCert = user + ".crt";
        String userCertString = FileUtils.fileToString(tempDirectory + "/" + userCert, false);
        String clientCertData = StringUtils.base64Encode(userCertString);
        LOG.debug("clientCertData: {}", clientCertData);

        Kubeconfig kubeconfigUser = new Kubeconfig(
                kubeconfigAdmin.getMasterUrl(),
                kubeconfigAdmin.getClusterName(),
                kubeconfigAdmin.getClusterCertData(),
                namespace,
                user,
                clientCertData,
                clientKeyData
        );

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return kubeconfigUser;
    }
}

