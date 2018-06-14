package com.cuspycode.netpaste;

import java.util.Enumeration;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class Publish {

    private static JmDNS jmdns = null;

    public static void main(String[] args) throws InterruptedException {

        try {

	    start("example", 1234);

            // Wait a bit
            Thread.sleep(25000);

	    stop();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void start(String name, int port) throws IOException {
	String myAddr = System.getProperty("peerpaste.server.ipaddr");
	String myInterface = System.getProperty("peerpaste.server.interface");
	if (myAddr == null) {
	    myAddr = getInterfaceIP(null, false);
	}
	jmdns = JmDNS.create(InetAddress.getByName(myAddr));

	ServiceInfo serviceInfo = ServiceInfo.create("_peerpaste._tcp.", name, port, "");
	jmdns.registerService(serviceInfo);
    }

    public static void stop() throws IOException {
	jmdns.unregisterAllServices();
    }

    private static String getInterfaceIP(String ifname, boolean ipv6) {
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface i = e.nextElement();
                if (ifname == null || "".equals(ifname) || ifname.equals(i.getName())) {
                    Enumeration<InetAddress> ea = i.getInetAddresses();
                    while (ea.hasMoreElements()) {
                        InetAddress a = ea.nextElement();
                        if (!i.isLoopback() && ipv6 == (a instanceof Inet6Address)) {
                            if (!a.isLinkLocalAddress()) {
                                String ip = a.getHostAddress();
				System.out.println("Own IP is " +ip+ " on interface " +i.getName());
                                return ip;
                            }
                        }
                    }
                }
            }
        } catch (SocketException se) {
	    System.out.println("Couldn't find interface " +ifname);
        }
        return null;
    }

}
