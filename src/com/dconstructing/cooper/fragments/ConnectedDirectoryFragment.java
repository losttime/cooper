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
package com.dconstructing.cooper.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;
import com.dconstructing.cooper.objects.FilePath;

public class ConnectedDirectoryFragment extends ListFragment {

	public final String TAG = getClass().getSimpleName();
	
	DirectoryListener mDirectoryCallback;
	ArrayList<String> mFiles;
	ArrayList<String> mDirectories;
	
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
			mFiles = arguments.getStringArrayList("files");
			mDirectories = arguments.getStringArrayList("directories");
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
			ArrayList<FilePath> list = new ArrayList<FilePath>();
			if (mDirectories != null) {
				for(String directory : mDirectories) {
					list.add(new FilePath(directory, true));
				}
			}
			if (mFiles != null) {
				for(String file : mFiles) {
					list.add(new FilePath(file, false));
				}
			}
			ArrayAdapter<FilePath> adapter = new ArrayAdapter<FilePath>(this.getActivity(),
	                android.R.layout.simple_list_item_1,
	                list);
			setListAdapter(adapter);
		}
	}

	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Position: " + position + " ID: " + id);
		super.onListItemClick(list, view, position, id);
		
		final FilePath item = (FilePath) list.getItemAtPosition(position);
		if (MainActivity.isDebuggable) Log.i(TAG, "String: " + item);
		
		mDirectoryCallback.onDirectoryItemSelected(getTag(), item);
	}
	
	

	
	
	
	
	
	public void processResponse(ArrayList<String> files, ArrayList<String> directories) {
		ArrayAdapter<FilePath> adapter = (ArrayAdapter<FilePath>)getListAdapter();
		adapter.setNotifyOnChange(false);
		adapter.clear();
		
		//String[] lines = response.split(System.getProperty("line.separator"));
		//adapter.add("..");
		//for(String line : lines) {
		//	adapter.add(line);
		//}
		for (String directory : directories) {
			adapter.add(new FilePath(directory, true));
		}
		for (String file : files) {
			adapter.add(new FilePath(file, false));
		}

		adapter.notifyDataSetChanged();
	}
	
	
	
	
	
	
	
	public interface DirectoryListener {
        public void onDirectoryItemSelected(String tag, FilePath filePath);
    }

}
