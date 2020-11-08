package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.GroupsDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Groups;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class HibernateGroupsDao extends AbstractHibernateDao<Groups> implements GroupsDao {

    private static final String DEFAULT_GROUP_NAME = "developer";

    public HibernateGroupsDao() {
        super();
        setClazz(Groups.class);
    }

    @Override
    @Transactional
    public Groups findByGroupName(String group) {
        Query<Groups> query = this.getCurrentSession().createQuery("from " + clazz.getName() + " where group = :group", Groups.class);
        query.setParameter("group", group);
        List<Groups> list = query.list();

        return (list.size() == 0) ? null : list.get(0);
    }

    private void createDefaultGroup() {
        Groups groups = new Groups();
        groups.setGroup(DEFAULT_GROUP_NAME);

        create(groups);
    }

    @Override
    public Groups getDefaultGroup() {
        Groups defaultGroup =  findByGroupName(DEFAULT_GROUP_NAME);
        if(defaultGroup == null) {
            createDefaultGroup();
            defaultGroup = findByGroupName(DEFAULT_GROUP_NAME);
        }

        return defaultGroup;
    }
}
