package com.dconstructing.cooper.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;
import com.dconstructing.cooper.objects.FilePath;

public class ConnectedFileFragment extends Fragment {

	public final String TAG = getClass().getSimpleName();
	
	FileListener mFileCallback;
	FilePath mPath;
	String mContent;
	Long mUuid;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Check to ensure the calling Activity implements DirectoryListener
		// and is therefore ready to handle callbacks.
		try {
			mFileCallback = (FileListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement FileListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		if (arguments != null) {
			mPath = new FilePath(arguments.getString("path"), false);
			mContent = arguments.getString("content");
			mUuid = arguments.getLong("uuid");
		}
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (MainActivity.isDebuggable) Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_file, container, false);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState == null) {
			insertContent();
		}
	}
	
	@Override
	public void onDestroyView() {
		if (MainActivity.isDebuggable) Log.i(TAG, "onDestroyView");
		saveContent();
		super.onDestroyView();
	}

	
	
	
	
	
	public void processResponse(String content) {
		mContent = content;
		insertContent();
	}
	
	public void insertContent() {
		EditText editor = (EditText)getView().findViewById(R.id.fileContent);
		if (mContent != null) {
			editor.setText(mContent);
		}
	}
	
	public void saveContent() {
		if (MainActivity.isDebuggable) Log.i(TAG, "Saving Content");
		String content = ((EditText)getView().findViewById(R.id.fileContent)).getText().toString();
		if (content != null) {
			mFileCallback.onFileSaved(mUuid, mPath, content);
		}
	}
	
	
	
	
	
	
	
	public interface FileListener {
        public void onFileSaved(Long Uuid, FilePath filePath, String content);
    }
}
