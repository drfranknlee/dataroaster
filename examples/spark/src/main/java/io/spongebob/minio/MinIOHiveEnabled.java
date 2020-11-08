package io.spongebob.minio;

import io.spongebob.util.StringUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Arrays;
import java.util.Properties;

public class MinIOHiveEnabled {

    public static void main(String[] args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.accepts("master").withRequiredArg().ofType(String.class);
        parser.accepts("s3Endpoint").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        String master = (String) options.valueOf("master");
        String s3EndPoint = (String) options.valueOf("s3Endpoint");

        SparkConf sparkConf = new SparkConf().setAppName("minio");
        sparkConf.setMaster(master);

        // delta lake log store for s3.
        sparkConf.set("spark.delta.logStore.class", "org.apache.spark.sql.delta.storage.S3SingleDriverLogStore");

        sparkConf.set("spark.sql.warehouse.dir", "s3a://mykidong/apps/spark/warehouse");
        sparkConf.set("spark.sql.hive.metastore.jars", "/home/pcp/spongebob/examples/spark/src/main/resources/lib/standalone-metastore-1.21.2.3.1.4.0-315-hive3.jar");


        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .enableHiveSupport()
                .getOrCreate();


        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.defaultFS", "s3a://mykidong");
        hadoopConfiguration.set("fs.s3a.endpoint", s3EndPoint); // MUST IP Address(Not Host Name) !!!!
        hadoopConfiguration.set("fs.s3a.access.key", "bWluaW8=");
        hadoopConfiguration.set("fs.s3a.secret.key", "bWluaW8xMjM=");
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");

        // hive configuration.
        Properties hiveProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("hive-conf.properties"));
        for (String key : hiveProps.stringPropertyNames()) {
            String value = hiveProps.getProperty(key);
            hadoopConfiguration.set(key, value);
        }

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
