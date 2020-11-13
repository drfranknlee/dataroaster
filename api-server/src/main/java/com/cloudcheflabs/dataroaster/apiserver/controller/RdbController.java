package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.CockroachDBService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RdbController {

    private static Logger LOG = LoggerFactory.getLogger(RdbController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("cockroachDBServiceImpl")
    private CockroachDBService cockroachDBService;

    @PostMapping("/apis/rdb/cockroachdb/create")
    public String createCockroachDB(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String storage = params.get("storage");
            String cpu = params.get("cpu");
            String memory = params.get("memory");
            String nodes = params.get("nodes");

            cockroachDBService.create(Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    Integer.valueOf(storage),
                    Integer.valueOf(cpu),
                    Integer.valueOf(memory),
                    Integer.valueOf(nodes));

            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/rdb/cockroachdb/delete")
    public String deleteCockroachDB(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            cockroachDBService.delete(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
