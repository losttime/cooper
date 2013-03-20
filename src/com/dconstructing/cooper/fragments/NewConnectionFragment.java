package com.dconstructing.cooper.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;

public class NewConnectionFragment extends Fragment {

	public final String TAG = getClass().getSimpleName();
	
	ActionMode mActionMode;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mActionMode = getActivity().startActionMode(mActionModeCallback);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_connection, container, false);
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
	                //shareCurrentItem();
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	    	if (MainActivity.isDebuggable) Log.i(TAG, "Destroying ActionMode");
	    	// TODO: Save should take place here (or as a result of this)
	        mActionMode = null;
	        getFragmentManager().popBackStack(); // to go back
	    }
	};	
}
