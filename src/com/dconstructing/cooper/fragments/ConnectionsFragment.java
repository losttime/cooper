package com.dconstructing.cooper.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;

public class ConnectionsFragment extends Fragment {

	public final String TAG = getClass().getSimpleName();
	
	OnAddConnectionOptionListener mAddConnectionCallback;
	
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.connections, menu); // Add this Fragment's menu items to the menu.
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_connection:
				if (MainActivity.isDebuggable) Log.i(TAG, "Menu Item - Add Connection");
				mAddConnectionCallback.onAddConnectionSelected();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	
	
	
	public interface OnAddConnectionOptionListener {
        public void onAddConnectionSelected();
    }
}
