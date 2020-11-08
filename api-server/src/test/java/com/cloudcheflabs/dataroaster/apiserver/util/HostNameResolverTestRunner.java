package com.cloudcheflabs.dataroaster.apiserver.util;

import org.junit.Test;

import java.net.InetAddress;

public class HostNameResolverTestRunner {
    @Test
    public void resolve() throws Exception {
        String hostName = System.getProperty("hostName");
        InetAddress[] addresses = InetAddress.getAllByName(hostName);;
        for(InetAddress inetAddress : addresses) {
            System.out.println("address: " + inetAddress.getHostAddress());
        }
    }
}
