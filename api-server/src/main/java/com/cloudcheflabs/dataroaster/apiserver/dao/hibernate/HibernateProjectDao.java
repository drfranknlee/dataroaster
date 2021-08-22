package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;


import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Project;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.ProjectDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateProjectDao extends AbstractHibernateDao<Project> implements ProjectDao {

    public HibernateProjectDao() {
        super();
        setClazz(Project.class);
    }
}
