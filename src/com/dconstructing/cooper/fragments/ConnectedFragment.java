package com.dconstructing.cooper.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;

public class ConnectedFragment extends ListFragment {

	public final String TAG = getClass().getSimpleName();
	
	ConnectedFragmentListener mFragmentCallback;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (MainActivity.isDebuggable) Log.i(TAG, "onAttach");

		// Check to ensure the calling Activity implements OnAddConnectionOptionListener
		// and is therefore ready to handle callbacks.
		try {
			mFragmentCallback = (ConnectedFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (MainActivity.isDebuggable) Log.i(TAG, "onCreate");
		String tag = getTag();
		if (MainActivity.isDebuggable) Log.i(TAG, "Fragment Tag: " + tag);
		mFragmentCallback.onFragmentLoaded(getTag());
		//setHasOptionsMenu(true); // Let the Activity know that this Fragment has menu options to add.
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (MainActivity.isDebuggable) Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_directory, container, false);
    }
	
	
	
	
	
	
	public void processResponse(String response) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Response: " + response);
	}

	
	
	
	
	
	public interface ConnectedFragmentListener {
        public void onFragmentLoaded(String tag);
    }
}
