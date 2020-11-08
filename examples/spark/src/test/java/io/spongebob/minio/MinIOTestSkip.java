package io.spongebob.minio;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MinIOTestSkip {

    @Test
    public void run() throws Exception
    {
        String s3Endpoint = System.getProperty("s3Endpoint", "http://10.233.51.179:9099");

        List<String> argsList = new ArrayList<>();
        argsList.add("--master");
        argsList.add("local[2]");
        argsList.add("--s3Endpoint");
        argsList.add(s3Endpoint);

        MinIO.main(argsList.toArray(new String[0]));
    }
}
