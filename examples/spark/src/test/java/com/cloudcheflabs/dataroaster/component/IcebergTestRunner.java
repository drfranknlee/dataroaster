package com.cloudcheflabs.dataroaster.component;

import com.cloudcheflabs.dataroaster.util.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructType;
import org.joda.time.DateTime;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
        sparkConf.set("spark.sql.catalog.spark_catalog.uri", hiveMetastoreUri);

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
    public void write() throws Exception {

        String date = System.getProperty("date", "2020-09-01");

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
        sparkConf.set("spark.sql.catalog.spark_catalog.uri", hiveMetastoreUri);
        sparkConf.set("spark.sql.sources.partitionOverwriteMode", "dynamic");

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


        // parse date for year, month and day.
        long time = new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime();
        DateTime dt = new DateTime(time);
        String year = String.valueOf(dt.getYear());
        String month = StringUtils.addZeroToMonthOrDay(dt.getMonthOfYear());
        String day = StringUtils.addZeroToMonthOrDay(dt.getDayOfMonth());


        // read json.
        String json = StringUtils.fileToString("data/test.json");
        String lines[] = json.split("\\r?\\n");
        Dataset<Row> df = spark.read().json(new JavaSparkContext(spark.sparkContext()).parallelize(Arrays.asList(lines)));

        // create temp view.
        df.createOrReplaceTempView("temp_test");

        // add columns of date.
        String sql = "select baseProperties, itemId, price, quantity, '" + year + "' as year, '" + month + "' as month, '" + day + "' as day from temp_test";
        Dataset<Row> newEventDf = spark.sql(sql);

        // make properties of baseProperties in order according to table schema.
        JavaRDD<Row> eventRdd = newEventDf.toJavaRDD().flatMap(new BasePropertiesInOrder());

        // get table schema.
        StructType schema = spark.table("spark_catalog.iceberg_test.test_event").schema();
        System.out.println("iceberg table schema...");
        schema.printTreeString();

        // event dataframe whose baseProperties has been in order.
        Dataset<Row> dfInOrder = spark.createDataFrame(eventRdd, schema);
        dfInOrder.show(10);

        // append.
        dfInOrder.sortWithinPartitions("year", "month", "day")
                .writeTo("spark_catalog.iceberg_test.test_event").overwritePartitions();

        // show appended rows.
        System.out.println("show the rows selected by date in the table...");
        String selectQuery = "select * from spark_catalog.iceberg_test.test_event where year='" + year + "' and month='" + month + "' and day='" + day + "'";
        spark.sql(selectQuery)
                .show(10);

        // show all rows.
        System.out.println("show all entries in the table...");
        spark.table("spark_catalog.iceberg_test.test_event").show(100);
    }

    private static class BasePropertiesInOrder implements FlatMapFunction<Row, Row> {
        @Override
        public Iterator<Row> call(Row row) throws Exception {

            List<Row> rowList = new ArrayList<>();

            Row basePropRow = row.getAs("baseProperties");

            Row newBasePropRow = RowFactory.create(
                    basePropRow.getAs("uid"),
                    basePropRow.getAs("eventType"),
                    basePropRow.getAs("version"),
                    basePropRow.getAs("ts")
            );

            Row newRow = RowFactory.create(
                    newBasePropRow,
                    row.getAs("itemId"),
                    row.getAs("price"),
                    row.getAs("quantity"),
                    row.getAs("year"),
                    row.getAs("month"),
                    row.getAs("day")
            );

            rowList.add(newRow);

            return rowList.iterator();
        }
    }


    @Test
    public void inspectTable() throws Exception {

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
        sparkConf.set("spark.sql.catalog.spark_catalog.uri", hiveMetastoreUri);
        sparkConf.set("spark.sql.sources.partitionOverwriteMode", "dynamic");

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

        // show history.
        System.out.println("table history...");
        spark.read().format("iceberg").load("iceberg_test.test_event.history").show(100);

        // show snapshots.
        System.out.println("table snapshots...");
        spark.read().format("iceberg").load("iceberg_test.test_event.snapshots").show(100);

        // show manifests.
        System.out.println("table manifests...");
        spark.read().format("iceberg").load("iceberg_test.test_event.manifests").show(100);

        // show files with dataframe.
        System.out.println("table files...");
        spark.read().format("iceberg").load("iceberg_test.test_event.files").show(100);

        // show files with dataframe.
        System.out.println("table partitions...");
        spark.read().format("iceberg").load("iceberg_test.test_event.partitions").show(100);
    }


    @Test
    public void timeTravel() throws Exception {

        String snapshotId = System.getProperty("snapshotId", "6404955758200894343");

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
        sparkConf.set("spark.sql.catalog.spark_catalog.uri", hiveMetastoreUri);
        sparkConf.set("spark.sql.sources.partitionOverwriteMode", "dynamic");

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

        // show snapshots.
        System.out.println("table snapshots...");
        spark.read().format("iceberg").load("iceberg_test.test_event.snapshots").show(100);

        // show rows in the specified snapshot.
        spark.read()
                .option("snapshot-id", Long.valueOf(snapshotId))
                .format("iceberg")
                .load("iceberg_test.test_event").show(100);
    }


    @Test
    public void alter() throws Exception {

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
        sparkConf.set("spark.sql.catalog.spark_catalog.uri", hiveMetastoreUri);
        sparkConf.set("spark.sql.sources.partitionOverwriteMode", "dynamic");

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

        // alter column name.
        System.out.println("alter column name itemId...");
        spark.sql("ALTER TABLE spark_catalog.iceberg_test.test_event RENAME COLUMN itemId TO item_id");

        spark.sql("select * from spark_catalog.iceberg_test.test_event")
                .show(10);

        // revert column name.
        System.out.println("revert to column name itemId...");
        spark.sql("ALTER TABLE spark_catalog.iceberg_test.test_event RENAME COLUMN item_id TO itemId");

        spark.sql("select * from spark_catalog.iceberg_test.test_event")
                .show(10);
    }
}
