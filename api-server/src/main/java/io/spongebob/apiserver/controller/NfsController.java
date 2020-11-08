package io.spongebob.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.service.NfsService;
import io.spongebob.apiserver.domain.Roles;
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
public class NfsController {

    private static Logger LOG = LoggerFactory.getLogger(NfsController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("nfsServiceImpl")
    private NfsService nfsService;


    @PostMapping("/apis/nfs/create")
    public String createNfs(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");
            String persistenceSize = params.get("persistence_size");

            nfsService.createNfs(Long.valueOf(serviceId), Long.valueOf(clusterId), Integer.valueOf(persistenceSize));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/nfs/delete")
    public String deleteNfs(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String serviceId = params.get("service_id");

            nfsService.deleteNfs(Long.valueOf(serviceId), Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }
}
