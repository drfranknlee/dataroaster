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

public class ObjectStorageOzoneHandler {

    private static Logger LOG = LoggerFactory.getLogger(ObjectStorageOzoneHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfig) {
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/object-storage/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // minio version.
        String version = k8sServices.getVersion();

        String objectStorageRootPath = "/templates/object-storage/";
        String versionedStorageDir = "ozone-" + version;

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(objectStorageRootPath, versionedStorageDir, tempDirectory);

        // write kubeconfig to temp dir.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfig);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String createOzone(K8sServices k8sServices,
                                     Kubeconfig kubeconfig,
                                     String namespace,
                                     int datanodeReplicas,
                                     int datanodeStorageSize,
                                     int omStorageSize,
                                     int s3gStorageSize,
                                     int scmStorageSize,
                                     String storageClass) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        String ozoneDeployYaml = "ozone-deploy.yaml";
        Map<String, String> kv = new HashMap<>();
        kv.put("namespace", namespace);
        kv.put("datanodeReplicas", String.valueOf(datanodeReplicas));
        kv.put("datanodeStorageSize", String.valueOf(datanodeStorageSize));
        kv.put("omStorageSize", String.valueOf(omStorageSize));
        kv.put("s3gStorageSize", String.valueOf(s3gStorageSize));
        kv.put("scmStorageSize", String.valueOf(scmStorageSize));
        kv.put("storageClass", storageClass);

        TemplateUtils.toFile(tempDirectory + "/" + ozoneDeployYaml, false, kv, tempDirectory + "/" + ozoneDeployYaml, false);

        String createSh = "create-ozone.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        kv.put("namespace", namespace);
        TemplateUtils.toFile(tempDirectory + "/" + createSh, false, kv, tempDirectory + "/" + createSh, true);
        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "ozone created...";
    }

    public static String deleteOzone(K8sServices k8sServices,
                                      Kubeconfig kubeconfig,
                                     String namespace) {

        String tempDirectory = moveFiles(k8sServices, kubeconfig);

        String ozoneDeployYaml = "ozone-deploy.yaml";
        Map<String, String> kv = new HashMap<>();
        kv.put("namespace", namespace);
        kv.put("datanodeReplicas", String.valueOf(0));
        kv.put("datanodeStorageSize", String.valueOf(0));
        kv.put("omStorageSize", String.valueOf(0));
        kv.put("s3gStorageSize", String.valueOf(0));
        kv.put("scmStorageSize", String.valueOf(0));
        kv.put("storageClass", "any-storage-class");

        TemplateUtils.toFile(tempDirectory + "/" + ozoneDeployYaml, false, kv, tempDirectory + "/" + ozoneDeployYaml, false);


        String deleteSh = "delete-ozone.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + deleteSh, false, kv, tempDirectory + "/" + deleteSh, true);

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "ozone deleted...";
    }

}

