package com.cloudcheflabs.dataroaster.cli.command.datacatalog;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "datacatalog",
        subcommands = {
                CreateDataCatalog.class,
                DeleteDataCatalog.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Data Catalog.")
public class DataCatalog implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}