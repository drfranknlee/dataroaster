package io.spongebob.apiserver.service;

import io.spongebob.apiserver.api.dao.K8sKubeconfigUserDao;
import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.api.service.K8sKubeconfigUserService;
import io.spongebob.apiserver.domain.model.K8sKubeconfigUser;
import io.spongebob.apiserver.service.common.AbstractHibernateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class K8sKubeconfigUserServiceImpl extends AbstractHibernateService<K8sKubeconfigUser> implements K8sKubeconfigUserService {

    @Autowired
    private K8sKubeconfigUserDao dao;

    public K8sKubeconfigUserServiceImpl() {
        super();
    }

    @Override
    protected Operations<K8sKubeconfigUser> getDao() {
        return dao;
    }

}
