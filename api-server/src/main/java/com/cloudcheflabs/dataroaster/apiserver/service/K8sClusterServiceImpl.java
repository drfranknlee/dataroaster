package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigAdmin;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.YamlUtils;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import com.cloudcheflabs.dataroaster.apiserver.util.TemplateUtils;
import com.google.common.collect.Sets;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sClusterDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.SecretDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.K8sClusterService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.secret.SecretPathTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class K8sClusterServiceImpl extends AbstractHibernateService<K8sCluster> implements K8sClusterService {

    private static Logger LOG = LoggerFactory.getLogger(K8sClusterServiceImpl.class);

    @Autowired
    private K8sClusterDao dao;

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    public K8sClusterServiceImpl() {
        super();
    }

    @Override
    protected Operations<K8sCluster> getDao() {
        return dao;
    }

    @Override
    public void createCluster(String clusterName, String description, String kubeconfig) {
        K8sCluster k8sCluster = new K8sCluster();
        k8sCluster.setClusterName(clusterName);
        k8sCluster.setDescription(description);

        // add cluster.
        dao.create(k8sCluster);

        // get cluster.
        K8sCluster creeatedK8sCluster = dao.findByName(clusterName);

        // kubeconfig yaml.
        Kubeconfig value = YamlUtils.readKubeconfigYaml(new ByteArrayInputStream(kubeconfig.getBytes()));
        Map<String, String> kv = new HashMap<>();
        kv.put("clusterId", String.valueOf(creeatedK8sCluster.getId()));
        kv.put("user", value.getUser());
        String path = TemplateUtils.replace(SecretPathTemplate.SECRET_KUBECONFIG_ADMIN, kv);
        LOG.debug("secret path: {}", path);

        // add secret for kubeconfig.
        secretDao.writeSecret(path, value);

        // update cluster with secret path.
        K8sKubeconfigAdmin k8sKubeconfigAdmin = new K8sKubeconfigAdmin();
        k8sKubeconfigAdmin.setSecretPath(path);
        k8sKubeconfigAdmin.setK8sCluster(creeatedK8sCluster);
        creeatedK8sCluster.setK8sKubeconfigAdminSet(Sets.newHashSet(k8sKubeconfigAdmin));
        dao.update(creeatedK8sCluster);
    }

    @Override
    public void updateCluster(long id, String description, String kubeconfig) {
        K8sCluster k8sCluster = dao.findOne(id);
        k8sCluster.setDescription(description);

        // kubeconfig yaml.
        Kubeconfig value = YamlUtils.readKubeconfigYaml(new ByteArrayInputStream(kubeconfig.getBytes()));
        Map<String, String> kv = new HashMap<>();
        kv.put("clusterId", String.valueOf(k8sCluster.getId()));
        kv.put("user", value.getUser());
        String path = TemplateUtils.replace(SecretPathTemplate.SECRET_KUBECONFIG_ADMIN, kv);
        LOG.debug("secret path: {}", path);

        // update secret for kubeconfig.
        secretDao.writeSecret(path, value);

        // update cluster.
        k8sCluster.getK8sKubeconfigAdminSet().forEach(k -> {
            k.setSecretPath(path);
        });
        dao.update(k8sCluster);
    }

    @Override
    public void deleteCluster(long id) {
        K8sCluster k8sCluster = dao.findOne(id);

        k8sCluster.getK8sKubeconfigAdminSet().forEach(k -> {
            String path = k.getSecretPath();

            // delete secret.
            secretDao.delete(path);
        });

        // delete cluster.
        dao.delete(k8sCluster);
    }
}
