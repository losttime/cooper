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
package com.dconstructing.cooper.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * @author burgundy
 *
 */
public class CooperOpenHelper extends SQLiteOpenHelper {

	public final String TAG = getClass().getSimpleName();
	
	public static final String DATABASE_NAME="connections.db";
	public static final int DATABASE_VERSION=1;
	
	public static final String CONNECTIONS_TABLE_NAME="connections";
	public static final String USERNAME_FIELD_NAME="username";
	public static final String HOST_FIELD_NAME="host";
	
	private static final String CONNECTIONS_TABLE_CREATE =
            "CREATE TABLE " + CONNECTIONS_TABLE_NAME + " (" +
            BaseColumns._ID + " INTEGER PRIMARY KEY, " +
            USERNAME_FIELD_NAME + " TEXT, " +
            HOST_FIELD_NAME + " TEXT);";
	

	public CooperOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CONNECTIONS_TABLE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
