package com.cloudcheflabs.dataroaster.cli.command.blueprint;

import com.cloudcheflabs.dataroaster.cli.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.ProjectDao;
import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.BlueprintGraph;
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
        description = "Create Blueprint Deployment.")
public class CreateBlueprint implements Callable<Integer> {

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


        // read blueprint yaml.
        String blueprintFileAbsolutePath = blueprintFile.getAbsolutePath();
        String blueprint = FileUtils.fileToString(blueprintFileAbsolutePath, false);

        // parset blueprint yaml.
        BlueprintGraph blueprintGraph = BlueprintUtils.parseBlueprintYaml(blueprint);

        // project.
        BlueprintGraph.Project project = blueprintGraph.getProject();

        // cluster.
        BlueprintGraph.Cluster cluster = blueprintGraph.getCluster();

        // service list with regard to dependencies.
        List<BlueprintGraph.Service> serviceDependencyList = blueprintGraph.getServiceDependencyList();

        // create project.
        System.out.println("create project...");
        CommandUtils.createProject(configProps, project.getName(), project.getDescription());

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

        String projectId = null;
        for(Map<String, Object> map : projectLists) {
            String tempProjectId = String.valueOf(map.get("id"));
            String projectName = (String) map.get("name");
            if(projectName.equals(project.getName())) {
                projectId = tempProjectId;
                System.out.printf("project id [%s] obtained\n", projectId);
                break;
            }
        }

        // create cluster.
        System.out.println("create cluster...");
        CommandUtils.createCluster(configProps, cluster.getName(), cluster.getDescription());

        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);
        restResponse = clusterDao.listClusters(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> clusterLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String clusterId = null;
        for(Map<String, Object> map : clusterLists) {
            String tempClusterId = String.valueOf(map.get("id"));
            String clusterName = (String) map.get("name");
            if(clusterName.equals(cluster.getName())) {
                clusterId = tempClusterId;
                System.out.printf("cluster id [%s] obtained\n", clusterId);
                break;
            }
        }

        // register kubeconfig.
        System.out.println("upload kubeconfig...");
        File kubeconfigFile = new File(cluster.getKubeconfig());
        String kubeconfigPath = kubeconfigFile.getAbsolutePath();
        String kubeconfig = FileUtils.fileToString(kubeconfigPath, false);
        CommandUtils.createKubeconfig(configProps, clusterId, kubeconfig);

        // create services.
        for(BlueprintGraph.Service service : serviceDependencyList) {
            String serviceName = service.getName();
            String depends = service.getDepends();
            boolean dependsOnIngressController = (depends != null) ? depends.equals("ingresscontroller") : false;
            // ingress controller.
            if(serviceName.equals("ingresscontroller")) {
                CommandUtils.createIngressController(configProps, projectId, clusterId);
            } else if(serviceName.equals("datacatalog")) {
                // show external ip of ingress controller nginx to register ingress host with the external ip of it to public dns server.
                if(dependsOnIngressController) {

                }

            }
        }

        return 0;
    }
}
