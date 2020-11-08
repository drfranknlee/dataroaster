package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigUser;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sKubeconfigUserDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateK8sKubeconfigUserDao extends AbstractHibernateDao<K8sKubeconfigUser> implements K8sKubeconfigUserDao {

    public HibernateK8sKubeconfigUserDao() {
        super();
        setClazz(K8sKubeconfigUser.class);
    }
}
