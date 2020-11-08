package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sClusterDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sNamespaceDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.ResourceControlDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.SecretDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.ResourceMonitorType;
import com.cloudcheflabs.dataroaster.apiserver.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.*;
import com.cloudcheflabs.dataroaster.apiserver.api.service.ResourceMonitorService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ResourceMonitorServiceImpl implements ResourceMonitorService {

    private static Logger LOG = LoggerFactory.getLogger(ResourceMonitorServiceImpl.class);

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    @Autowired
    @Qualifier("hibernateK8sClusterDao")
    private K8sClusterDao k8sClusterDao;

    @Autowired
    @Qualifier("hibernateK8sNamespaceDao")
    private K8sNamespaceDao k8sNamespaceDao;

    @Autowired
    @Qualifier("kubernetesResourceControlDao")
    private ResourceControlDao resourceControlDao;

    private ObjectMapper mapper = new ObjectMapper();

    public ResourceMonitorServiceImpl() {
        super();
    }

    @Override
    public String monitorResource(long clusterId, String namespace, String type) {
        try {
            Kubeconfig kubeconfig = null;
            if(clusterId != -1) {
                K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);
                String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
                kubeconfig = secretDao.readSecret(path, Kubeconfig.class);
            }

            String json = null;
            if (type.equals(ResourceMonitorType.NODE.name())) {
                List<Node> nodes = resourceControlDao.getNodes(kubeconfig);
                List<Map<String, Object>> mapList = new ArrayList<>();
                for (Node node : nodes) {
                    String name = node.getMetadata().getName();
                    List<NodeCondition> nodeConditions = node.getStatus().getConditions();
                    NodeCondition lastNodeCondition = nodeConditions.get(nodeConditions.size() - 1);
                    String status = lastNodeCondition.getType();
                    Map<String, String> labels = node.getMetadata().getLabels();
                    String role = (labels.containsKey("node-role.kubernetes.io/master")) ? "master" : "<none>";

                    // creationTimestamp: "2020-09-29T09:58:43Z"
                    String creationTimestamp = node.getMetadata().getCreationTimestamp();

                    org.joda.time.format.DateTimeFormatter formatter =
                            org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    DateTime dt = formatter.parseDateTime(creationTimestamp);

                    DateTime currentDt = DateTime.now();

                    long diffInSeconds = (currentDt.getMillis() - dt.getMillis()) / 1000;
                    String age = diffInSeconds + "s";

                    String version = node.getStatus().getNodeInfo().getKubeletVersion();

                    String internalIp = null;
                    String externalIp = null;
                    for (NodeAddress nodeAddress : node.getStatus().getAddresses()) {
                        if (nodeAddress.getType().equals("InternalIP")) {
                            internalIp = nodeAddress.getAddress();
                        } else if (nodeAddress.getType().equals("ExternalIP")) {
                            externalIp = nodeAddress.getAddress();
                        }
                    }

                    NodeStatus nodeStatus = node.getStatus();
                    String osImage = nodeStatus.getNodeInfo().getOsImage();
                    String kernelVersion = nodeStatus.getNodeInfo().getKernelVersion();
                    String containerRuntime = nodeStatus.getNodeInfo().getContainerRuntimeVersion();

                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("status", status);
                    map.put("role", role);
                    map.put("age", age);
                    map.put("version", version);
                    map.put("internalIp", internalIp);
                    map.put("externalIp", externalIp);
                    map.put("osImage", osImage);
                    map.put("kernelVersion", kernelVersion);
                    map.put("containerRuntime", containerRuntime);

                    mapList.add(map);
                }

                return JsonUtils.toJson(mapper, mapList);

            } else if (type.equals(ResourceMonitorType.NS.name())) {
                List<K8sCluster> k8sClusters = k8sClusterDao.findAll();
                List<Map<String, Object>> mapList = new ArrayList<>();
                for(K8sCluster k8sCluster : k8sClusters) {
                    try {
                        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
                        Kubeconfig kubeconfigAdmin = secretDao.readSecret(path, Kubeconfig.class);
                        List<Namespace> namespaces = resourceControlDao.getNamespaces(kubeconfigAdmin);

                        List<String> nsList = new ArrayList<>();
                        for (Namespace clusterNamespace : namespaces) {
                            nsList.add(clusterNamespace.getMetadata().getName());
                        }

                        Map<String, Object> map = new HashMap<>();
                        map.put("id", k8sCluster.getId());
                        map.put("clusterName", k8sCluster.getClusterName());
                        map.put("namespaces", nsList);

                        mapList.add(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }

                return JsonUtils.toJson(mapper, mapList);

            } else if (type.equals(ResourceMonitorType.SERVICE.name())) {
                List<io.fabric8.kubernetes.api.model.Service> services = resourceControlDao.getServices(kubeconfig, namespace);

                List<Map<String, Object>> mapList = new ArrayList<>();
                for(io.fabric8.kubernetes.api.model.Service service : services) {
                    String name = service.getMetadata().getName();
                    String serviceType = service.getSpec().getType();
                    String clusterIp = service.getSpec().getClusterIP();
                    String externalIp = "<none>";
                    LoadBalancerStatus loadBalancerStatus = service.getStatus().getLoadBalancer();
                    if(loadBalancerStatus != null) {
                        List<LoadBalancerIngress> loadBalancerIngresses = loadBalancerStatus.getIngress();
                        if(loadBalancerIngresses != null) {
                            if(loadBalancerIngresses.size() > 0) {
                                externalIp = loadBalancerIngresses.get(0).getIp();
                            }
                        }
                    }

                    String ports = "";
                    for(ServicePort servicePort : service.getSpec().getPorts()) {
                        int port = servicePort.getPort();
                        int nodePort = -1;
                        if(servicePort.getNodePort() != null) {
                            nodePort = servicePort.getNodePort();
                        }
                        String protocol = servicePort.getProtocol();
                        String portString = port + ((nodePort > 30000) ? ":" + nodePort : "") + "/" + protocol;
                        ports += ((ports.equals("")) ? "" : ",") + portString;
                    }

                    // creationTimestamp: "2020-09-29T09:58:43Z"
                    String creationTimestamp = service.getMetadata().getCreationTimestamp();

                    org.joda.time.format.DateTimeFormatter formatter =
                            org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    DateTime dt = formatter.parseDateTime(creationTimestamp);

                    DateTime currentDt = DateTime.now();

                    long diffInSeconds = (currentDt.getMillis() - dt.getMillis()) / 1000;
                    String age = diffInSeconds + "s";

                    String selector = "";
                    Map<String, String> selectors = service.getSpec().getSelector();
                    for(String key : selectors.keySet()) {
                        String value = selectors.get(key);
                        String kv = key + "=" + value;

                        selector += ((selector.equals("")) ? "" : ",") + kv;
                    }


                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("type", serviceType);
                    map.put("clusterIp", clusterIp);
                    map.put("externalIp", externalIp);
                    map.put("ports", ports);
                    map.put("age", age);
                    map.put("selector", selector);

                    mapList.add(map);
                }


                return JsonUtils.toJson(mapper, mapList);

            } else if (type.equals(ResourceMonitorType.ENDPOINT.name())) {
                List<Endpoints> endpoints = resourceControlDao.getEndpoints(kubeconfig, namespace);

                List<Map<String, Object>> mapList = new ArrayList<>();
                for(Endpoints ep : endpoints) {
                    String name = ep.getMetadata().getName();
                    String endpoint = "";
                    for(EndpointSubset endpointSubset : ep.getSubsets()) {
                        List<EndpointPort> endpointPorts = endpointSubset.getPorts();
                        for(EndpointAddress endpointAddress : endpointSubset.getAddresses()) {
                            String ip = endpointAddress.getIp();
                            for(EndpointPort endpointPort : endpointPorts) {
                                int port = endpointPort.getPort();
                                endpoint += ((endpoint.equals("") ? "" : ",") ) + ip + ":" + port;
                            }
                        }
                    }

                    // creationTimestamp: "2020-09-29T09:58:43Z"
                    String creationTimestamp = ep.getMetadata().getCreationTimestamp();

                    org.joda.time.format.DateTimeFormatter formatter =
                            org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    DateTime dt = formatter.parseDateTime(creationTimestamp);

                    DateTime currentDt = DateTime.now();

                    long diffInSeconds = (currentDt.getMillis() - dt.getMillis()) / 1000;
                    String age = diffInSeconds + "s";


                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("endpoint", endpoint);
                    map.put("age", age);

                    mapList.add(map);
                }


                return JsonUtils.toJson(mapper, mapList);

            } else if (type.equals(ResourceMonitorType.POD.name())) {
                List<Pod> pods = resourceControlDao.getPods(kubeconfig, namespace);

                List<Map<String, Object>> mapList = new ArrayList<>();
                for(Pod pod : pods) {

                    String name = pod.getMetadata().getName();
                    List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
                    int totalContainers = containerStatuses.size();
                    int readyCount = 0;
                    for(ContainerStatus containerStatus : containerStatuses) {
                        if(containerStatus.getReady()) {
                            readyCount++;
                        }
                    }
                    String ready = readyCount + "/" + totalContainers;
                    String status = pod.getStatus().getPhase();
                    int restarts = 0;
                    for(ContainerStatus containerStatus : containerStatuses) {
                        int restartCount = containerStatus.getRestartCount();
                        if(restartCount > restarts) {
                            restarts = restartCount;
                        }
                    }

                    // creationTimestamp: "2020-09-29T09:58:43Z"
                    String creationTimestamp = pod.getMetadata().getCreationTimestamp();

                    org.joda.time.format.DateTimeFormatter formatter =
                            org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    DateTime dt = formatter.parseDateTime(creationTimestamp);

                    DateTime currentDt = DateTime.now();

                    long diffInSeconds = (currentDt.getMillis() - dt.getMillis()) / 1000;
                    String age = diffInSeconds + "s";

                    String ip = pod.getStatus().getPodIP();
                    String node = pod.getSpec().getNodeName();

                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("ready", ready);
                    map.put("status", status);
                    map.put("restarts", restarts);
                    map.put("age", age);
                    map.put("ip", ip);
                    map.put("node", node);

                    mapList.add(map);
                }
                return JsonUtils.toJson(mapper, mapList);

            } else if (type.equals(ResourceMonitorType.DEPLOYMENT.name())) {
                List<Deployment> deployments = resourceControlDao.getDeployments(kubeconfig, namespace);

                List<Map<String, Object>> mapList = new ArrayList<>();
                for(Deployment deployment : deployments) {
                    String name = deployment.getMetadata().getName();
                    String ready = deployment.getStatus().getReadyReplicas() + "/" + deployment.getStatus().getReplicas();
                    int upToDate = deployment.getStatus().getUpdatedReplicas();
                    int available = deployment.getStatus().getAvailableReplicas();

                    // creationTimestamp: "2020-09-29T09:58:43Z"
                    String creationTimestamp = deployment.getMetadata().getCreationTimestamp();

                    org.joda.time.format.DateTimeFormatter formatter =
                            org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    DateTime dt = formatter.parseDateTime(creationTimestamp);

                    DateTime currentDt = DateTime.now();

                    long diffInSeconds = (currentDt.getMillis() - dt.getMillis()) / 1000;
                    String age = diffInSeconds + "s";

                    String containers = "";
                    String images = "";
                    List<Container> containerList = deployment.getSpec().getTemplate().getSpec().getContainers();
                    for(Container container : containerList) {
                        containers += ((containers.equals("")) ? "" : ",") + container.getName();
                        images += ((images.equals("")) ? "" : ",") + container.getImage();
                    }

                    String selectors = "";
                    Map<String, String> labels = deployment.getSpec().getSelector().getMatchLabels();
                    for(String key : labels.keySet()) {
                        String value = labels.get(key);
                        String kv = key + "=" + value;
                        selectors += ((selectors.equals("")) ? "" : ",") + kv;
                    }


                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("ready", ready);
                    map.put("upToDate", upToDate);
                    map.put("available", available);
                    map.put("age", age);
                    map.put("containers", containers);
                    map.put("images", images);
                    map.put("selectors", selectors);

                    mapList.add(map);
                }

                return JsonUtils.toJson(mapper, mapList);

            } else if (type.equals(ResourceMonitorType.STATEFULSET.name())) {
                List<StatefulSet> statefulSets = resourceControlDao.getStatefulsets(kubeconfig, namespace);

                List<Map<String, Object>> mapList = new ArrayList<>();
                for(StatefulSet statefulSet : statefulSets) {

                    String name = statefulSet.getMetadata().getName();
                    String ready = ((statefulSet.getStatus().getReadyReplicas() == null) ? 0 : statefulSet.getStatus().getReadyReplicas()) + "/" + statefulSet.getStatus().getReplicas();
                    // creationTimestamp: "2020-09-29T09:58:43Z"
                    String creationTimestamp = statefulSet.getMetadata().getCreationTimestamp();

                    org.joda.time.format.DateTimeFormatter formatter =
                            org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    DateTime dt = formatter.parseDateTime(creationTimestamp);

                    DateTime currentDt = DateTime.now();

                    long diffInSeconds = (currentDt.getMillis() - dt.getMillis()) / 1000;
                    String age = diffInSeconds + "s";

                    String containers = "";
                    String images = "";
                    List<Container> containerList = statefulSet.getSpec().getTemplate().getSpec().getContainers();
                    for(Container container : containerList) {
                        containers += ((containers.equals("")) ? "" : ",") + container.getName();
                        images += ((images.equals("")) ? "" : ",") + container.getImage();
                    }


                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("ready", ready);
                    map.put("age", age);
                    map.put("containers", containers);
                    map.put("images", images);

                    mapList.add(map);
                }


                return JsonUtils.toJson(mapper, mapList);

            } else if (type.equals(ResourceMonitorType.INGRESS.name())) {
                List<Ingress> ingresses = resourceControlDao.getIngresses(kubeconfig, namespace);

                List<Map<String, Object>> mapList = new ArrayList<>();
                for(Ingress ingress : ingresses) {

                    String name = ingress.getMetadata().getName();
                    String hosts = "";
                    for(IngressRule ingressRule : ingress.getSpec().getRules()) {
                        hosts = ((hosts.equals("")) ? "" : ",") + ingressRule.getHost();
                    }

                    String address = ingress.getStatus().getLoadBalancer().getIngress().get(0).getIp();

                    // creationTimestamp: "2020-09-29T09:58:43Z"
                    String creationTimestamp = ingress.getMetadata().getCreationTimestamp();

                    org.joda.time.format.DateTimeFormatter formatter =
                            org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    DateTime dt = formatter.parseDateTime(creationTimestamp);

                    DateTime currentDt = DateTime.now();

                    long diffInSeconds = (currentDt.getMillis() - dt.getMillis()) / 1000;
                    String age = diffInSeconds + "s";


                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("hosts", hosts);
                    map.put("address", address);
                    map.put("age", age);

                    mapList.add(map);
                }
                return JsonUtils.toJson(mapper, mapList);
            } else if (type.equals("SPARK_SA")) {
                List<Map<String, Object>> mapList = new ArrayList<>();
                for(K8sCluster k8sCluster : k8sClusterDao.findAll()) {
                    try {
                        long currentClusterId = k8sCluster.getId();
                        String clusterName = k8sCluster.getClusterName();
                        String path = k8sCluster.getK8sKubeconfigAdminSet().iterator().next().getSecretPath();
                        kubeconfig = secretDao.readSecret(path, Kubeconfig.class);

                        List<ServiceAccount> serviceAccounts = resourceControlDao.getSparkServiceAccount(kubeconfig);
                        for (ServiceAccount serviceAccount : serviceAccounts) {
                            String name = serviceAccount.getMetadata().getName();
                            // creationTimestamp: "2020-09-29T09:58:43Z"
                            String creationTimestamp = serviceAccount.getMetadata().getCreationTimestamp();

                            org.joda.time.format.DateTimeFormatter formatter =
                                    org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                            DateTime dt = formatter.parseDateTime(creationTimestamp);

                            DateTime currentDt = DateTime.now();

                            long diffInSeconds = (currentDt.getMillis() - dt.getMillis()) / 1000;
                            String age = diffInSeconds + "s";

                            String namespaceName = serviceAccount.getMetadata().getNamespace();

                            Map<String, Object> map = new HashMap<>();
                            map.put("name", name);
                            map.put("age", age);
                            map.put("clusterId", currentClusterId);
                            map.put("clusterName", clusterName);
                            map.put("namespaceName", namespaceName);

                            mapList.add(map);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                return JsonUtils.toJson(mapper, mapList);
            }

            return json;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
