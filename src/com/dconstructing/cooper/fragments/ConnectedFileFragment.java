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
	String mContent;
	
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
			mContent = arguments.getString("content");
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
	
	
	
	
	
	
	
	public interface FileListener {
        public void onFileSaved(String tag, FilePath filePath);
    }
}
