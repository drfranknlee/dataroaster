package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.MonitoringService;
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
public class MonitoringController {

    private static Logger LOG = LoggerFactory.getLogger(MonitoringController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("monitoringServiceImpl")
    private MonitoringService monitoringService;


    @PostMapping("/apis/monitoring/prometheus_stack/create")
    public String createPrometheusStack(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");
            String storageClass = params.get("storage_class");
            String storageSize = params.get("storage_size");

            monitoringService.createPrometheusStack(Long.valueOf(serviceId),
                    Long.valueOf(clusterId), storageClass, Integer.valueOf(storageSize));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/monitoring/prometheus_stack/delete")
    public String deletePrometheusStack(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            monitoringService.deletePrometheusStack(Long.valueOf(serviceId), Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }



    @PostMapping("/apis/monitoring/metrics_server/create")
    public String createMetricsServer(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");


            monitoringService.createMetricsServer(Long.valueOf(serviceId), Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/monitoring/metrics_server/delete")
    public String deleteMetricsServer(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            monitoringService.deleteMetricsServer(Long.valueOf(serviceId), Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }
}
