package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.controller.UsersController;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler.UserCertsHandler;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import com.cloudcheflabs.dataroaster.apiserver.util.TemplateUtils;
import com.google.common.collect.Sets;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.UsersService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.*;
import com.cloudcheflabs.dataroaster.apiserver.secret.SecretPathTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Transactional
public class UsersServiceImpl extends AbstractHibernateService<Users> implements UsersService {

    private static Logger LOG = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private UsersDao dao;

    @Autowired
    @Qualifier("hibernateGroupsDao")
    private GroupsDao groupsDao;

    @Autowired
    @Qualifier("hibernateK8sClusterDao")
    private K8sClusterDao k8sClusterDao;

    @Autowired
    @Qualifier("hibernateK8sNamespaceDao")
    private K8sNamespaceDao k8sNamespaceDao;

    @Autowired
    @Qualifier("kubernetesResourceControlDao")
    private ResourceControlDao resourceControlDao;

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    public UsersServiceImpl() {
        super();
    }

    @Override
    protected Operations<Users> getDao() {
        return dao;
    }

    @Override
    public void signUp(Users users) {
        // set default group 'developer'.
        Groups defaultGroup = groupsDao.getDefaultGroup();
        users.setGroupsSet(Sets.newHashSet(defaultGroup));

        // set default role 'ROLE_USER'.
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setAuthority(Roles.ROLE_USER.name());
        userAuthorities.setUsers(users);

        users.setUserAuthoritiesSet(Sets.newHashSet(userAuthorities));

        dao.create(users);
    }

    @Override
    public Users findByUserName(String userName) {
        return dao.findByUserName(userName);
    }

    @Override
    public List<Users> findUsersByGroupName(String groupName) {
        if(groupName == null) {
            return dao.findAll();
        } else {
            Groups groups = groupsDao.findByGroupName(groupName);
            return dao.findAllByGroup(groups);
        }
    }

    @Override
    public void changePassword(String userName, String newPassword) {
        Users users = dao.findByUserName(userName);
        users.setPassword(newPassword);

        dao.update(users);
    }

    @Override
    public void changeRole(String userName, String role) {
        Users users = dao.findByUserName(userName);
        for (UserAuthorities userAuthorities : users.getUserAuthoritiesSet()) {
            userAuthorities.setAuthority(role);
        }

        dao.update(users);
    }

    @Override
    public void joinGroup(List<Long> userIdList, long groupId) {
        for(long userId : userIdList) {
            Users users = dao.findOne(userId);
            Set<Groups> groupsSet = users.getGroupsSet();
            for(Groups groups : groupsSet) {
                long gId = groups.getId();
                // already joined the group.
                if(gId == Long.valueOf(groupId)) {
                    continue;
                }
            }
            Groups newGroup = groupsDao.findOne(groupId);
            groupsSet.add(newGroup);

            users.setGroupsSet(groupsSet);
            dao.update(users);
        }
    }

    @Override
    public void leaveGroup(List<Long> userIdList, long groupId) {
        for(long userId : userIdList) {
            Users users = dao.findOne(userId);
            Set<Groups> groupsSet = users.getGroupsSet();
            for(Groups groups : groupsSet) {
                long gId = groups.getId();
                // user will leave this group.
                if(gId == groupId) {
                    groupsSet.remove(groups);
                    LOG.debug("user [{}] left group [{}]", users.getUserName(), groups.getGroup());
                }
            }
            users.setGroupsSet(groupsSet);
            dao.update(users);
        }
    }

    @Override
    public void creatCerts(long clusterId, long groupId, long namespaceId, List<Long> userIdList) {
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
        Kubeconfig kubeconfig = secretDao.readSecret(path, Kubeconfig.class);

        Groups groups = groupsDao.findOne(groupId);
        String group = groups.getGroup();

        K8sNamespace k8sNamespace = k8sNamespaceDao.findOne(namespaceId);
        String namespace = k8sNamespace.getNamespaceName();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<String>> futureList = new ArrayList<Future<String>>();

        for(long userId : userIdList) {
            Future<String> future = executor.submit(() -> {
                Users users = dao.findOne(userId);
                String user = users.getUserName();

                Kubeconfig kubeconfigUser = UserCertsHandler.craeteCerts(user, group, namespace, kubeconfig);
                Map<String, String> kv = new HashMap<>();
                kv.put("clusterId", String.valueOf(clusterId));
                kv.put("user", user);
                kv.put("namespace", namespace);
                String userSecretPath = TemplateUtils.replace(SecretPathTemplate.SECRET_KUBECONFIG_USER, kv);
                LOG.debug("secret path: {}", userSecretPath);

                // add secret for user kubeconfig.
                secretDao.writeSecret(userSecretPath, kubeconfigUser);

                // update user with secret path.
                K8sKubeconfigUser k8sKubeconfigUser = new K8sKubeconfigUser();
                k8sKubeconfigUser.setSecretPath(userSecretPath);
                k8sKubeconfigUser.setUsers(users);
                k8sKubeconfigUser.setGroups(groups);
                k8sKubeconfigUser.setK8sNamespace(k8sNamespace);

                // update user with secret path.
                users.getK8sKubeconfigUserSet().add(k8sKubeconfigUser);
                dao.update(users);

                return "kubernetes user certs created for the user [" + user + "]";

            });
            futureList.add(future);
        }

        for (Future<String> fut : futureList) {
            try {
                LOG.info(new Date() + "::" + fut.get());
            } catch (InterruptedException | ExecutionException e) {
                LOG.error(e.getMessage());
            }
        }
        executor.shutdown();
    }
}
