package com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler;

import com.cloudcheflabs.dataroaster.apiserver.kubernetes.YamlUtils;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.util.FileUtils;
import com.cloudcheflabs.dataroaster.apiserver.util.ProcessExecutor;
import com.cloudcheflabs.dataroaster.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SparkHandler {

    private static Logger LOG = LoggerFactory.getLogger(SparkHandler.class);

    private static String moveEnvFiles(Kubeconfig kubeconfig) {

        String serviceTypeName = "spark";
        String serviceName = "env";

        String tempDirectory = System.getProperty("java.io.tmpdir") + "/" + serviceTypeName + "/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);


        String rootPath = "/templates/" + serviceTypeName + "/";

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, serviceName, tempDirectory);

        // write kubeconfig to temp dir.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfig);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String createEnv(Kubeconfig kubeconfig,
                                  String namespace) {

        String tempDirectory = moveEnvFiles(kubeconfig);

        String envYaml = "rbac-pvc.yaml";
        Map<String, String> kv = new HashMap<>();
        kv.put("namespace", namespace);
        TemplateUtils.toFile(tempDirectory + "/" + envYaml, false, kv, tempDirectory + "/" + envYaml, false);

        String createSh = "create.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + createSh, false, kv, tempDirectory + "/" + createSh, true);
        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "spark env created...";
    }

    public static String deleteEnv(Kubeconfig kubeconfig,
                                   String namespace) {

        String tempDirectory = moveEnvFiles(kubeconfig);

        String envYaml = "rbac-pvc.yaml";
        Map<String, String> kv = new HashMap<>();
        kv.put("namespace", namespace);
        TemplateUtils.toFile(tempDirectory + "/" + envYaml, false, kv, tempDirectory + "/" + envYaml, false);


        String deleteSh = "delete.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + deleteSh, false, kv, tempDirectory + "/" + deleteSh, true);

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "spark env deleted...";
    }

}

