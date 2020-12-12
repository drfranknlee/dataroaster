package com.cloudcheflabs.dataroaster.backup.component;

import com.cloudcheflabs.dataroaster.backup.util.FileUtils;
import com.cloudcheflabs.dataroaster.backup.util.SparkJobUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.text.SimpleDateFormat;
import java.util.Properties;

public class S3ToS3Backup {
    public static void main(String[] args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.accepts("master").withRequiredArg().ofType(String.class);
        parser.accepts("sourceS3Prop").withRequiredArg().ofType(String.class);
        parser.accepts("targetS3Prop").withRequiredArg().ofType(String.class);
        parser.accepts("inputBase").withRequiredArg().ofType(String.class);
        parser.accepts("outputBase").withRequiredArg().ofType(String.class);
        parser.accepts("date").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        String master = (String) options.valueOf("master");
        String sourceS3Prop = (String) options.valueOf("sourceS3Prop");
        String targetS3Prop = (String) options.valueOf("targetS3Prop");
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
        Properties sourceS3Props = PropertiesLoaderUtils.loadProperties(new ClassPathResource(sourceS3Prop));
        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        for (String key : sourceS3Props.stringPropertyNames()) {
            String value = sourceS3Props.getProperty(key);
            hadoopConfiguration.set(key, value);
        }

        Dataset<Row> df = spark.read().format("parquet").load(input);

        df.show(10);


        // target aws s3 configuration.
        Properties targetS3Props = PropertiesLoaderUtils.loadProperties(new ClassPathResource(targetS3Prop));
        hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        for (String key : targetS3Props.stringPropertyNames()) {
            String value = targetS3Props.getProperty(key);
            hadoopConfiguration.set(key, value);
        }

        df.write()
                .format("parquet")
                .mode(SaveMode.Overwrite)
                .save(output);
    }
}
