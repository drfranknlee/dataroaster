package com.cloudcheflabs.dataroaster.cli.command.distributedtracing;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "distributedtracing",
        subcommands = {
                CreateDistributedTracing.class,
                DeleteDistributedTracing.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Distributed Tracing.")
public class DistributedTracing implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
