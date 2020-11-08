package io.spongebob.apiserver.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.service.SecretService;
import io.spongebob.apiserver.api.service.UsersService;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.Roles;
import io.spongebob.apiserver.domain.model.*;
import io.spongebob.apiserver.filter.AuthorizationFilter;
import io.spongebob.apiserver.kubernetes.YamlUtils;
import io.spongebob.apiserver.util.JsonUtils;
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
public class UsersController {

    private static Logger LOG = LoggerFactory.getLogger(UsersController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("usersServiceImpl")
    private UsersService usersService;

    @Autowired
    @Qualifier("kubeconfigSecretServiceImpl")
    private SecretService<Kubeconfig> secretService;


    @PostMapping("/signup")
    public String signUp(@RequestParam Map<String, String> params) {

        String userName = params.get("username");
        String password = params.get("password");

        // password encoding with bcrypt.
        password = BCrypt.withDefaults().hashToString(8, password.toCharArray());

        Users users = new Users();
        users.setUserName(userName);
        users.setPassword(password);
        users.setEnabled(true);

        usersService.signUp(users);

        return ControllerUtils.successMessage();
    }

    @PostMapping("/apis/users/create_certs")
    public String createCerts(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_GROUP_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String groupId = params.get("group_id");
            String namespaceId = params.get("namespace_id");
            String userIds = params.get("user_ids");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            String[] tokens = userIds.split(",");
            List<Long> userIdList = new ArrayList<>();
            for(String token : tokens) {
                userIdList.add(Long.valueOf(token.trim()));
            }

            int maxRoleLevel = RoleUtils.getMaxRoleLevel(context);
            Roles roles = RoleUtils.getMaxRole(maxRoleLevel);

            // if user is group admin.
            if(roles.name().equals(Roles.ROLE_GROUP_ADMIN.name())) {
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
            }

            usersService.creatCerts(Long.valueOf(clusterId), Long.valueOf(groupId), Long.valueOf(namespaceId), userIdList);

            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/apis/users/change_password")
    public String changePassword(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String userName = params.get("username");
            String newPassword = params.get("new_password");
            // password encoding with bcrypt.
            newPassword = BCrypt.withDefaults().hashToString(8, newPassword.toCharArray());

            usersService.changePassword(userName, newPassword);
            return ControllerUtils.successMessage();
        });
    }

    @PostMapping("/apis/users/join_group")
    public String joinGroup(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_GROUP_ADMIN, context, () -> {
            String userIds = params.get("user_ids");
            String groupId = params.get("group_id");

            String[] tokens = userIds.split(",");
            List<Long> userIdList = new ArrayList<>();
            for(String token : tokens) {
                userIdList.add(Long.valueOf(token.trim()));
            }

            usersService.joinGroup(userIdList, Long.valueOf(groupId));
            return ControllerUtils.successMessage();
        });
    }


    @PostMapping("/apis/users/leave_group")
    public String leaveGroup(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_GROUP_ADMIN, context, () -> {
            String userIds = params.get("user_ids");
            String groupId = params.get("group_id");

            String[] tokens = userIds.split(",");
            List<Long> userIdList = new ArrayList<>();
            for(String token : tokens) {
                userIdList.add(Long.valueOf(token.trim()));
            }

            usersService.leaveGroup(userIdList, Long.valueOf(groupId));
            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/apis/users/change_role")
    public String changeRole(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String userName = params.get("username");
            String role = params.get("role");

            usersService.changeRole(userName, role);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/users/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String id = params.get("id");

            Users users = usersService.findOne(Long.valueOf(id));
            usersService.delete(users);
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/apis/users/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String groupName = params.get("group_name");

            List<Users> usersList = usersService.findUsersByGroupName(groupName);
            List<Map<String, Object>> mapList = new ArrayList<>();
            for(Users users : usersList) {
                long id = users.getId();
                String userName = users.getUserName();

                Set<Groups> groupsSet = users.getGroupsSet();
                List<Map<String, Object>> groupsSetList = new ArrayList<>();
                for(Groups groups : groupsSet) {
                    long groupId = groups.getId();
                    String group = groups.getGroup();

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", groupId);
                    map.put("groupName", group);
                    groupsSetList.add(map);
                }

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("userName", userName);
                map.put("groups", groupsSetList);

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }

    @GetMapping("/apis/users/user_profile")
    public String userProfile(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);
            Users users = usersService.findByUserName(userName);
            Map<String, Object> map = new HashMap<>();
            map.put("userName", userName);
            List<Map<String, Object>> groupList = new ArrayList<>();
            for(Groups groups : users.getGroupsSet()) {
                long id = groups.getId();
                String groupName = groups.getGroup();
                Map<String, Object> groupMap = new HashMap<>();
                groupMap.put("groupId", id);
                groupMap.put("groupName", groupName);

                groupList.add(groupMap);
            }
            map.put("groups", groupList);
            map.put("role", users.getUserAuthoritiesSet().iterator().next().getAuthority());

            return JsonUtils.toJson(mapper, map);
        });
    }


    @GetMapping("/apis/users/list_cluster_namespace")
    public String listClusterWithNamespaces(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);
            Users users = usersService.findByUserName(userName);

            Map<Long, Map<String, Object>> allClusterMap = new HashMap<>();
            for(K8sKubeconfigUser k8sKubeconfigUser : users.getK8sKubeconfigUserSet()) {
                K8sNamespace k8sNamespace = k8sKubeconfigUser.getK8sNamespace();
                Map<String, Object> namespaceMap = new HashMap<>();
                namespaceMap.put("namespaceId", k8sNamespace.getId());
                namespaceMap.put("namespaceName", k8sNamespace.getNamespaceName());

                K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
                long clusterId = k8sCluster.getId();
                Map<String, Object> clusterMap = null;
                List<Map<String, Object>> namespaceList = null;
                if(allClusterMap.containsKey(clusterId)) {
                    clusterMap = allClusterMap.get(clusterId);
                    namespaceList = (List<Map<String, Object>>) clusterMap.get("namespaces");
                } else {
                    clusterMap = new HashMap<>();
                    clusterMap.put("id", clusterId);
                    clusterMap.put("clusterName", k8sCluster.getClusterName());
                    namespaceList = new ArrayList<>();
                }

                namespaceList.add(namespaceMap);
                clusterMap.put("namespaces", namespaceList);
                allClusterMap.put(clusterId, clusterMap);
            }

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(Map<String, Object> clusterMap : allClusterMap.values()) {
                mapList.add(clusterMap);
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }


    @GetMapping("/apis/users/get_kubeconfig")
    public String getKubeconfig(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespaceId = params.get("namespace_id");
            String yaml = params.get("yaml");

            LOG.debug("namespaceId: {}, yaml: {}", namespaceId, yaml);

            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            Users users = usersService.findByUserName(userName);
            String path = null;
            for(K8sKubeconfigUser k8sKubeconfigUser : users.getK8sKubeconfigUserSet()) {
                K8sNamespace k8sNamespace = k8sKubeconfigUser.getK8sNamespace();
                if(k8sNamespace.getId() == Long.valueOf(namespaceId)) {
                    path = k8sKubeconfigUser.getSecretPath();
                    break;
                }
            }

            Kubeconfig kubeconfig = secretService.readSecret(path, Kubeconfig.class);

            if(Boolean.valueOf(yaml)) {
                return YamlUtils.getKubeconfigYaml(kubeconfig);
            } else {
                return JsonUtils.toJson(mapper, kubeconfig);
            }
        });
    }


    @GetMapping("/apis/users/list_user_role")
    public String listAllUserRole(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();
            for(Users users : usersService.findAll()) {
                Map<String, Object> map = new HashMap<>();
                map.put("userName", users.getUserName());
                List<Map<String, Object>> groupList = new ArrayList<>();
                for (Groups groups : users.getGroupsSet()) {
                    long id = groups.getId();
                    String groupName = groups.getGroup();
                    Map<String, Object> groupMap = new HashMap<>();
                    groupMap.put("groupId", id);
                    groupMap.put("groupName", groupName);

                    groupList.add(groupMap);
                }
                map.put("groups", groupList);
                map.put("role", users.getUserAuthoritiesSet().iterator().next().getAuthority());

                mapList.add(map);
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }


    @GetMapping("/apis/users/list_user_role_namespace")
    public String listAllUsersWithNamespace(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(Users users : usersService.findAll()) {
                List<Map<String, Object>> namespaceList = new ArrayList<>();
                for(K8sKubeconfigUser k8sKubeconfigUser : users.getK8sKubeconfigUserSet()) {
                    K8sNamespace k8sNamespace = k8sKubeconfigUser.getK8sNamespace();
                    long namespaceId = k8sNamespace.getId();
                    String namespaceName = k8sNamespace.getNamespaceName();

                    K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
                    long clusterId = k8sCluster.getId();
                    String clusterName = k8sCluster.getClusterName();

                    Map<String, Object> kubeconfigNamespaceMap = new HashMap<>();
                    kubeconfigNamespaceMap.put("namespaceId", namespaceId);
                    kubeconfigNamespaceMap.put("namespaceName", namespaceName);
                    kubeconfigNamespaceMap.put("clusterId", clusterId);
                    kubeconfigNamespaceMap.put("clusterName", clusterName);

                    namespaceList.add(kubeconfigNamespaceMap);
                }


                Map<String, Object> map = new HashMap<>();
                map.put("userName", users.getUserName());
                List<Map<String, Object>> groupList = new ArrayList<>();
                for (Groups groups : users.getGroupsSet()) {
                    long id = groups.getId();
                    String groupName = groups.getGroup();
                    Map<String, Object> groupMap = new HashMap<>();
                    groupMap.put("groupId", id);
                    groupMap.put("groupName", groupName);

                    groupList.add(groupMap);
                }
                map.put("groups", groupList);
                map.put("role", users.getUserAuthoritiesSet().iterator().next().getAuthority());
                map.put("namespaces", namespaceList);

                mapList.add(map);
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
