package io.spongebob.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.domain.Roles;
import io.spongebob.apiserver.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RolesController {

    private static Logger LOG = LoggerFactory.getLogger(RolesController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;


    @RequestMapping("/apis/roles/list")
    public String list() {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();
            for(Roles roles : Roles.values()) {
                String roleName = roles.name();
                Map<String, Object> map = new HashMap<>();
                map.put("roleName", roleName);

                mapList.add(map);
            }

            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
