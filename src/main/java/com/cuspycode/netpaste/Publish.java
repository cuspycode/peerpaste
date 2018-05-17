package com.cuspycode.netpaste;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class Publish {

    public static void main(String[] args) throws InterruptedException {

        try {
            // Create a JmDNS instance
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

            // Register a service
            ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", "example", 1234, "path=index.html");
            jmdns.registerService(serviceInfo);

            // Wait a bit
            Thread.sleep(25000);

            // Unregister all services
            jmdns.unregisterAllServices();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
