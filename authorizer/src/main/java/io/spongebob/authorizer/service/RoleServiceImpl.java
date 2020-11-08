package io.spongebob.authorizer.service;

import io.spongebob.authorizer.api.dao.UserDetailsDao;
import io.spongebob.authorizer.api.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private UserDetailsDao userDetailsDao;

    @Override
    public List<String> getRoles(String userName) {
        return userDetailsDao.getRoles(userName);
    }
}
