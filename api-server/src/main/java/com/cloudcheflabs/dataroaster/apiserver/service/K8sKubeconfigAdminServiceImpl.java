package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfigAdmin;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sKubeconfigAdminDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.K8sKubeconfigAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class K8sKubeconfigAdminServiceImpl extends AbstractHibernateService<K8sKubeconfigAdmin> implements K8sKubeconfigAdminService {

    @Autowired
    private K8sKubeconfigAdminDao dao;

    public K8sKubeconfigAdminServiceImpl() {
        super();
    }

    @Override
    protected Operations<K8sKubeconfigAdmin> getDao() {
        return dao;
    }

}
