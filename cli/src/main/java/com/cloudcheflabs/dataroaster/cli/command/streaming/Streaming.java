package com.cloudcheflabs.dataroaster.cli.command.streaming;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "streaming",
        subcommands = {
                CreateStreaming.class,
                DeleteStreaming.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Streaming.")
public class Streaming implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
