package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudcheflabs.dataroaster.apiserver.api.service.PrestoService;
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
public class PrestoController {

    private static Logger LOG = LoggerFactory.getLogger(PrestoController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("prestoServiceImpl")
    private PrestoService prestoService;

    @PostMapping("/apis/presto/create")
    public String createPresto(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String serverMaxMemory = params.get("server_max_memory");
            String cpu = params.get("cpu");
            String tempDataStorage = params.get("temp_data_storage");
            String dataStorage = params.get("data_storage");
            String workers = params.get("workers");
            String s3StorageType = params.get("s3_storage_type");

            prestoService.createPresto(Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    Integer.valueOf(serverMaxMemory),
                    Integer.valueOf(cpu),
                    Integer.valueOf(tempDataStorage),
                    Integer.valueOf(dataStorage),
                    Integer.valueOf(workers),
                    s3StorageType);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/presto/delete")
    public String deletePresto(@RequestParam Map<String, String> params) {

        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            prestoService.deletePresto(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
