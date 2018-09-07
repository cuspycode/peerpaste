package com.cuspycode.peerpaste;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

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

	long timeout = 30*1000L;
	if (!Resolver.resolve(target, timeout)) {
	    GUI.println("Couldn't resolve peer '" +target+ "', exiting.");
	    return;
	}
	Socket socket = new Socket(Resolver.getAddress(), Resolver.getPort());

	OutputStream out = socket.getOutputStream();
	String line = ME_COMMAND+ " " +Publish.getOwnName()+ "\n";
	if (command.startsWith("+")) {
	    command = command.substring(1);
	    line = OOB_COMMAND+ line.substring(ME_COMMAND.length());
	}
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
		GUI.println(greeting);
		break;
	    }
	    buf[i] = (byte) value;
	}
	handleCommand(command, myData, target, out, in);
	socket.close();
    }

    private static void handleCommand(String command, String myData, String remoteName, OutputStream out, InputStream in) throws Exception {
	if (SEND_COMMAND.equals(command)) {
	    myData = GUI.copy();
	    if (myData == null) {
		myData = "";
	    }
	    byte[] myBytes = Crypto.encrypt(myData.getBytes(), Secrets.getSecret(remoteName));
	    String line = command+ " " +myBytes.length+ "\n";
	    out.write(line.getBytes());
	    out.write(myBytes);
	    Server.swallowStream(in);
	    GUI.println("Sent " +myBytes.length+ " bytes.");
	} else if (RECEIVE_COMMAND.equals(command)){
	    String line = command+ "\n";
	    out.write(line.getBytes());
	    String peerCommand = Server.readPeerCommand(in);
	    // Check for peerCommand == null here...
	    String sendCmdPrefix = SEND_COMMAND+ " ";
	    if (peerCommand.startsWith(sendCmdPrefix)) {
		int declaredSize = Integer.parseInt(peerCommand.substring(sendCmdPrefix.length()));

		byte[] resultBytes = Server.readBytes(in, declaredSize);
		String result = new String(Crypto.decrypt(resultBytes, Secrets.getSecret(remoteName)));

		GUI.println("Received " +resultBytes.length+ " bytes.");
		GUI.printlnDebug("Received text: \"" +result+ "\"");
		GUI.paste(result);
	    }
	}
    }

}

