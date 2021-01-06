package com.cloudcheflabs.dataroaster.component;

import com.cloudcheflabs.dataroaster.util.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

import java.util.Arrays;

public class IcebergTestRunner {

    @Test
    public void createTable() throws Exception {
        String master = System.getProperty("master", "local[2]");
        String s3PropDefaultFs = System.getProperty("s3PropDefaultFs", "s3a://mykidong");
        String s3PropEndpoint = System.getProperty("s3PropEndpoint", "https://smartlife-tenant.minio.cloudchef-labs.com");
        String s3PropAccessKey = System.getProperty("s3PropAccessKey", "bWluaW8=");
        String s3PropSecretKey = System.getProperty("s3PropSecretKey", "bWluaW8xMjM=");
        String hiveMetastoreDelay = System.getProperty("hiveMetastoreDelay", "5");
        String hiveMetastoreTimeout = System.getProperty("hiveMetastoreTimeout", "1800");
        String hiveMetastoreUri = System.getProperty("hiveMetastoreUri", "thrift://localhost:9083");


        SparkConf sparkConf = new SparkConf().setAppName(IcebergTestRunner.class.getName());
        sparkConf.setMaster(master);

        // add iceberg catalog.
        sparkConf.set("spark.sql.catalog.spark_catalog", "org.apache.iceberg.spark.SparkSessionCatalog");
        sparkConf.set("spark.sql.catalog.spark_catalog.type", "hive");
        sparkConf.set("spark.sql.catalog.spark_catalog.uri", "thrift://localhost:9083");

        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .enableHiveSupport()
                .getOrCreate();

        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        hadoopConfiguration.set("fs.defaultFS", s3PropDefaultFs);
        hadoopConfiguration.set("fs.s3a.endpoint", s3PropEndpoint);
        hadoopConfiguration.set("fs.s3a.access.key", s3PropAccessKey);
        hadoopConfiguration.set("fs.s3a.secret.key", s3PropSecretKey);
        hadoopConfiguration.set("hive.metastore.client.connect.retry.delay", hiveMetastoreDelay);
        hadoopConfiguration.set("hive.metastore.client.socket.timeout", hiveMetastoreTimeout);
        hadoopConfiguration.set("hive.metastore.uris", hiveMetastoreUri);


        String sql = "";
        sql += "CREATE TABLE spark_catalog.iceberg_test.test_event ( ";
        sql += "        baseProperties   STRUCT<uid:             string, ";
        sql += "                                eventType:       string, ";
        sql += "                                version:         string, ";
        sql += "                                ts:              long>, ";
        sql += "        itemId 		string, ";
        sql += "        price 		long, ";
        sql += "        quantity	long, ";
        sql += "        year 		string, ";
        sql += "        month 		string, ";
        sql += "        day 		string ";
        sql += ") ";
        sql += "USING iceberg ";
        sql += "PARTITIONED BY (year, month, day) ";
        sql += "LOCATION 's3a://mykidong/iceberg_test_warehouse/test_event' ";
        sql += "TBLPROPERTIES ( ";
        sql += "        'fs.s3a.access.key' = 'bWluaW8=', ";
        sql += "        'fs.s3a.secret.key' = 'bWluaW8xMjM=', ";
        sql += "        'fs.s3a.endpoint' = 'https://smartlife-tenant.minio.cloudchef-labs.com', ";
        sql += "        'fs.s3a.path.style.access' = 'true' ";
        sql += ") ";

        // create table.
        spark.sql(sql);
    }


    @Test
    public void append() throws Exception {
        String master = System.getProperty("master", "local[2]");
        String s3PropDefaultFs = System.getProperty("s3PropDefaultFs", "s3a://mykidong");
        String s3PropEndpoint = System.getProperty("s3PropEndpoint", "https://smartlife-tenant.minio.cloudchef-labs.com");
        String s3PropAccessKey = System.getProperty("s3PropAccessKey", "bWluaW8=");
        String s3PropSecretKey = System.getProperty("s3PropSecretKey", "bWluaW8xMjM=");
        String hiveMetastoreDelay = System.getProperty("hiveMetastoreDelay", "5");
        String hiveMetastoreTimeout = System.getProperty("hiveMetastoreTimeout", "1800");
        String hiveMetastoreUri = System.getProperty("hiveMetastoreUri", "thrift://localhost:9083");


        SparkConf sparkConf = new SparkConf().setAppName(IcebergTestRunner.class.getName());
        sparkConf.setMaster(master);

        // add iceberg catalog.
        sparkConf.set("spark.sql.catalog.spark_catalog", "org.apache.iceberg.spark.SparkSessionCatalog");
        sparkConf.set("spark.sql.catalog.spark_catalog.type", "hive");
        sparkConf.set("spark.sql.catalog.spark_catalog.uri", "thrift://localhost:9083");

        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .enableHiveSupport()
                .getOrCreate();

        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        hadoopConfiguration.set("fs.defaultFS", s3PropDefaultFs);
        hadoopConfiguration.set("fs.s3a.endpoint", s3PropEndpoint);
        hadoopConfiguration.set("fs.s3a.access.key", s3PropAccessKey);
        hadoopConfiguration.set("fs.s3a.secret.key", s3PropSecretKey);
        hadoopConfiguration.set("hive.metastore.client.connect.retry.delay", hiveMetastoreDelay);
        hadoopConfiguration.set("hive.metastore.client.socket.timeout", hiveMetastoreTimeout);
        hadoopConfiguration.set("hive.metastore.uris", hiveMetastoreUri);


        // read json.
        String json = StringUtils.fileToString("data/test.json");
        String lines[] = json.split("\\r?\\n");
        Dataset<Row> df = spark.read().json(new JavaSparkContext(spark.sparkContext()).parallelize(Arrays.asList(lines)));

        // create temp view.
        df.createOrReplaceTempView("temp_test");

        // add columns of date.
        Dataset<Row> newEventDf = spark.sql("select a.*, '2020' as year, '09' as month, '01' as day from temp_test a");

        newEventDf.show(10);
        newEventDf.printSchema();

        // append.
        newEventDf.writeTo("spark_catalog.iceberg_test.test_event").append();

        // show appended rows.
        spark.table("spark_catalog.iceberg_test.test_event").where("year='2020' and month='09' and day='01'")
                .show();
    }
}
