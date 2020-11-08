package com.cloudcheflabs.dataroaster.apiserver.util;

import org.junit.Test;

public class StringUtilsTestRunner {

    @Test
    public void substring() throws Exception {
        String subDir = "resource/";
        String str = "/any/path/resource/test.yaml";

        str = str.substring(str.lastIndexOf(subDir) + subDir.length());
        System.out.println(str);
    }

    @Test
    public void ipRanges() throws  Exception {
        String ip = "52.231.165.174";
        String[] tokens = ip.split("\\.");
        int lastNum = Integer.valueOf(tokens[tokens.length - 1]);
        int from = lastNum + 5;
        int to = from + 5;
        String prefix = ip.substring(0, ip.lastIndexOf("."));
        String fromIP = prefix + "." + from;
        String toIP = prefix + "." + to;
        System.out.printf("fromIP: %s, toIP: %s", fromIP, toIP);
    }
}
