package com.cuspycode.peerpaste;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Persistence {

    public static String settingsPath() {
	return System.getProperty("home.dir") + "/.peerpaste-settings";
    }

    public static void load() {
	try {
	    String content = new String(Files.readAllBytes(Paths.get(settingsPath())), "UTF-8");
	    JSONArray json = new JSONArray(content);
	} catch (IOException|JSONException e) {
	}
    }

    public static void save() {
    }
}

