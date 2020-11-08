package io.spongebob.apiserver.service;

import io.spongebob.apiserver.api.dao.GroupsDao;
import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.api.service.GroupsService;
import io.spongebob.apiserver.domain.model.Groups;
import io.spongebob.apiserver.service.common.AbstractHibernateService;
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
