package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.Groups;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.GroupsDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.GroupsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroupsServiceImpl extends AbstractHibernateService<Groups> implements GroupsService {

    @Autowired
    private GroupsDao dao;

    public GroupsServiceImpl() {
        super();
    }

    @Override
    protected Operations<Groups> getDao() {
        return dao;
    }

    @Override
    public Groups findByGroupName(String group) {
        return dao.findByGroupName(group);
    }
}
