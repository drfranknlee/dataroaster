package com.cloudcheflabs.dataroaster.cli.command.workflow;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "workflow",
        subcommands = {
                CreateWorkflow.class,
                DeleteWorkflow.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Workflow.")
public class Workflow implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
