package com.dconstructing.cooper.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;
import com.dconstructing.cooper.contentproviders.ConnectionsContentProvider;
import com.dconstructing.cooper.database.CooperOpenHelper;

public class ConnectionsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public final String TAG = getClass().getSimpleName();
	
    public static final int MSG_SHOW_ADD_PAGE = 101;

    OnAddConnectionOptionListener mAddConnectionCallback;
    SimpleCursorAdapter mAdapter;
    
    boolean defaultedToAdd = false;

    private Handler mHandler = new Handler() {
    	/**
    	 * Have to jump through this hoop because we want to move immediately to the add page
    	 * if the home page is loaded without any connections to display. We can't to a
    	 * fragment transaction called from onLoadFinished of the Loader Callbacks.
    	 * Even the Contextual Action Bar uses this message because we don't want to do the
    	 * same thing from multiple locations.
    	 */
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_SHOW_ADD_PAGE) {
            	mAddConnectionCallback.onAddConnectionSelected();
            }
        }
    };
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Check to ensure the calling Activity implements OnAddConnectionOptionListener
		// and is therefore ready to handle callbacks.
		try {
			mAddConnectionCallback = (OnAddConnectionOptionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnAddConnectionOptionListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true); // Let the Activity know that this Fragment has menu options to add.
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connections, container, false);
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState == null) {
			mAdapter = new SimpleCursorAdapter(this.getActivity(),
	                android.R.layout.simple_list_item_2,
	                null,
	                new String[] {CooperOpenHelper.USERNAME_FIELD_NAME,CooperOpenHelper.HOST_FIELD_NAME},
	                new int[] {android.R.id.text1, android.R.id.text2},
	                0);
			setListAdapter(mAdapter);
			
			// Open the database
			getLoaderManager().initLoader(ConnectionsContentProvider.ALL_CONNECTIONS, null, this);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.connections, menu); // Add this Fragment's menu items to the menu.
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_connection:
				if (MainActivity.isDebuggable) Log.i(TAG, "Menu Item - Add Connection");
				mHandler.sendEmptyMessage(MSG_SHOW_ADD_PAGE);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		switch(loaderId) {
			case ConnectionsContentProvider.ALL_CONNECTIONS:
				String[] projection = {BaseColumns._ID,
						CooperOpenHelper.USERNAME_FIELD_NAME,
						CooperOpenHelper.HOST_FIELD_NAME};
				String selection = null;
				String[] args = null;
				String sort = null;
				return new CursorLoader(this.getActivity(), ConnectionsContentProvider.CONTENT_URI, projection, selection, args, sort);
			default:
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Finished Loading Loader");
		switch (loader.getId()) {
			case ConnectionsContentProvider.ALL_CONNECTIONS:
				if (cursor.getCount() > 0) {
					if (MainActivity.isDebuggable) Log.i(TAG, "Got " + Integer.toString(cursor.getCount()) + " connections from database.");
					mAdapter.swapCursor(cursor);
				} else {
					if (MainActivity.isDebuggable) Log.i(TAG, "No connections in database. Prompt for a new one.");
					mAdapter.swapCursor(null);
					if (!defaultedToAdd) {
						defaultedToAdd = true; // Don't keep forcing the add page if they move away from it the first time it's pushed on them.
						mHandler.sendEmptyMessage(MSG_SHOW_ADD_PAGE);
					}
				}
				break;
			default:
				if (MainActivity.isDebuggable) Log.i(TAG, "Unknown loader id: " + Integer.toString(loader.getId()));
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Loader being reset");
		mAdapter.swapCursor(null);
	}

	
	
	
	
	
	
	
	public interface OnAddConnectionOptionListener {
        public void onAddConnectionSelected();
    }
}
