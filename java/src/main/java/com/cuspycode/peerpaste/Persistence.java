package com.cuspycode.peerpaste;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Persistence {

    private static final String APP_DIRNAME = "peerpaste";
    private static final String SETTINGS_FILENAME = "peerpaste-data.json";
    private static final String settingsDir = obtainSettingsDir();

    private static String obtainSettingsDir() {
	String dirParent = System.getenv("XDG_DATA_HOME");
	if (dirParent == null) {
	    dirParent = System.getProperty("user.home") + "/.local/share";
	}
	Path dir = Paths.get(dirParent + "/" + APP_DIRNAME);
	if (!Files.isDirectory(dir)) {
	    try {
		Files.createDirectories(dir);
		Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("rwx------"));
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	}
	return dir.toString();
    }

    public static String settingsPath() {
	return settingsDir + "/" + SETTINGS_FILENAME;
    }

    private static long latestLoadTime = 0;

    public static long getLatestLoadTime() {
	return latestLoadTime;
    }

    public static JSONArray load() {
	Path path = Paths.get(settingsPath());
	if (Files.isReadable(path)) {
	    try {
		String content = new String(Files.readAllBytes(path), "UTF-8");
		latestLoadTime = System.currentTimeMillis();
		return new JSONArray(content);
	    } catch (IOException|JSONException e) {
		// ignore, let the settings be empty
		e.printStackTrace();
	    }
	}
	return null;
    }

    public static void save(JSONArray json) {
	try {
	    byte[] data = json.toString(4).getBytes("UTF-8");
	    Path tmpPath = Files.createTempFile(Paths.get(settingsDir), SETTINGS_FILENAME+ "-", "-tmp");
	    Files.write(tmpPath, data);
	    Files.move(tmpPath, Paths.get(settingsPath()), ATOMIC_MOVE, REPLACE_EXISTING);
	} catch (IOException|JSONException e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) throws Exception {
	if ("load".equals(args[0])) {
	    JSONArray json = load();
	    if (json != null) {
		System.out.println(json.toString(4));
	    } else {
		System.out.println("Can't read " +settingsPath());
	    }
	} else if ("save".equals(args[0])) {
	    JSONArray json = new JSONArray();
	    JSONObject obj = new JSONObject();
	    obj.put("comment", "PeerPaste settings");
	    json.put(obj);
	    obj = new JSONObject();
	    obj.put("peer", "Dummy Device");
	    obj.put("secret", "abc");
	    obj.put("created", 123);
	    obj.put("version", 1);
	    json.put(obj);
	    save(json);
	}
    }
}

