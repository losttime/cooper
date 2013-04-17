package com.dconstructing.cooper.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;

public class ConnectedDirectoryFragment extends ConnectedFragment {

	public final String TAG = getClass().getSimpleName();
	
	DirectoryListener mDirectoryCallback;
	String mLs;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Check to ensure the calling Activity implements DirectoryListener
		// and is therefore ready to handle callbacks.
		try {
			mDirectoryCallback = (DirectoryListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DirectoryListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		if (arguments != null) {
			mLs = arguments.getString("response");
		}
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (MainActivity.isDebuggable) Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_directory, container, false);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState == null) {
			ArrayList<String> list = new ArrayList<String>();
			if (mLs != null) {
				String[] lines = mLs.split(System.getProperty("line.separator"));
				for(String line : lines) {
					list.add(line);
				}
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
	                android.R.layout.simple_list_item_1,
	                list);
			setListAdapter(adapter);
		}
	}

	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Position: " + position + " ID: " + id);
		super.onListItemClick(list, view, position, id);
		
		final String item = (String) list.getItemAtPosition(position);
		if (MainActivity.isDebuggable) Log.i(TAG, "String: " + item);
		
		mDirectoryCallback.onDirectoryItemSelected(getTag(), item);
	}
	
	

	
	
	
	
	
	public void processResponse(String response) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Response: " + response);
		ArrayAdapter<String> adapter = (ArrayAdapter<String>)getListAdapter();
		adapter.setNotifyOnChange(false);
		adapter.clear();
		
		String[] lines = response.split(System.getProperty("line.separator"));
		adapter.add("..");
		for(String line : lines) {
			adapter.add(line);
		}
		
		adapter.notifyDataSetChanged();
	}
	
	
	
	
	
	
	
	public interface DirectoryListener {
        public void onDirectoryItemSelected(String tag, String itemName);
    }

}
