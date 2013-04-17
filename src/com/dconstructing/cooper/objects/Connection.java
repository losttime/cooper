package com.dconstructing.cooper.objects;

import android.util.Log;

import com.dconstructing.cooper.MainActivity;
import com.jcraft.jsch.Session;

public class Connection {

	public final String TAG = getClass().getSimpleName();
	
	public String path;
	public Session session;
	
	public void updatePath(String pathChange) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Updating path: " + pathChange);
		
		if (pathChange.equals("..")) {
			// go up a directory
			path = path.substring(0, path.lastIndexOf("/"));
		} else {
			// add a directory
			path = path + "/" + pathChange.replaceAll("\n", "");
		}
		
		if (MainActivity.isDebuggable) Log.i(TAG, "New path: " + path);
	}

}
