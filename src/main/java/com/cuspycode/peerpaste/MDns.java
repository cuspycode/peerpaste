package com.cuspycode.peerpaste;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class MDns {
    public static void main(String[] args) throws IOException {
	final JmDNS jmdns = JmDNS.create();


	Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() {
		    try {
			System.out.println("\nShutting down...");
			jmdns.close();
		    } catch (IOException e) {
		    }
		}
	    });

	String serviceType = "_peerpaste._tcp.local.";
	jmdns.addServiceListener(serviceType, new SampleListener());
	while (true) {
	    try {
		Thread.sleep(5000);
	    } catch (InterruptedException e) {
	    }
	}
    }

    static class SampleListener implements ServiceListener {
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
            System.out.println("Service resolved: " + serviceInfo);
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
	    System.out.println("      " +theAddress+ ":" +port);
        }
    }

}

