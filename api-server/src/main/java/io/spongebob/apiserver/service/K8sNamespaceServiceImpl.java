package io.spongebob.apiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.Pod;
import io.spongebob.apiserver.api.dao.K8sNamespaceDao;
import io.spongebob.apiserver.api.dao.ResourceControlDao;
import io.spongebob.apiserver.api.dao.SecretDao;
import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.api.service.K8sNamespaceService;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.model.K8sNamespace;
import io.spongebob.apiserver.kubernetes.YamlUtils;
import io.spongebob.apiserver.service.common.AbstractHibernateService;
import io.spongebob.apiserver.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class K8sNamespaceServiceImpl extends AbstractHibernateService<K8sNamespace> implements K8sNamespaceService {

    private static Logger LOG = LoggerFactory.getLogger(K8sNamespaceServiceImpl.class);

    @Autowired
    private K8sNamespaceDao dao;

    @Autowired
    @Qualifier("kubernetesResourceControlDao")
    private ResourceControlDao resourceControlDao;

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    public K8sNamespaceServiceImpl() {
        super();
    }

    @Override
    protected Operations<K8sNamespace> getDao() {
        return dao;
    }

    @Override
    public void craeteNamespace(K8sNamespace k8sNamespace) {
        dao.create(k8sNamespace);

        String path = k8sNamespace.getK8sCluster().getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfig = secretDao.readSecret(path, Kubeconfig.class);
        LOG.debug("kubeconfig yaml: \n{}", YamlUtils.getKubeconfigYaml(kubeconfig));

        resourceControlDao.createNamespace(kubeconfig, k8sNamespace.getNamespaceName());
    }

    @Override
    public void deleteNamespace(K8sNamespace k8sNamespace) {
        String path = k8sNamespace.getK8sCluster().getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfig = secretDao.readSecret(path, Kubeconfig.class);

        List<Pod> pods = resourceControlDao.listPods(kubeconfig, k8sNamespace.getNamespaceName());
        if(pods.size() > 0) {
            throw new RuntimeException("Namespace cannot be deleted, because Pods are running in this namespace: " +
                    JsonUtils.toJson(new ObjectMapper(), pods));
        }

        resourceControlDao.deleteNamespace(kubeconfig, k8sNamespace.getNamespaceName());

        dao.delete(k8sNamespace);
    }
}
