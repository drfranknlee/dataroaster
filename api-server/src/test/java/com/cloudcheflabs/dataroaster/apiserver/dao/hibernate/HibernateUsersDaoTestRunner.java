package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.GroupsDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.UserAuthoritiesDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.UsersDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Groups;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.UserAuthorities;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.properties"
})
public class HibernateUsersDaoTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(HibernateUsersDaoTestRunner.class);

    @Autowired
    private UsersDao dao;

    @Autowired
    @Qualifier("hibernateGroupsDao")
    private GroupsDao groupsDao;

    @Autowired
    @Qualifier("hibernateUserAuthoritiesDao")
    private UserAuthoritiesDao userAuthoritiesDao;

    @Test
    public void findAll() throws Exception {
        List<Users> users = dao.findAll();
        users.stream().forEach(u -> {
            LOG.info("username: {}", u.getUserName());
            u.getUserAuthoritiesSet().forEach(role -> {
                LOG.info("- role: {}", role.getAuthority());
            });
        });
    }

    @Test
    public void findByName() throws Exception {
        String userName = "mykidong";
        Users exist = dao.findByUserName(userName);
        Assert.assertNotNull(exist);

        userName = "mykidongNotExist";
        Users notExist = dao.findByUserName(userName);
        Assert.assertNull(notExist);
    }

    @Test
    public void getGroups() throws Exception {
        String userName = "mykidong";
        Users users = dao.findByUserName(userName);

        users.getGroupsSet().forEach(groups -> {
            LOG.info("group name: {}", groups.getGroup());
        });
    }

    @Test
    public void createUser() throws Exception {
        Users users = new Users();
        users.setUserName("mykidongTest");
        users.setPassword("anyPassword");
        users.setEnabled(true);

        Groups groups = new Groups();
        groups.setGroup("test-group1");

        groupsDao.create(groups);
        Groups newGroups = groupsDao.findByGroupName("test-group1");

        users.setGroupsSet(Sets.newSet(newGroups));

        dao.create(users);

        Users ret = dao.findByUserName("mykidongTest");
        Assert.assertNotNull(ret);

        LOG.info("user name: {}", ret.getUserName());
        ret.getGroupsSet().forEach(g -> {
            LOG.info("group name: {}", g.getGroup());
            Assert.assertTrue(g.getGroup().equals("test-group1"));
        });

        dao.delete(users);

        ret = dao.findByUserName("mykidongTest");
        Assert.assertNull(ret);

        groupsDao.delete(newGroups);
        Assert.assertNull(groupsDao.findByGroupName(newGroups.getGroup()));
    }

    @Test
    public void createUserWithAuthorities() throws Exception {
        Users users = new Users();
        users.setUserName("mykidongTest");
        users.setPassword("anyPassword");
        users.setEnabled(true);

        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setAuthority(Roles.ROLE_USER.name());
        userAuthorities.setUsers(users);

        users.setUserAuthoritiesSet(Sets.newSet(userAuthorities));

        dao.create(users);

        Users ret = dao.findByUserName("mykidongTest");
        Assert.assertNotNull(ret);

        ret.getUserAuthoritiesSet().forEach(ua -> {
            LOG.info("user authorities: {}", ua.getAuthority());
            Assert.assertTrue(Roles.ROLE_USER.name().equals(ua.getAuthority()));
        });

        dao.delete(users);

        Assert.assertNull(userAuthoritiesDao.findOne(ret.getUserAuthoritiesSet().iterator().next().getId()));
    }

    @Test
    public void deleteUser() throws Exception {
        Users users = dao.findByUserName("testuser");
        dao.delete(users);

        Assert.assertNull(dao.findByUserName("testuser"));
    }
}
