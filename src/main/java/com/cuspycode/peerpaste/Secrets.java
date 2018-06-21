package com.cuspycode.peerpaste;

import java.util.Map;
import java.util.LinkedHashMap;
import java.security.SecureRandom;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Secrets {
    private static final long CACHE_EXPIRY_MILLIS = 10000;

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
	maybeLoad();
	return ephemeralMap.get(peerName);
    }

    public static String getSecret(String peerName) {
	Entry entry = getEntry(peerName);
	return (entry != null? entry.secret : null);
    }

    public static void putSecret(String peerName, String secret) {
	maybeLoad();
	Entry entry = new Entry(secret, System.currentTimeMillis(), 1);
	ephemeralMap.put(peerName, entry);
	Persistence.save(exportToJSON());
    }

    public static void removeSecret(String peerName) {
	maybeLoad();
	ephemeralMap.remove(peerName);
	Persistence.save(exportToJSON());
    }

    public static String newSecret() {
	byte[] bytes = new byte[128/8];
	rng.nextBytes(bytes);
	return Base64.getEncoder().encodeToString(bytes);
    }

    public static Map<String,Entry> getAllSecrets() {
	maybeLoad();
	return ephemeralMap;
    }

    private static void maybeLoad() {
	if (System.currentTimeMillis() > Persistence.getLatestLoadTime() + CACHE_EXPIRY_MILLIS) {
	    JSONArray json = Persistence.load();
	    if (json != null) {
		importFromJSON(json);
	    }
	}
    }

    public static void importFromJSON(JSONArray json) {
	Map<String,Entry> map = new LinkedHashMap<String,Entry>();
	try {
	    for (int i=0; i<json.length(); i++) {
		JSONObject obj = json.getJSONObject(i);
		String key = obj.optString("peer", null);
		if (key != null) {
		    map.put(key, Entry.fromJSON(json.getJSONObject(i)));
		}
	    }
	} catch (JSONException e) {
	    throw new RuntimeException(e);
	}
	ephemeralMap = map;
    }

    public static JSONArray exportToJSON() {
	JSONArray json = new JSONArray();
	try {
	    for (String key : ephemeralMap.keySet()) {
		Entry entry = ephemeralMap.get(key);
		JSONObject obj = new JSONObject();
		obj.put("peer", key);
		obj.put("secret", entry.secret);
		obj.put("created", entry.created);
		obj.put("version", entry.version);
		json.put(obj);
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return json;
    }

}
