package io.spongebob.apiserver.kubernetes.handler;

import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.model.K8sServices;
import io.spongebob.apiserver.kubernetes.YamlUtils;
import io.spongebob.apiserver.util.FileUtils;
import io.spongebob.apiserver.util.ProcessExecutor;
import io.spongebob.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoadBalancerMetalLBHandler {

    private static Logger LOG = LoggerFactory.getLogger(LoadBalancerMetalLBHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "load-balancer";
        String serviceName = "metallb";

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

    public static String create(K8sServices k8sServices,
                                Kubeconfig kubeconfig,
                                String fromIP,
                                String toIP) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        String configYaml = "config.yaml";
        Map<String, String> kv = new HashMap<>();
        kv.put("fromIP", fromIP);
        kv.put("toIP", toIP);
        TemplateUtils.toFile(tempDirectory + "/" + configYaml, false, kv, tempDirectory + "/" + configYaml, false);

        String createSh = "create.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + createSh, false, kv, tempDirectory + "/" + createSh, true);
        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "metal lb created...";
    }

    public static String delete(K8sServices k8sServices,
                                Kubeconfig kubeconfig) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        String deleteSh = "delete.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + deleteSh, false, kv, tempDirectory + "/" + deleteSh, true);

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "metal lb deleted...";
    }

}

