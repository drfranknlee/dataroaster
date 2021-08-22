package com.cloudcheflabs.dataroaster.cli.command.metricsmonitoring;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "metricsmonitoring",
        subcommands = {
                CreateMetricsMonitoring.class,
                DeleteMetricsMonitoring.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Metrics Monitoring.")
public class MetricsMonitoring implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
