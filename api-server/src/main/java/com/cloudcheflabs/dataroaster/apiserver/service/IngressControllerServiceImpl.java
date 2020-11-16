package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigAdmin;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.IngressNginxHandler;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.IngressServiceMinIOHandler;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.IngressServiceOzoneHandler;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.LoadBalancerMetalLBHandler;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.IngressControllerService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class IngressControllerServiceImpl implements IngressControllerService {

    private static Logger LOG = LoggerFactory.getLogger(IngressControllerServiceImpl.class);

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    @Autowired
    @Qualifier("hibernateK8sServicesDao")
    private K8sServicesDao k8sServicesDao;

    @Autowired
    @Qualifier("hibernateK8sClusterDao")
    private K8sClusterDao k8sClusterDao;

    @Autowired
    @Qualifier("hibernateK8sNamespaceDao")
    private K8sNamespaceDao k8sNamespaceDao;

    @Autowired
    @Qualifier("hibernateUsersDao")
    private UsersDao usersDao;

    @Autowired
    @Qualifier("kubernetesResourceControlDao")
    private ResourceControlDao resourceControlDao;

    @Value("${objectStorage.minio.defaultAccessKey}")
    private String minioAccessKey;

    @Value("${objectStorage.minio.defaultSecretKey}")
    private String minioSecretKey;

    public IngressControllerServiceImpl() {
        super();
    }

    private Kubeconfig getAdminKubeconfig(K8sNamespace k8sNamespace) {
        K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
        return getAdminKubeconfig(k8sCluster);
    }
    private Kubeconfig getAdminKubeconfig(K8sCluster k8sCluster) {
        K8sKubeconfigAdmin kubeconfigAdmin = k8sCluster.getK8sKubeconfigAdminSet().iterator().next();
        String secretPath = kubeconfigAdmin.getSecretPath();

        return secretDao.readSecret(secretPath, Kubeconfig.class);
    }


    @Override
    public void createIngressNginx(long clusterId, long serviceId, int port) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check the service type.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.INGRESS_CONTROLLER.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.INGRESS_CONTROLLER.name());
        }

        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        k8sServices.getIngressControllerK8sClusterSet().add(k8sCluster);
        k8sServicesDao.update(k8sServices);

        // create in real k8s.
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);
        ExecutorUtils.runTask(() -> {
            return IngressNginxHandler.createIngrssNginx(k8sServices, kubeconfigAdmin, port);
        });
    }

    @Override
    public void deleteIngressNginx(long clusterId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check the service type.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.INGRESS_CONTROLLER.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.INGRESS_CONTROLLER.name());
        }

        Set<K8sCluster> k8sClusterSet = k8sServices.getIngressControllerK8sClusterSet();
        for(K8sCluster k8sCluster : k8sClusterSet) {
            long id = k8sCluster.getId();
            // remove cluster.
            if(id == clusterId) {
                k8sClusterSet.remove(k8sCluster);
            }
        }
        k8sServices.setIngressControllerK8sClusterSet(k8sClusterSet);
        k8sServicesDao.update(k8sServices);

        // delete in real k8s.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        // get current ingress nginx port from real k8s.
        int port = resourceControlDao.getNginxPort(kubeconfigAdmin, "ingress-nginx");

        ExecutorUtils.runTask(() -> {
            return IngressNginxHandler.deleteIngressNginx(k8sServices, kubeconfigAdmin, port);
        });
    }

    @Override
    public void createMetalLB(long clusterId, long serviceId, String fromIp, String toIp) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check the service type.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.LOAD_BALANCER.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.LOAD_BALANCER.name());
        }

        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        k8sServices.getLoadBalancerK8sClusterSet().add(k8sCluster);
        k8sServicesDao.update(k8sServices);

        // create in real k8s.
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        if(fromIp == null) {
            // get external ip.
            String ip = resourceControlDao.getExternalIPForMetalLB(kubeconfigAdmin);
            String[] tokens = ip.split("\\.");
            int lastNum = Integer.valueOf(tokens[tokens.length - 1]);
            int from = lastNum + 5;
            int to = from + 5;
            String prefix = ip.substring(0, ip.lastIndexOf("."));
            String fromIpFinal = prefix + "." + from;
            String toIpFinal = prefix + "." + to;
            ExecutorUtils.runTask(() -> {
                return LoadBalancerMetalLBHandler.create(k8sServices, kubeconfigAdmin, fromIpFinal, toIpFinal);
            });
        } else {
            ExecutorUtils.runTask(() -> {
                return LoadBalancerMetalLBHandler.create(k8sServices, kubeconfigAdmin, fromIp, toIp);
            });
        }
    }

    @Override
    public void deleteMetalLB(long clusterId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check the service type.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.LOAD_BALANCER.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.LOAD_BALANCER.name());
        }

        Set<K8sCluster> k8sClusterSet = k8sServices.getLoadBalancerK8sClusterSet();
        for(K8sCluster k8sCluster : k8sClusterSet) {
            long id = k8sCluster.getId();
            // remove cluster.
            if(id == clusterId) {
                k8sClusterSet.remove(k8sCluster);
            }
        }
        k8sServices.setLoadBalancerK8sClusterSet(k8sClusterSet);
        k8sServicesDao.update(k8sServices);

        // delete in real k8s.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        ExecutorUtils.runTask(() -> {
            return LoadBalancerMetalLBHandler.delete(k8sServices, kubeconfigAdmin);
        });
    }

    @Override
    public void createMinIOIngress(long namespaceId, long serviceId, String host) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // check if minio tenant exists in this namespae or not.
        if(!resourceControlDao.existsTenant(kubeconfig, namespace)) {
            throw new RuntimeException("MinIO Tenant not installed in this namespace, please install it first.");
        }

        // check it cert manager is installed or not.
        if(!resourceControlDao.existsCertManager(kubeconfig)) {
            throw new RuntimeException("Cert Manager not installed, please install it first.");
        }

        // get tenant name of minio in this namespace from real k8s.
        String tenantName = resourceControlDao.getTenantName(kubeconfig, namespace);

        // create ingress in real k8s.
        ExecutorUtils.runTask(() -> {
            return IngressServiceMinIOHandler.create(kubeconfig, namespace, host, tenantName);
        });
    }

    @Override
    public void deleteMinIOIngress(long namespaceId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // delete ingress.
        resourceControlDao.deleteMinIOIngress(kubeconfig, namespace);
    }

    @Override
    public void createOzoneIngress(long namespaceId, long serviceId, String host) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // check if ozone install or not.
        if(!resourceControlDao.existsOzone(kubeconfig, namespace)) {
            throw new RuntimeException("Ozone not installed in this namespace, please install it first.");
        }

        // check it cert manager is installed or not.
        if(!resourceControlDao.existsCertManager(kubeconfig)) {
            throw new RuntimeException("Cert Manager not installed, please install it first.");
        }

        // create ingress in real k8s.
        ExecutorUtils.runTask(() -> {
            return IngressServiceOzoneHandler.create(kubeconfig, namespace, host);
        });
    }

    @Override
    public void deleteOzoneIngress(long namespaceId, long serviceId) {
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Object Storage service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
            throw new RuntimeException("It is not type of " + K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name());
        }

        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        // delete ingress.
        resourceControlDao.deleteOzoneIngress(kubeconfig, namespace);
    }

    @Override
    public List<Ingress> getIngresses(long namespaceId) {
        // namespace.
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        Kubeconfig kubeconfig = getAdminKubeconfig(k8sNamespace);

        return resourceControlDao.getIngresses(kubeconfig, namespace);
    }
}
