package com.cloudcheflabs.dataroaster.cli.command.blueprint;

import com.cloudcheflabs.dataroaster.cli.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.ProjectDao;
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

@CommandLine.Command(name = "delete",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Delete Blueprint Deployment.")
public class DeleteBlueprint implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Blueprint parent;

    @CommandLine.Option(names = {"--blueprint"}, description = "Blueprint Yaml File Path.", required = true)
    private File blueprintFile;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();
        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }

        // show project list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.listProjects(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> projectLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%n";

        System.out.printf(format,"PROJECT ID", "PROJECT NAME", "PROJECT DESCRIPTION");
        for(Map<String, Object> map : projectLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }

        String projectId = cnsl.readLine("Select Project : ");
        if(projectId == null) {
            throw new RuntimeException("project id is required!");
        }

        System.out.printf("\n");


        // show cluster list.
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);
        restResponse = clusterDao.listClusters(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> clusterLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        System.out.printf(format,"CLUSTER ID", "CLUSTER NAME", "CLUSTER DESCRIPTION");
        for(Map<String, Object> map : clusterLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }

        System.out.printf("\n");

        String clusterId = cnsl.readLine("Select Cluster : ");
        if(clusterId == null) {
            throw new RuntimeException("cluster id is required!");
        }

        System.out.printf("\n");

        // read blueprint yaml.
        String blueprintFileAbsolutePath = blueprintFile.getAbsolutePath();
        String blueprint = FileUtils.fileToString(blueprintFileAbsolutePath, false);

        // TODO:


        return 0;
    }
}
