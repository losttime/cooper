package com.dconstructing.cooper.contentproviders;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.database.CooperOpenHelper;

public class ConnectionsContentProvider extends ContentProvider {

	public final String TAG = getClass().getSimpleName();
	
	public static final int ALL_CONNECTIONS = 1;
	
	private static final int CONNECTIONS = 10;
	private static final int CONNECTION_ID = 20;
	
	private static final String AUTHORITY = "com.dconstructing.cooper.contentproviders";
	private static final String BASE_PATH = "connections";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/todos";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/todo";
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONNECTIONS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONNECTION_ID);
	}
	
	protected CooperOpenHelper mOpenHelper;
	
	public ConnectionsContentProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Insert URI: " + uri.toString());
	    long rowId = 0;
	    
	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
	    	case CONNECTIONS:
	    		rowId = mOpenHelper.getWritableDatabase().insert(CooperOpenHelper.CONNECTIONS_TABLE_NAME, null, values);
	    		if (MainActivity.isDebuggable) Log.i(TAG, "New Row: " + Long.toString(rowId));
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Unknown URI");
	    }
	    
	    if (rowId > 0) {
	    	if (MainActivity.isDebuggable) Log.i(TAG, "Notifying of update");
		    getContext().getContentResolver().notifyChange(uri, null);

		    Uri newUri = uri.buildUpon().appendPath(String.valueOf(rowId)).build();
	    	getContext().getContentResolver().notifyChange(newUri, null);
	    	return newUri;
	    }

	    throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new CooperOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Query URI: " + uri.toString());
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case CONNECTIONS:
				SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
				queryBuilder.setTables(CooperOpenHelper.CONNECTIONS_TABLE_NAME);
				Cursor cursor = queryBuilder.query(mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
				cursor.setNotificationUri(getContext().getContentResolver(), uri); // to get this to update when the URI is notified of change (in updates/inserts)
				return cursor;
			default:
				return null;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int numOfRows = 0;
	    
	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
	    	case CONNECTIONS:
	    		numOfRows = mOpenHelper.getWritableDatabase().update(CooperOpenHelper.CONNECTIONS_TABLE_NAME, values, selection, selectionArgs);
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Unknown URI: " + uri.toString());
	    }

	    if (numOfRows > 0) {
	    	getContext().getContentResolver().notifyChange(uri, null);
	    }
	    
	    return numOfRows;
	}

}
