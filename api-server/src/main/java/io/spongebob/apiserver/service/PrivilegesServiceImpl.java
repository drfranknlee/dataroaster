package io.spongebob.apiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.dao.AuthorizerDao;
import io.spongebob.apiserver.api.dao.PrivilegesDao;
import io.spongebob.apiserver.api.service.PrivilegesService;
import io.spongebob.apiserver.domain.AuthorizerResponse;
import io.spongebob.apiserver.domain.Privileges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrivilegesServiceImpl implements PrivilegesService {

    private static Logger LOG = LoggerFactory.getLogger(PrivilegesServiceImpl.class);

    @Autowired
    private PrivilegesDao privilegesDao;

    @Autowired
    private AuthorizerDao authorizerDao;

    private ObjectMapper mapper = new ObjectMapper();


    @Override
    public void putPrivileges(String accessKey, Privileges privileges) {
        privilegesDao.putPrivileges(accessKey, privileges);
    }

    @Override
    public Privileges getPrivileges(String accessToken) {

        Privileges privileges = privilegesDao.getPrivileges(accessToken);

        // cache 에 access token 이 없을 경우.
        if(privileges == null)
        {
            if(LOG.isDebugEnabled()) LOG.debug("access token [{}] does NOT EXIST in the cache...", accessToken);

            privileges = new Privileges();

            AuthorizerResponse authorizerResponse = authorizerDao.getPrivileges(accessToken);

            privileges.setAuthorizerResponse(authorizerResponse);

            // 인증 server 로 부터 권한 정보를 성공적으로 받았을 경우.
            if(authorizerResponse.getStatusCode() == AuthorizerResponse.STATUS_OK)
            {
                // put privileges to the cache.
                privilegesDao.putPrivileges(accessToken, privileges);
            }
        }
        else
        {
            if(LOG.isDebugEnabled()) LOG.debug("access token [{}] exists in the cache...", accessToken);
        }

        return privileges;
    }
}
