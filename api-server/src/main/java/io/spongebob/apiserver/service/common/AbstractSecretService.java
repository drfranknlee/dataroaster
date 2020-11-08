package io.spongebob.apiserver.service.common;

import io.spongebob.apiserver.api.dao.SecretDao;
import io.spongebob.apiserver.api.service.SecretService;

public abstract class AbstractSecretService<T> implements SecretService<T> {

    protected abstract SecretDao<T> getDao();

    @Override
    public void writeSecret(String path, Object value) {
        getDao().writeSecret(path, value);
    }

    @Override
    public T readSecret(String path, Class<T> clazz) {
        return getDao().readSecret(path, clazz);
    }

    @Override
    public void delete(String path) {
        getDao().delete(path);
    }
}
