package io.spongebob.apiserver.dao.hibernate;

import io.spongebob.apiserver.api.dao.K8sNamespaceDao;
import io.spongebob.apiserver.dao.common.AbstractHibernateDao;
import io.spongebob.apiserver.domain.model.K8sNamespace;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateK8sNamespaceDao extends AbstractHibernateDao<K8sNamespace> implements K8sNamespaceDao {

    public HibernateK8sNamespaceDao() {
        super();
        setClazz(K8sNamespace.class);
    }
}
