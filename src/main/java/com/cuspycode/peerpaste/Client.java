package com.cuspycode.peerpaste;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

public class Client {

    final static String ME_COMMAND = "ME";
    final static String OOB_COMMAND = "OOB";
    final static String SEND_COMMAND = "SEND";
    final static String RECEIVE_COMMAND = "RECEIVE";

    public static void main(String[] args) throws Exception {
	String target = args[0];
	String command = args[1];
	String myData = "The quick fox pasted a lazy dog";
	if (args.length > 2) {
	    myData = args[2];
	}

	Socket socket = new Socket(target, 1234);	// Replace with mDNS-resolved info...
	OutputStream out = socket.getOutputStream();
	String line = ME_COMMAND+ " Your friendly Java server\n";
	out.write(line.getBytes());
	InputStream in = socket.getInputStream();
	byte buf[] = new byte[1024];
	for (int i=0; i<buf.length; i++) {
	    int value = in.read();
	    if (value == -1) {
		break;
	    }
	    if (value == '\n') {
		String greeting = new String(buf, 0, i);
		System.out.println(greeting);
		break;
	    }
	    buf[i] = (byte) value;
	}
	handleCommand(command, myData, out, in);
    }

    private static void handleCommand(String command, String myData, OutputStream out, InputStream in) throws IOException {
	if (SEND_COMMAND.equals(command)) {
	    byte[] myBytes = myData.getBytes();
	    String line = command+ " " +myBytes.length+ "\n";
	    out.write(line.getBytes());
	    out.write(myBytes);
	} else if (RECEIVE_COMMAND.equals(command)){
	    String line = command+ "\n";
	    out.write(line.getBytes());
	    String peerCommand = Server.readPeerCommand(in);
	    String sendCmdPrefix = SEND_COMMAND+ " ";
	    if (peerCommand.startsWith(sendCmdPrefix)) {
		int declaredSize = Integer.parseInt(peerCommand.substring(sendCmdPrefix.length()));
		StringBuilder sb = new StringBuilder();
		byte buf[] = new byte[1024];
		int nbytes;
		int receivedSize = 0;
		while (receivedSize < declaredSize) {
		    nbytes = in.read(buf);
		    if (nbytes == -1) {
			break;
		    }
		    if (receivedSize + nbytes > declaredSize) {
			nbytes = declaredSize - receivedSize;
		    }
		    sb.append(new String(buf, 0, nbytes));
		    receivedSize += nbytes;
		}
		if (receivedSize != declaredSize) {
		    System.out.println("error: received " +receivedSize+ " bytes, expected " +declaredSize);
		    sb.delete(0, sb.length());
		}
		String result = sb.toString();

		System.out.println("result string: '" +result+ "'");
	    }
	}
    }

}

