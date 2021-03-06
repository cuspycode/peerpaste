package com.cuspycode.peerpaste;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Base64;			// For debugging
import javax.crypto.AEADBadTagException;

import static com.cuspycode.peerpaste.GUI.VERSION;

public class Server {

    final static String ME_COMMAND = "ME";
    final static String OOB_COMMAND = "OOB";
    final static String SEND_COMMAND = "SEND";
    final static String RECEIVE_COMMAND = "RECEIVE";
    final static String GREETING_STRING = "P2 JavaSE-PeerPaste v" +VERSION+ "\n";

    private static String remoteName = null;				// Kludge

    public static void main(String[] args) throws Exception {
	String myData = "The quick fox pasted a lazy dog";
	if (args.length > 0) {
	    myData = args[0];
	}
	ServerSocket server = new ServerSocket(Publish.servicePort);
	Publish.servicePort = server.getLocalPort();			// Put back real port number
	server.setSoTimeout(3000);

	Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() {
		    if (Publish.ifAddr != null) {
			String shutdownMsg = "\nShutting down...";
			GUI.println(shutdownMsg);
			if (GUI.rootFrame != null) {
			    System.err.println(shutdownMsg);		// Window may already have disappeared
			}
			try {
			    Publish.stop();
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    }
		}
	    });

	// Publish the service on mDNS
	Publish.start();

	if (Publish.ifAddr == null) {
	    System.exit(1);
	}

	for (;;) {
	    try {
		Socket socket = server.accept();
		OutputStream out = socket.getOutputStream();
		out.write(GREETING_STRING.getBytes());
		String peerCommand = readPeerCommand(socket.getInputStream());
		handleCommand(socket, peerCommand, myData);
	    } catch (SocketTimeoutException e) {
		// ignore
	    }
	}

	//Publish.stop();
    }

    private static void handleCommand(Socket socket, String peerCommand, String data) {
	if (peerCommand == null) {
	    GUI.println("Lost connection to peer");
	    return;
	}
	GUI.printlnDebug("handling peerCommand: '" +peerCommand+ "'");
	GUI.hideQRCodeImage();
	try {
	    OutputStream out = socket.getOutputStream();
	    InputStream in = socket.getInputStream();
	    String meCmdPrefix = ME_COMMAND + " ";
	    String oobCmdPrefix = OOB_COMMAND + " ";
	    String sendCmdPrefix = SEND_COMMAND + " ";
	    if (peerCommand.startsWith(meCmdPrefix)) {
		remoteName = peerCommand.substring(meCmdPrefix.length());
		handleCommand(socket, readPeerCommand(socket.getInputStream()), data);

	    } else if (peerCommand.startsWith(oobCmdPrefix)) {
		remoteName = peerCommand.substring(oobCmdPrefix.length());
		String newSecret = Secrets.newSecret();
		Secrets.putSecret(remoteName, newSecret);
		GUI.println("\nPlease scan QR code for \"" +remoteName+ "\"\n");
		GenQR.showQRCode(newSecret);

	    } else if (peerCommand.startsWith(sendCmdPrefix)) {
		int declaredSize = Integer.parseInt(peerCommand.substring(sendCmdPrefix.length()));
		byte[] resultBytes = readBytes(in, declaredSize);
		GUI.printlnDebug("encrypted string: '" +Base64.getEncoder().encodeToString(resultBytes)+ "'");

		String sharedSecret = Secrets.getSecret(remoteName);
		if (sharedSecret != null) {
		    try {
			String result = new String(Crypto.decrypt(resultBytes, sharedSecret));
			out.write("OK\n".getBytes());
			GUI.println("Received " +result.getBytes().length+ " bytes.");
			if (GUI.showData) {
			    GUI.println("Received text is \"" +result+ "\"");
			}
			GUI.paste(result);
		    } catch (AEADBadTagException e) {
			GUI.println("\nWrong cryptographic key. Please delete the saved peer secret and try again\n");
		    }
		} else {
		    GUI.println("\nMissing decryption secret. Please delete the key from the peer device and try again\n");
		}

	    } else if (peerCommand.equals(RECEIVE_COMMAND)) {
		data = GUI.copy();
		if (data == null) {
		    data = "";
		}
		byte[] dataBytes = Crypto.encrypt(data.getBytes(), Secrets.getSecret(remoteName));
		String command = sendCmdPrefix + dataBytes.length + "\n";
		out.write(command.getBytes());
		out.write(dataBytes);
		readOK(in);
	    }
	    socket.close();
	} catch (Exception e) {
	    System.err.println("error when handling '" +peerCommand+ "' : " +e);
	    e.printStackTrace();
	}
    }

    private static String readBoundedLine(InputStream in, int bound) throws IOException {
	String line = null;
	byte buf[] = new byte[bound];
	for (int i=0; i<buf.length; i++) {
	    int value = in.read();
	    if (value == -1) {
		break;
	    }
	    if (value == '\n') {
		line = new String(buf, 0, i);
		break;
	    }
	    buf[i] = (byte) value;
	}
	return line;
    }

    public static String readPeerCommand(InputStream in) throws IOException {
	return readBoundedLine(in, 1024);
    }

    public static void readOK(InputStream in) throws IOException {
	String line = readBoundedLine(in, 3);
	swallowStream(in);
	if (!"OK".equals(line)) {
	    throw new IOException("Peer connection failed");
	}
    }

    public static void swallowStream(InputStream in) throws IOException {
	byte buf[] = new byte[1024];
	while (in.read(buf) != -1) {}
    }

    public static byte[] readBytes(InputStream in, int declaredSize) throws IOException {
	ArrayList<byte[]> accum = new ArrayList<byte[]>();
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
	    accum.add(Arrays.copyOf(buf, nbytes));
	    receivedSize += nbytes;
	}
	if (receivedSize != declaredSize) {
	    GUI.println("Error: received " +receivedSize+ " bytes, expected " +declaredSize);
	    accum.clear();
	    receivedSize = 0;
	}
	byte[] resultBytes = new byte[receivedSize];
	int i=0;
	for (byte[] chunk : accum) {
	    // This can probably be optimized?
	    for (int j=0; j<chunk.length; j++) {
		resultBytes[i++] = chunk[j];
	    }
	}
	return resultBytes;
    }
}

