package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.UserAuthoritiesDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.UserAuthoritiesService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.UserAuthorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAuthoritiesServiceImpl extends AbstractHibernateService<UserAuthorities> implements UserAuthoritiesService {

    @Autowired
    private UserAuthoritiesDao dao;

    public UserAuthoritiesServiceImpl() {
        super();
    }

    @Override
    protected Operations<UserAuthorities> getDao() {
        return dao;
    }

}
