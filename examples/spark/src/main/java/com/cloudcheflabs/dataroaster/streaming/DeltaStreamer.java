package com.cloudcheflabs.dataroaster.streaming;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class DeltaStreamer {

    public static void main(String[] args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.accepts("master").withRequiredArg().ofType(String.class);
        parser.accepts("bootstrap.servers").withRequiredArg().ofType(String.class);
        parser.accepts("topic").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);
        String master = (String) options.valueOf("master");
        String bootstrapServers = (String) options.valueOf("bootstrap.servers");
        String topic = (String) options.valueOf("topic");

        SparkConf sparkConf = new SparkConf().setAppName("delta-streamer");
        sparkConf.setMaster(master);

        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .enableHiveSupport()
                .getOrCreate();

        Dataset<Row> df = spark.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", bootstrapServers)
                .option("subscribe", topic)
                .option("startingOffsets", "latest")
                .option("failOnDataLoss", "false")
                .load();

        // create delta table.
        spark.sql("CREATE TABLE IF NOT EXISTS my_delta USING DELTA LOCATION 's3a://mykidong/my-delta'");

        df.writeStream()
                .format("delta")
                .outputMode("append")
                .option("checkpointLocation", "s3a://mykidong/my-delta/_checkpoints/from-kafka")
                .start("s3a://mykidong/my-delta") ;

        Thread.sleep(Long.MAX_VALUE);
    }
}
