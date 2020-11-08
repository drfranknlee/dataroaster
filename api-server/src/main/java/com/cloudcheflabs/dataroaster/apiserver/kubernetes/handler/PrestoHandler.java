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

public class PrestoHandler {

    private static Logger LOG = LoggerFactory.getLogger(PrestoHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "presto";
        String serviceName = "presto";

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
                                String accessKey,
                                String secretKey,
                                int serverMaxMemory,
                                int cpu,
                                int tempDataStorage,
                                int dataStorage,
                                String s3Endpoint,
                                boolean s3SSLEnabled,
                                int workers,
                                String hiveMetastoreEndpoint,
                                String prestoCoordinatorEndpoint) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                namespace,
                accessKey,
                secretKey,
                serverMaxMemory,
                cpu,
                tempDataStorage,
                dataStorage,
                s3Endpoint,
                s3SSLEnabled,
                workers,
                hiveMetastoreEndpoint,
                prestoCoordinatorEndpoint);

        String createSh = "create.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "presto created...";
    }


    public static String delete(K8sServices k8sServices,
                                Kubeconfig kubeconfig,
                                String namespace) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                namespace,
                "any-access-key",
                "any-secret-key",
                0,
                0,
                0,
                0,
                "any-endpoint",
                false,
                0,
                "any-hive-metastore-ep",
                "any-presto-coordinator-ep");

        String deleteSh = "delete.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "presto deleted...";
    }

    private static void substitute(String tempDirectory,
                                   String namespace,
                                   String accessKey,
                                   String secretKey,
                                   int serverMaxMemory,
                                   int cpu,
                                   int tempDataStorage,
                                   int dataStorage,
                                   String s3Endpoint,
                                   boolean s3SSLEnabled,
                                   int workers,
                                   String hiveMetastoreEndpoint,
                                   String prestoCoordinatorEndpoint) {

        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("namespace", namespace);
        kv.put("accessKey", accessKey);
        kv.put("secretKey", secretKey);
        kv.put("serverMaxMemory", String.valueOf(serverMaxMemory));
        kv.put("cpu", String.valueOf(cpu));
        kv.put("tempDataStorage", String.valueOf(tempDataStorage));
        kv.put("dataStorage", String.valueOf(dataStorage));
        kv.put("s3Endpoint", s3Endpoint);
        kv.put("s3SSLEnabled", String.valueOf(s3SSLEnabled));
        kv.put("workers", String.valueOf(workers));
        kv.put("hiveMetastoreEndpoint", hiveMetastoreEndpoint);
        kv.put("prestoCoordinatorEndpoint", prestoCoordinatorEndpoint);

        List<String> files = Arrays.asList(
                "config.properties.coordinator",
                "config.properties.worker",
                "create.sh",
                "delete.sh",
                "hive.properties.template",
                "jvm.config",
                "node.properties.template",
                "presto.yaml"
        );

        for (String file : files) {
            TemplateUtils.toFile(tempDirectory + "/" + file, false, kv, tempDirectory + "/" + file, true);
        }
    }

}

