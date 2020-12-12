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

        argsList.add("--sourceS3Prop");
        argsList.add("/s3conf/source-s3.properties");

        argsList.add("--targetS3Prop");
        argsList.add("/s3conf/target-s3.properties");

        argsList.add("--inputBase");
        argsList.add("/test-parquet");

        argsList.add("--outputBase");
        argsList.add("/slbc/backup/test-parquet");

        argsList.add("--date");
        argsList.add("NULL");

        S3ToS3Backup.main(argsList.toArray(new String[0]));
    }
}
