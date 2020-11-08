package io.spongebob.apiserver.dao.hibernate;

import io.spongebob.apiserver.api.dao.K8sKubeconfigUserDao;
import io.spongebob.apiserver.dao.common.AbstractHibernateDao;
import io.spongebob.apiserver.domain.model.K8sKubeconfigUser;
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
