package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.ExecutorUtils;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.CsiHandler;
import com.cloudcheflabs.dataroaster.apiserver.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sClusterDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sServicesDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.ResourceControlDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.SecretDao;
import com.cloudcheflabs.dataroaster.apiserver.api.service.CsiService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CsiServiceImpl implements CsiService {

    private static Logger LOG = LoggerFactory.getLogger(CsiServiceImpl.class);

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

    public CsiServiceImpl() {
        super();
    }

    @Override
    @Transactional
    public void createCsi(long serviceId, long clusterId, String dataRootPath, int dataDirCount) {
        // add csi and cluster mapping to db.
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is csi service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.CSI.name())) {
            throw new RuntimeException("It is not type of CSI");
        }

        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        k8sServices.getK8sClusterSet().add(k8sCluster);

        k8sServicesDao.update(k8sServices);

        // create csi in real k8s.
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        ExecutorUtils.runTask(() -> {
            return CsiHandler.craeteCsi(k8sServices, dataRootPath, dataDirCount, kubeconfigAdmin);
        });
    }

    @Override
    @Transactional
    public void deleteCsi(long serviceId, long clusterId) {
        // remove csi and cluster mapping to db.
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);
        // check if it is csi service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.CSI.name())) {
            throw new RuntimeException("It is not type of CSI");
        }

        Set<K8sCluster> k8sClusterSet = k8sServices.getK8sClusterSet();
        for(K8sCluster k8sCluster : k8sClusterSet) {
            long id = k8sCluster.getId();
            // remove csi / cluster mapping.
            if(id == clusterId) {
                k8sClusterSet.remove(k8sCluster);
            }
        }
        k8sServices.setK8sClusterSet(k8sClusterSet);
        k8sServicesDao.update(k8sServices);

        // delete csi in real k8s.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        // get pvc list using minio direct csi storage class.
        List<PersistentVolumeClaim> persistentVolumeClaims = resourceControlDao.listPvcUsingStorageClass(kubeconfigAdmin, "direct.csi.min.io");
        if(persistentVolumeClaims.size() > 0) {
            throw new RuntimeException("CSI cannot be deleted, because currently Other PVCs are using CSI StorageClass: " +
                    JsonUtils.toJson(new ObjectMapper(), persistentVolumeClaims));
        }

        ExecutorUtils.runTask(() -> {
            return CsiHandler.deleteCsi(k8sServices, kubeconfigAdmin);
        });
    }
}
