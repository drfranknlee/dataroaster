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

public class HiveMetastoreHandler {

    private static Logger LOG = LoggerFactory.getLogger(HiveMetastoreHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {

        String serviceTypeName = "hive";
        String serviceName = "hive-metastore";

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
                                String clusterName,
                                String bucket,
                                String accessKey,
                                String secretKey,
                                String endpoint,
                                int storageSize,
                                String storageClass) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(namespace, clusterName, bucket, accessKey, secretKey, endpoint, storageSize, storageClass, tempDirectory);

        String createSh = "create.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "hive metastore created...";
    }

    public static String delete(K8sServices k8sServices,
                                Kubeconfig kubeconfig,
                                String namespace,
                                String clusterName,
                                String bucket,
                                String accessKey,
                                String secretKey,
                                String endpoint,
                                int storageSize,
                                String storageClass) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        substitute(namespace, clusterName, bucket, accessKey, secretKey, endpoint, storageSize, storageClass, tempDirectory);

        String deleteSh = "delete.sh";

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "hive metastore deleted...";
    }

    private static void substitute(String namespace,
                                   String clusterName,
                                   String bucket,
                                   String accessKey,
                                   String secretKey,
                                   String endpoint,
                                   int storageSize,
                                   String storageClass,
                                   String tempDirectory) {

        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("namespace", namespace);
        kv.put("clusterName", clusterName);
        kv.put("bucket", bucket);
        kv.put("accessKey", accessKey);
        kv.put("secretKey", secretKey);
        kv.put("endpoint", endpoint);
        kv.put("storageSize", String.valueOf(storageSize));
        kv.put("storageClass", storageClass);

        List<String> files = Arrays.asList(
                "core-site.xml",
                "create.sh",
                "delete.sh",
                "init-schema.yaml",
                "metastore.yaml",
                "metastore-site.xml",
                "mysql.yaml"
        );

        for (String file : files) {
            TemplateUtils.toFile(tempDirectory + "/" + file, false, kv, tempDirectory + "/" + file, true);
        }
    }

}

