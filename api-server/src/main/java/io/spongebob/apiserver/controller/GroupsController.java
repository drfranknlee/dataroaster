package io.spongebob.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.service.GroupsService;
import io.spongebob.apiserver.domain.Roles;
import io.spongebob.apiserver.domain.model.Groups;
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
public class GroupsController {

    private static Logger LOG = LoggerFactory.getLogger(GroupsController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("groupsServiceImpl")
    private GroupsService groupsService;

    @PostMapping(value = "/apis/groups/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String groupName = params.get("group_name");

            Groups groups = new Groups();
            groups.setGroup(groupName);
            groupsService.create(groups);

            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping(value = "/apis/groups/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String id = params.get("id");

            Groups groups = groupsService.findOne(Long.valueOf(id));
            groupsService.delete(groups);

            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/apis/groups/list")
    public String list() {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            List<Groups> groupsList = groupsService.findAll();

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(Groups groups : groupsList) {
                long id = groups.getId();
                String groupName = groups.getGroup();
                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("groupName", groupName);

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
