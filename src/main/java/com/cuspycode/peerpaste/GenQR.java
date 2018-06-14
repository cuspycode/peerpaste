package com.cuspycode.peerpaste;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class GenQR {

    public static void showQRCode(String text) {
	try {
	    generateQRCodeImage(text, 32, 32);
	} catch (WriterException|IOException e) {
	    throw new RuntimeException(e);
	}
    }

    private static void generateQRCodeImage(String text, int width, int height)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

	int prevState = -1;
	for (int i=0; i<height; i++) {
	    StringBuilder line = new StringBuilder();
	    for (int j=0; j<width; j++) {
		int newState = (bitMatrix.get(i,j)? 1 : 0);
		if (newState != prevState) {
		    line.append("\u001b[" +(newState==1? "40" : "47")+ "m");
		    prevState = newState;
		}
		line.append("  ");
	    }
	    line.append("\u001b[0m");
	    System.out.println(line.toString());
	    prevState = -1;
	}
    }

    public static void main(String[] args) {
        try {
            generateQRCodeImage(args[0], 32, 32);
        } catch (WriterException e) {
            System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Could not generate QR Code, IOException :: " + e.getMessage());
        }
    }
}
