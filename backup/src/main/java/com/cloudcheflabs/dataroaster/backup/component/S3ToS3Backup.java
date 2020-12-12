package com.cloudcheflabs.dataroaster.backup.component;

import com.cloudcheflabs.dataroaster.backup.util.SparkJobUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;

public class S3ToS3Backup {
    public static void main(String[] args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.accepts("master").withRequiredArg().ofType(String.class);
        parser.accepts("sourceS3PropDefaultFs").withRequiredArg().ofType(String.class);
        parser.accepts("sourceS3PropEndpoint").withRequiredArg().ofType(String.class);
        parser.accepts("sourceS3PropAccessKey").withRequiredArg().ofType(String.class);
        parser.accepts("sourceS3PropSecretKey").withRequiredArg().ofType(String.class);
        parser.accepts("targetS3PropDefaultFs").withRequiredArg().ofType(String.class);
        parser.accepts("targetS3PropEndpoint").withRequiredArg().ofType(String.class);
        parser.accepts("targetS3PropAccessKey").withRequiredArg().ofType(String.class);
        parser.accepts("targetS3PropSecretKey").withRequiredArg().ofType(String.class);
        parser.accepts("inputBase").withRequiredArg().ofType(String.class);
        parser.accepts("outputBase").withRequiredArg().ofType(String.class);
        parser.accepts("date").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        String master = (String) options.valueOf("master");
        String sourceS3PropDefaultFs = (String) options.valueOf("sourceS3PropDefaultFs");
        String sourceS3PropEndpoint = (String) options.valueOf("sourceS3PropEndpoint");
        String sourceS3PropAccessKey = (String) options.valueOf("sourceS3PropAccessKey");
        String sourceS3PropSecretKey = (String) options.valueOf("sourceS3PropSecretKey");
        String targetS3PropDefaultFs = (String) options.valueOf("targetS3PropDefaultFs");
        String targetS3PropEndpoint = (String) options.valueOf("targetS3PropEndpoint");
        String targetS3PropAccessKey = (String) options.valueOf("targetS3PropAccessKey");
        String targetS3PropSecretKey = (String) options.valueOf("targetS3PropSecretKey");

        String inputBase = (String) options.valueOf("inputBase");
        String outputBase = (String) options.valueOf("outputBase");
        String date = (String) options.valueOf("date");

        String input = inputBase;
        String output = outputBase;
        if(!date.equals("NULL")) {
            long targetTime = -1L;
            if(date.equals("NOW")) {
                long now = new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime();
                DateTime nowDt = new DateTime(targetTime);
                targetTime = nowDt.minusDays(1).getMillis();
            } else {
                targetTime = new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime();
            }
            DateTime dt = new DateTime(targetTime);
            System.out.println("target date: " + dt.toString());

            input = SparkJobUtils.getYearMonthDayPath(inputBase, dt);
            output = SparkJobUtils.getYearMonthDayPath(outputBase, dt);
        }

        System.out.println("input: " + input);
        System.out.println("output: " + output);

        SparkConf sparkConf = new SparkConf().setAppName(S3ToS3Backup.class.getName());
        sparkConf.setMaster(master);

        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .getOrCreate();

        // source s3 configuration.
        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        hadoopConfiguration.set("fs.defaultFS", sourceS3PropDefaultFs);
        hadoopConfiguration.set("fs.s3a.endpoint", sourceS3PropEndpoint);
        hadoopConfiguration.set("fs.s3a.access.key", sourceS3PropAccessKey);
        hadoopConfiguration.set("fs.s3a.secret.key", sourceS3PropSecretKey);

        String finalInput = sourceS3PropDefaultFs + input;
        System.out.println("final input: " + finalInput);
        Dataset<Row> df = spark.read().format("parquet").load(finalInput);

        df.show(10);

        df.persist(StorageLevel.DISK_ONLY());

        // target aws s3 configuration.
        hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.defaultFS", targetS3PropDefaultFs);
        hadoopConfiguration.set("fs.s3a.endpoint", targetS3PropEndpoint);
        hadoopConfiguration.set("fs.s3a.access.key", targetS3PropAccessKey);
        hadoopConfiguration.set("fs.s3a.secret.key", targetS3PropSecretKey);

        String finalOutput = targetS3PropDefaultFs + output;
        System.out.println("final output: " + finalOutput);
        df.write()
                .format("parquet")
                .mode(SaveMode.Overwrite)
                .save(finalOutput);
    }
}
