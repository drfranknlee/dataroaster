package com.cloudcheflabs.dataroaster.cli.command.ingresscontroller;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "ingresscontroller",
        subcommands = {
                CreateIngressController.class,
                DeleteIngressController.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Ingress Controller.")
public class IngressController implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
