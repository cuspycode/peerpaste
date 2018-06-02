package com.cuspycode.netpaste;

import java.util.Map;
import java.util.LinkedHashMap;
import java.security.SecureRandom;
import java.util.Base64;

public class Secrets {
    private static SecureRandom rng = new SecureRandom();

    private static Map<String,String> ephemeralMap = new LinkedHashMap<String,String>();

    public static String getSecret(String peerName) {
	return ephemeralMap.get(peerName);
    }

    public static void putSecret(String peerName, String secret) {
	ephemeralMap.put(peerName, secret);
    }

    public static void removeSecret(String peerName) {
	ephemeralMap.remove(peerName);
    }

    public static String newSecret() {
	byte[] bytes = new byte[128/8];
	rng.nextBytes(bytes);
	return Base64.getEncoder().encodeToString(bytes);
    }

    public static Map<String,String> getAllSecrets() {
	return ephemeralMap;
    }
}

