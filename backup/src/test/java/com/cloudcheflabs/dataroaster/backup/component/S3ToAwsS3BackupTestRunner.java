package com.cloudcheflabs.dataroaster.backup.component;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

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
        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.defaultFS", "s3a://mykidong");
        hadoopConfiguration.set("fs.s3a.endpoint", "https://smartlife-tenant.minio.cloudchef-labs.com");
        hadoopConfiguration.set("fs.s3a.access.key", "bWluaW8=");
        hadoopConfiguration.set("fs.s3a.secret.key", "bWluaW8xMjM=");
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");

        Dataset<Row> df = spark.read().format("parquet").load("/test-parquet");

        df.show(10);


        // target aws s3 configuration.
        hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.defaultFS", "s3a://cloudcheflabs");
        hadoopConfiguration.set("fs.s3a.endpoint", "https://s3.amazonaws.com");
        hadoopConfiguration.set("fs.s3a.access.key", "AKIARJUR6DKSVEB3HZHH");
        hadoopConfiguration.set("fs.s3a.secret.key", "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");

        df.write()
                .format("parquet")
                .mode(SaveMode.Overwrite)
                .save("s3a://cloudcheflabs/slbc/backup/test-parquet");
    }
}
