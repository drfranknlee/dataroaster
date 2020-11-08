package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sServicesDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class HibernateK8sServicesDao extends AbstractHibernateDao<K8sServices> implements K8sServicesDao {

    public HibernateK8sServicesDao() {
        super();
        setClazz(K8sServices.class);
    }


    @Override
    @Transactional
    public List<K8sServices> findListByType(String type) {
        Query<K8sServices> query = this.getCurrentSession().createQuery("from " + clazz.getName() + " where type = :type", K8sServices.class);
        query.setParameter("type", type);
        List<K8sServices> list = query.list();

        return list;
    }
}
