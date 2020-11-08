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

public class CsiHandler {

    private static Logger LOG = LoggerFactory.getLogger(CsiHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfigAdmin) {
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/csi/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // csi version.
        String version = k8sServices.getVersion();

        String csiRootPath = "/templates/csi";
        String versionedCsiDir = "direct-csi-" + version;
        String csiPath = csiRootPath + "/" + versionedCsiDir;

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(csiRootPath, versionedCsiDir, tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(csiPath, "resources", tempDirectory + "/" + "resources");

        // write kubeconfig to temp dir.
        String kubeconfig = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfigAdmin);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfig, false);

        return tempDirectory;
    }

    public static String craeteCsi(K8sServices k8sServices, String dataRootPath, int dataDirCount, Kubeconfig kubeconfigAdmin) {

        String tempDirectory = moveFiles(k8sServices, kubeconfigAdmin);

        String createCsiSh = "create-csi.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("dataDirCount", String.valueOf(dataDirCount));
        kv.put("dataRootPath", dataRootPath);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + createCsiSh, false, kv, tempDirectory + "/" + createCsiSh, true);

        // run create csi shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createCsiSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "csi created...";
    }

    public static String deleteCsi(K8sServices k8sServices, Kubeconfig kubeconfigAdmin) {
        String tempDirectory = moveFiles(k8sServices, kubeconfigAdmin);

        String deleteCsiSh = "delete-csi.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + deleteCsiSh, false, kv, tempDirectory + "/" + deleteCsiSh, true);

        // run create csi shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteCsiSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "csi deleted...";
    }
}

