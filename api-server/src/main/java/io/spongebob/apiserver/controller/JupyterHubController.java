package io.spongebob.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.service.JupyterHubService;
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
public class JupyterHubController {

    private static Logger LOG = LoggerFactory.getLogger(JupyterHubController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("jupyterHubServiceImpl")
    private JupyterHubService jupyterHubService;

    @PostMapping("/apis/jupyterhub/create")
    public String createJupyterHub(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String storage = params.get("storage");

            jupyterHubService.createJupyterHub(Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    Integer.valueOf(storage));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/jupyterhub/delete")
    public String deleteJupyterHub(@RequestParam Map<String, String> params) {

        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            jupyterHubService.deleteJupyterHub(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
