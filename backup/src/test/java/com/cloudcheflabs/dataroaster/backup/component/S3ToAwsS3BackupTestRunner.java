package com.cloudcheflabs.dataroaster.backup.component;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class S3ToAwsS3BackupTestRunner {

    @Test
    public void s3ToAwsS3Backup() throws Exception {

        List<String> argsList = new ArrayList<>();
        argsList.add("--master");
        argsList.add("local[2]");

        argsList.add("--sourceS3PropDefaultFs");
        argsList.add("s3a://mykidong");

        argsList.add("--sourceS3PropEndpoint");
        argsList.add("https://smartlife-tenant.minio.cloudchef-labs.com");

        argsList.add("--sourceS3PropAccessKey");
        argsList.add("bWluaW8=");

        argsList.add("--sourceS3PropSecretKey");
        argsList.add("bWluaW8xMjM=");

        argsList.add("--targetS3PropDefaultFs");
        argsList.add("s3a://cloudcheflabs");

        argsList.add("--targetS3PropEndpoint");
        argsList.add("https://s3.amazonaws.com");

        argsList.add("--targetS3PropAccessKey");
        argsList.add("AKIARJUR6DKSVEB3HZHH");

        argsList.add("--targetS3PropSecretKey");
        argsList.add("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");

        argsList.add("--inputBase");
        argsList.add("/test-parquet");

        argsList.add("--outputBase");
        argsList.add("/slbc/backup/test-parquet");

        argsList.add("--date");
        argsList.add("NULL");

        S3ToS3Backup.main(argsList.toArray(new String[0]));
    }
}
