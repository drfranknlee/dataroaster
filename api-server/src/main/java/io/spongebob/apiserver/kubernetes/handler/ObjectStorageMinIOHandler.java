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

public class ObjectStorageMinIOHandler {

    private static Logger LOG = LoggerFactory.getLogger(ObjectStorageMinIOHandler.class);

    private static String moveMinIOFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/object-storage/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // minio version.
        String version = k8sServices.getVersion();

        String objectStorageRootPath = "/templates/object-storage/";
        String versionedMinIODir = "minio-" + version;
        String minioPath = objectStorageRootPath + "/" + versionedMinIODir;

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(objectStorageRootPath, versionedMinIODir, tempDirectory);

        // write kubeconfig to temp dir.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfig);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String createMinIO(K8sServices k8sServices,
                                     String accessKey,
                                     String secretKey,
                                     String namespace,
                                     String tenantName,
                                     int servers,
                                     int volumes,
                                     int capacity,
                                     Kubeconfig kubeconfig) {

        String tempDirectory = moveMinIOFiles(k8sServices, kubeconfig);

        String createMinIOSh = "create-minio.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("tenantName", tenantName);
        kv.put("servers", String.valueOf(servers));
        kv.put("volumes", String.valueOf(volumes));
        kv.put("capacity", String.valueOf(capacity));
        kv.put("namespace", namespace);
        kv.put("minioVersion", k8sServices.getVersion());
        kv.put("accessKey", accessKey);
        kv.put("secretKey", secretKey);
        TemplateUtils.toFile(tempDirectory + "/" + createMinIOSh, false, kv, tempDirectory + "/" + createMinIOSh, true);

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createMinIOSh);

        // delete temp directory.
        // FileUtils.deleteDirectory(tempDirectory);

        return "minio created...";
    }



    public static String expandMinIO(K8sServices k8sServices,
                                     String namespace,
                                     String tenantName,
                                     int servers,
                                     int volumes,
                                     int capacity,
                                     Kubeconfig kubeconfig) {

        String tempDirectory = moveMinIOFiles(k8sServices, kubeconfig);

        String expandMinIOSh = "expand-minio.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("tenantName", tenantName);
        kv.put("servers", String.valueOf(servers));
        kv.put("volumes", String.valueOf(volumes));
        kv.put("capacity", String.valueOf(capacity));
        kv.put("namespace", namespace);
        TemplateUtils.toFile(tempDirectory + "/" + expandMinIOSh, false, kv, tempDirectory + "/" + expandMinIOSh, true);
        if(LOG.isDebugEnabled()) {
            String replacedText = FileUtils.fileToString(tempDirectory + "/" + expandMinIOSh, false);
            LOG.debug("replacedText: \n{}", replacedText);
        }

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + expandMinIOSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "minio expanded...";
    }



    public static String deleteTenant(K8sServices k8sServices,
                                     String namespace,
                                     String tenantName,
                                     Kubeconfig kubeconfig) {

        String tempDirectory = moveMinIOFiles(k8sServices, kubeconfig);

        String deleteTenantSh = "delete-tenant.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("tenantName", tenantName);
        kv.put("namespace", namespace);
        TemplateUtils.toFile(tempDirectory + "/" + deleteTenantSh, false, kv, tempDirectory + "/" + deleteTenantSh, true);

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteTenantSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "tenant deleted...";
    }

    private static String moveMinIOOperatorFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/object-storage/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // minio version.
        String version = k8sServices.getVersion();

        String objectStorageRootPath = "/templates/object-storage/";
        String versionedMinIOOperatorDir = "minio-operator-" + version;
        String minioPath = objectStorageRootPath + "/" + versionedMinIOOperatorDir;

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(objectStorageRootPath, versionedMinIOOperatorDir, tempDirectory);

        // write kubeconfig to temp dir.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfig);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String createOperator(K8sServices k8sServices,
                                        Kubeconfig kubeconfig) {

        String tempDirectory = moveMinIOOperatorFiles(k8sServices, kubeconfig);

        String createOperatorSh = "create-operator.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("version", k8sServices.getVersion());
        TemplateUtils.toFile(tempDirectory + "/" + createOperatorSh, false, kv, tempDirectory + "/" + createOperatorSh, true);

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createOperatorSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "operator created...";
    }

    public static String deleteOperator(K8sServices k8sServices,
                                      Kubeconfig kubeconfig) {

        String tempDirectory = moveMinIOOperatorFiles(k8sServices, kubeconfig);

        String deleteOperatorSh = "delete-operator.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + deleteOperatorSh, false, kv, tempDirectory + "/" + deleteOperatorSh, true);

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteOperatorSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "operator deleted...";
    }

}

