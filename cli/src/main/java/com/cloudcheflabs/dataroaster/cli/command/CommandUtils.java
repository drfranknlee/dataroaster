package com.cloudcheflabs.dataroaster.cli.command;

import com.cloudcheflabs.dataroaster.cli.api.dao.*;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.cli.domain.ServiceDef;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public class CommandUtils {

    public static int createProject(ConfigProps configProps,
                                    String name,
                                    String description) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.createProject(configProps, name, description);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("project created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createCluster(ConfigProps configProps,
                                    String name,
                                    String description) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);

        RestResponse restResponse = clusterDao.createCluster(configProps, name, description);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("cluster created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createKubeconfig(ConfigProps configProps,
                                       String clusterId,
                                       String kubeconfig) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        KubeconfigDao kubeconfigDao = applicationContext.getBean(KubeconfigDao.class);
        RestResponse restResponse = kubeconfigDao.createKubeconfig(configProps, Long.valueOf(clusterId), kubeconfig);
        if(restResponse.getStatusCode() == 200) {
            System.out.println("kubeconfig created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int createIngressController(ConfigProps configProps,
                                              String projectId,
                                              String clusterId) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = serviceDefDao.listServiceDef(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.INGRESS_CONTROLLER.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }


        // create.
        IngressControllerDao ingressControllerDao = applicationContext.getBean(IngressControllerDao.class);
        restResponse = ingressControllerDao.createIngressController(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId));

        if(restResponse.getStatusCode() == 200) {
            System.out.println("ingress controller service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }

    public static int makeSureIngressHostRegistered(ConfigProps configProps,
                                                    String clusterId,
                                                    java.io.Console cnsl) {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        IngressControllerDao ingressControllerDao = applicationContext.getBean(IngressControllerDao.class);

        int count = 0;
        int MAX = 5;
        String externalIp = null;
        for(int i = 0; i < MAX; i++) {
            RestResponse restResponse = ingressControllerDao.getExternalIpOfIngressControllerNginx(configProps, Long.valueOf(clusterId));

            // if response status code is not ok.
            if (restResponse.getStatusCode() != RestResponse.STATUS_OK) {
                count++;
                if(count == MAX) {
                    throw new RuntimeException(restResponse.getErrorMessage());
                } else {
                    System.out.println("service of ingress controller nginx is not yet ready...");
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {}
                }
            } else {
                externalIp = restResponse.getSuccessMessage();
                break;
            }
        }

        System.out.printf("External IP Address of Ingress Controller NGINX Service: %s\n", externalIp);
        System.out.println("\n");
        System.out.println("Before moving on, make sure that your Ingress Hosts have been registered with this IP Address to your public DNS server.");

        String yN = cnsl.readLine("Continue(y/N) : ");
        if(yN != null) {
            if(yN.toLowerCase().equals("n") || yN.toLowerCase().equals("no")) {
                System.err.println("blueprint cancelled...");
                return -1;
            } else if(yN.toLowerCase().equals("y") || yN.toLowerCase().equals("yes")) {
                System.out.println("ok...");
            } else {
                System.err.println("blueprint cancelled...");
                return -1;
            }
        } else {
            System.err.println("blueprint cancelled...");
            return -1;
        }

        return 0;
    }
}
