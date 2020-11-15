package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import com.cloudcheflabs.dataroaster.apiserver.api.service.IngressControllerService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class IngressController {

    private static Logger LOG = LoggerFactory.getLogger(IngressController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("ingressControllerServiceImpl")
    private IngressControllerService ingressControllerService;

    @PostMapping("/apis/ingress_controller/nginx/create")
    public String createIngressNginx(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");
            String port = params.get("port");

            ingressControllerService.createIngressNginx(Long.valueOf(clusterId), Long.valueOf(serviceId), Integer.valueOf(port));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/ingress_controller/nginx/delete")
    public String deleteIngressNginx(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            ingressControllerService.deleteIngressNginx(Long.valueOf(clusterId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }


    @PostMapping("/apis/load_balancer/metallb/create")
    public String createMetalLB(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");
            String fromIp = params.get("from_ip");
            String toIp = params.get("to_ip");

            ingressControllerService.createMetalLB(Long.valueOf(clusterId), Long.valueOf(serviceId), fromIp, toIp);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/load_balancer/metallb/delete")
    public String deleteMetalLB(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            ingressControllerService.deleteMetalLB(Long.valueOf(clusterId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }

    @PostMapping("/apis/ingress/minio/create")
    public String createMinIOIngress(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String host = params.get("host");

            ingressControllerService.createMinIOIngress(Long.valueOf(namespaceId), Long.valueOf(serviceId), host);
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/apis/ingress/minio/delete")
    public String deleteMinIOIngress(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            ingressControllerService.deleteMinIOIngress(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/apis/ingress/minio/list")
    public String listMinIOIngress(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String namespaceId = params.get("namespace_id");

            List<Ingress> ingresses = ingressControllerService.getIngresses(Long.valueOf(namespaceId));
            List<Ingress> selectedIngresses = new ArrayList<>();
            for(Ingress ingress : ingresses) {
                if(ingress.getMetadata().getName().startsWith("ingress-minio")) {
                    selectedIngresses.add(ingress);
                }
            }

            return JsonUtils.toJson(mapper, selectedIngresses);
        });
    }

    @PostMapping("/apis/ingress/ozone/create")
    public String createOzoneIngress(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String host = params.get("host");

            ingressControllerService.createOzoneIngress(Long.valueOf(namespaceId), Long.valueOf(serviceId), host);
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/apis/ingress/ozone/delete")
    public String deleteOzoneIngress(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            ingressControllerService.deleteOzoneIngress(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
