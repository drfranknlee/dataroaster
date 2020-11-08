package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudcheflabs.dataroaster.apiserver.api.service.K8sServicesService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.StorageClass;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sServices;
import com.cloudcheflabs.dataroaster.apiserver.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class K8sServicesController {

    private static Logger LOG = LoggerFactory.getLogger(K8sServicesController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("k8sServicesServiceImpl")
    private K8sServicesService k8sServicesService;

    @GetMapping("/apis/services/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String type = params.get("type");

            List<K8sServices> list = (type == null) ? k8sServicesService.findAll() : k8sServicesService.findListByType(type);

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(K8sServices k8sServices : list) {
                long id = k8sServices.getId();
                String currentType = k8sServices.getType();
                String name = k8sServices.getName();
                String version = k8sServices.getVersion();

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("type", currentType);
                map.put("name", name);
                map.put("version", version);

                mapList.add(map);
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }

    @GetMapping("/apis/services/object_storage/list")
    public String listObjectStorage(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespaceId = params.get("namespace_id");

            List<K8sServices> k8sServicesList = k8sServicesService.listObjectStorageByNamespace(Long.valueOf(namespaceId));
            List<Map<String, Object>> mapList = new ArrayList<>();
            for(K8sServices k8sServices : k8sServicesList) {
                long id = k8sServices.getId();
                String currentType = k8sServices.getType();
                String name = k8sServices.getName();
                String version = k8sServices.getVersion();

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("type", currentType);
                map.put("name", name);
                map.put("version", version);

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }

    @GetMapping("/apis/storage_class/list")
    public String listStorageClasses(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String clusterId = params.get("cluster_id");

            List<StorageClass> storageClasses = k8sServicesService.listStorageClasses(Long.valueOf(clusterId));
            return JsonUtils.toJson(mapper, storageClasses);
        });
    }
}
