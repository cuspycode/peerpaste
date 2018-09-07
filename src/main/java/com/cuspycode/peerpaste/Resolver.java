package com.cuspycode.peerpaste;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class Resolver {
    private final static Object lock = new Object();

    private static InetAddress address;
    private static int port;

    public static void main(String[] args) throws IOException {
	if (resolve("foobar", 30000)) {
	    System.out.println("Resolved: " +getAddress()+ ":" +getPort());
	} else {
	    System.out.println("Timeout");
	}
    }

    static class MyServiceListener implements ServiceListener {
	private String target;

	public MyServiceListener(String target) {
	    this.target = target;
	}

        @Override
        public void serviceAdded(ServiceEvent event) {
            System.out.println("Service added   : " + event.getName() + "." + event.getType());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            System.out.println("Service removed : " + event.getName() + "." + event.getType());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
	    ServiceInfo serviceInfo = event.getInfo();
	    int port = serviceInfo.getPort();
	    InetAddress ia4 = null;
	    InetAddress ia6 = null;
	    for (InetAddress ia : serviceInfo.getInetAddresses()) {
		if (ia instanceof Inet4Address) {
		    ia4 = ia;
		}
		if (ia instanceof Inet6Address) {
		    ia6 = ia;
		}
	    }
	    InetAddress theAddress = ia4;
	    if (theAddress == null) {
		theAddress = ia6;
	    }
	    if (target.equals(serviceInfo.getName())) {
		Resolver.address = theAddress;
		Resolver.port = port;
		synchronized(lock) {
		    lock.notify();
		}
	    }
        }
    }

    public static boolean resolve(String peerName, long timeout) throws IOException {
	final JmDNS jmdns = JmDNS.create();

	address = null;
	port = 0;

	ServiceListener serviceListener = new MyServiceListener(peerName);
	String serviceType = "_peerpaste._tcp.local.";
	jmdns.addServiceListener(serviceType, serviceListener);
	long stopTime = System.currentTimeMillis() + timeout;
	synchronized(lock) {
	    for (long now; address == null && (now = System.currentTimeMillis()) < stopTime;) {
		try {
		    lock.wait(stopTime - now);
		} catch (InterruptedException e) {
		    // just keep going
		}
	    }
	}
	//jmdns.removeServiceListener(serviceType, serviceListener);
	jmdns.close();
	return (address != null);
    }

    public static InetAddress getAddress() {
	return address;
    }

    public static int getPort() {
	return port;
    }
}

