package io.spongebob.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.service.ElasticsearchService;
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
public class ElasticsearchController {

    private static Logger LOG = LoggerFactory.getLogger(ElasticsearchController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("elasticsearchServiceImpl")
    private ElasticsearchService elasticsearchService;

    @PostMapping("/apis/elasticsearch/create")
    public String createElasticsearch(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String esClusterName = params.get("es_cluster_name");
            String masterStorage = params.get("master_storage");
            String dataNodes = params.get("data_nodes");
            String dataStorage = params.get("data_storage");
            String clientNodes = params.get("client_nodes");
            String clientStorage = params.get("client_storage");

            elasticsearchService.createElasticsearch(Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    esClusterName,
                    Integer.valueOf(masterStorage),
                    Integer.valueOf(dataNodes),
                    Integer.valueOf(dataStorage),
                    Integer.valueOf(clientNodes),
                    Integer.valueOf(clientStorage));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/elasticsearch/delete")
    public String deleteElasticsearch(@RequestParam Map<String, String> params) {

        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            elasticsearchService.deleteElasticsearch(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
