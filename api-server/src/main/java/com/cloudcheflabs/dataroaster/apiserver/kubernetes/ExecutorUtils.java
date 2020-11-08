package com.cloudcheflabs.dataroaster.apiserver.kubernetes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class ExecutorUtils {

    private static Logger LOG = LoggerFactory.getLogger(ExecutorUtils.class);

    public static <T> void runTask(Callable<T> task) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<T>> futureList = new ArrayList<Future<T>>();
        for(int i = 0; i < 1; i++) {
            Future<T> future = executor.submit(task);
            futureList.add(future);
        }

        for (Future<T> fut : futureList) {
            try {
                LOG.info(new Date() + "::" + fut.get());
            } catch (InterruptedException | ExecutionException e) {
                LOG.error(e.getMessage());
            }
        }
        executor.shutdown();
    }
}
