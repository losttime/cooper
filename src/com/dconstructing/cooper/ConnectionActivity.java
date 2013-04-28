package com.dconstructing.cooper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.dconstructing.cooper.fragments.ConnectedDirectoryFragment;
import com.dconstructing.cooper.fragments.ConnectedDirectoryFragment.DirectoryListener;
import com.dconstructing.cooper.objects.FilePath;
import com.dconstructing.cooper.services.ConnectionService;

public class ConnectionActivity extends Activity implements DirectoryListener {

	public final String TAG = getClass().getSimpleName();
	
	Messenger mService = null;
	ArrayList<Long> mConnectionQueue = new ArrayList<Long>();
	
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    if (MainActivity.isDebuggable) Log.i(TAG, "onCreate");
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
        if (savedInstanceState == null) {
            // During initial setup
        	Intent intent = getIntent();
        	long uuid = intent.getLongExtra("uuid", 0);
        	showConnection(uuid);
       		//bringToFront(uuid);
        }
	    
	}
	
	@Override
	public void onDirectoryItemSelected(String tag, FilePath filePath) {
		if (mService == null) {
			//this.queueRead(tag, filePath);
			
			if (MainActivity.isDebuggable) Log.i(TAG, "Gotta start the Connection Service");
	        try {
	        	getApplicationContext().bindService(new Intent(this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
	        } catch (SecurityException e) {
	        	if (MainActivity.isDebuggable) Log.e(TAG, "Could not bind to service", e);
	        }
		} else {
			if (filePath.isDirectory) {
				loadDirectoryContent(Long.parseLong(tag), filePath.name);			
			} else {
				openSelectedItem(Long.parseLong(tag), filePath.name);
			}
		}
	}

	
	
	
	
	
	
	
	
    
    /*
    public void initiateQueuedConnections() {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Working through the queue");
    	
    	Iterator<HashMap<String, Object>> it = mOpenQueue.iterator();
    	while (it.hasNext()) {
    		HashMap<String, Object> read = (HashMap<String, Object>) it.next();
    		onDirectoryItemSelected((String)read.get("tag"),(FilePath)read.get("filePath"));
    		it.remove();
    	}
    }
	*/
    
    public void showConnection(long uuid) {
    	if (mService == null) {
			this.queueConnection(uuid);
			
			if (MainActivity.isDebuggable) Log.i(TAG, "Gotta start the Connection Service");
	        try {
	        	getApplicationContext().bindService(new Intent(this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
	        } catch (SecurityException e) {
	        	if (MainActivity.isDebuggable) Log.e(TAG, "Could not bind to service", e);
	        }
    	} else {
    		loadDirectoryContent(uuid, null);
    	}
    }
    
    public void queueConnection(long uuid) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Queuing connection " + Long.toString(uuid));
    	mConnectionQueue.add(uuid);
    }

    public void showQueuedConnections() {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Working through the queue");
    	
    	Iterator<Long> it = mConnectionQueue.iterator();
    	while (it.hasNext()) {
    		long uuid = (Long) it.next();
    		showConnection(uuid);
    		it.remove();
    	}
    }
    
    /*
    public void bringToFront(long uuid) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Looking for fragment with tag: " + Long.toString(uuid));
    	FragmentManager fm = getFragmentManager();
    	ConnectedDirectoryFragment fragment = (ConnectedDirectoryFragment)fm.findFragmentByTag(Long.toString(uuid));
    	if (fragment == null) {
    		if (MainActivity.isDebuggable) Log.i(TAG, "Fragment is null");
    	} else {
    		if (MainActivity.isDebuggable) Log.i(TAG, "Fragment is found");
    	}
    	
    }
    */

    public void loadDirectoryContent(long uuid, String itemName) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Loading Directory content");
    	Bundle bundle = new Bundle();
    	bundle.putLong("uuid", uuid);
    	bundle.putString("command", "ls");
    	bundle.putString("pathChange", itemName);
        Message msg = Message.obtain(null, ConnectionService.MSG_COMMAND_DISPATCH);
        msg.setData(bundle);
        msg.replyTo = mMessenger;
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    public void openSelectedItem(long uuid, String itemName) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Loading Directory content");
    	Bundle bundle = new Bundle();
    	bundle.putLong("uuid", uuid);
    	bundle.putString("command", "vi");
    	bundle.putString("pathChange", itemName);
        Message msg = Message.obtain(null, ConnectionService.MSG_COMMAND_DISPATCH);
        msg.setData(bundle);
        msg.replyTo = mMessenger;
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}    	
    }
 
    public void handleResponse(long uuid, ArrayList<String> files, ArrayList<String> directories) {
    	sendResponseToDirectoryFragment(uuid, files, directories);
    }
    
    public void handleResponse(long uuid, String command, String response) {
   		// display file fragment
    	if (MainActivity.isDebuggable) Log.i(TAG, "Displaying file: " + response);
    	if (MainActivity.isDebuggable) Log.i(TAG, "Looking for fragment with tag: " + Long.toString(uuid));
    	// TODO: Actually display file contents on file fragment
   		//sendResponseToFileFragment(uuid, response);
    }

    public void sendResponseToDirectoryFragment(long uuid, ArrayList<String> files, ArrayList<String> directories) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Looking for fragment with tag: " + Long.toString(uuid));
    	FragmentManager fm = getFragmentManager();
    	ConnectedDirectoryFragment fragment = (ConnectedDirectoryFragment)fm.findFragmentByTag(Long.toString(uuid));
    	if (fragment == null) {
    		if (MainActivity.isDebuggable) Log.i(TAG, "Fragment is null");
    		// create fragment with response
    		Bundle bundle = new Bundle();
    		bundle.putStringArrayList("files", files);
    		bundle.putStringArrayList("directories", directories);
    		ConnectedDirectoryFragment newDirectory = new ConnectedDirectoryFragment();
    		newDirectory.setArguments(bundle);
    		FragmentTransaction transaction = fm.beginTransaction();
    		transaction.replace(android.R.id.content, newDirectory, Long.toString(uuid));
    		transaction.addToBackStack(null);
    		transaction.commit();
    	} else {
    		fragment.processResponse(files, directories);
    	}
    }

    
    
    
    
    
    
    
    
    
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectionService.MSG_COMMAND_RETURN:
                	Bundle cmdBundle = msg.getData();
                	String response = cmdBundle.getString("response");
                	if (response == null) {
                		ArrayList<String> files = cmdBundle.getStringArrayList("files");
                		ArrayList<String> directories = cmdBundle.getStringArrayList("directories");
                		handleResponse(cmdBundle.getLong("uuid"), files, directories);
                	} else {
                		handleResponse(cmdBundle.getLong("uuid"), cmdBundle.getString("command"), response);
                	}
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	if (MainActivity.isDebuggable) Log.i(TAG, "Bound to service - confirmed");
        	
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

           	showQueuedConnections();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            // As part of the sample, tell the user what happened.
            if (MainActivity.isDebuggable) Log.e(TAG, "Disconnected from service unintentionally - confirmed");
        }
    };
    
}
