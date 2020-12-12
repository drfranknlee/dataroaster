package com.cloudcheflabs.dataroaster.backup.component;

import com.cloudcheflabs.dataroaster.backup.util.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

import java.util.Properties;

public class S3ToAwsS3BackupTestRunner {

    @Test
    public void s3ToAwsS3Backup() throws Exception {

        String master = "local[2]";

        SparkConf sparkConf = new SparkConf().setAppName(S3ToAwsS3BackupTestRunner.class.getName());
        sparkConf.setMaster(master);

        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .getOrCreate();

        // source s3 configuration.
        Properties sourceS3Props = FileUtils.loadProperties("target/test-classes/s3conf/source-s3.properties");
        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        for (String key : sourceS3Props.stringPropertyNames()) {
            String value = sourceS3Props.getProperty(key);
            hadoopConfiguration.set(key, value);
        }

        Dataset<Row> df = spark.read().format("parquet").load("/test-parquet");

        df.show(10);


        // target aws s3 configuration.
        Properties targetS3Props = FileUtils.loadProperties("target/test-classes/s3conf/target-s3.properties");
        hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        for (String key : targetS3Props.stringPropertyNames()) {
            String value = targetS3Props.getProperty(key);
            hadoopConfiguration.set(key, value);
        }

        df.write()
                .format("parquet")
                .mode(SaveMode.Overwrite)
                .save("/slbc/backup/test-parquet");
    }
}
