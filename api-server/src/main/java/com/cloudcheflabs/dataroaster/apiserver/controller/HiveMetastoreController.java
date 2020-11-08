package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudcheflabs.dataroaster.apiserver.api.service.HiveMetastoreService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
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
public class HiveMetastoreController {

    private static Logger LOG = LoggerFactory.getLogger(HiveMetastoreController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("hiveMetastoreServiceImpl")
    private HiveMetastoreService hiveMetastoreService;


    @PostMapping("/apis/hive_metastore/create")
    public String createHiveMetastore(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String s3StorageType = params.get("s3_storage_type");
            String bucket = params.get("bucket");
            String storageSize = params.get("storageSize");

            hiveMetastoreService.createHiveMetastore(Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    bucket,
                    Integer.valueOf(storageSize),
                    s3StorageType);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/hive_metastore/delete")
    public String deleteHiveMetastore(@RequestParam Map<String, String> params) {

        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            hiveMetastoreService.deleteHiveMetastore(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
