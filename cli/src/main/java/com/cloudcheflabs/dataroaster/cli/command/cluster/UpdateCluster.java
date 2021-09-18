package com.cloudcheflabs.dataroaster.cli.command.cluster;

import com.cloudcheflabs.dataroaster.cli.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "update",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Update Kubernetes Cluster Metadata.")
public class UpdateCluster implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Cluster parent;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();

        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }

        // show cluster list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);
        RestResponse restResponse = clusterDao.listClusters(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> clusterLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%n";

        System.out.printf(format,"CLUSTER ID", "CLUSTER NAME", "CLUSTER DESCRIPTION");
        for(Map<String, Object> map : clusterLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }

        String clusterId = cnsl.readLine("Select Cluster ID to be updated : ");
        while(clusterId.equals("")) {
            System.err.println("cluster id is required!");
            clusterId = cnsl.readLine("Select Cluster ID to be updated : ");
            if(!clusterId.equals("")) {
                break;
            }
        }

        String name = cnsl.readLine("Enter Cluster Name : ");
        while(name.equals("")) {
            System.err.println("cluster name is required!");
            name = cnsl.readLine("Enter Cluster Name : ");
            if(!name.equals("")) {
                break;
            }
        }

        String description = cnsl.readLine("Enter Cluster Description : ");
        while(description.equals("")) {
            System.err.println("cluster description is required!");
            description = cnsl.readLine("Enter Cluster Description : ");
            if(!description.equals("")) {
                break;
            }
        }


        // create kubernetes cluster.
        restResponse = clusterDao.updateCluster(configProps, Long.valueOf(clusterId), name, description);
        if(restResponse.getStatusCode() == 200) {
            System.out.println("cluster updated successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }
}
