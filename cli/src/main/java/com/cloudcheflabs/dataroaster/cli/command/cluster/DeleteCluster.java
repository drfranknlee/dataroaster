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

@CommandLine.Command(name = "delete",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Delete Kubernetes Cluster Metadata.")
public class DeleteCluster implements Callable<Integer> {

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

        String clusterId = cnsl.readLine("Select Cluster to be deleted : ");

        // create kubernetes cluster.
        restResponse = clusterDao.deleteCluster(configProps, Long.valueOf(clusterId));
        if(restResponse.getStatusCode() == 200) {
            System.out.println("cluster deleted successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }
}
