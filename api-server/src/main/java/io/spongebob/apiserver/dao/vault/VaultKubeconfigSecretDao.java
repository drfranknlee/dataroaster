package io.spongebob.apiserver.dao.vault;

import io.spongebob.apiserver.dao.common.AbstractVaultDao;
import io.spongebob.apiserver.domain.Kubeconfig;
import org.springframework.stereotype.Repository;
import org.springframework.vault.support.VaultResponseSupport;

@Repository
public class VaultKubeconfigSecretDao extends AbstractVaultDao<Kubeconfig> {
    @Override
    public Kubeconfig readSecret(String path, Class<Kubeconfig> clazz) {
        VaultResponseSupport<Kubeconfig> response = vaultTemplate.read(path, clazz);
        return response.getData();
    }
}
