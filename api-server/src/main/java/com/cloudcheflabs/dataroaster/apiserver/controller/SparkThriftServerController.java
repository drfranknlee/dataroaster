package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudcheflabs.dataroaster.apiserver.api.service.SparkThriftServerService;
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
public class SparkThriftServerController {

    private static Logger LOG = LoggerFactory.getLogger(SparkThriftServerController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("sparkThriftServerServiceImpl")
    private SparkThriftServerService sparkThriftServerService;


    @PostMapping("/apis/spark_thrift_server/create")
    public String createSparkThriftServer(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String bucket = params.get("bucket");
            String executors = params.get("executors");
            String executorMemory = params.get("executor_memory");
            String executorCore = params.get("executor_core");
            String driverMemory = params.get("driver_memory");
            String s3StorageType = params.get("s3_storage_type");


            sparkThriftServerService.createSparkThriftServer(Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    bucket,
                    Integer.valueOf(executors),
                    Integer.valueOf(executorMemory),
                    Integer.valueOf(executorCore),
                    Integer.valueOf(driverMemory),
                    s3StorageType);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/spark_thrift_server/delete")
    public String deleteSparkThriftServer(@RequestParam Map<String, String> params) {

        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            sparkThriftServerService.deleteSparkThriftServer(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
