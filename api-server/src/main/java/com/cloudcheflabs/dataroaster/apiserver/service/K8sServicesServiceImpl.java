package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.StorageClass;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigAdmin;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.K8sServicesService;
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
