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

public class NfsHandler {

    private static Logger LOG = LoggerFactory.getLogger(NfsHandler.class);

    private static String moveFiles(K8sServices k8sServices, Kubeconfig kubeconfigAdmin) {
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/nfs/" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // nfs version.
        String version = k8sServices.getVersion();

        String nfsRootPath = "/templates/nfs";
        String versionedNfsDir = "nfs-server-provisioner-" + version;
        String nfsPath = nfsRootPath + "/" + versionedNfsDir;

        // move the files in classpath to temp dir in file system.
        FileUtils.copyFilesFromClasspathToFileSystem(nfsRootPath, versionedNfsDir, tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(nfsPath, "templates", tempDirectory + "/" + "templates");

        // write kubeconfig to temp dir.
        String kubeconfig = "kubeconfig";
        String kubeconfigString = YamlUtils.getKubeconfigYaml(kubeconfigAdmin);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfig, false);

        return tempDirectory;
    }

    public static String craeteNfs(K8sServices k8sServices, int persistenceSize, Kubeconfig kubeconfigAdmin) {

        String tempDirectory = moveFiles(k8sServices, kubeconfigAdmin);

        String createNfsSh = "create-nfs.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("persistenceSize", String.valueOf(persistenceSize));
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + createNfsSh, false, kv, tempDirectory + "/" + createNfsSh, true);

        // run create nfs shell.
        ProcessExecutor.doExec(tempDirectory + "/" + createNfsSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "nfs created...";
    }

    public static String deleteNfs(K8sServices k8sServices, Kubeconfig kubeconfigAdmin) {
        String tempDirectory = moveFiles(k8sServices, kubeconfigAdmin);

        String deleteNfsSh = "delete-nfs.sh";
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", "kubeconfig");
        TemplateUtils.toFile(tempDirectory + "/" + deleteNfsSh, false, kv, tempDirectory + "/" + deleteNfsSh, true);

        // run create nfs shell.
        ProcessExecutor.doExec(tempDirectory + "/" + deleteNfsSh);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);

        return "nfs deleted...";
    }
}

