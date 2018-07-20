package com.cuspycode.peerpaste;

import com.google.zxing.common.BitMatrix;

import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class GUI {
    public static RootFrame rootFrame = null;

    public static void main(String[] args) throws Exception {
	if (GraphicsEnvironment.isHeadless()) {
	    System.out.println("Headless");
	} else {
	    System.out.println("Graphic");
	    rootFrame = new RootFrame();
	    rootFrame.setVisible(true);
	}
	startServer();
    }

    public static class RootFrame extends JFrame {
	public RootFrame() {
	    init();
	}

	private void init() {
	    setTitle("PeerPaste");
	    setSize(300, 200);
	    setLocationRelativeTo(null);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
    }

    private static void startServer() throws Exception {
	Server.main(new String[] {});
    }

    public static void println(String message) {
	if (rootFrame != null) {

	} else {
	    System.out.println(message);
	}
    }

    public static void printlnDebug(String message) {
	System.err.println(message);
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
	JFrame frame = new JFrame();
	frame.setTitle("PeerPaste encryption key");
	frame.setLocationRelativeTo(rootFrame);
	Icon icon = new ImageIcon(image);
        JLabel label = new JLabel(icon);
	frame.getContentPane().setLayout(new FlowLayout());
	frame.getContentPane().add(label);
	frame.pack();
	frame.setVisible(true);
    }
}

