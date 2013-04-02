package com.dconstructing.cooper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.dconstructing.cooper.fragments.ConnectionsFragment;
import com.dconstructing.cooper.fragments.ConnectionsFragment.OnAddConnectionOptionListener;
import com.dconstructing.cooper.fragments.NewConnectionFragment;
import com.dconstructing.cooper.objects.Connection;
import com.dconstructing.cooper.services.ConnectionService;


public class MainActivity extends Activity implements OnAddConnectionOptionListener {
	
	public final String TAG = getClass().getSimpleName();
    public static boolean isDebuggable = false;
    
    public final static String EXTRA_MESSAGE = "com.dconstructing.cooper.MESSAGE";
    
    Messenger mService = null;
    ArrayList<Connection> mConnectionQueue = new ArrayList<Connection>();
    
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
        
        // TODO: Move the connection kickoff to a more appropriate place.
        /*
        try {
        	getApplicationContext().bindService(new Intent(this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
        } catch (SecurityException e) {
        	if (MainActivity.isDebuggable) Log.e(TAG, "Could not bind to service", e);
        }
        */
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
	public void connectToServer(String host, String username) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Connecting to host " + host + " with username " + username);
		
		if (mService == null) {
			this.queueConnection(host, username);
			
			if (MainActivity.isDebuggable) Log.i(TAG, "Gotta start the Connection Service");
	        try {
	        	getApplicationContext().bindService(new Intent(this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
	        } catch (SecurityException e) {
	        	if (MainActivity.isDebuggable) Log.e(TAG, "Could not bind to service", e);
	        }
		} else {
			this.initiateConnection(host, username);
		}
	}


	
	


	
	
	
	
	
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
    
    public void queueConnection(String host, String username) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Queuing host " + host + " with username " + username);
    	Connection connection = new Connection();
    	connection.host = host;
    	connection.username = username;
    	mConnectionQueue.add(connection);
    }
    
    public void initiateQueuedConnections() {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Working through the queue");
    	
    	Iterator<Connection> it = mConnectionQueue.iterator();
    	while (it.hasNext()) {
    		Connection connection = (Connection) it.next();
    		connectToServer(connection.host, connection.username);
    		it.remove();
    	}
    }
    
    public void initiateConnection(String host, String username) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Attempting the connection with " + host + " and " + username);
        try {
        	Bundle bundle = new Bundle();
        	bundle.putString("uuid","123456789");
        	bundle.putString("host",host);
        	bundle.putInt("port",22);
        	bundle.putString("username",username);
        	bundle.putString("password","abc123");
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

    
    
    
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectionService.MSG_CONNECTION_ESTABLISHED:
                    // Do something become the connection was complete
                	if (MainActivity.isDebuggable) Log.i(TAG, "Service connection to server - confirmed");
                    break;
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
            
            // We want to monitor the service for as long as we are
            // connected to it.
            /*
            try {
            	Bundle bundle = new Bundle();
            	bundle.putString("uuid","123456789");
            	bundle.putString("host","example.com");
            	bundle.putInt("port",22);
            	bundle.putString("username","developer");
            	bundle.putString("password","abc123");
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
            */
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