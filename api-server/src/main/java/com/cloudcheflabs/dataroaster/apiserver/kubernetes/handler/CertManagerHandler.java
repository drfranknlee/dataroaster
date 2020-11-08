package com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler;

import com.cloudcheflabs.dataroaster.apiserver.kubernetes.YamlUtils;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.util.FileUtils;
import com.cloudcheflabs.dataroaster.apiserver.util.ProcessExecutor;
import com.cloudcheflabs.dataroaster.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CertManagerHandler {

    private static Logger LOG = LoggerFactory.getLogger(CertManagerHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "cert-manager";
        String serviceName = "cert-manager";

        String tempDirectory = System.getProperty("java.io.tmpdir") + "/" + serviceTypeName + "/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // version.
        String version = k8sServices.getVersion();

        String rootPath = "/templates/" + serviceTypeName + "/";
        String versionedPath = serviceName + "-" + version;

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, versionedPath, tempDirectory);

        // write kubeconfig to temp dir.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfig);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String create(K8sServices k8sServices,
                                Kubeconfig kubeconfig) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(k8sServices.getVersion(), tempDirectory);

        String createSh = "create.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "cert-manager created...";
    }

    public static String delete(K8sServices k8sServices,
                                Kubeconfig kubeconfig) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(k8sServices.getVersion(), tempDirectory);

        String deleteSh = "delete.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "cert-manager deleted...";
    }

    private static void substitute(String version, String tempDirectory) {

        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("version", version);

        List<String> files = Arrays.asList(
                "prod-issuer.yaml",
                "create.sh",
                "delete.sh"
        );

        for (String file : files) {
            TemplateUtils.toFile(tempDirectory + "/" + file, false, kv, tempDirectory + "/" + file, true);
        }
    }

}

