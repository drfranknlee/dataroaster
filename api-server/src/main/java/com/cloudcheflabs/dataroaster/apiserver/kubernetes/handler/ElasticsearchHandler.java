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

public class ElasticsearchHandler {

    private static Logger LOG = LoggerFactory.getLogger(ElasticsearchHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "elasticsearch";
        String serviceName = "elasticsearch";

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
                                String namespace,
                                String esClusterName,
                                int masterStorage,
                                int dataNodes,
                                int dataStorage,
                                int clientNodes,
                                int clientStorage) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                namespace,
                esClusterName,
                masterStorage,
                dataNodes,
                dataStorage,
                clientNodes,
                clientStorage);

        String createSh = "create.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "elasticsearch created...";
    }


    public static String delete(K8sServices k8sServices,
                                Kubeconfig kubeconfig,
                                String namespace,
                                String esClusterName) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                namespace,
                esClusterName,
                0,
                0,
                0,
                0,
                0);

        String deleteSh = "delete.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "elasticsearch deleted...";
    }

    private static void substitute(String tempDirectory,
                                   String namespace,
                                   String esClusterName,
                                   int masterStorage,
                                   int dataNodes,
                                   int dataStorage,
                                   int clientNodes,
                                   int clientStorage) {


        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("namespace", namespace);
        kv.put("esClusterName", esClusterName);
        kv.put("masterStorage", String.valueOf(masterStorage));
        kv.put("dataNodes", String.valueOf(dataNodes));
        kv.put("dataStorage", String.valueOf(dataStorage));
        kv.put("clientNodes", String.valueOf(clientNodes));
        kv.put("clientStorage", String.valueOf(clientStorage));

        List<String> files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "es.yaml",
                "kibana.yaml"
        );

        for (String file : files) {
            TemplateUtils.toFile(tempDirectory + "/" + file, false, kv, tempDirectory + "/" + file, true);
        }
    }

}

