package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.GroupsDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Groups;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.properties"
})
public class HibernateGroupsDaoTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(HibernateGroupsDaoTestRunner.class);

    @Autowired
    private GroupsDao dao;

    @Test
    public void findByGroupName() throws Exception {
        dao.findAll().forEach(g -> {
            LOG.info("groups: {}", g.getGroup());
        });

        Groups groups = new Groups();
        groups.setGroup("new-test-group");

        dao.create(groups);

        Groups ret = dao.findByGroupName("new-test-group");
        Assert.assertNotNull(ret);

        dao.delete(ret);

        ret = dao.findByGroupName("new-test-group");
        Assert.assertNull(ret);
    }

}
