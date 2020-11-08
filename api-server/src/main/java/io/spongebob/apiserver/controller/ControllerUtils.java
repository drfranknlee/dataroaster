package io.spongebob.apiserver.controller;

import io.spongebob.apiserver.domain.Roles;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.Callable;

public class ControllerUtils {

    public static String successMessage() {
        return "{ 'result': 'SUCCESS'}";
    }

    public static String doProcess(Roles roles, HttpServletRequest context, Callable<String> task) {
        // role level.
        int allowedRoleLevel = roles.getLevel();

        // max role level of request user.
        int maxRoleLevel = RoleUtils.getMaxRoleLevel(context);

        if(maxRoleLevel >= allowedRoleLevel) {
            try {
                return task.call();
            } catch (Exception e) {
                if(e instanceof ResponseStatusException) {
                    throw (ResponseStatusException) e;
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT ALLOWED: NO PRIVILEGES");
        }
    }
}
