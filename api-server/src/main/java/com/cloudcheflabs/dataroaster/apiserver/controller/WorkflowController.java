package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.ArgoService;
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
public class WorkflowController {

    private static Logger LOG = LoggerFactory.getLogger(WorkflowController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("argoServiceImpl")
    private ArgoService argoService;

    @PostMapping("/apis/workflow/argo/create")
    public String createArgo(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String bucket = params.get("bucket");
            String s3Endpoint = params.get("s3_endpoint");
            String insecure = params.get("insecure");
            String accessKey = params.get("access_key");
            String secretKey = params.get("secret_key");

            argoService.createArgo(Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    bucket,
                    s3Endpoint,
                    Boolean.valueOf(insecure),
                    accessKey,
                    secretKey);

            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/workflow/argo/delete")
    public String deleteArgo(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            argoService.deleteArgo(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
