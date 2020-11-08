package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudcheflabs.dataroaster.apiserver.api.service.ObjectStorageService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.filter.AuthorizationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ObjectStorageController {

    private static Logger LOG = LoggerFactory.getLogger(ObjectStorageController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("objectStorageServiceImpl")
    private ObjectStorageService objectStorageService;

    @PostMapping("/apis/object_storage/minio/create_minio")
    public String createMinIO(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String tenantName = params.get("tenant_name");
            String servers = params.get("servers");
            String volumes = params.get("volumes");
            String capacity = params.get("capacity");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            objectStorageService.createMinIO(userName,
                    Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    tenantName,
                    Integer.valueOf(servers),
                    Integer.valueOf(volumes),
                    Integer.valueOf(capacity));
            return ControllerUtils.successMessage();
        });
    }


    @PostMapping("/apis/object_storage/minio/expand_minio")
    public String expandMinIO(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String servers = params.get("servers");
            String volumes = params.get("volumes");
            String capacity = params.get("capacity");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            objectStorageService.expandMinIO(userName,
                    Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    Integer.valueOf(servers),
                    Integer.valueOf(volumes),
                    Integer.valueOf(capacity));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/object_storage/minio/delete_tenant")
    public String deleteMinIOTenant(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            objectStorageService.deleteMinIOTenant(userName, Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }

    @PostMapping("/apis/object_storage/minio/create_operator")
    public String createMinIOOperator(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            objectStorageService.createMinIOOperator(Long.valueOf(clusterId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/object_storage/minio/delete_operator")
    public String deleteMinIOOperator(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            objectStorageService.deleteMinIOOperator(Long.valueOf(clusterId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }


    @PostMapping("/apis/object_storage/ozone/create")
    public String createOzone(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String datanodeReplicas = params.get("datanode_replicas");
            String datanodeStorageSize = params.get("datanode_storage_size");
            String omStorageSize = params.get("om_storage_size");
            String s3gStorageSize = params.get("s3g_storage_size");
            String scmStorageSize = params.get("scm_storage_size");
            String storageClass = params.get("storage_class");

            objectStorageService.createOzone(Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    Integer.valueOf(datanodeReplicas),
                    Integer.valueOf(datanodeStorageSize),
                    Integer.valueOf(omStorageSize),
                    Integer.valueOf(s3gStorageSize),
                    Integer.valueOf(scmStorageSize),
                    storageClass);
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/apis/object_storage/ozone/delete")
    public String deleteOzone(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            objectStorageService.deleteOzone(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
