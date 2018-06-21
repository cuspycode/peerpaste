package com.cuspycode.peerpaste;

import java.util.Map;
import java.util.LinkedHashMap;
import java.security.SecureRandom;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Secrets {
    private static SecureRandom rng = new SecureRandom();

    private static Map<String,Entry> ephemeralMap = new LinkedHashMap<String,Entry>();

    public static class Entry {
	public String secret;
	public long created;
	public int version;

	public Entry(String secret, long created, int version ) {
	    this.secret = secret;
	    this.created = created;
	    this.version = version;
	}

	public static Entry fromJSON(JSONObject json) {
	    Entry entry = null;
	    try {
		entry = new Entry(json.getString("secret"), json.getLong("created"), json.getInt("version"));
	    } catch (JSONException e) {
		entry = null;
	    }
	    return entry;
	}

	public String toJSON() {
	    String result = null;
	    JSONObject obj = new JSONObject();
	    try {
		obj.put("secret", secret);
		obj.put("created", created);
		obj.put("version", version);
		result = obj.toString();
	    } catch (JSONException e) {
		result = null;
	    }
	    return result;
	}
    }

    public static Entry getEntry(String peerName) {
	return ephemeralMap.get(peerName);
    }

    public static String getSecret(String peerName) {
	Entry entry = getEntry(peerName);
	return (entry != null? entry.secret : null);
    }

    public static void putSecret(String peerName, String secret) {
	Entry entry = new Entry(secret, System.currentTimeMillis(), 1);
	ephemeralMap.put(peerName, entry);
    }

    public static void removeSecret(String peerName) {
	ephemeralMap.remove(peerName);
    }

    public static String newSecret() {
	byte[] bytes = new byte[128/8];
	rng.nextBytes(bytes);
	return Base64.getEncoder().encodeToString(bytes);
    }

    public static Map<String,Entry> getAllSecrets() {
	return ephemeralMap;
    }

    public static void importFromJSON(JSONArray json) throws JSONException {
	Map<String,Entry> map = new LinkedHashMap<String,Entry>();
	for (int i=0; i<json.length(); i++) {
	    JSONObject obj = json.getJSONObject(i);
	    String key = obj.optString("peer", null);
	    if (key != null) {
		map.put(key, Entry.fromJSON(json.getJSONObject(i)));
	    }
	}
	ephemeralMap = map;
    }
}

