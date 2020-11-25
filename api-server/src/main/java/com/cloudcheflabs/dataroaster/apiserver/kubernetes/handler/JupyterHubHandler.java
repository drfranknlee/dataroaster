package com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler;

import com.cloudcheflabs.dataroaster.apiserver.kubernetes.YamlUtils;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.util.FileUtils;
import com.cloudcheflabs.dataroaster.apiserver.util.ProcessExecutor;
import com.cloudcheflabs.dataroaster.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class JupyterHubHandler {

    private static Logger LOG = LoggerFactory.getLogger(JupyterHubHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "jupyterhub";
        String serviceName = "jupyterhub";

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
                                int storage,
                                String config) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        // create temp config yaml.
        String tempConfig = "tempconfig.yaml";
        String tempConfigPath = tempDirectory + "/" + tempConfig;
        if(config == null) {
            File tempConfigFile = new File(tempConfigPath);
            try {
                tempConfigFile.createNewFile();
            } catch (Exception e) {
                LOG.error(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            FileUtils.stringToFile(config, tempConfigPath, false);
        }

        substitute(tempDirectory,
                namespace,
                k8sServices.getVersion(),
                storage);

        String createSh = "create.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "jupyterhub created...";
    }


    public static String delete(K8sServices k8sServices,
                                Kubeconfig kubeconfig,
                                String namespace) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                namespace,
                "any-version",
                0);

        String deleteSh = "delete.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "jupyterhub deleted...";
    }

    private static void substitute(String tempDirectory,
                                   String namespace,
                                   String version,
                                   int storage) {

        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("namespace", namespace);
        kv.put("version", version);
        kv.put("storage", String.valueOf(storage));

        List<String> files = Arrays.asList(
                "create.sh",
                "delete.sh"
        );

        for (String file : files) {
            TemplateUtils.toFile(tempDirectory + "/" + file, false, kv, tempDirectory + "/" + file, true);
        }
    }

}

