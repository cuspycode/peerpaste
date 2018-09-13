package com.cuspycode.peerpaste;

import java.io.InputStream;
import java.io.IOException;

import com.google.zxing.common.BitMatrix;

import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Insets;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class GUI {
    private static final String VERSION = "0.9";
    private static final String AA_PROP_KEY = "awt.useSystemAAFontSettings";
    private static final int TEXT_ROWS = 5;
    private static final int TEXT_COLUMNS = 50;
    private static int debugLevel = 0;

    public static RootFrame rootFrame = null;
    public static JFrame qrCodeFrame = null;
    public static String headlessClipboard = "foobar";
    public static String connectTarget = null;
    public static boolean connectReceiveMode = false;

    public static void main(String[] args) throws Exception {
	parseOptions(args);
	if (GraphicsEnvironment.isHeadless()) {
	    System.out.println("Running headless");
	} else {
	    if (System.getProperty(AA_PROP_KEY) == null) {
		if ("Linux".equals(System.getProperty("os.name"))) {
		    System.setProperty(AA_PROP_KEY, "on");
		}
	    }
	    rootFrame = new RootFrame();
	    rootFrame.setVisible(true);
	}
	if (connectTarget != null) {
	    String op = (connectReceiveMode? "Receiving from" : "Sending to");
	    println(op + " '" +connectTarget+ "'");
	    startClient();
	    if (rootFrame != null) {
		if (connectReceiveMode) {
		    println("Close this window to exit.");
		} else {
		    printlnDebug("Exiting.");
		    rootFrame.dispose();
		}
	    }
	} else {
	    println("Starting server");
	    startServer();
	}
    }

    public static class RootFrame extends JFrame {
	public JTextArea text;

	public RootFrame() {
	    init();
	}

	private void init() {
	    setTitle("PeerPaste");
	    setLocationRelativeTo(null);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    text = new JTextArea(TEXT_ROWS, TEXT_COLUMNS);
	    text.setEditable(false);
	    text.setMargin(new Insets(5, 10, 5, 10));
	    getContentPane().add(text);
	    pack();
	}
    }

    private static void startClient() throws Exception {
	String command = (connectReceiveMode? "RECEIVE" : "SEND");
	Client.main(new String[] { connectTarget, command });
    }

    private static void startServer() throws Exception {
	Server.main(new String[] {});
    }

    public static void println(String message) {
	if (rootFrame != null) {
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			JTextArea textArea = rootFrame.text;
			String x = textArea.getText();
			x += message;
			String[] lines = x.split("\n");
			StringBuffer buf = new StringBuffer();
			for (int i=lines.length-TEXT_ROWS; i<lines.length; i++) {
			    if (i >= 0) {
				buf.append(lines[i] + "\n");
			    }
			}
			textArea.setText(buf.toString());
		    }
		});
	} else {
	    System.out.println(message);
	}
    }

    public static void printlnDebug(String message) {
	if (debugLevel > 0) {
	    System.err.println(message);
	}
    }

    private static void parseOptions(String[] args) {
	int i = 0;
	while (i < args.length) {
	    String opt = args[i++];
	    switch (opt) {
	    case "--help":
		if (printHelp()) {
		    System.exit(0);
		} else {
		    System.exit(1);
		}
		break;
	    case "--version":
		System.out.println(VERSION);
		System.exit(0);
		break;
	    case "--aa":
		System.setProperty(AA_PROP_KEY, args[i++]);
		break;
	    case "--debug-level":
		debugLevel = Integer.parseInt(args[i++]);
		break;
	    case "--paste":
		headlessClipboard = args[i++];
		break;
	    case "--name":
		Publish.serviceName = args[i++];
		break;
	    case "--send-to":
		connectTarget = args[i++];
		connectReceiveMode = false;
		break;
	    case "--receive-from":
		connectTarget = args[i++];
		connectReceiveMode = true;
		break;
	    case "--port":
		Publish.servicePort = Integer.parseInt(args[i++]);
		break;
	    case "--address":
		Publish.ifAddr = args[i++];
		break;
	    case "--interface":
		Publish.ifName = args[i++];
		break;
	    default:
		System.err.println("Unrecognized option: " +opt);
	    }
	}
    }

    private static boolean printHelp() {
	InputStream s = GUI.class.getResourceAsStream("help.txt");
	try {
	    byte[] buf = new byte[8192];
	    int n;
	    while ((n = s.read(buf)) != -1) {
		System.out.write(buf, 0, n);
	    }
	    s.close();
	    return true;
	} catch(IOException e) {
	    e.printStackTrace();
	}
	return false;
    }

    public static void paste(String data) {
	if (rootFrame != null) {
	    // Preliminary AWT paste (i.e. CTRL-V, but not xsel)
	    StringSelection selection = new StringSelection(data);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	} else {
	    printlnDebug("Faking paste of '" +data+ "'");
	}
    }

    public static String copy() {
	String data = null;

	if (rootFrame != null) {
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
	} else {
	    data = "Faked cliboard copy data";
	}
	return data;
    }

    public static void showQRCodeImage(BitMatrix bitMatrix, int width, int height) {
	final int dotSize = 12;
	BufferedImage image = new BufferedImage(width*dotSize, height*dotSize, BufferedImage.TYPE_INT_RGB);
	Graphics2D graphics = (Graphics2D) image.getGraphics();
	graphics.setColor(Color.WHITE);
	graphics.fillRect(0, 0, width*dotSize, height*dotSize);
	graphics.setColor(Color.BLACK);
	for (int i=0; i<height; i++) {
	    for (int j=0; j<width; j++) {
		if (bitMatrix.get(j, i)) {
		    graphics.fillRect(j*dotSize, i*dotSize, dotSize, dotSize);
		}
	    }
	}
	JFrame frame = qrCodeFrame;
	if (frame == null) {
	    frame = new JFrame();
	    qrCodeFrame = frame;
	}
	frame.getContentPane().removeAll();
	frame.setTitle("PeerPaste encryption key");
	frame.setLocationRelativeTo(rootFrame);
	Icon icon = new ImageIcon(image);
        JLabel label = new JLabel(icon);
	frame.getContentPane().setLayout(new FlowLayout());
	frame.getContentPane().add(label);
	frame.pack();
	frame.setVisible(true);
    }

    public static void hideQRCodeImage() {
	if (qrCodeFrame != null) {
	    qrCodeFrame.setVisible(false);
	    qrCodeFrame.dispose();
	}
    }
}

