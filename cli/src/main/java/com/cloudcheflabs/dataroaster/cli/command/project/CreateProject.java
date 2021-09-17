package com.cloudcheflabs.dataroaster.cli.command.project;

import com.cloudcheflabs.dataroaster.cli.api.dao.ProjectDao;
import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Project.")
public class CreateProject implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Project parent;

    @CommandLine.Option(names = {"--name"}, description = "Project Name", required = true)
    private String name;

    @CommandLine.Option(names = {"--description"}, description = "Project Description", required = true)
    private String description;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        return CommandUtils.createProject(configProps, name, description);
    }
}
