package com.cloudcheflabs.dataroaster.cli.command;

import com.cloudcheflabs.dataroaster.cli.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.KubeconfigDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.ProjectDao;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import org.springframework.context.ApplicationContext;

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
}
