package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sNamespaceDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
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
