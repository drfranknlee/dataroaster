package io.spongebob.apiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.spongebob.apiserver.api.dao.K8sClusterDao;
import io.spongebob.apiserver.api.dao.K8sServicesDao;
import io.spongebob.apiserver.api.dao.ResourceControlDao;
import io.spongebob.apiserver.api.dao.SecretDao;
import io.spongebob.apiserver.api.service.NfsService;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.model.K8sCluster;
import io.spongebob.apiserver.domain.model.K8sServices;
import io.spongebob.apiserver.kubernetes.ExecutorUtils;
import io.spongebob.apiserver.kubernetes.handler.NfsHandler;
import io.spongebob.apiserver.util.JsonUtils;
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
public class NfsServiceImpl implements NfsService {

    private static Logger LOG = LoggerFactory.getLogger(NfsServiceImpl.class);

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

    public NfsServiceImpl() {
        super();
    }

    @Override
    @Transactional
    public void createNfs(long serviceId, long clusterId, int persistenceSize) {
        // add Nfs and cluster mapping to db.
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);

        // check if it is Nfs service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.NFS.name())) {
            throw new RuntimeException("It is not type of NFS");
        }

        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        k8sServices.getNfsK8sClusterSet().add(k8sCluster);

        k8sServicesDao.update(k8sServices);

        // create Nfs in real k8s.
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        // check if minio direct csi storage class exists or not.
        if(!resourceControlDao.existsMinIOStorageClass(kubeconfigAdmin)) {
            throw new RuntimeException("MinIO Direct CSI not installed, please install it first.");
        }

        ExecutorUtils.runTask(() -> {
            return NfsHandler.craeteNfs(k8sServices, persistenceSize, kubeconfigAdmin);
        });
    }

    @Override
    @Transactional
    public void deleteNfs(long serviceId, long clusterId) {
        // remove Nfs and cluster mapping to db.
        K8sServices k8sServices = k8sServicesDao.findOne(serviceId);
        // check if it is Nfs service.
        if(!k8sServices.getType().equals(K8sServices.ServiceTypeEnum.NFS.name())) {
            throw new RuntimeException("It is not type of NFS");
        }

        Set<K8sCluster> k8sClusterSet = k8sServices.getNfsK8sClusterSet();
        for(K8sCluster k8sCluster : k8sClusterSet) {
            long id = k8sCluster.getId();
            // remove Nfs / cluster mapping.
            if(id == clusterId) {
                k8sClusterSet.remove(k8sCluster);
            }
        }
        k8sServices.setNfsK8sClusterSet(k8sClusterSet);
        k8sServicesDao.update(k8sServices);

        // delete Nfs in real k8s.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);

        // get pvc list using nfs.
        List<PersistentVolumeClaim> persistentVolumeClaims = resourceControlDao.listPvcUsingStorageClass(kubeconfigAdmin, "nfs");
        if(persistentVolumeClaims.size() > 0) {
            throw new RuntimeException("NFS cannot be deleted, because currently Other PVCs are using NFS StorageClass: " +
                    JsonUtils.toJson(new ObjectMapper(), persistentVolumeClaims));
        }

        ExecutorUtils.runTask(() -> {
            return NfsHandler.deleteNfs(k8sServices, kubeconfigAdmin);
        });
    }
}
