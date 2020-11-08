package io.spongebob.apiserver.api.dao;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.StorageClass;

import java.util.List;

public interface ResourceControlDao {
    void createNamespace(Kubeconfig kubeconfig, String namespace);
    void deleteNamespace(Kubeconfig kubeconfig, String namespace);
    String getTenantName(Kubeconfig kubeconfig, String namespace);
    List<StorageClass> listStorageClasses(Kubeconfig kubeconfig);
    int getNginxPort(Kubeconfig kubeconfig, String namespace);
    String getExternalIPForMetalLB(Kubeconfig kubeconfig);
    void deleteMinIOIngress(Kubeconfig kubeconfig, String namespace);
    List<Ingress> getIngresses(Kubeconfig kubeconfig, String namespace);
    Secret getSecret(Kubeconfig kubeconfig, String namespace);
    boolean existsMinIOStorageClass(Kubeconfig kubeconfig);
    boolean existsNfsStorageClass(Kubeconfig kubeconfig);
    boolean existsTenant(Kubeconfig kubeconfig, String namespace);
    String getMinIOS3Endpoint(Kubeconfig kubeconfig, String namespace);
    List<PersistentVolumeClaim> listPvcUsingStorageClass(Kubeconfig kubeconfig, String storageClass);
    List<Pod> listPods(Kubeconfig kubeconfig, String namespace);
    boolean existsOzone(Kubeconfig kubeconfig, String namespace);
    void deleteOzoneIngress(Kubeconfig kubeconfig, String namespace);
    String getMinIOIngressHost(Kubeconfig kubeconfig, String namespace);
    boolean existsCertManager(Kubeconfig kubeconfig);
    boolean existsIngressControllerNginx(Kubeconfig kubeconfig);
    String getOzoneS3Endpoint(Kubeconfig kubeconfig, String namespace);
    void killSparkThriftServer(Kubeconfig kubeconfig, String namespace);
    boolean existsSparkServiceAccountPVC(Kubeconfig kubeconfig, String namespace);
    boolean existsHiveMetastore(Kubeconfig kubeconfig, String namespace);
    String getElasticsearchClusterName(Kubeconfig kubeconfig, String namespace);
    String getHiveMetastoreExternalEndpoint(Kubeconfig kubeconfig, String namespace);
    String getPrestoExternalEndpoint(Kubeconfig kubeconfig, String namespace);
    List<Node> getNodes(Kubeconfig kubeconfig);
    List<Service> getServices(Kubeconfig kubeconfig, String namespace);
    List<Endpoints> getEndpoints(Kubeconfig kubeconfig, String namespace);
    List<Pod> getPods(Kubeconfig kubeconfig, String namespace);
    List<Deployment> getDeployments(Kubeconfig kubeconfig, String namespace);
    List<StatefulSet> getStatefulsets(Kubeconfig kubeconfig, String namespace);
    List<Namespace> getNamespaces(Kubeconfig kubeconfig);
    List<ServiceAccount> getSparkServiceAccount(Kubeconfig kubeconfig);
}
