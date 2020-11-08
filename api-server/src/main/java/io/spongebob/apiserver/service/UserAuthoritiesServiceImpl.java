package io.spongebob.apiserver.service;

import io.spongebob.apiserver.api.dao.UserAuthoritiesDao;
import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.api.service.UserAuthoritiesService;
import io.spongebob.apiserver.domain.model.UserAuthorities;
import io.spongebob.apiserver.service.common.AbstractHibernateService;
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
