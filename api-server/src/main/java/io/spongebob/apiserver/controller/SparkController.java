package io.spongebob.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.service.SparkService;
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
public class SparkController {

    private static Logger LOG = LoggerFactory.getLogger(SparkController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("sparkServiceImpl")
    private SparkService sparkService;


    @PostMapping("/apis/spark/env/create")
    public String createEnv(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");

            sparkService.createEnv(Long.valueOf(namespaceId));
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/apis/spark/env/delete")
    public String deleteEnv(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");

            sparkService.deleteEnv(Long.valueOf(namespaceId));
            return ControllerUtils.successMessage();
        });
    }
}
