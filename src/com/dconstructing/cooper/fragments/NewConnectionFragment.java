package com.dconstructing.cooper.fragments;

import android.app.Fragment;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;
import com.dconstructing.cooper.contentproviders.ConnectionsContentProvider;
import com.dconstructing.cooper.database.CooperOpenHelper;

public class NewConnectionFragment extends Fragment {

	public final String TAG = getClass().getSimpleName();
	
	ActionMode mActionMode;
	EditText mAddressField;
	EditText mUsernameField;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mActionMode = getActivity().startActionMode(mActionModeCallback);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_new_connection, container, false);
    	
    	mAddressField = (EditText) view.findViewById(R.id.connection_address_field);
    	mUsernameField = (EditText) view.findViewById(R.id.connection_username_field);

    	return view;
    }

	
	
	
	private void saveConnection() {
		if (!mAddressField.getText().toString().equals("") && !mUsernameField.getText().toString().equals("")) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(CooperOpenHelper.HOST_FIELD_NAME, mAddressField.getText().toString()); // string
			contentValues.put(CooperOpenHelper.USERNAME_FIELD_NAME, mUsernameField.getText().toString()); // string
				
			boolean idSet = false;
			if (idSet) {
				String selection = null;
				String[] selectionArgs = null;
				
				int rowsUpdated = getActivity().getContentResolver().update(ConnectionsContentProvider.CONTENT_URI, contentValues, selection, selectionArgs);
			} else {
				Uri newUri = getActivity().getContentResolver().insert(ConnectionsContentProvider.CONTENT_URI, contentValues);
			}
		}
	}
	
	
	
	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.new_connection, menu);
	    	mode.setTitle(R.string.add_connection);
	        return true;
	    }

	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
	    // may be called multiple times if the mode is invalidated.
	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.cancel_new_connection:
	            	if (MainActivity.isDebuggable) Log.i(TAG, "Canceling save");
	            	// TODO: In order to cancel, key information will need to be cleared before destroying.
	            	mAddressField.setText("");
	            	mUsernameField.setText("");
	            	
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	            	if (MainActivity.isDebuggable) Log.i(TAG, "Pressed: " + Integer.toString(item.getItemId()));
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	    	if (MainActivity.isDebuggable) Log.i(TAG, "Address to Save: " + mAddressField.getText());
	    	if (MainActivity.isDebuggable) Log.i(TAG, "Username to Save: " + mUsernameField.getText());
	    	if (MainActivity.isDebuggable) Log.i(TAG, "Destroying ActionMode");
	        mActionMode = null;
	    	saveConnection();
	        getFragmentManager().popBackStack(); // to go back
	    }
	};	
}
