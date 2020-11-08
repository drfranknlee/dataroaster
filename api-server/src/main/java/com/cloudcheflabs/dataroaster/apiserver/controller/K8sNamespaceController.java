package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudcheflabs.dataroaster.apiserver.api.service.GroupsService;
import com.cloudcheflabs.dataroaster.apiserver.api.service.K8sClusterService;
import com.cloudcheflabs.dataroaster.apiserver.api.service.K8sNamespaceService;
import com.cloudcheflabs.dataroaster.apiserver.api.service.UsersService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Groups;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;
import com.cloudcheflabs.dataroaster.apiserver.filter.AuthorizationFilter;
import com.cloudcheflabs.dataroaster.apiserver.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class K8sNamespaceController {

    private static Logger LOG = LoggerFactory.getLogger(K8sNamespaceController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("k8sNamespaceServiceImpl")
    private K8sNamespaceService k8sNamespaceService;

    @Autowired
    @Qualifier("usersServiceImpl")
    private UsersService usersService;

    @Autowired
    @Qualifier("groupsServiceImpl")
    private GroupsService groupsService;

    @Autowired
    @Qualifier("k8sClusterServiceImpl")
    private K8sClusterService k8sClusterService;

    @PostMapping("/apis/k8s/create_namespace")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_GROUP_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String groupId = params.get("group_id");
            String namespacePrefix = params.get("namespace_prefix");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            int maxRoleLevel = RoleUtils.getMaxRoleLevel(context);
            Roles roles = RoleUtils.getMaxRole(maxRoleLevel);
            // if user is platform admin.
            if(roles.name().equals(Roles.ROLE_PLATFORM_ADMIN.name())) {
                Groups groups = groupsService.findOne(Long.valueOf(groupId));
                String groupName = groups.getGroup();
                createNamespace(clusterId, namespacePrefix, groupName);
            } else {
                Users users = usersService.findByUserName(userName);
                String groupName = null;
                for(Groups groups : users.getGroupsSet()) {
                    if(Long.valueOf(groupId) == groups.getId()) {
                        groupName = groups.getGroup();
                        break;
                    }
                }
                // if user is not involved in the group.
                if(groupName == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT ALLOWED: NO PRIVILEGES");
                }

                createNamespace(clusterId, namespacePrefix, groupName);
            }
            return ControllerUtils.successMessage();
        });
    }

    private void createNamespace(String clusterId, String namespacePrefix, String groupName) {
        K8sCluster k8sCluster = k8sClusterService.findOne(Long.valueOf(clusterId));
        Groups groups = groupsService.findByGroupName(groupName);

        String namespace = namespacePrefix + "-" + groupName;
        K8sNamespace k8sNamespace = new K8sNamespace();
        k8sNamespace.setNamespaceName(namespace);
        k8sNamespace.setK8sCluster(k8sCluster);
        k8sNamespace.getGroupsSet().add(groups);

        k8sNamespaceService.craeteNamespace(k8sNamespace);
    }

    @GetMapping("/apis/k8s/list_namespace")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String clusterId = params.get("cluster_id");
            String groupId = params.get("group_id");

            List<K8sNamespace> allNamespaces = k8sNamespaceService.findAll();
            List<Map<String, Object>> list = new ArrayList<>();
            // namespaces in cluster.
            if(clusterId != null) {
                // namespaces in group and in cluster.
                if(groupId != null) {
                    for (K8sNamespace k8sNamespace : allNamespaces) {
                        K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
                        long id = k8sCluster.getId();
                        if (id == Long.valueOf(clusterId)) {
                            for(Groups groups : k8sNamespace.getGroupsSet()) {
                                if(Long.valueOf(groupId) == groups.getId()) {
                                    addList(list, k8sNamespace, k8sCluster, groups);
                                }
                            }
                        }
                    }
                }
                // namespaces in cluster.
                else {
                    for (K8sNamespace k8sNamespace : allNamespaces) {
                        K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
                        long id = k8sCluster.getId();
                        if (id == Long.valueOf(clusterId)) {
                            for(Groups groups : k8sNamespace.getGroupsSet()) {
                                addList(list, k8sNamespace, k8sCluster, groups);
                            }
                        }
                    }
                }
            } else {
                // namespaces in the group.
                if(groupId != null) {
                    for (K8sNamespace k8sNamespace : allNamespaces) {
                        K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
                        for(Groups groups : k8sNamespace.getGroupsSet()) {
                            if(Long.valueOf(groupId) == groups.getId()) {
                                addList(list, k8sNamespace, k8sCluster, groups);
                            }
                        }
                    }
                }
                else {
                    for (K8sNamespace k8sNamespace : allNamespaces) {
                        K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
                        for(Groups groups : k8sNamespace.getGroupsSet()) {
                            addList(list, k8sNamespace, k8sCluster, groups);
                        }
                    }
                }
            }

            return JsonUtils.toJson(mapper, list);
        });
    }

    private void addList(List<Map<String, Object>> list, K8sNamespace k8sNamespace, K8sCluster k8sCluster, Groups groups) {
        Map<String, Object> map = new HashMap<>();
        map.put("namespaceId", k8sNamespace.getId());
        map.put("namespace", k8sNamespace.getNamespaceName());
        map.put("clusterId", k8sCluster.getId());
        map.put("clusterName", k8sCluster.getClusterName());
        map.put("groupId", groups.getId());
        map.put("groupName", groups.getGroup());

        list.add(map);
    }

    @DeleteMapping("/apis/k8s/delete_namespace")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_GROUP_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");

            K8sNamespace k8sNamespace = k8sNamespaceService.findOne(Long.valueOf(namespaceId));
            k8sNamespace.setGroupsSet(new HashSet<>());
            k8sNamespaceService.deleteNamespace(k8sNamespace);
            return ControllerUtils.successMessage();
        });
    }
}
