package io.spongebob.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spongebob.apiserver.api.service.K8sClusterService;
import io.spongebob.apiserver.api.service.K8sServicesService;
import io.spongebob.apiserver.api.service.SecretService;
import io.spongebob.apiserver.domain.Kubeconfig;
import io.spongebob.apiserver.domain.Roles;
import io.spongebob.apiserver.domain.model.K8sCluster;
import io.spongebob.apiserver.domain.model.K8sNamespace;
import io.spongebob.apiserver.domain.model.K8sServices;
import io.spongebob.apiserver.kubernetes.YamlUtils;
import io.spongebob.apiserver.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.Namespace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class K8sClusterController {

    private static Logger LOG = LoggerFactory.getLogger(K8sClusterController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("k8sClusterServiceImpl")
    private K8sClusterService k8sClusterService;

    @Autowired
    @Qualifier("kubeconfigSecretServiceImpl")
    private SecretService<Kubeconfig> secretService;

    @Autowired
    @Qualifier("k8sServicesServiceImpl")
    private K8sServicesService k8sServicesService;

    @PostMapping("/apis/k8s/create_cluster")
    public String createCluster(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterName = params.get("cluster_name");
            String description = params.get("description");
            String kubeconfig = params.get("kubeconfig");

            k8sClusterService.createCluster(clusterName, description, kubeconfig);
            return ControllerUtils.successMessage();
        });
    }



    @PutMapping("/apis/k8s/update_cluster")
    public String updateCluster(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String description = params.get("description");
            String kubeconfig = params.get("kubeconfig");

            k8sClusterService.updateCluster(Long.valueOf(clusterId), description, kubeconfig);
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/apis/k8s/delete_cluster")
    public String deleteCluster(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");

            k8sClusterService.deleteCluster(Long.valueOf(clusterId));
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/apis/k8s/get_kubeconfig_admin")
    public String getKubeconfigAdmin(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String clusterId = params.get("cluster_id");
            String yaml = params.get("yaml");

            K8sCluster k8sCluster = k8sClusterService.findOne(Long.valueOf(clusterId));

            String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
            Kubeconfig kubeconfig = secretService.readSecret(path, Kubeconfig.class);

            if(Boolean.valueOf(yaml)) {
                return YamlUtils.getKubeconfigYaml(kubeconfig);
            } else {
                return JsonUtils.toJson(mapper, kubeconfig);
            }
        });
    }


    @GetMapping("/apis/k8s/list_cluster")
    public String listCluster(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            List<K8sCluster> list = k8sClusterService.findAll();

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(K8sCluster k8sCluster : list) {
                long id = k8sCluster.getId();
                String clusterName = k8sCluster.getClusterName();

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("clusterName", clusterName);

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }


    @GetMapping("/apis/k8s/list_cluster_namespace")
    public String listClusterNamespace(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            List<K8sCluster> list = k8sClusterService.findAll();

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(K8sCluster k8sCluster : list) {
                long id = k8sCluster.getId();
                String clusterName = k8sCluster.getClusterName();

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("clusterName", clusterName);

                List<Map<String, Object>> namespaceList = new ArrayList<>();
                for(K8sNamespace k8sNamespace : k8sCluster.getK8sNamespaceSet()) {
                    Map<String, Object> namespaceMap = new HashMap<>();
                    namespaceMap.put("namespaceId", k8sNamespace.getId());
                    namespaceMap.put("namespaceName", k8sNamespace.getNamespaceName());
                    namespaceList.add(namespaceMap);
                }
                map.put("namespaces", namespaceList);

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }



    @GetMapping("/apis/k8s/list_storages")
    public String listStorages(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String type = params.get("type");

            Map<String, Object> map = new HashMap<>();
            if(type == null) {
                map.put("csi", csiMapList());
                map.put("nfs", nfsMapList());
                map.put("minioOperator", minioOperatorMapList());
                map.put("minio", minioMapList());
                map.put("ozone", ozoneMapList());
            } else {
                if(type.equals("CSI")) {
                    map.put("csi", csiMapList());
                } else if(type.equals("NFS")) {
                    map.put("nfs", nfsMapList());
                } else if(type.equals("MINIO_OPERATOR")) {
                    map.put("minioOperator", minioOperatorMapList());
                } else if(type.equals("MINIO")) {
                    map.put("minio", minioMapList());
                } else if(type.equals("OZONE")) {
                    map.put("ozone", ozoneMapList());
                }
            }
            return JsonUtils.toJson(mapper, map);
        });
    }


    @GetMapping("/apis/k8s/list_ingress_controller")
    public String listIngressController(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String type = params.get("type");

            Map<String, Object> map = new HashMap<>();
            if(type == null) {
                map.put("nginx", nginxMapList());
                map.put("certManager", certManagerMapList());
                map.put("metalLB", metalLBMapList());
            } else {
                if(type.equals("NGINX")) {
                    map.put("nginx", nginxMapList());
                } else if(type.equals("CERT_MANAGER")) {
                    map.put("certManager", certManagerMapList());
                } else if(type.equals("METAL_LB")) {
                    map.put("metalLB", metalLBMapList());
                }
            }
            return JsonUtils.toJson(mapper, map);
        });
    }

    @GetMapping("/apis/k8s/list_data_platform")
    public String listDataPlatform(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String type = params.get("type");

            Map<String, Object> map = new HashMap<>();
            if(type == null) {
                map.put("hiveMetastore", hiveMetastoreMapList());
                map.put("sparkThriftServer", sparkThriftServerMapList());
                map.put("presto", prestoMapList());
                map.put("kafka", kafkaMapList());
                map.put("elasticsearch", elasticsearchMapList());
            } else {
                if(type.equals("HIVE_METASTORE")) {
                    map.put("hiveMetastore", hiveMetastoreMapList());
                } else if(type.equals("SPARK_THRIFT_SERVER")) {
                    map.put("sparkThriftServer", sparkThriftServerMapList());
                } else if(type.equals("PRESTO")) {
                    map.put("presto", prestoMapList());
                } else if(type.equals("KAFKA")) {
                    map.put("kafka", kafkaMapList());
                } else if(type.equals("ELASTICSEARCH")) {
                    map.put("elasticsearch", elasticsearchMapList());
                }
            }
            return JsonUtils.toJson(mapper, map);
        });
    }

    @GetMapping("/apis/k8s/list_bi")
    public String listBI(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String type = params.get("type");

            Map<String, Object> map = new HashMap<>();
            if(type == null) {
                map.put("redash", redashMapList());

            } else {
                if(type.equals("REDASH")) {
                    map.put("redash", redashMapList());
                }
            }
            return JsonUtils.toJson(mapper, map);
        });
    }

    @GetMapping("/apis/k8s/list_eda")
    public String listEDA(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String type = params.get("type");

            Map<String, Object> map = new HashMap<>();
            if(type == null) {
                map.put("jupyterhub", jupyterhubMapList());
            } else {
                if(type.equals("JUPYTERHUB")) {
                    map.put("jupyterhub", jupyterhubMapList());
                }
            }
            return JsonUtils.toJson(mapper, map);
        });
    }

    private List<Map<String, Object>> jupyterhubMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(14);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sNamespace k8sNamespace : k8sServices.getJupyterHubK8sNamespaceSet()) {
            long namespaceId = k8sNamespace.getId();
            String namespaceName = k8sNamespace.getNamespaceName();
            K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> redashMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(13);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sNamespace k8sNamespace : k8sServices.getRedashK8sNamespaceSet()) {
            long namespaceId = k8sNamespace.getId();
            String namespaceName = k8sNamespace.getNamespaceName();
            K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> hiveMetastoreMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(9);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sNamespace k8sNamespace : k8sServices.getHiveMetastoreK8sNamespaceSet()) {
            long namespaceId = k8sNamespace.getId();
            String namespaceName = k8sNamespace.getNamespaceName();
            K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> sparkThriftServerMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(11);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sNamespace k8sNamespace : k8sServices.getSparkThriftServerK8sNamespaceSet()) {
            long namespaceId = k8sNamespace.getId();
            String namespaceName = k8sNamespace.getNamespaceName();
            K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> prestoMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(12);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sNamespace k8sNamespace : k8sServices.getPrestoK8sNamespaceSet()) {
            long namespaceId = k8sNamespace.getId();
            String namespaceName = k8sNamespace.getNamespaceName();
            K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> kafkaMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(2);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sNamespace k8sNamespace : k8sServices.getKafkaK8sNamespaceSet()) {
            long namespaceId = k8sNamespace.getId();
            String namespaceName = k8sNamespace.getNamespaceName();
            K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> elasticsearchMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(15);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sNamespace k8sNamespace : k8sServices.getElasticsearchK8sNamespaceSet()) {
            long namespaceId = k8sNamespace.getId();
            String namespaceName = k8sNamespace.getNamespaceName();
            K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }


    private List<Map<String, Object>> nginxMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(3);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sCluster k8sCluster : k8sServices.getIngressControllerK8sClusterSet()) {
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();
            long namespaceId = -1;
            String namespaceName = "ingress-nginx";

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }


    private List<Map<String, Object>> certManagerMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(10);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sCluster k8sCluster : k8sServices.getCertManagerK8sClusterSet()) {
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();
            long namespaceId = -1;
            String namespaceName = "cert-manager";

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> metalLBMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(4);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sCluster k8sCluster : k8sServices.getLoadBalancerK8sClusterSet()) {
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();
            long namespaceId = -1;
            String namespaceName = "metallb-system";

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> csiMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(1);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sCluster k8sCluster : k8sServices.getK8sClusterSet()) {
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();
            long namespaceId = -1;
            String namespaceName = "minio";

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> nfsMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(5);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sCluster k8sCluster : k8sServices.getNfsK8sClusterSet()) {
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();
            long namespaceId = -1;
            String namespaceName = "minio";

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> minioOperatorMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(7);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sCluster k8sCluster : k8sServices.getObjectStorageOperatorK8sClusterSet()) {
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();
            long namespaceId = -1;
            String namespaceName = "minio";

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }


    private List<Map<String, Object>> minioMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(6);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sNamespace k8sNamespace : k8sServices.getK8sNamespaceSet()) {
            long namespaceId = k8sNamespace.getId();
            String namespaceName = k8sNamespace.getNamespaceName();
            K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }

    private List<Map<String, Object>> ozoneMapList() {
        K8sServices k8sServices = k8sServicesService.findOne(8);
        long serviceId = k8sServices.getId();
        String serviceName = k8sServices.getName();
        String version = k8sServices.getVersion();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for(K8sNamespace k8sNamespace : k8sServices.getK8sNamespaceSet()) {
            long namespaceId = k8sNamespace.getId();
            String namespaceName = k8sNamespace.getNamespaceName();
            K8sCluster k8sCluster = k8sNamespace.getK8sCluster();
            long clusterId = k8sCluster.getId();
            String clusterName = k8sCluster.getClusterName();

            Map<String, Object> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("clusterName", clusterName);
            map.put("namespaceId", namespaceId);
            map.put("namespaceName", namespaceName);
            map.put("serviceId", serviceId);
            map.put("serviceName", serviceName);
            map.put("version", version);

            mapList.add(map);
        }

        return mapList;
    }
}
