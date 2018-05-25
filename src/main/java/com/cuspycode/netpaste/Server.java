package com.cuspycode.netpaste;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

public class Server {

    final static String SEND_COMMAND = "SEND";
    final static String RECEIVE_COMMAND = "RECEIVE";

    public static void main(String[] args) throws Exception {
	String myData = "The quick fox pasted a lazy dog";
	if (args.length > 0) {
	    myData = args[0];
	}
	// Initialize a server socket on the next available port.
	////ServerSocket server = new ServerSocket(0);
	ServerSocket server = new ServerSocket(1234);
	server.setSoTimeout(3000);
	for (;;) {
	    try {
		Socket socket = server.accept();
		OutputStream out = socket.getOutputStream();
		out.write("JavaSE-NetPaste v0.1\n".getBytes());
		String peerCommand = readPeerCommand(socket.getInputStream());
		System.out.println("peerCommand: '" +peerCommand+ "'");
		handleCommand(socket, peerCommand, myData);
	    } catch (SocketTimeoutException e) {
		// ignore
	    }
	}
    }

    private static void handleCommand(Socket socket, String peerCommand, String data) {
	try {
	    OutputStream out = socket.getOutputStream();
	    InputStream in = socket.getInputStream();
	    String sendCmdPrefix = SEND_COMMAND + " ";
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
	    } else if (peerCommand.equals(RECEIVE_COMMAND)) {
		byte[] dataBytes = data.getBytes();
		String command = sendCmdPrefix + dataBytes.length + "\n";
		out.write(command.getBytes());
		out.write(dataBytes);
	    }
	    socket.close();
	} catch (IOException e) {
	    System.out.println("error when handling '" +peerCommand+ "' : " +e);
	}
    }

    public static String readPeerCommand(InputStream in) throws IOException {
	String peerCommand = null;
	byte buf[] = new byte[1024];
	for (int i=0; i<buf.length; i++) {
	    int value = in.read();
	    if (value == -1) {
		break;
	    }
	    if (value == '\n') {
		peerCommand = new String(buf, 0, i);
		break;
	    }
	    buf[i] = (byte) value;
	}
	return peerCommand;
    }

}

