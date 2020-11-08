package io.spongebob.apiserver.service;

import io.spongebob.apiserver.api.dao.K8sKubeconfigAdminDao;
import io.spongebob.apiserver.api.dao.common.Operations;
import io.spongebob.apiserver.api.service.K8sKubeconfigAdminService;
import io.spongebob.apiserver.domain.model.K8sKubeconfigAdmin;
import io.spongebob.apiserver.service.common.AbstractHibernateService;
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
