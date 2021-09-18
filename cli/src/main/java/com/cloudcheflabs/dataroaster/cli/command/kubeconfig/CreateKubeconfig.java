package com.cloudcheflabs.dataroaster.cli.command.kubeconfig;

import com.cloudcheflabs.dataroaster.cli.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Kubeconfig.")
public class CreateKubeconfig implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Kubeconfig parent;

    @CommandLine.Option(names = {"--kubeconfig"}, description = "Kubeconfig File Path.", required = true)
    private File kubeconfigFile;

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

        String clusterId = cnsl.readLine("Select Cluster ID : ");
        while(clusterId.equals("")) {
            System.err.println("cluster id is required!\n");
            clusterId = cnsl.readLine("Select Cluster ID : ");
            if(!clusterId.equals("")) {
                break;
            }
        }

        // create kubeconfig.
        String kubeconfigPath = kubeconfigFile.getAbsolutePath();
        String kubeconfig = FileUtils.fileToString(kubeconfigPath, false);

        return CommandUtils.createKubeconfig(configProps, clusterId, kubeconfig);
    }
}
