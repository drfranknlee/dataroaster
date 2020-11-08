package io.spongebob.apiserver.dao.hibernate;

import io.spongebob.apiserver.api.dao.UsersDao;
import io.spongebob.apiserver.dao.common.AbstractHibernateDao;
import io.spongebob.apiserver.domain.model.Groups;
import io.spongebob.apiserver.domain.model.Users;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class HibernateUsersDao extends AbstractHibernateDao<Users> implements UsersDao {

    public HibernateUsersDao() {
        super();
        setClazz(Users.class);
    }

    @Override
    @Transactional
    public Users findByUserName(String userName) {
        Query<Users> query = this.getCurrentSession().createQuery("from " + clazz.getName() + " where userName = :userName", clazz);
        query.setParameter("userName", userName);

        List<Users> list = query.list();
        return (list.size() == 0) ? null : list.get(0);
    }

    @Override
    public List<Users> findAllByGroup(Groups groups) {
        Query<Users> query = this.getCurrentSession().createQuery("select u from " + clazz.getName() + " u inner join fetch u.groupsSet g  where g.id = :groupId", Users.class);
        query.setParameter("groupId", groups.getId());

        return query.list();
    }

}
