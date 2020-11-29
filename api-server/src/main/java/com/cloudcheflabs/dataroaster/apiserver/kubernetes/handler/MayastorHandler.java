package com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler;

import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.YamlUtils;
import com.cloudcheflabs.dataroaster.apiserver.util.FileUtils;
import com.cloudcheflabs.dataroaster.apiserver.util.ProcessExecutor;
import com.cloudcheflabs.dataroaster.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MayastorHandler {

    private static Logger LOG = LoggerFactory.getLogger(MayastorHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "csi";
        String serviceName = "mayastor";

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
                                String workerNodes,
                                String disks) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                k8sServices.getVersion(),
                workerNodes,
                disks);

        String createSh = "create.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "mayastor created...";
    }


    public static String delete(K8sServices k8sServices,
                                Kubeconfig kubeconfig) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                "any-version",
                "",
                "");

        String deleteSh = "delete.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "mayastor deleted...";
    }

    private static void substitute(String tempDirectory,
                                   String version,
                                   String workerNodes,
                                   String disks) {

        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("version", version);
        kv.put("workerNodes", workerNodes.replaceAll(",", " "));
        kv.put("disks", disks.replaceAll(",", " "));


        List<String> files = Arrays.asList(
                "create.sh",
                "csi-daemonset.yaml",
                "delete.sh",
                "mayastor-daemonset.yaml",
                "mayastorpool.yaml",
                "moac-deployment.yaml",
                "moac-rbac.yaml",
                "nats-deployment.yaml",
                "storageclass.yaml"
        );

        for (String file : files) {
            TemplateUtils.toFile(tempDirectory + "/" + file, false, kv, tempDirectory + "/" + file, true);
        }
    }

}

