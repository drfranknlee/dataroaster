package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractSecretService;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.SecretDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class KubeconfigSecretServiceImpl extends AbstractSecretService<Kubeconfig> {

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> dao;

    @Override
    protected SecretDao<Kubeconfig> getDao() {
        return dao;
    }
}
