package com.dconstructing.cooper.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.dconstructing.cooper.MainActivity;
import com.dconstructing.cooper.R;

public class PasswordDialogFragment extends DialogFragment implements OnEditorActionListener {

	public final String TAG = getClass().getSimpleName();
	
	private String mHost;
	private String mUsername;
	private EditText mEditText;
	
	public PasswordDialogFragment() {
	}
	
	public static PasswordDialogFragment create(String host, String username) {
		PasswordDialogFragment fragment = new PasswordDialogFragment();
		
    	Bundle bundle = new Bundle();
    	bundle.putString("host", host);
    	bundle.putString("username", username);
    	fragment.setArguments(bundle);
    	
    	return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		mHost = args.getString("host");
		mUsername = args.getString("username");
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    View view = inflater.inflate(R.layout.dialog_password, null);
		mEditText = (EditText) view.findViewById(R.id.password_field);
		
		// Show soft keyboard automatically
        mEditText.requestFocus();
        
        // set listeners
        mEditText.setOnEditorActionListener(this);

        builder.setView(view)
	    	   .setTitle(mUsername + "@" + mHost)
	    // Add action buttons
	           .setPositiveButton(R.string.login_button, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   if (MainActivity.isDebuggable) Log.i(TAG, "'Login' button pressed in dialog");
	            	   passwordSubmitted();
	               }
	           })
	           .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   if (MainActivity.isDebuggable) Log.i(TAG, "'Cancel' button pressed in dialog");
	                   PasswordDialogFragment.this.getDialog().cancel();
	               }
	           });      
	    return builder.create();
	}

	/**
	 * This is a hint that prompts Android to show the soft keyboard (?)
	 * according to: http://android-developers.blogspot.com/2012/05/using-dialogfragments.html
	 * This must be implemented here because implementing it in onCreateDialog is too early.
	 * The "requestFocus()" portion is implemented in onCreateDialog because the desired EditText
	 * view is not available here.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        
		return null;
	}
	
	@Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
        	if (MainActivity.isDebuggable) Log.i(TAG, "DONE pressed on keyboard");
            passwordSubmitted();
            return true;
        }
        return false;
    }
	
	
	
	
	
	public void passwordSubmitted() {
		if (MainActivity.isDebuggable) Log.i(TAG, "Password submitted, but we're not logging it ;)");
		((PasswordDialogListener)getActivity()).onPasswordEntered(mHost, mUsername, mEditText.getText().toString());
		this.dismiss();
	}
	
	
	
	
	
	
	public interface PasswordDialogListener {
        void onPasswordEntered(String host, String username, String password);
    }	
}
