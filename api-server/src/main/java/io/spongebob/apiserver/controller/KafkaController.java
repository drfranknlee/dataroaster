package io.spongebob.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.service.KafkaService;
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
public class KafkaController {

    private static Logger LOG = LoggerFactory.getLogger(KafkaController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("kafkaServiceImpl")
    private KafkaService kafkaService;

    @PostMapping("/apis/kafka/create")
    public String createKafka(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");
            String kafkaStorage = params.get("kafka_storage");
            String zookeeperStorage = params.get("zookeeper_storage");

            kafkaService.createKafka(Long.valueOf(namespaceId),
                    Long.valueOf(serviceId),
                    Integer.valueOf(kafkaStorage),
                    Integer.valueOf(zookeeperStorage));
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/kafka/delete")
    public String deleteKafka(@RequestParam Map<String, String> params) {

        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String namespaceId = params.get("namespace_id");
            String serviceId = params.get("service_id");

            kafkaService.deleteKafka(Long.valueOf(namespaceId), Long.valueOf(serviceId));
            return ControllerUtils.successMessage();
        });
    }
}
