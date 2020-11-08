package io.spongebob.apiserver.service;

import io.spongebob.apiserver.api.dao.SecretDao;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.service.common.AbstractSecretService;
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
