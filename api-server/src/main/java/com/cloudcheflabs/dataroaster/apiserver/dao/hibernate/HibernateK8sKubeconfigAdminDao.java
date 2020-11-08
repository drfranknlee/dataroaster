package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigAdmin;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sKubeconfigAdminDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateK8sKubeconfigAdminDao extends AbstractHibernateDao<K8sKubeconfigAdmin> implements K8sKubeconfigAdminDao {

    public HibernateK8sKubeconfigAdminDao() {
        super();
        setClazz(K8sKubeconfigAdmin.class);
    }
}
