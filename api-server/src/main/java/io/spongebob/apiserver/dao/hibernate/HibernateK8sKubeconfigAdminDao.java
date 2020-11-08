package io.spongebob.apiserver.dao.hibernate;

import io.spongebob.apiserver.api.dao.K8sKubeconfigAdminDao;
import io.spongebob.apiserver.dao.common.AbstractHibernateDao;
import io.spongebob.apiserver.domain.model.K8sKubeconfigAdmin;
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
