package com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler;

import com.cloudcheflabs.dataroaster.apiserver.kubernetes.YamlUtils;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.util.FileUtils;
import com.cloudcheflabs.dataroaster.apiserver.util.ProcessExecutor;
import com.cloudcheflabs.dataroaster.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IngressNginxHandler {

    private static Logger LOG = LoggerFactory.getLogger(IngressNginxHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "ingress-controller";
        String serviceName = "ingress-nginx";

        String tempDirectory = System.getProperty("java.io.tmpdir") + "/" + serviceTypeName + "/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // minio version.
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

    public static String createIngrssNginx(K8sServices k8sServices,
                                     Kubeconfig kubeconfig,
                                     int port) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        String deployYaml = "ingress-nginx-deploy.yaml";
        Map<String, String> kv = new HashMap<>();
        kv.put("port", String.valueOf(port));
        TemplateUtils.toFile(tempDirectory + "/" + deployYaml, false, kv, tempDirectory + "/" + deployYaml, false);

        String createSh = "create.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + createSh, false, kv, tempDirectory + "/" + createSh, true);
        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "ingress nginx created...";
    }

    public static String deleteIngressNginx(K8sServices k8sServices,
                                      Kubeconfig kubeconfig,
                                     int port) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        String deployYaml = "ingress-nginx-deploy.yaml";
        Map<String, String> kv = new HashMap<>();
        kv.put("port", String.valueOf(port));
        TemplateUtils.toFile(tempDirectory + "/" + deployYaml, false, kv, tempDirectory + "/" + deployYaml, false);


        String deleteSh = "delete.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + deleteSh, false, kv, tempDirectory + "/" + deleteSh, true);

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "ingress nginx deleted...";
    }

}

