package com.dconstructing.cooper;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.dconstructing.cooper.fragments.ConnectedDirectoryFragment;
import com.dconstructing.cooper.fragments.ConnectedDirectoryFragment.DirectoryListener;
import com.dconstructing.cooper.fragments.ConnectedFragment.ConnectedFragmentListener;
import com.dconstructing.cooper.fragments.ConnectionsFragment;
import com.dconstructing.cooper.fragments.ConnectionsFragment.OnAddConnectionOptionListener;
import com.dconstructing.cooper.fragments.NewConnectionFragment;
import com.dconstructing.cooper.fragments.PasswordDialogFragment;
import com.dconstructing.cooper.fragments.PasswordDialogFragment.PasswordDialogListener;
import com.dconstructing.cooper.objects.Address;
import com.dconstructing.cooper.objects.FilePath;
import com.dconstructing.cooper.services.ConnectionService;


public class MainActivity extends Activity implements OnAddConnectionOptionListener, PasswordDialogListener, ConnectedFragmentListener, DirectoryListener {
	
	public final String TAG = getClass().getSimpleName();
    public static boolean isDebuggable = false;
    
    public final static String EXTRA_MESSAGE = "com.dconstructing.cooper.MESSAGE";
    
    Messenger mService = null;
    ArrayList<Address> mConnectionQueue = new ArrayList<Address>();
    
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.isDebuggable = (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
        
        if (MainActivity.isDebuggable) {
        	Log.i(TAG, "Debuggable");
        } else {
        	Log.i(TAG, "Not Debuggable");
        }
        
        if (savedInstanceState == null) {
            // During initial setup
        	
        	// plug in the connections fragment.
        	// TODO: Add a secondary fragment for large/wide screens for two-pane view
            ConnectionsFragment connections = new ConnectionsFragment();
            connections.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(android.R.id.content, connections, "connections").commit();
        }
        
    }
    
	@Override
	public void onAddConnectionSelected() {
		// TODO: Adjust the function for a two-paned view (instead of replacing the primary fragment, replace the secondary).
		NewConnectionFragment newConnection = new NewConnectionFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(android.R.id.content, newConnection, "addConnection");
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	@Override
	public void connectToServer(long id, String host, String username) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Connecting to host " + host + " with username " + username);
		
		if (mService == null) {
			this.queueConnection(id, host, username);
			
			if (MainActivity.isDebuggable) Log.i(TAG, "Gotta start the Connection Service");
	        try {
	        	getApplicationContext().bindService(new Intent(this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
	        } catch (SecurityException e) {
	        	if (MainActivity.isDebuggable) Log.e(TAG, "Could not bind to service", e);
	        }
		} else {
			promptForPassword(id, host, username);
		}
	}

	@Override
	public void onPasswordEntered(long id, String host, String username, String password) {
		this.initiateConnection(id, host, username, password);
	}

	@Override
	public void onDirectoryItemSelected(String tag, FilePath filePath) {
		if (filePath.isDirectory) {
			loadDirectoryContent(Long.parseLong(tag), filePath.name);			
		} else {
			openSelectedItem(Long.parseLong(tag), filePath.name);
		}
	}


	
	


	
	
	
	
	
	/*
	public void sendToList(Cursor cursor) {
		// Send cursor to Connections Fragment to populate list.
	}

    public void sendCommand() {
        try {
            Message reply = Message.obtain(null, ConnectionService.MSG_COMMAND_DISPATCH, this.hashCode(), 0);
        	mService.send(reply);
        } catch (RemoteException e) {
        	
        }
    }
    */
    
    public void queueConnection(long id, String host, String username) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Queuing host " + host + " with username " + username);
    	Address connection = new Address();
    	connection.id = id;
    	connection.host = host;
    	connection.username = username;
    	mConnectionQueue.add(connection);
    }
    
    public void initiateQueuedConnections() {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Working through the queue");
    	
    	Iterator<Address> it = mConnectionQueue.iterator();
    	while (it.hasNext()) {
    		Address connection = (Address) it.next();
    		connectToServer(connection.id, connection.host, connection.username);
    		it.remove();
    	}
    }
    
    public void promptForPassword(long id, String host, String username) {
    	FragmentManager fm = getFragmentManager();
    	PasswordDialogFragment passwordDialog = PasswordDialogFragment.create(id, host, username);
    	passwordDialog.show(fm, "fragment_password");
    }
    
    public void initiateConnection(long id, String host, String username, String password) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Attempting the connection with " + host + " and " + username);
        try {
        	Bundle bundle = new Bundle();
        	bundle.putLong("uuid",id);
        	bundle.putString("host",host);
        	bundle.putInt("port",22);
        	bundle.putString("username",username);
        	bundle.putString("password",password);
            Message msg = Message.obtain(null, ConnectionService.MSG_CONNECTION_INITIATE);
            msg.setData(bundle);
            msg.replyTo = mMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we can count on soon being
            // disconnected (and then reconnected if it can be restarted)
            // so there is no need to do anything here.
        }
    }
    
    public void connectionEstablished(long id) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Creating a new fragment for this connection with tag (" + Long.toString(id) + ")");
		//ConnectedDirectoryFragment newDirectory = new ConnectedDirectoryFragment();
		//FragmentTransaction transaction = getFragmentManager().beginTransaction();
		//transaction.replace(android.R.id.content, newDirectory, Long.toString(id));
		//transaction.addToBackStack(null);
		//transaction.commit();
    }

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
                case ConnectionService.MSG_CONNECTION_ESTABLISHED:
                    // Do something become the connection was complete
                	if (MainActivity.isDebuggable) Log.i(TAG, "Service connection to server - confirmed");
                	Bundle bundle = msg.getData();
                	connectionEstablished(bundle.getLong("uuid"));
                    break;
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

           	initiateQueuedConnections();
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