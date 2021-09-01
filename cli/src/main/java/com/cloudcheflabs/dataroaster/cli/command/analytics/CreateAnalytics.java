package com.cloudcheflabs.dataroaster.cli.command.analytics;

import com.cloudcheflabs.dataroaster.cli.api.dao.*;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.cli.domain.ServiceDef;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Analytics.")
public class CreateAnalytics implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Analytics parent;

    @CommandLine.Option(names = {"--jupyterhub-github-client-id"}, description = "Jupyterhub GitHub OAuth Client ID.", required = true)
    private String jupyterhubGithubClientId;

    @CommandLine.Option(names = {"--jupyterhub-github-client-secret"}, description = "Jupyterhub GitHub OAuth Client Secret.", required = true)
    private String jupyterhubGithubClientSecret;

    @CommandLine.Option(names = {"--jupyterhub-ingress-host"}, description = "Jupyterhub Ingress Host.", required = true)
    private String jupyterhubIngressHost;

    @CommandLine.Option(names = {"--jupyterhub-storage-size"}, description = "Jupyterhub Storage Size in GiB.", required = true)
    private int jupyterhubStorageSize;

    @CommandLine.Option(names = {"--redash-storage-size"}, description = "Redash Storage Size in GiB.", required = true)
    private int redashStorageSize;

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

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        restResponse = serviceDefDao.listServiceDef(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.ANALYTICS.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // show storage classes.
        ResourceControlDao resourceControlDao = applicationContext.getBean(ResourceControlDao.class);
        restResponse = resourceControlDao.listStorageClasses(configProps, Long.valueOf(clusterId));

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> storageClasses =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        format = "%-20s%-20s%-20s%-20s%n";

        System.out.printf(format,"STORAGE CLASS NAME", "RECLAIM POLICY", "VOLUME BIDING MODE", "PROVISIONER");
        for(Map<String, Object> map : storageClasses) {
            System.out.printf(format,
                    String.valueOf(map.get("name")),
                    (String) map.get("reclaimPolicy"),
                    (String) map.get("volumeBindingMode"),
                    (String) map.get("provisioner"));
        }

        System.out.printf("\n");

        String storageClass = cnsl.readLine("Select Storage Class : ");
        if(storageClass == null) {
            throw new RuntimeException("storage class is required!");
        }

        System.out.printf("\n");


        // create.
        AnalyticsDao analyticsDao = applicationContext.getBean(AnalyticsDao.class);
        restResponse = analyticsDao.createAnalytics(
                configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                jupyterhubGithubClientId,
                jupyterhubGithubClientSecret,
                jupyterhubIngressHost,
                storageClass,
                jupyterhubStorageSize,
                redashStorageSize);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("analytics service created successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }
}
