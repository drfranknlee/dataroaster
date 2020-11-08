package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudcheflabs.dataroaster.apiserver.api.service.CertManagerService;
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
public class CertManagerController {

    private static Logger LOG = LoggerFactory.getLogger(CertManagerController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("certManagerServiceImpl")
    private CertManagerService certManagerService;


    @PostMapping("/apis/cert_manager/create")
    public String createCsi(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            certManagerService.createCertManager(Long.valueOf(serviceId), Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/cert_manager/delete")
    public String deleteCsi(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            certManagerService.deleteCertManager(Long.valueOf(serviceId), Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }
}
