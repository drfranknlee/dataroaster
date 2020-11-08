package io.spongebob.apiserver.dao.hibernate;

import io.spongebob.apiserver.api.dao.UserAuthoritiesDao;
import io.spongebob.apiserver.dao.common.AbstractHibernateDao;
import io.spongebob.apiserver.domain.model.UserAuthorities;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateUserAuthoritiesDao extends AbstractHibernateDao<UserAuthorities> implements UserAuthoritiesDao {

    public HibernateUserAuthoritiesDao() {
        super();
        setClazz(UserAuthorities.class);
    }
}
