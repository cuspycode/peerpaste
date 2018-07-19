package com.cuspycode.peerpaste;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GUI {
    public static void main(String[] args) throws Exception {
	if (GraphicsEnvironment.isHeadless()) {
	    System.out.println("Headless");
	} else {
	    System.out.println("Graphic");
	    Example ex = new Example();
	    ex.setVisible(true);
	}
    }

    public static class Example extends JFrame {
	public Example() {
	    setTitle("Simple example");
	    setSize(300, 200);
	    setLocationRelativeTo(null);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
    }

}



