package com.cloudcheflabs.dataroaster.sample;

import com.cloudcheflabs.dataroaster.util.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

import java.util.Arrays;

public class SampleTable {

    @Test
    public void run() throws Exception {
        String metastoreUrl = System.getProperty("metastoreUrl");

        SparkConf sparkConf = new SparkConf().setAppName("create sample table");
        sparkConf.setMaster("local[2]");

        // delta lake log store for s3.
        sparkConf.set("spark.delta.logStore.class", "org.apache.spark.sql.delta.storage.S3SingleDriverLogStore");

        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .enableHiveSupport()
                .getOrCreate();


        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.defaultFS", "s3a://mykidong");
        hadoopConfiguration.set("fs.s3a.endpoint", "https://ceph-rgw-test.cloudchef-labs.com");
        hadoopConfiguration.set("fs.s3a.access.key", "TOW32G9ULH63MTUI6NNW");
        hadoopConfiguration.set("fs.s3a.secret.key", "jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi");
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        hadoopConfiguration.set("hive.metastore.uris", "thrift://" + metastoreUrl);
        hadoopConfiguration.set("hive.server2.enable.doAs", "false");
        hadoopConfiguration.set("hive.metastore.client.socket.timeout", "1800");
        hadoopConfiguration.set("hive.execution.engine", "spark");


        // read json.
        String json = StringUtils.fileToString("data/test.json");
        String lines[] = json.split("\\r?\\n");
        Dataset<Row> df = spark.read().json(new JavaSparkContext(spark.sparkContext()).parallelize(Arrays.asList(lines)));

        df.show(10);

        // write delta to ceph.
        df.write().format("delta")
                .option("path", "s3a://mykidong/test-delta")
                .mode(SaveMode.Overwrite)
                .save();

        // create delta table.
        spark.sql("CREATE TABLE IF NOT EXISTS test_delta USING DELTA LOCATION 's3a://mykidong/test-delta'");

        // read delta from ceph.
        Dataset<Row> delta = spark.sql("select * from test_delta");

        delta.show(10);

        // create persistent parquet table with external path.
        delta.write().format("parquet")
                .option("path", "s3a://mykidong/test-parquet")
                .mode(SaveMode.Overwrite)
                .saveAsTable("test_parquet");

        // read parquet from table.
        Dataset<Row> parquet = spark.sql("select * from test_parquet");

        parquet.show(10);
    }
}
