package com.cloudcheflabs.dataroaster.cli.command.privateregistry;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "privateregistry",
        subcommands = {
                CreatePrivateRegistry.class,
                DeletePrivateRegistry.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Private Registry.")
public class PrivateRegistry implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
