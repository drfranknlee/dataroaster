package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.CsiService;
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
public class CsiController {

    private static Logger LOG = LoggerFactory.getLogger(CsiController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("csiServiceImpl")
    private CsiService csiService;


    @PostMapping("/apis/csi/create")
    public String createCsi(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");
            String dataRootPath = params.get("data_root_path");
            String dataDirCount = params.get("data_dir_count");

            csiService.createCsi(Long.valueOf(serviceId), Long.valueOf(clusterId), dataRootPath, Integer.valueOf(dataDirCount));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/csi/delete")
    public String deleteCsi(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            csiService.deleteCsi(Long.valueOf(serviceId), Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }



    @PostMapping("/apis/csi/mayastor/create")
    public String createMayastor(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");
            String workerNodes = params.get("worker_nodes");
            String disks = params.get("disks");

            csiService.createMayastor(Long.valueOf(serviceId), Long.valueOf(clusterId), workerNodes, disks);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/csi/mayastor/delete")
    public String deleteMayastor(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            csiService.deleteMayastor(Long.valueOf(serviceId), Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }
}
