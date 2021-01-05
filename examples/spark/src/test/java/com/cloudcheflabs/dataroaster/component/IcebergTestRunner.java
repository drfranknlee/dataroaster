package com.cloudcheflabs.dataroaster.component;

import com.cloudcheflabs.dataroaster.util.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.hive.HiveCatalog;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.joda.time.DateTime;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IcebergTestRunner {

    @Test
    public void run() throws Exception {
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
        sparkConf.set("spark.sql.catalog.hive_iceberg", "org.apache.iceberg.spark.SparkSessionCatalog");
        sparkConf.set("spark.sql.catalog.hive_iceberg.type", "hive");
        sparkConf.set("spark.sql.catalog.hive_iceberg.uri", "thrift://localhost:9083");

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

        df.show(10);

        // create table: create table as select...
        df.writeTo("iceberg_test.test_event").create();



//        HiveCatalog catalog = new HiveCatalog(hadoopConfiguration);
//
//        // iceberg hive enabled.
//        Map<String, String> tableProperties = new HashMap<String, String>();
//        tableProperties.put(TableProperties.ENGINE_HIVE_ENABLED, "true"); //engine.hive.enabled=true
//        catalog.createTable(TableIdentifier.of("iceberg_test", "test_event"), schema, PartitionSpec.unpartitioned(), tableProperties);
    }
}
