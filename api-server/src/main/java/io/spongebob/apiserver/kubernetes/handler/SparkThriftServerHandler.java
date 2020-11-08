package io.spongebob.apiserver.kubernetes.handler;

import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.model.K8sServices;
import io.spongebob.apiserver.kubernetes.YamlUtils;
import io.spongebob.apiserver.util.FileUtils;
import io.spongebob.apiserver.util.ProcessExecutor;
import io.spongebob.apiserver.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SparkThriftServerHandler {

    private static Logger LOG = LoggerFactory.getLogger(SparkThriftServerHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "hive";
        String serviceName = "spark-thrift-server";

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
                                String masterUrl,
                                String namespace,
                                String endpoint,
                                String bucket,
                                String accessKey,
                                String secretKey,
                                int executors,
                                int executorMemory,
                                int executorCore,
                                int driverMemory,
                                String hiveMetastore) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(tempDirectory,
                masterUrl,
                namespace,
                endpoint,
                bucket,
                accessKey,
                secretKey,
                executors,
                executorMemory,
                executorCore,
                driverMemory,
                hiveMetastore);

        String createSh = "create.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "spark thrift server created...";
    }

    private static void substitute(String tempDirectory,
                                   String masterUrl,
                                   String namespace,
                                   String endpoint,
                                   String bucket,
                                   String accessKey,
                                   String secretKey,
                                   int executors,
                                   int executorMemory,
                                   int executorCore,
                                   int driverMemory,
                                   String hiveMetastore) {

        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("masterUrl", masterUrl);
        kv.put("namespace", namespace);
        kv.put("endpoint", endpoint);
        kv.put("bucket", bucket);
        kv.put("accessKey", accessKey);
        kv.put("secretKey", secretKey);
        kv.put("executors", String.valueOf(executors));
        kv.put("executorMemory", String.valueOf(executorMemory));
        kv.put("executorCore", String.valueOf(executorCore));
        kv.put("driverMemory", String.valueOf(driverMemory));
        kv.put("hiveMetastore", hiveMetastore);

        List<String> files = Arrays.asList(
                "create.sh",
                "spark-thrift-server-service.yaml"
        );

        for (String file : files) {
            TemplateUtils.toFile(tempDirectory + "/" + file, false, kv, tempDirectory + "/" + file, true);
        }
    }

}

