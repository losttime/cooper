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
            BaseColumns._ID + " INT, " +
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
