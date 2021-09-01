package com.cloudcheflabs.dataroaster.cli.command.queryengine;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "queryengine",
        subcommands = {
                CreateQueryEngine.class,
                DeleteQueryEngine.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Query Engine.")
public class QueryEngine implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
