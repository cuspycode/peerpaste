package com.cuspycode.peerpaste;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.ArrayList;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import java.util.Base64;			// For debugging

public class Server {

    final static String ME_COMMAND = "ME";
    final static String OOB_COMMAND = "OOB";
    final static String SEND_COMMAND = "SEND";
    final static String RECEIVE_COMMAND = "RECEIVE";

    private static String remoteName = null;				// Kludge

    public static void main(String[] args) throws Exception {
	String myData = "The quick fox pasted a lazy dog";
	if (args.length > 0) {
	    myData = args[0];
	}
	// Initialize a server socket on the next available port.
	////ServerSocket server = new ServerSocket(0);
	ServerSocket server = new ServerSocket(1235);
	server.setSoTimeout(3000);

	Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() {
		    GUI.println("\nShutting down...");
		    try {
			Publish.stop();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    });

	// Publish the service on mDNS
	Publish.start("Your friendly Java server", server.getLocalPort());

	for (;;) {
	    try {
		Socket socket = server.accept();
		OutputStream out = socket.getOutputStream();
		out.write("JavaSE-PeerPaste v0.1\n".getBytes());
		String peerCommand = readPeerCommand(socket.getInputStream());
		handleCommand(socket, peerCommand, myData);
	    } catch (SocketTimeoutException e) {
		// ignore
	    }
	}

	//Publish.stop();
    }

    private static void handleCommand(Socket socket, String peerCommand, String data) {
 System.out.println("handling peerCommand: '" +peerCommand+ "'");
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
		GUI.println("encrypted string: '" +Base64.getEncoder().encodeToString(resultBytes)+ "'");

		String sharedSecret = Secrets.getSecret(remoteName);
		if (sharedSecret != null) {
		    String result = new String(Crypto.decrypt(resultBytes, sharedSecret));
		    GUI.println("result string: '" +result+ "'");

		    // Preliminary AWT paste (i.e. CTRL-V, but not xsel)
		    StringSelection selection = new StringSelection(result);
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents(selection, selection);
		} else {
		    GUI.println("\nMissing decryption secret. Please delete the key from the peer device and try again\n");
		}

	    } else if (peerCommand.equals(RECEIVE_COMMAND)) {

		// Preliminary AWT copy (text-only for now)
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable clip = clipboard.getContents(null);
		if (clip != null) {
		    if (clip.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
			    data = (String) clip.getTransferData(DataFlavor.stringFlavor);
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }
		}

		byte[] dataBytes = Crypto.encrypt(data.getBytes(), Secrets.getSecret(remoteName));
		String command = sendCmdPrefix + dataBytes.length + "\n";
		out.write(command.getBytes());
		out.write(dataBytes);
	    }
	    socket.close();
	} catch (Exception e) {
	    System.out.println("error when handling '" +peerCommand+ "' : " +e);
	    e.printStackTrace();
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
	    System.out.println("error: received " +receivedSize+ " bytes, expected " +declaredSize);
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

