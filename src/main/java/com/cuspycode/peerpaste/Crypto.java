package com.cuspycode.peerpaste;

import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;
import java.security.MessageDigest;
import java.security.SecureRandom;

import java.util.Base64;

public class Crypto {
    private static SecureRandom rng = new SecureRandom();

    public static void main(String[] args) throws Exception {
	String[] parts = args[1].split(":");
	byte[] iv = Base64.getDecoder().decode(parts[0]);
	byte[] data = Base64.getDecoder().decode(parts[1]);
	System.out.println(new String(new Crypto().decrypt(data, args[0], iv), "UTF-8"));
    }

    private Cipher setup(int mode, byte[] iv, String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes("UTF-8"));
        byte[] sha256 = md.digest();
        SecretKey key = new SecretKeySpec(Arrays.copyOf(sha256, 128/8), "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
	if (mode == Cipher.ENCRYPT_MODE) {
	    rng.nextBytes(iv);			// Do this again for each new encryption
	}
        GCMParameterSpec spec = new GCMParameterSpec(16*8, iv);
        cipher.init(mode, key, spec);
        return cipher;
    }

    public byte[] encrypt(byte[] plaintext, String password, byte[] iv) throws Exception {
        return setup(Cipher.ENCRYPT_MODE, iv, password).doFinal(plaintext);
    }

    public static byte[] encrypt(byte[] plaintext, String password) throws Exception {
	byte[] iv = new byte[12];
	byte[] cryptext = new Crypto().encrypt(plaintext, password, iv);
	byte[] result = Arrays.copyOf(iv, iv.length + cryptext.length);
	for (int i=0; i<cryptext.length; i++) {
	    result[iv.length + i] = cryptext[i];
	}
        return result;
    }

    public byte[] decrypt(byte[] cryptext, String password, byte[] iv) throws Exception {
       return setup(Cipher.DECRYPT_MODE, iv, password).doFinal(cryptext);
    }

    public static byte[] decrypt(byte[] cryptext, String password) throws Exception {
	byte[] iv = Arrays.copyOf(cryptext, 12);
	byte[] data = Arrays.copyOfRange(cryptext, 12, cryptext.length);
	return new Crypto().decrypt(data, password, iv);
    }

}

