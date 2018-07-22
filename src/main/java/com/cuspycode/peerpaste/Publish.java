package com.cuspycode.peerpaste;

import java.util.Enumeration;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class Publish {

    public static String ifName = null;
    public static String ifAddr = null;
    public static String serviceName = null;
    public static int servicePort = 0;

    private static JmDNS jmdns = null;

    public static void main(String[] args) throws InterruptedException {

        try {

	    start();

            // Wait a bit
            Thread.sleep(25000);

	    stop();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void start() throws IOException {
	if (ifAddr == null) {
	    ifAddr = getInterfaceIP(ifName, false);
	}
	if (ifAddr != null) {
	    GUI.println("Service name: " +serviceName);
	    jmdns = JmDNS.create(InetAddress.getByName(ifAddr));
	    ServiceInfo serviceInfo = ServiceInfo.create("_peerpaste._tcp.", serviceName, servicePort, "");
	    jmdns.registerService(serviceInfo);
	}
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
				GUI.println("Own IP is " +ip+ " on interface \"" +i.getName()+ "\"");
				if (serviceName == null) {
				    try {
					serviceName = InetAddress.getLocalHost().getHostName();
				    } catch (UnknownHostException ue) {
					serviceName = a.getHostName();
				    }
				}
                                return ip;
                            }
                        }
                    }
                }
            }
	    throw new SocketException("No such interface: " +ifname);
        } catch (SocketException se) {
	    System.out.println("Couldn't find interface " +ifname);
        }
        return null;
    }

}
