package com.dconstructing.cooper.contentproviders;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.dconstructing.cooper.database.CooperOpenHelper;

public class ConnectionsContentProvider extends ContentProvider {

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new CooperOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case CONNECTIONS:
				SQLiteDatabase db = mOpenHelper.getReadableDatabase();
				SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
				queryBuilder.setTables(CooperOpenHelper.CONNECTIONS_TABLE_NAME);
				return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			default:
				return null;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
