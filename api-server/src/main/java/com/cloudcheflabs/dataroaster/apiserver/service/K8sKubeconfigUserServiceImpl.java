package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigUser;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sKubeconfigUserDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.K8sKubeconfigUserService;
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
