package io.spongebob.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.service.CertManagerService;
import io.spongebob.apiserver.api.service.ResourceMonitorService;
import io.spongebob.apiserver.domain.Roles;
import io.spongebob.apiserver.domain.model.*;
import io.spongebob.apiserver.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ResourceMonitorController {

    private static Logger LOG = LoggerFactory.getLogger(ResourceMonitorController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("resourceMonitorServiceImpl")
    private ResourceMonitorService resourceMonitorService;

    @GetMapping("/apis/resource_monitor/monitor")
    public String monitor(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String clusterId = params.get("cluster_id");
            clusterId = (clusterId == null) ? "-1" : clusterId;
            String namespaceName = params.get("namespace_name");
            String type = params.get("type");

            return resourceMonitorService.monitorResource(Long.valueOf(clusterId), namespaceName, type);
        });
    }
}
