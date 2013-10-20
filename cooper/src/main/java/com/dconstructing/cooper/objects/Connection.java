/**
 * This file is part of Cooper, a touch-friendly SSH client for Android devices
 * Copyright (C) 2013  David Cox <losttime.shuffle@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dconstructing.cooper.objects;

import android.util.Log;

import com.dconstructing.cooper.MainActivity;
import com.jcraft.jsch.Session;

public class Connection {

	public final String TAG = getClass().getSimpleName();
	
    public final static int NOT_CONNECTED = 0;
    public final static int CONNECTING = 1;
    public final static int CONNECTED = 10;
    public final static int DISCONNECTING = 11;
    
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
