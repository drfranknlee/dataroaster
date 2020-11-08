package io.spongebob.apiserver.dao.common;

import com.google.common.base.Preconditions;
import io.spongebob.apiserver.api.dao.common.Operations;

import java.io.Serializable;

public abstract class AbstractDao<T extends Serializable> implements Operations<T> {

    protected Class<T> clazz;

    protected final void setClazz(final Class<T> clazzToSet) {
        clazz = Preconditions.checkNotNull(clazzToSet);
    }
}
