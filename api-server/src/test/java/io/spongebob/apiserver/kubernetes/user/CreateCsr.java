package io.spongebob.apiserver.kubernetes.user;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.spongebob.apiserver.kubernetes.client.KubernetesClientManager;
import io.spongebob.apiserver.util.FileUtils;
import io.spongebob.apiserver.util.ProcessExecutor;
import io.spongebob.apiserver.util.StringUtils;
import io.spongebob.apiserver.util.TemplateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateCsr {

    private static Logger LOG = LoggerFactory.getLogger(CreateCsr.class);

    @Test
    public void csr() throws Exception
    {
        String user = "kidong";
        String group = "data-engineer";
        String generateCsrShFileName = "generate-csr.sh";
        String keyFileName = user + "-k8s.key";
        String csrFileName = user + "-k8s.csr";
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/csr/" + user + "-" + UUID.randomUUID().toString();

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        // generate-csr.sh template.
        Map<String, String> kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("keyFileName", keyFileName);
        kv.put("csrFileName", csrFileName);
        kv.put("user", user);
        kv.put("group", group);

        String generateCsrShString = TemplateUtils.replace("/templates/k8s-user-account/" + generateCsrShFileName, true, kv);
        LOG.info("csrString: {}", generateCsrShString);

        // create csr shell file.
        String generateCsrShFilePath = tempDirectory + "/" + generateCsrShFileName;
        FileUtils.stringToFile(generateCsrShString, generateCsrShFilePath, true);

        // run csr shell.
        ProcessExecutor.doExec(generateCsrShFilePath);

        // read key file.
        String keyFilePath = tempDirectory + "/" + keyFileName;
        String keyString = FileUtils.fileToString(keyFilePath, false);
        // encode key string as base64.
        String clientKeyData = StringUtils.base64Encode(keyString);
        LOG.info("clientKeyData: {}", clientKeyData);

        // read csr file.
        String csrFilePath = tempDirectory + "/" + csrFileName;
        String csrString = FileUtils.fileToString(csrFilePath, false);
        // encode csr string as base64.
        String csrData = StringUtils.base64Encode(csrString);
        LOG.info("csrData: {}", csrData);

        // namespace.
        // ns: <namespace-prefix>-<group-name>
        String namespace = "csr-test" + "-" + group;

        // k8s-user-csr.yaml template.
        String csrName = user + "-" + group + "-" + namespace + "-k8s-csr";
        String k8sUserCsrYamlFileName = "k8s-user-csr.yaml";

        kv = new HashMap<>();
        kv.put("csrName", csrName);
        kv.put("csrData", csrData);
        String k8sUserCsrYamlString = TemplateUtils.replace("/templates/k8s-user-account/" + k8sUserCsrYamlFileName, true, kv);
        LOG.info("k8sUserCsrYamlString: \n{}", k8sUserCsrYamlString);

        KubernetesClient adminClient = getClientWithClusterAdminUser();

        // create namespace.
        Namespace newNs = adminClient.namespaces().createOrReplaceWithNew()
                .withNewMetadata()
                .withName(namespace)
                .addToLabels("name", namespace)
                .endMetadata()
                .done();

        // NOTE: Error -
        // https://10.0.4.5:6443/apis/certificates.k8s.io/v1beta1/namespaces/csr-test-data-engineer-cluster-local/certificatesigningrequests.
        // Message: the server could not find the requested resource.
        //adminClient.load(new ByteArrayInputStream(k8sUserCsrYamlString.getBytes())).inNamespace(namespace).createOrReplace();

        // write kubeconfig to temp dir.
        String kubeconfig = "kubeconfig";
        String kubeconfigString = getClusterAdminKubeconfig();
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfig, false);

        // write csr manifest to temp dir.
        String k8sUserCsrYamlFilePath = tempDirectory + "/" + k8sUserCsrYamlFileName;
        FileUtils.stringToFile(k8sUserCsrYamlString, k8sUserCsrYamlFilePath, false);

        // read kubectl-csr.sh.
        String kubectlShFileName = "kubectl-csr.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("kubeconfig", kubeconfig);
        kv.put("namespace", namespace);
        kv.put("csrName", csrName);
        kv.put("user", user);
        kv.put("group", group);

        String kubectlShString = TemplateUtils.replace("/templates/k8s-user-account/" + kubectlShFileName, true, kv);

        // write kubectl exec sh.
        FileUtils.stringToFile(kubectlShString, tempDirectory + "/" + kubectlShFileName, true);

        // run kubectl exec shell.
        ProcessExecutor.doExec(tempDirectory + "/" + kubectlShFileName);

        String userCert = user + ".crt";
        String userCertString = FileUtils.fileToString(tempDirectory + "/" + userCert, false);
        String clientCertData = StringUtils.base64Encode(userCertString);
        LOG.info("clientCertData: {}", clientCertData);

        String userKubeconfigString = getUserKubeconfig(clientCertData, clientKeyData, user, namespace);
        LOG.info("userKubeconfigString: \n{}", userKubeconfigString);

        String userKubeconfig = user + "-" + kubeconfig;
        FileUtils.stringToFile(userKubeconfigString, tempDirectory + "/" + userKubeconfig, false);

        // test if the user can access resources in the specified namespace.
        String testKubectlShFileName = "kubectl-user.sh";
        kv = new HashMap<>();
        kv.put("tempDirectory", tempDirectory);
        kv.put("userKubeconfig", userKubeconfig);

        String testKubectlShString = TemplateUtils.replace("/templates/k8s-user-account/" + testKubectlShFileName, true, kv);

        // write to test kubectl shell.
        FileUtils.stringToFile(testKubectlShString, tempDirectory + "/" + testKubectlShFileName, true);

        // run test shell.
        ProcessExecutor.doExec(tempDirectory + "/" + testKubectlShFileName);

        // test with kubernetes client.
        KubernetesClient userClient = getUserClient(clientCertData, clientKeyData);

        // create a pod.
        userClient.load(FileUtils.readFileFromClasspath("/manifests/a-pod.yaml")).inNamespace(namespace).createOrReplace();

        // watcher.
        userClient.pods().inNamespace(namespace).watch(new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod pod) {
                LOG.info("action: {},  pod: {}", action.name(), pod.getStatus().toString());
            }

            @Override
            public void onClose(KubernetesClientException e) {
                LOG.info("Watcher close due to {}", e.getMessage());
            }
        });

        Thread.sleep(15000);

        userClient.pods().inNamespace(namespace).list().getItems().forEach(a -> {
            LOG.info("name: {}", a.getMetadata().getName());
        });
        Assert.assertEquals(1, userClient.pods().inNamespace(namespace).list().getItems().size());

        // delete a pod.
        userClient.load(FileUtils.readFileFromClasspath("/manifests/a-pod.yaml")).inNamespace(namespace).delete();

        // delete temp directory.
        //FileUtils.deleteDirectory(tempDirectory);
    }


    private String getUserKubeconfig(String clientCertData,
                                     String clientKeyData,
                                     String user,
                                     String namespace) {
        // master url.
        String masterUrl = "https://10.0.4.5:6443";

        // cluster cert data: certificate-authority-data
        String clusterCertData = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUN5RENDQWJDZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJd01EY3lOREEzTWpJek5sb1hEVE13TURjeU1qQTNNakl6Tmxvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBUDRsCk40cGJNOU10SkZaUzVsUXJsZmEyd1o0THI3Z3kzZ1hKeU8vdW9oNGphODY3VUlJNG8veXRWNG5VTFRueFU1QjQKbUJPTGZleXpBMUt2WkdPcVBBbDROdnBBQ2sxK3BBV0ViRXdlRVJYY1RkVk96VmRzQXQ2TFVCN2h5SUhoY1Q4RApwaThNdzBTU2tEUmVLdHNjMzZOUng0TTZGT3pXVTg1amRZVFF1OXhqQ2NOTkFDakdkaC9aUm9pNk5RanFWUGJUCjg2NG9UN1hYK2c4QU1uMXR6blY1bkt5SUhuUk5pT1ZEZ2p2Yy9WdVQ3SjlUMDI1UUx4NVBIVVYwa0xrOWdwQnUKek9TWmdjZlVDNzVQSzAzSzIzeVcyTk5aeG1Sc2tDbXpJNitVOWVneDBLQkpCbExlcCtDZ0s3YlZRRERwR2N1aApBdDFBODRpOUluN3FMZ01zc2FVQ0F3RUFBYU1qTUNFd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFGNHNSTThEVTZHK2lQNmFLMTJ1Znc2VjZNTjQKckthbnVlRlRnREQ1OExtRGpQZElGUkNHU2I5TnpwM1JjZmJ6SFhlTnZ3REpLdStaeU5SRUlub0tjREtZYmtUaApQRkw2NmcvdnRJakFFcWJNbW0zZmRIeFpnY1FNWmtsbHpycEVVaHdiMGpEWnowOHZoVklWbUMvbHRzbVdDMWE2Cm43VW1Sa3l3WDdOWXYrQjczcXBsNFBzdU8vRnoxaVd6RU9WdG9IcTY1OS8zOTBpZ2RzYkw2dzd3UzJEZkRGcGYKOUw5U3RiZDJYSzNYRXJQZ0FSQTVvM1V0VXNhWThTaFRWQ3o3ZDF1cDNFME1KS0JFRzlEQURXVmUrbnFlMEE1eQo0cVBVNGF6MVdOMU53b3RHOFFXWkxtd2V0WkVHQ3dlNFdzSjhRQ1YvTjMwcU9iRE5VWVNhN3lSOUZwQT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=";

        // cluster name.
        String clusterName = "cluster.local";

        return getKubeconfig(masterUrl, clusterCertData, clientCertData, clientKeyData, clusterName, namespace, user);
    }

    private KubernetesClient getUserClient(String clientCertData, String clientKeyData) {
        // master url.
        String masterUrl = "https://10.0.4.5:6443";

        // cluster cert data: certificate-authority-data
        String clusterCertData = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUN5RENDQWJDZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJd01EY3lOREEzTWpJek5sb1hEVE13TURjeU1qQTNNakl6Tmxvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBUDRsCk40cGJNOU10SkZaUzVsUXJsZmEyd1o0THI3Z3kzZ1hKeU8vdW9oNGphODY3VUlJNG8veXRWNG5VTFRueFU1QjQKbUJPTGZleXpBMUt2WkdPcVBBbDROdnBBQ2sxK3BBV0ViRXdlRVJYY1RkVk96VmRzQXQ2TFVCN2h5SUhoY1Q4RApwaThNdzBTU2tEUmVLdHNjMzZOUng0TTZGT3pXVTg1amRZVFF1OXhqQ2NOTkFDakdkaC9aUm9pNk5RanFWUGJUCjg2NG9UN1hYK2c4QU1uMXR6blY1bkt5SUhuUk5pT1ZEZ2p2Yy9WdVQ3SjlUMDI1UUx4NVBIVVYwa0xrOWdwQnUKek9TWmdjZlVDNzVQSzAzSzIzeVcyTk5aeG1Sc2tDbXpJNitVOWVneDBLQkpCbExlcCtDZ0s3YlZRRERwR2N1aApBdDFBODRpOUluN3FMZ01zc2FVQ0F3RUFBYU1qTUNFd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFGNHNSTThEVTZHK2lQNmFLMTJ1Znc2VjZNTjQKckthbnVlRlRnREQ1OExtRGpQZElGUkNHU2I5TnpwM1JjZmJ6SFhlTnZ3REpLdStaeU5SRUlub0tjREtZYmtUaApQRkw2NmcvdnRJakFFcWJNbW0zZmRIeFpnY1FNWmtsbHpycEVVaHdiMGpEWnowOHZoVklWbUMvbHRzbVdDMWE2Cm43VW1Sa3l3WDdOWXYrQjczcXBsNFBzdU8vRnoxaVd6RU9WdG9IcTY1OS8zOTBpZ2RzYkw2dzd3UzJEZkRGcGYKOUw5U3RiZDJYSzNYRXJQZ0FSQTVvM1V0VXNhWThTaFRWQ3o3ZDF1cDNFME1KS0JFRzlEQURXVmUrbnFlMEE1eQo0cVBVNGF6MVdOMU53b3RHOFFXWkxtd2V0WkVHQ3dlNFdzSjhRQ1YvTjMwcU9iRE5VWVNhN3lSOUZwQT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=";

        return KubernetesClientManager.newClient(masterUrl, clusterCertData, clientCertData, clientKeyData);
    }

    private KubernetesClient getClientWithClusterAdminUser() {
        // master url.
        String masterUrl = "https://10.0.4.5:6443";

        // cluster cert data: certificate-authority-data
        String clusterCertData = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUN5RENDQWJDZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJd01EY3lOREEzTWpJek5sb1hEVE13TURjeU1qQTNNakl6Tmxvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBUDRsCk40cGJNOU10SkZaUzVsUXJsZmEyd1o0THI3Z3kzZ1hKeU8vdW9oNGphODY3VUlJNG8veXRWNG5VTFRueFU1QjQKbUJPTGZleXpBMUt2WkdPcVBBbDROdnBBQ2sxK3BBV0ViRXdlRVJYY1RkVk96VmRzQXQ2TFVCN2h5SUhoY1Q4RApwaThNdzBTU2tEUmVLdHNjMzZOUng0TTZGT3pXVTg1amRZVFF1OXhqQ2NOTkFDakdkaC9aUm9pNk5RanFWUGJUCjg2NG9UN1hYK2c4QU1uMXR6blY1bkt5SUhuUk5pT1ZEZ2p2Yy9WdVQ3SjlUMDI1UUx4NVBIVVYwa0xrOWdwQnUKek9TWmdjZlVDNzVQSzAzSzIzeVcyTk5aeG1Sc2tDbXpJNitVOWVneDBLQkpCbExlcCtDZ0s3YlZRRERwR2N1aApBdDFBODRpOUluN3FMZ01zc2FVQ0F3RUFBYU1qTUNFd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFGNHNSTThEVTZHK2lQNmFLMTJ1Znc2VjZNTjQKckthbnVlRlRnREQ1OExtRGpQZElGUkNHU2I5TnpwM1JjZmJ6SFhlTnZ3REpLdStaeU5SRUlub0tjREtZYmtUaApQRkw2NmcvdnRJakFFcWJNbW0zZmRIeFpnY1FNWmtsbHpycEVVaHdiMGpEWnowOHZoVklWbUMvbHRzbVdDMWE2Cm43VW1Sa3l3WDdOWXYrQjczcXBsNFBzdU8vRnoxaVd6RU9WdG9IcTY1OS8zOTBpZ2RzYkw2dzd3UzJEZkRGcGYKOUw5U3RiZDJYSzNYRXJQZ0FSQTVvM1V0VXNhWThTaFRWQ3o3ZDF1cDNFME1KS0JFRzlEQURXVmUrbnFlMEE1eQo0cVBVNGF6MVdOMU53b3RHOFFXWkxtd2V0WkVHQ3dlNFdzSjhRQ1YvTjMwcU9iRE5VWVNhN3lSOUZwQT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=";

        // client key and cert data: user 'kubernetes-admin'.
        String clientCertData = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM4akNDQWRxZ0F3SUJBZ0lJRXh1T0FiRlZGR0l3RFFZSktvWklodmNOQVFFTEJRQXdGVEVUTUJFR0ExVUUKQXhNS2EzVmlaWEp1WlhSbGN6QWVGdzB5TURBM01qUXdOekl5TXpaYUZ3MHlNVEEzTWpRd056SXpOVGRhTURReApGekFWQmdOVkJBb1REbk41YzNSbGJUcHRZWE4wWlhKek1Sa3dGd1lEVlFRREV4QnJkV0psY201bGRHVnpMV0ZrCmJXbHVNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXphWkdHVmZDRGgxa00zTU4KNDVPenNqSzM5RWZQcDlERVFzNGNHMTA3TWZCYUZJRkpQOVpoVlVRM1lQSmlGaFdJM1NSR2RtSVVpeXdRcUR2SAorZUJmeUZoeisya2hMOERubVN3YURheUFhSStGSmZKbGRWc0xJYVN5QkVPYUxPUklSQlhYb3ByNjRxaWRnVG1NCjEwYVVMUjZnNTlITFRnMkVFQ3pvVEVPZGJkZDJNYUw5Y256NDM2SWc5THFrcDYvcVJtWGpRQzNML3ZYenFBcDEKcVBpc1N4V2dEM0ptYXZPTUR4cmE1cFNOSDZvVFRWaEdabEpSRDQ4TE1ycGdGenRpSk0xQi9kU1BLVWNsc2ZFaQptQkhvUW41VnZqNnRraGdVWG02YzFFVnVwSDIxQjVsdWg1WndZWFVBZjNSVHBiOE9VMllPaVJwMFNVZVNFNmFxCjJDNmt2d0lEQVFBQm95Y3dKVEFPQmdOVkhROEJBZjhFQkFNQ0JhQXdFd1lEVlIwbEJBd3dDZ1lJS3dZQkJRVUgKQXdJd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFJVEQ2cFNnUkhZMllXajUzcTFsZkxra3cxRGxBNzl6djh3SgpzUm9PekExc1hER0NzSlpMWHh1TnVkWVlubEwvZVlxQldoVHZYYjN5djlhTWxUaTN1S0ZHSExuZS82bWpKNy9iClVOQXhWV3QzK0dNMmRtU0F5YWN5OS95VzVvRDFMNy9nM3lkQnl3b3l3TzBLcHNUemlXVksrWDdOTmgwMWN3WHAKdlo0OUI0Q3NzSFlmYkw2aWxFZk1QeHg4eGd6RW5jcUhhREgyUnRoVks2cnJ4OWE1RDc0KzJmQk5jVXVHYU9BcQprTVZ4UnVKREd5YVQ3L2ZXY0tDWDdJVlZwY1ovY2d4QXRlQWZyNzljMkErekk3djd1SlpqOU5XcHdGY21KdlRMCkw2ZkhmTjZOTU5BUno1Um9UV1ROT3ZQMmo0dWhKbitERzlzZUsyVG93ZlF6Z1gwRUozZz0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=";
        String clientKeyData = "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcGdJQkFBS0NBUUVBemFaR0dWZkNEaDFrTTNNTjQ1T3pzakszOUVmUHA5REVRczRjRzEwN01mQmFGSUZKClA5WmhWVVEzWVBKaUZoV0kzU1JHZG1JVWl5d1FxRHZIK2VCZnlGaHorMmtoTDhEbm1Td2FEYXlBYUkrRkpmSmwKZFZzTElhU3lCRU9hTE9SSVJCWFhvcHI2NHFpZGdUbU0xMGFVTFI2ZzU5SExUZzJFRUN6b1RFT2RiZGQyTWFMOQpjbno0MzZJZzlMcWtwNi9xUm1YalFDM0wvdlh6cUFwMXFQaXNTeFdnRDNKbWF2T01EeHJhNXBTTkg2b1RUVmhHClpsSlJENDhMTXJwZ0Z6dGlKTTFCL2RTUEtVY2xzZkVpbUJIb1FuNVZ2ajZ0a2hnVVhtNmMxRVZ1cEgyMUI1bHUKaDVad1lYVUFmM1JUcGI4T1UyWU9pUnAwU1VlU0U2YXEyQzZrdndJREFRQUJBb0lCQVFDc0VIMENZMHo0WmxzYQpPUi9iMUE4Ny8vZXVLZzl5eDlnT1ZVbHJFOXlkY2c3TXJVZk9ZOTdZYXRVekJscFBSQUZabGlwbWpyWEZwRDdqCm8vRXoveW5sQlMwMW84YWluL0tuVkNFUVk4NmtyY0NuK1dJdWlOaU5jN0dHbzlGeDBpY3M0R0dscWFwVXp1UEoKNVk4VDUvZStzS3ZNaXRwaUdpanZKOFBOVzUxeThwM2d6cWh2Mm5YM0NNb0Q4NnNBei9vbkVmbkR6UG8xZHo0SApSOW1ENTF2bEN2TDM3VDlaTGF3bFkweVVFWHBmMzZhSzVVLzlQdXpKc0toY3pMRTYyS0xlRjBoY3dZTTZpYlo4CnJJNExXdUU3dFZITWpTMG45OVo0WWxFaFczVGxPLzBmaWp1MmJBZE5lK1htL09EUGhLaGw3Z0sxak9rM3R5L3QKSW9jMEZ0anhBb0dCQU9yR09mTnIwZU5MaUhsR3NwckRac2VQd1lJS1kxbVRZTzU1WlFlWCtlZmZIa0ZybXdScAoyM2lIVFlCdjRVZ2lwVC93Q0tRSVdBWFF1d25OQWZFaTN5aXVqZDhBQlVpdExDMnJTTWlkMzlHQURoa3JVQ3J4Ck5BOWZkcXBNMFZPQXhmSFU5R29LcUNwV3hSL0Q2TnJFMlJaYU5jU1o4S3l5SHNXUUIrVWpNek5OQW9HQkFPQTkKOXBybW1aUG5veVExSWtXY1ZtRjBmS2V5djREdHBMOGZTWGFqbjZaQVNydVpDWC9WdWh2d081bEU1UjUrYWliYgpHQm55YXRyR2JUWVA1WXFwRGtjN0lmUVhyS3ZsaS9rYmVaR0JxblB6dkpTWTFtNUpXOFNhNkJxekY5eW11WmRICkNrc0hMZnVzTXBNVVoyd0NpSjF2dTVSSityS1J3RTZncGMwNjNCbzdBb0dCQUpIa3VTSnh3QzUxUTh2SmlUZnYKY2JGVnZqU25hL0hBU2g0bnhnYWdCS1o0Mm41ZzlpWVorazYrRmdWWWdUQ29odlJpbjV2L3EyT0J3Smxva21wYwo5eng2cktNRmlrTU5pa1NmQ2szUS9jTmN4bVlScW5IbERpcjNjZkNHYUJaeUVaQWtlL1poeFByNmpPa2VmMWRqCnVGdlJsMVFqTFpMRDVhZHp4OVEydVp5SkFvR0JBSWlpbU9ubnl2cUpjU20xeW8wNTVwUjVReDkzMWlKOEt4OWQKdEFpN1NLTW5sNkhaYlNWY21Jcy9oVUV0N3FIM0N6MWowTHEyc0k5Znl0bmZNOUdha3gycUZWVkRPNjUrTHh6NQp0Y2lJaHRFaVdlejlkK001aGRZMVFXcExhQ1hGM1Y0bEprdHpNM3lmZnkySmlEOFRDQ1ZPR0xFUnB0VTU1RURFCnVHSm1GQWxUQW9HQkFLL25CdVNQaENoRjlhMWt1U0xpdnhCRzMvVmxReHhLWmd0Rjd2aVNOR0U4clc3RDU4MU8KY2NxV3FZY1RDOENxRnlqZHNmcU85a21XQjZpM1gvbFQzZ0Z0NlBNa09pM0ZkQ1BzU3B1Ylpwd0VWcmwyM3REcgpUUFI3ODBtbUdZWHpMcDFZbm9HN3QwQXZOMUNtT0x6blJ6VGRMd3FlWVNVZ0FGYVQvaHRsM1c1TAotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=";

        return KubernetesClientManager.newClient(masterUrl, clusterCertData, clientCertData, clientKeyData);
    }


    private String getClusterAdminKubeconfig() {
        // master url.
        String masterUrl = "https://10.0.4.5:6443";

        // cluster cert data: certificate-authority-data
        String clusterCertData = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUN5RENDQWJDZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJd01EY3lOREEzTWpJek5sb1hEVE13TURjeU1qQTNNakl6Tmxvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBUDRsCk40cGJNOU10SkZaUzVsUXJsZmEyd1o0THI3Z3kzZ1hKeU8vdW9oNGphODY3VUlJNG8veXRWNG5VTFRueFU1QjQKbUJPTGZleXpBMUt2WkdPcVBBbDROdnBBQ2sxK3BBV0ViRXdlRVJYY1RkVk96VmRzQXQ2TFVCN2h5SUhoY1Q4RApwaThNdzBTU2tEUmVLdHNjMzZOUng0TTZGT3pXVTg1amRZVFF1OXhqQ2NOTkFDakdkaC9aUm9pNk5RanFWUGJUCjg2NG9UN1hYK2c4QU1uMXR6blY1bkt5SUhuUk5pT1ZEZ2p2Yy9WdVQ3SjlUMDI1UUx4NVBIVVYwa0xrOWdwQnUKek9TWmdjZlVDNzVQSzAzSzIzeVcyTk5aeG1Sc2tDbXpJNitVOWVneDBLQkpCbExlcCtDZ0s3YlZRRERwR2N1aApBdDFBODRpOUluN3FMZ01zc2FVQ0F3RUFBYU1qTUNFd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFGNHNSTThEVTZHK2lQNmFLMTJ1Znc2VjZNTjQKckthbnVlRlRnREQ1OExtRGpQZElGUkNHU2I5TnpwM1JjZmJ6SFhlTnZ3REpLdStaeU5SRUlub0tjREtZYmtUaApQRkw2NmcvdnRJakFFcWJNbW0zZmRIeFpnY1FNWmtsbHpycEVVaHdiMGpEWnowOHZoVklWbUMvbHRzbVdDMWE2Cm43VW1Sa3l3WDdOWXYrQjczcXBsNFBzdU8vRnoxaVd6RU9WdG9IcTY1OS8zOTBpZ2RzYkw2dzd3UzJEZkRGcGYKOUw5U3RiZDJYSzNYRXJQZ0FSQTVvM1V0VXNhWThTaFRWQ3o3ZDF1cDNFME1KS0JFRzlEQURXVmUrbnFlMEE1eQo0cVBVNGF6MVdOMU53b3RHOFFXWkxtd2V0WkVHQ3dlNFdzSjhRQ1YvTjMwcU9iRE5VWVNhN3lSOUZwQT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=";

        // client key and cert data: user 'kubernetes-admin'.
        String clientCertData = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM4akNDQWRxZ0F3SUJBZ0lJRXh1T0FiRlZGR0l3RFFZSktvWklodmNOQVFFTEJRQXdGVEVUTUJFR0ExVUUKQXhNS2EzVmlaWEp1WlhSbGN6QWVGdzB5TURBM01qUXdOekl5TXpaYUZ3MHlNVEEzTWpRd056SXpOVGRhTURReApGekFWQmdOVkJBb1REbk41YzNSbGJUcHRZWE4wWlhKek1Sa3dGd1lEVlFRREV4QnJkV0psY201bGRHVnpMV0ZrCmJXbHVNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXphWkdHVmZDRGgxa00zTU4KNDVPenNqSzM5RWZQcDlERVFzNGNHMTA3TWZCYUZJRkpQOVpoVlVRM1lQSmlGaFdJM1NSR2RtSVVpeXdRcUR2SAorZUJmeUZoeisya2hMOERubVN3YURheUFhSStGSmZKbGRWc0xJYVN5QkVPYUxPUklSQlhYb3ByNjRxaWRnVG1NCjEwYVVMUjZnNTlITFRnMkVFQ3pvVEVPZGJkZDJNYUw5Y256NDM2SWc5THFrcDYvcVJtWGpRQzNML3ZYenFBcDEKcVBpc1N4V2dEM0ptYXZPTUR4cmE1cFNOSDZvVFRWaEdabEpSRDQ4TE1ycGdGenRpSk0xQi9kU1BLVWNsc2ZFaQptQkhvUW41VnZqNnRraGdVWG02YzFFVnVwSDIxQjVsdWg1WndZWFVBZjNSVHBiOE9VMllPaVJwMFNVZVNFNmFxCjJDNmt2d0lEQVFBQm95Y3dKVEFPQmdOVkhROEJBZjhFQkFNQ0JhQXdFd1lEVlIwbEJBd3dDZ1lJS3dZQkJRVUgKQXdJd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFJVEQ2cFNnUkhZMllXajUzcTFsZkxra3cxRGxBNzl6djh3SgpzUm9PekExc1hER0NzSlpMWHh1TnVkWVlubEwvZVlxQldoVHZYYjN5djlhTWxUaTN1S0ZHSExuZS82bWpKNy9iClVOQXhWV3QzK0dNMmRtU0F5YWN5OS95VzVvRDFMNy9nM3lkQnl3b3l3TzBLcHNUemlXVksrWDdOTmgwMWN3WHAKdlo0OUI0Q3NzSFlmYkw2aWxFZk1QeHg4eGd6RW5jcUhhREgyUnRoVks2cnJ4OWE1RDc0KzJmQk5jVXVHYU9BcQprTVZ4UnVKREd5YVQ3L2ZXY0tDWDdJVlZwY1ovY2d4QXRlQWZyNzljMkErekk3djd1SlpqOU5XcHdGY21KdlRMCkw2ZkhmTjZOTU5BUno1Um9UV1ROT3ZQMmo0dWhKbitERzlzZUsyVG93ZlF6Z1gwRUozZz0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=";
        String clientKeyData = "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcGdJQkFBS0NBUUVBemFaR0dWZkNEaDFrTTNNTjQ1T3pzakszOUVmUHA5REVRczRjRzEwN01mQmFGSUZKClA5WmhWVVEzWVBKaUZoV0kzU1JHZG1JVWl5d1FxRHZIK2VCZnlGaHorMmtoTDhEbm1Td2FEYXlBYUkrRkpmSmwKZFZzTElhU3lCRU9hTE9SSVJCWFhvcHI2NHFpZGdUbU0xMGFVTFI2ZzU5SExUZzJFRUN6b1RFT2RiZGQyTWFMOQpjbno0MzZJZzlMcWtwNi9xUm1YalFDM0wvdlh6cUFwMXFQaXNTeFdnRDNKbWF2T01EeHJhNXBTTkg2b1RUVmhHClpsSlJENDhMTXJwZ0Z6dGlKTTFCL2RTUEtVY2xzZkVpbUJIb1FuNVZ2ajZ0a2hnVVhtNmMxRVZ1cEgyMUI1bHUKaDVad1lYVUFmM1JUcGI4T1UyWU9pUnAwU1VlU0U2YXEyQzZrdndJREFRQUJBb0lCQVFDc0VIMENZMHo0WmxzYQpPUi9iMUE4Ny8vZXVLZzl5eDlnT1ZVbHJFOXlkY2c3TXJVZk9ZOTdZYXRVekJscFBSQUZabGlwbWpyWEZwRDdqCm8vRXoveW5sQlMwMW84YWluL0tuVkNFUVk4NmtyY0NuK1dJdWlOaU5jN0dHbzlGeDBpY3M0R0dscWFwVXp1UEoKNVk4VDUvZStzS3ZNaXRwaUdpanZKOFBOVzUxeThwM2d6cWh2Mm5YM0NNb0Q4NnNBei9vbkVmbkR6UG8xZHo0SApSOW1ENTF2bEN2TDM3VDlaTGF3bFkweVVFWHBmMzZhSzVVLzlQdXpKc0toY3pMRTYyS0xlRjBoY3dZTTZpYlo4CnJJNExXdUU3dFZITWpTMG45OVo0WWxFaFczVGxPLzBmaWp1MmJBZE5lK1htL09EUGhLaGw3Z0sxak9rM3R5L3QKSW9jMEZ0anhBb0dCQU9yR09mTnIwZU5MaUhsR3NwckRac2VQd1lJS1kxbVRZTzU1WlFlWCtlZmZIa0ZybXdScAoyM2lIVFlCdjRVZ2lwVC93Q0tRSVdBWFF1d25OQWZFaTN5aXVqZDhBQlVpdExDMnJTTWlkMzlHQURoa3JVQ3J4Ck5BOWZkcXBNMFZPQXhmSFU5R29LcUNwV3hSL0Q2TnJFMlJaYU5jU1o4S3l5SHNXUUIrVWpNek5OQW9HQkFPQTkKOXBybW1aUG5veVExSWtXY1ZtRjBmS2V5djREdHBMOGZTWGFqbjZaQVNydVpDWC9WdWh2d081bEU1UjUrYWliYgpHQm55YXRyR2JUWVA1WXFwRGtjN0lmUVhyS3ZsaS9rYmVaR0JxblB6dkpTWTFtNUpXOFNhNkJxekY5eW11WmRICkNrc0hMZnVzTXBNVVoyd0NpSjF2dTVSSityS1J3RTZncGMwNjNCbzdBb0dCQUpIa3VTSnh3QzUxUTh2SmlUZnYKY2JGVnZqU25hL0hBU2g0bnhnYWdCS1o0Mm41ZzlpWVorazYrRmdWWWdUQ29odlJpbjV2L3EyT0J3Smxva21wYwo5eng2cktNRmlrTU5pa1NmQ2szUS9jTmN4bVlScW5IbERpcjNjZkNHYUJaeUVaQWtlL1poeFByNmpPa2VmMWRqCnVGdlJsMVFqTFpMRDVhZHp4OVEydVp5SkFvR0JBSWlpbU9ubnl2cUpjU20xeW8wNTVwUjVReDkzMWlKOEt4OWQKdEFpN1NLTW5sNkhaYlNWY21Jcy9oVUV0N3FIM0N6MWowTHEyc0k5Znl0bmZNOUdha3gycUZWVkRPNjUrTHh6NQp0Y2lJaHRFaVdlejlkK001aGRZMVFXcExhQ1hGM1Y0bEprdHpNM3lmZnkySmlEOFRDQ1ZPR0xFUnB0VTU1RURFCnVHSm1GQWxUQW9HQkFLL25CdVNQaENoRjlhMWt1U0xpdnhCRzMvVmxReHhLWmd0Rjd2aVNOR0U4clc3RDU4MU8KY2NxV3FZY1RDOENxRnlqZHNmcU85a21XQjZpM1gvbFQzZ0Z0NlBNa09pM0ZkQ1BzU3B1Ylpwd0VWcmwyM3REcgpUUFI3ODBtbUdZWHpMcDFZbm9HN3QwQXZOMUNtT0x6blJ6VGRMd3FlWVNVZ0FGYVQvaHRsM1c1TAotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=";

        // cluster name.
        String clusterName = "cluster.local";

        // user.
        String user = "kubernetes-admin";

        // namespace.
        String namespace = "default";

        return getKubeconfig(masterUrl, clusterCertData, clientCertData, clientKeyData, clusterName, namespace, user);
    }

    private String getKubeconfig(String masterUrl,
                                 String clusterCertData,
                                 String clientCertData,
                                 String clientKeyData,
                                 String clusterName,
                                 String namespace,
                                 String user)  {

        String kubeconfigFileName = "config";
        Map<String, String> kv = new HashMap<>();
        kv.put("masterUrl", masterUrl);
        kv.put("clusterCertData", clusterCertData);
        kv.put("clientCertData", clientCertData);
        kv.put("clientKeyData", clientKeyData);
        kv.put("clusterName", clusterName);
        kv.put("namespace", namespace);
        kv.put("user", user);

        String kubeconfigString = TemplateUtils.replace("/templates/kubeconfig/" + kubeconfigFileName, true, kv);

        return kubeconfigString;
    }



    @Test
    public void createResourceWithExternal() throws Exception {
        KubernetesClient adminClient = getClientWithClusterAdminUser();

        String createNsYamlString = FileUtils.fileToString("/manifests/create-ns.yaml", true);

        adminClient.load(new ByteArrayInputStream(createNsYamlString.getBytes())).createOrReplace();
    }
}
