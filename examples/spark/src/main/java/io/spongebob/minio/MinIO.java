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

import java.util.Arrays;

public class MinIO {

    public static void main(String[] args) {

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

        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .getOrCreate();


        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.defaultFS", "s3a://mykidong");
        hadoopConfiguration.set("fs.s3a.endpoint", s3EndPoint); // MUST IP Address(Not Host Name) !!!!
        hadoopConfiguration.set("fs.s3a.access.key", "bWluaW8=");
        hadoopConfiguration.set("fs.s3a.secret.key", "bWluaW8xMjM=");
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");

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

        // read delta from ceph.
        Dataset<Row> deltaFromCeph = spark.read().format("delta")
                .load("s3a://mykidong/test-delta");

        deltaFromCeph.show(10);

        deltaFromCeph.printSchema();
    }
}
