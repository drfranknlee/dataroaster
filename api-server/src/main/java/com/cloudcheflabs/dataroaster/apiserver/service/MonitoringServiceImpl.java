package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sClusterDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sServicesDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.ResourceControlDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.SecretDao;
import com.cloudcheflabs.dataroaster.apiserver.api.service.MonitoringService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.MetricsServerHandler;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.PrometheusStackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class MonitoringServiceImpl implements MonitoringService {

    private static Logger LOG = LoggerFactory.getLogger(MonitoringServiceImpl.class);

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
    @Qualifier("kubernetesResourceControlDao")
    private ResourceControlDao resourceControlDao;

    public MonitoringServiceImpl() {
        super();
    }

    @Override
    public void createPrometheusStack(long serviceId, long clusterId, String storageClass, int storageSize) {
        // add csi and cluster mapping to db.
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is csi service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.MONITORING.name())) {
            throw new RuntimeException("It is not type of MONITORING");
        }

        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        k8sServices.getMonitoringK8sClusterSet().add(k8sCluster);

        k8sServicesDao.update(k8sServices);

        // create csi in real k8s.
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        ExecutorUtils.runTask(() -> {
            return PrometheusStackHandler.create(k8sServices, kubeconfigAdmin, storageClass, storageSize);
        });
    }

    @Override
    public void deletePrometheusStack(long serviceId, long clusterId) {
        // remove csi and cluster mapping to db.
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);
        // check if it is csi service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.MONITORING.name())) {
            throw new RuntimeException("It is not type of MONITORING");
        }

        Set<K8sCluster> k8sClusterSet = k8sServices.getMonitoringK8sClusterSet();
        for(K8sCluster k8sCluster : k8sClusterSet) {
            long id = k8sCluster.getId();
            // remove csi / cluster mapping.
            if(id == clusterId) {
                k8sClusterSet.remove(k8sCluster);
            }
        }
        k8sServices.setMonitoringK8sClusterSet(k8sClusterSet);
        k8sServicesDao.update(k8sServices);

        // delete csi in real k8s.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        ExecutorUtils.runTask(() -> {
            return PrometheusStackHandler.delete(k8sServices, kubeconfigAdmin);
        });
    }

    @Override
    public void createMetricsServer(long serviceId, long clusterId) {
        // add csi and cluster mapping to db.
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is csi service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.MONITORING.name())) {
            throw new RuntimeException("It is not type of MONITORING");
        }

        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        k8sServices.getMonitoringK8sClusterSet().add(k8sCluster);

        k8sServicesDao.update(k8sServices);

        // create csi in real k8s.
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        ExecutorUtils.runTask(() -> {
            return MetricsServerHandler.create(k8sServices, kubeconfigAdmin);
        });
    }

    @Override
    public void deleteMetricsServer(long serviceId, long clusterId) {
        // remove csi and cluster mapping to db.
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);
        // check if it is csi service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.MONITORING.name())) {
            throw new RuntimeException("It is not type of MONITORING");
        }

        Set<K8sCluster> k8sClusterSet = k8sServices.getMonitoringK8sClusterSet();
        for(K8sCluster k8sCluster : k8sClusterSet) {
            long id = k8sCluster.getId();
            // remove csi / cluster mapping.
            if(id == clusterId) {
                k8sClusterSet.remove(k8sCluster);
            }
        }
        k8sServices.setMonitoringK8sClusterSet(k8sClusterSet);
        k8sServicesDao.update(k8sServices);

        // delete csi in real k8s.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        ExecutorUtils.runTask(() -> {
            return MetricsServerHandler.delete(k8sServices, kubeconfigAdmin);
        });
    }
}
