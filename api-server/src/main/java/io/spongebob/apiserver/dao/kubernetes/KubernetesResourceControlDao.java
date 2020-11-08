package io.spongebob.apiserver.dao.kubernetes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.discovery.Endpoint;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.spongebob.apiserver.api.dao.ResourceControlDao;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.StorageClass;
import io.spongebob.apiserver.kubernetes.client.KubernetesClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class KubernetesResourceControlDao implements ResourceControlDao {

    private static Logger LOG = LoggerFactory.getLogger(KubernetesResourceControlDao.class);

    @Override
    public void createNamespace(Kubeconfig kubeconfig, String namespace) {
        try {
            KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
            // create namespace.
            Namespace newNs = adminClient.namespaces().createOrReplaceWithNew()
                    .withNewMetadata()
                    .withName(namespace)
                    .addToLabels("name", namespace)
                    .endMetadata()
                    .done();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteNamespace(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        adminClient.namespaces().list().getItems().forEach(ns -> {
            String namespaceName = ns.getMetadata().getName();
            LOG.debug("namespaceName: {}", namespaceName);
            if(namespaceName.equals(namespace)) {
                adminClient.namespaces().delete(ns);
                LOG.info("namespace [{}] removed...", namespace);
            }
        });
    }

    @Override
    public String getTenantName(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        String tenantName = null;
        List<CustomResourceDefinition> customResourceDefinitionList = adminClient.customResourceDefinitions().list().getItems();
        for(CustomResourceDefinition customResourceDefinition : customResourceDefinitionList) {
            if(customResourceDefinition.getMetadata().getName().equals("tenants.minio.min.io")) {
                CustomResourceDefinitionContext customResourceDefinitionContext = CustomResourceDefinitionContext.fromCrd(customResourceDefinition);
                Map<String, Object> cr = adminClient.customResource(customResourceDefinitionContext).list(namespace);
                List<Map<String, Object>> items = (List<Map<String, Object>>) cr.get("items");
                if(items.size() > 0) {
                    Map<String, Object> firstItem = items.get(0);
                    tenantName= (String) ((Map<String, Object>)firstItem.get("metadata")).get("name");
                    break;
                }
            }
        }

        return tenantName;
    }

    @Override
    public List<StorageClass> listStorageClasses(Kubeconfig kubeconfig) {
        List<StorageClass> storageClasses = new ArrayList<>();

        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<io.fabric8.kubernetes.api.model.storage.StorageClass> storageClassList = adminClient.storage().storageClasses().list().getItems();
        for(io.fabric8.kubernetes.api.model.storage.StorageClass storageClass : storageClassList) {
            String name = storageClass.getMetadata().getName();
            String provisioner = storageClass.getProvisioner();
            String reclaimPolicy = storageClass.getReclaimPolicy();
            String volumeBindingMode = storageClass.getVolumeBindingMode();

            storageClasses.add(new StorageClass(name, provisioner, reclaimPolicy, volumeBindingMode));
        }

        return storageClasses;
    }

    @Override
    public int getNginxPort(Kubeconfig kubeconfig, String namespace) {
        int port = -1;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<Service> services = adminClient.services().inNamespace(namespace).list().getItems();
        for(Service service : services) {
            String serviceName = service.getMetadata().getName();
            if(serviceName.equals("ingress-nginx-controller")) {
                for(ServicePort servicePort : service.getSpec().getPorts()) {
                    if(servicePort.getName().equals("http")) {
                        port = servicePort.getPort();
                        break;
                    }
                }

                break;
            }
        }

        return port;
    }

    @Override
    public String getExternalIPForMetalLB(Kubeconfig kubeconfig) {
        String externalIP = null;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        boolean selected = false;
        for(Node node : adminClient.nodes().list().getItems()) {
            for(NodeAddress nodeAddress : node.getStatus().getAddresses()) {
                if(nodeAddress.getType().equals("ExternalIP")) {
                    String ip = nodeAddress.getAddress();
                    LOG.info("ip: {}", ip);

                    String[] tokens = ip.split("\\.");
                    int lastNum = Integer.valueOf(tokens[tokens.length - 1]);
                    if(lastNum < 240) {
                        externalIP = ip;
                        selected = true;
                        break;
                    }
                }
            }
            if(selected) {
                break;
            }
        }

        return externalIP;
    }

    @Override
    public void deleteMinIOIngress(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        Ingress selectedIngress = null;
        for(Ingress ingress : adminClient.extensions().ingresses().inNamespace(namespace).list().getItems()) {
            String ingressName = ingress.getMetadata().getName();
            if(ingressName.equals("ingress-minio-" + namespace)) {
                selectedIngress = ingress;
                break;
            }
        }

        adminClient.extensions().ingresses().inNamespace(namespace).delete(selectedIngress);
    }

    @Override
    public List<Ingress> getIngresses(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);

        return adminClient.extensions().ingresses().inNamespace(namespace).list().getItems();
    }

    @Override
    public Secret getSecret(Kubeconfig kubeconfig, String namespace) {
        String tenantName = getTenantName(kubeconfig, namespace);
        Secret selectedSecret = null;
        if(tenantName != null) {
            KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
            List<Secret> secrets = adminClient.secrets().inNamespace(namespace).list().getItems();
            for(Secret secret : secrets) {
                if(secret.getMetadata().getName().startsWith(tenantName)) {
                    selectedSecret = secret;
                    break;
                }
            }
        }
        return selectedSecret;
    }

    @Override
    public boolean existsMinIOStorageClass(Kubeconfig kubeconfig) {
        boolean exists = false;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<io.fabric8.kubernetes.api.model.storage.StorageClass> storageClassList = adminClient.storage().storageClasses().list().getItems();
        for(io.fabric8.kubernetes.api.model.storage.StorageClass storageClass : storageClassList) {
            String name = storageClass.getMetadata().getName();
            if(name.equals("direct.csi.min.io")) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    @Override
    public boolean existsNfsStorageClass(Kubeconfig kubeconfig) {
        boolean exists = false;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<io.fabric8.kubernetes.api.model.storage.StorageClass> storageClassList = adminClient.storage().storageClasses().list().getItems();
        for(io.fabric8.kubernetes.api.model.storage.StorageClass storageClass : storageClassList) {
            String name = storageClass.getMetadata().getName();
            if(name.equals("nfs")) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    @Override
    public boolean existsTenant(Kubeconfig kubeconfig, String namespace) {
        boolean exists = false;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<CustomResourceDefinition> customResourceDefinitionList = adminClient.customResourceDefinitions().list().getItems();
        for(CustomResourceDefinition customResourceDefinition : customResourceDefinitionList) {
            if(customResourceDefinition.getMetadata().getName().equals("tenants.minio.min.io")) {
                CustomResourceDefinitionContext customResourceDefinitionContext = CustomResourceDefinitionContext.fromCrd(customResourceDefinition);
                Map<String, Object> cr = adminClient.customResource(customResourceDefinitionContext).list(namespace);
                List<Map<String, Object>> items = (List<Map<String, Object>>) cr.get("items");
                if(items.size() > 0) {
                    exists = true;
                    break;
                }
            }
        }

        return exists;
    }

    @Override
    public String getMinIOS3Endpoint(Kubeconfig kubeconfig, String namespace) {
        String endpoint = null;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<Service> services = adminClient.services().inNamespace(namespace).list().getItems();
        for(Service service : services) {
            String tenantName = getTenantName(kubeconfig, namespace);
            String serviceName = tenantName + "-internal-service";
            if(service.getMetadata().getName().equals(serviceName)) {
                int port = service.getSpec().getPorts().get(0).getPort();
                endpoint = "https://" + serviceName + ":" + port;
                break;
            }
        }

        return endpoint;
    }

    @Override
    public List<PersistentVolumeClaim> listPvcUsingStorageClass(Kubeconfig kubeconfig, String storageClass) {
        List<PersistentVolumeClaim> selectedResources = new ArrayList<>();
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<PersistentVolumeClaim> persistentVolumeClaims = adminClient.persistentVolumeClaims().list().getItems();
        for(PersistentVolumeClaim persistentVolumeClaim : persistentVolumeClaims) {
            if(persistentVolumeClaim.getSpec().getStorageClassName().equals(storageClass)) {
                selectedResources.add(persistentVolumeClaim);
            }
        }

        return selectedResources;
    }

    @Override
    public List<Pod> listPods(Kubeconfig kubeconfig, String namespace) {
        List<Pod> selectedResources = new ArrayList<>();
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<Pod> pods = adminClient.pods().inNamespace(namespace).list().getItems();
        for(Pod pod : pods) {
            if(pod.getStatus().getPhase().equals("Running")) {
                selectedResources.add(pod);
            }
        }

        return selectedResources;
    }

    @Override
    public boolean existsOzone(Kubeconfig kubeconfig, String namespace) {
        boolean exists = false;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<Pod> pods = adminClient.pods().inNamespace(namespace).list().getItems();
        for(Pod pod : pods) {
            if(pod.getMetadata().getName().equals("s3g-0")) {
                if (pod.getStatus().getPhase().equals("Running")) {
                    exists = true;
                    break;
                }
            }
        }

        return exists;
    }

    @Override
    public void deleteOzoneIngress(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        Ingress selectedIngress = null;
        for(Ingress ingress : adminClient.extensions().ingresses().inNamespace(namespace).list().getItems()) {
            String ingressName = ingress.getMetadata().getName();
            if(ingressName.equals("ingress-ozone-" + namespace)) {
                selectedIngress = ingress;
                break;
            }
        }

        adminClient.extensions().ingresses().inNamespace(namespace).delete(selectedIngress);
    }

    @Override
    public String getMinIOIngressHost(Kubeconfig kubeconfig, String namespace) {
        String host = null;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        Ingress selectedIngress = null;
        for(Ingress ingress : adminClient.extensions().ingresses().inNamespace(namespace).list().getItems()) {
            String ingressName = ingress.getMetadata().getName();
            if(ingressName.equals("ingress-minio-" + namespace)) {
                host = ingress.getSpec().getRules().get(0).getHost();
                break;
            }
        }

        return host;
    }

    @Override
    public boolean existsCertManager(Kubeconfig kubeconfig) {
        boolean exists = false;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<Pod> pods = adminClient.pods().inNamespace("cert-manager").list().getItems();
        for(Pod pod : pods) {
            if (pod.getStatus().getPhase().equals("Running")) {
                exists = true;
                break;
            }
        }

        return exists;
    }

    @Override
    public boolean existsIngressControllerNginx(Kubeconfig kubeconfig) {
        boolean exists = false;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<Pod> pods = adminClient.pods().inNamespace("ingress-nginx").list().getItems();
        for(Pod pod : pods) {
            if(pod.getMetadata().getName().startsWith("ingress-nginx-controller")) {
                if (pod.getStatus().getPhase().equals("Running")) {
                    exists = true;
                    break;
                }
            }
        }

        return exists;
    }

    @Override
    public String getOzoneS3Endpoint(Kubeconfig kubeconfig, String namespace) {
        String endpoint = null;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        for(Service service : adminClient.services().inNamespace(namespace).list().getItems()) {
            if(service.getMetadata().getName().equals("s3g-service")) {
                String externalIP = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
                int port = service.getSpec().getPorts().get(0).getPort();
                endpoint = externalIP + ":" + port;
            }
        }

        return endpoint;
    }

    @Override
    public void killSparkThriftServer(Kubeconfig kubeconfig, String namespace) {
        try {
            KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
            Pod selectedPod = null;
            for (Pod pod : adminClient.pods().inNamespace(namespace).list().getItems()) {
                String podName = pod.getMetadata().getName();
                Map<String, String> labels = pod.getMetadata().getLabels();
                if(labels != null) {
                    if (labels.containsKey("spark-role")) {
                        String value = labels.get("spark-role");
                        if (value.equals("driver")) {
                            if (podName.startsWith("spark-thrift-server")) {
                                selectedPod = pod;
                                LOG.info("selected pod: {}", selectedPod.getMetadata().getName());
                                break;
                            }
                        }
                    }
                }
            }

            // delete spark thrift server driver pod.
            adminClient.pods().inNamespace(namespace).delete(selectedPod);

            // delete service 'spark-thrift-server-service'.
            for (Service service : adminClient.services().inNamespace(namespace).list().getItems()) {
                if (service.getMetadata().getName().equals("spark-thrift-server-service")) {
                    adminClient.services().inNamespace(namespace).delete(service);
                    LOG.info("service: {} deleted", service.getMetadata().getName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsSparkServiceAccountPVC(Kubeconfig kubeconfig, String namespace) {
        boolean exists = false;

        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        for(ServiceAccount serviceAccount : adminClient.serviceAccounts().inNamespace(namespace).list().getItems()) {
            if(serviceAccount.getMetadata().getName().equals("spark")) {
                List<String> pvcNameList = new ArrayList<>();
                for(PersistentVolumeClaim persistentVolumeClaim : adminClient.persistentVolumeClaims().inNamespace(namespace).list().getItems()) {
                    String pvcName = persistentVolumeClaim.getMetadata().getName();
                    pvcNameList.add(pvcName);
                }

                List<String> sparkPvcList = Arrays.asList(
                        "spark-driver-localdir-pvc",
                        "spark-driver-pvc",
                        "spark-exec-localdir-pvc",
                        "spark-exec-pvc");
                boolean existsAll = true;
                for(String sparkPvc : sparkPvcList) {
                    if(!pvcNameList.contains(sparkPvc)) {
                        existsAll = false;
                        break;
                    }
                }
                if(existsAll) {
                    exists = true;
                    break;
                } else {
                    exists = false;
                    break;
                }
            }
        }

        return exists;
    }

    @Override
    public boolean existsHiveMetastore(Kubeconfig kubeconfig, String namespace) {
        boolean exists = false;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        for(Pod pod : adminClient.pods().inNamespace(namespace).list().getItems()) {
            Map<String, String> labels = pod.getMetadata().getLabels();
            if(labels.containsKey("app")) {
                String value = labels.get("app");
                if(value.equals("metastore")) {
                    if(pod.getStatus().getPhase().equals("Running")) {
                        exists = true;
                        break;
                    }
                }
            }
        }

        return exists;
    }

    @Override
    public String getElasticsearchClusterName(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        String esClusterName = null;
        List<CustomResourceDefinition> customResourceDefinitionList = adminClient.customResourceDefinitions().list().getItems();
        for(CustomResourceDefinition customResourceDefinition : customResourceDefinitionList) {
            if(customResourceDefinition.getMetadata().getName().equals("elasticsearches.elasticsearch.k8s.elastic.co")) {
                CustomResourceDefinitionContext customResourceDefinitionContext = CustomResourceDefinitionContext.fromCrd(customResourceDefinition);
                Map<String, Object> cr = adminClient.customResource(customResourceDefinitionContext).list(namespace);
                List<Map<String, Object>> items = (List<Map<String, Object>>) cr.get("items");
                if(items.size() > 0) {
                    Map<String, Object> firstItem = items.get(0);
                    esClusterName= (String) ((Map<String, Object>)firstItem.get("metadata")).get("name");
                    break;
                }
            }
        }

        return esClusterName;
    }

    @Override
    public String getHiveMetastoreExternalEndpoint(Kubeconfig kubeconfig, String namespace) {
        String endpoint = null;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        for(Service service : adminClient.services().inNamespace(namespace).list().getItems()) {
            if(service.getMetadata().getName().equals("metastore")) {
                String externalIP = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
                int port = service.getSpec().getPorts().get(0).getPort();
                endpoint = externalIP + ":" + port;
            }
        }

        return endpoint;
    }

    @Override
    public String getPrestoExternalEndpoint(Kubeconfig kubeconfig, String namespace) {
        String endpoint = null;
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        for(Service service : adminClient.services().inNamespace(namespace).list().getItems()) {
            if(service.getMetadata().getName().equals("presto")) {
                String externalIP = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
                int port = service.getSpec().getPorts().get(0).getPort();
                endpoint = externalIP + ":" + port;
            }
        }

        return endpoint;
    }

    @Override
    public List<Node> getNodes(Kubeconfig kubeconfig) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);

        return adminClient.nodes().list().getItems();
    }

    @Override
    public List<Service> getServices(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        return adminClient.services().inNamespace(namespace).list().getItems();
    }

    @Override
    public List<Endpoints> getEndpoints(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        return adminClient.endpoints().inNamespace(namespace).list().getItems();
    }

    @Override
    public List<Pod> getPods(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        return adminClient.pods().inNamespace(namespace).list().getItems();
    }

    @Override
    public List<Deployment> getDeployments(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        return adminClient.apps().deployments().inNamespace(namespace).list().getItems();
    }

    @Override
    public List<StatefulSet> getStatefulsets(Kubeconfig kubeconfig, String namespace) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        return adminClient.apps().statefulSets().inNamespace(namespace).list().getItems();
    }

    @Override
    public List<Namespace> getNamespaces(Kubeconfig kubeconfig) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        return adminClient.namespaces().list().getItems();
    }

    @Override
    public List<ServiceAccount> getSparkServiceAccount(Kubeconfig kubeconfig) {
        KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);
        List<ServiceAccount> serviceAccounts = new ArrayList<>();
        for(ServiceAccount serviceAccount : adminClient.serviceAccounts().list().getItems()) {
            if(serviceAccount.getMetadata().getName().equals("spark")) {
                serviceAccounts.add(serviceAccount);
            }
        }
        return serviceAccounts;
    }
}
