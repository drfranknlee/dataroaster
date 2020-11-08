package io.spongebob.apiserver.service;

import io.spongebob.apiserver.api.dao.*;
import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.api.service.K8sServicesService;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.StorageClass;
import io.spongebob.apiserver.domain.model.K8sCluster;
import io.spongebob.apiserver.domain.model.K8sKubeconfigAdmin;
import io.spongebob.apiserver.domain.model.K8sNamespace;
import io.spongebob.apiserver.domain.model.K8sServices;
import io.spongebob.apiserver.service.common.AbstractHibernateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class K8sServicesServiceImpl extends AbstractHibernateService<K8sServices> implements K8sServicesService {

    @Autowired
    private K8sServicesDao dao;

    @Autowired
    @Qualifier("hibernateK8sNamespaceDao")
    private K8sNamespaceDao k8sNamespaceDao;

    @Autowired
    @Qualifier("kubernetesResourceControlDao")
    private ResourceControlDao resourceControlDao;

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    @Autowired
    @Qualifier("hibernateK8sClusterDao")
    private K8sClusterDao k8sClusterDao;

    public K8sServicesServiceImpl() {
        super();
    }

    @Override
    protected Operations<K8sServices> getDao() {
        return dao;
    }

    @Override
    public List<K8sServices> findListByType(String type) {
        return dao.findListByType(type);
    }

    private Kubeconfig getAdminKubeconfig(long clusterId) {
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        return getAdminKubeconfig(k8sCluster);
    }

    private Kubeconfig getAdminKubeconfig(K8sCluster k8sCluster) {
        K8sKubeconfigAdmin kubeconfigAdmin = k8sCluster.getK8sKubeconfigAdminSet().iterator().next();
        String secretPath = kubeconfigAdmin.getSecretPath();

        return secretDao.readSecret(secretPath, Kubeconfig.class);
    }

    @Override
    public List<K8sServices> listObjectStorageByNamespace(long namespaceId) {
        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        Set<K8sServices> k8sServicesSet = k8sNamespace.getK8sServicesSet();
        List<K8sServices> k8sServicesList = new ArrayList<>();
        for(K8sServices k8sServices : k8sServicesSet) {
            String type = k8sServices.getType();
            if(type.equals(K8sServices.ServiceTypeEnum.OBJECT_STORAGE.name())) {
                k8sServicesList.add(k8sServices);
            }
        }

        return k8sServicesList;
    }

    @Override
    public List<StorageClass> listStorageClasses(long clusterId) {
        return resourceControlDao.listStorageClasses(getAdminKubeconfig(clusterId));
    }
}
