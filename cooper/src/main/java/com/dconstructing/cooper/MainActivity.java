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
package com.dconstructing.cooper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

import com.dconstructing.cooper.fragments.ConnectionsFragment;
import com.dconstructing.cooper.fragments.ConnectionsFragment.OnAddConnectionOptionListener;
import com.dconstructing.cooper.fragments.NewConnectionFragment;
import com.dconstructing.cooper.fragments.PasswordDialogFragment;
import com.dconstructing.cooper.fragments.PasswordDialogFragment.PasswordDialogListener;
import com.dconstructing.cooper.objects.Address;
import com.dconstructing.cooper.objects.Connection;
import com.dconstructing.cooper.services.ConnectionService;


public class MainActivity extends Activity implements OnAddConnectionOptionListener, PasswordDialogListener {
	
	public final String TAG = ((Object) this).getClass().getSimpleName();
    public static boolean isDebuggable = false;
    
    public final static String EXTRA_MESSAGE = "com.dconstructing.cooper.MESSAGE";
    
    Messenger mService = null;
    ArrayList<Address> mConnectionQueue = new ArrayList<Address>();
    ArrayList<Long> mDisconnectionQueue = new ArrayList<Long>();
    public Map<Long, Integer> mConnectionsStatus = new HashMap<Long, Integer>();
    
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));

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
	public void connectToServer(long uuid, String host, String username, boolean recycle) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Connecting to host " + host + " with username " + username);
		
		if (mService == null) {
			this.queueConnection(uuid, host, username);
			
			if (MainActivity.isDebuggable) Log.i(TAG, "Gotta start the Connection Service");
	        try {
	        	getApplicationContext().bindService(new Intent(this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
	        } catch (SecurityException e) {
	        	if (MainActivity.isDebuggable) Log.e(TAG, "Could not bind to service", e);
	        }
		} else if (recycle) {
			if (MainActivity.isDebuggable) Log.i(TAG, "Trying to recycle connection");
			checkForRunningConnection(uuid, host, username);
		} else {
			if (MainActivity.isDebuggable) Log.i(TAG, "Start a new connection, requiring password");
			promptForPassword(uuid, host, username);
		}
	}

    @Override
    public void disconnectFromServer(long uuid) {
        if (MainActivity.isDebuggable) Log.i(TAG, "Disconnecting to from server " + Long.toString(uuid));

        if (mService == null) {
            this.queueDisconnection(uuid);

            if (MainActivity.isDebuggable) Log.i(TAG, "Gotta start the Connection Service");
            try {
                getApplicationContext().bindService(new Intent(this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
            } catch (SecurityException e) {
                if (MainActivity.isDebuggable) Log.e(TAG, "Could not bind to service", e);
            }
        } else {
            if (MainActivity.isDebuggable) Log.i(TAG, "Trying to recycle connection");
            initiateDisconnection(uuid);
        }
    }

	@Override
	public void onPasswordEntered(long uuid, String host, String username, String password) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Got password");
		this.initiateConnection(uuid, host, username, password);
	}

	
	


	
	
	
	
	public void updateConnections(long uuid, int status) {
		mConnectionsStatus.put(uuid, status);
        if (MainActivity.isDebuggable) Log.i(TAG, "Map:" + mConnectionsStatus.toString());
		
		ConnectionsFragment fragment = (ConnectionsFragment) getFragmentManager().findFragmentByTag("connections");
        if (fragment != null) {
		    fragment.updateConnectionStatus(uuid, status);
        }
	}
	
    public void queueConnection(long uuid, String host, String username) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Queuing host " + host + " with username " + username);
    	Address connection = new Address();
    	connection.id = uuid;
    	connection.host = host;
    	connection.username = username;
    	mConnectionQueue.add(connection);
    }

    public void queueDisconnection(long uuid) {
        if (MainActivity.isDebuggable) Log.i(TAG, "Queuing host #" + Long.toString(uuid));
        mDisconnectionQueue.add(uuid);
    }

    public void initiateQueuedConnections() {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Working through the queue");
    	
    	Iterator<Address> it = mConnectionQueue.iterator();
    	while (it.hasNext()) {
    		Address connection = (Address) it.next();
    		connectToServer(connection.id, connection.host, connection.username, true);
    		it.remove();
    	}
    }

    public void initiateQueuedDisconnections() {
        if (MainActivity.isDebuggable) Log.i(TAG, "Working through the disconnect queue");

        Iterator<Long> it = mDisconnectionQueue.iterator();
        while (it.hasNext()) {
            Long uuid = (Long) it.next();
            disconnectFromServer(uuid);
            it.remove();
        }
    }

    public void checkForRunningConnection(long uuid, String host, String username) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Checking for connection");
        try {
        	Bundle bundle = new Bundle();
        	bundle.putLong("uuid",uuid);
        	bundle.putString("host", host);
        	bundle.putString("username", username);
            Message msg = Message.obtain(null, ConnectionService.MSG_CONNECTION_CHECK);
            if (msg != null) {
                msg.setData(bundle);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we can count on soon being
            // disconnected (and then reconnected if it can be restarted)
            // so there is no need to do anything here.
        }
    }
    
    public void handleConnectionCheck(long uuid, String host, String username, boolean hasConnection) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Connection check results");
    	if (hasConnection) {
    		if (MainActivity.isDebuggable) Log.i(TAG, "Already has connection");
    		// open activity without starting new connection
        	Intent intent = new Intent(this, ConnectionActivity.class);
        	intent.putExtra("uuid", uuid);
        	startActivity(intent);
    	} else {
    		if (MainActivity.isDebuggable) Log.i(TAG, "Must start new connection");
    		// start a new connection
    		connectToServer(uuid, host, username, false);
    	}
    }
    
    public void promptForPassword(long uuid, String host, String username) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Show password prompt");
    	FragmentManager fm = getFragmentManager();
    	PasswordDialogFragment passwordDialog = PasswordDialogFragment.create(uuid, host, username);
    	passwordDialog.show(fm, "fragment_password");
    }
    
    public void initiateConnection(long uuid, String host, String username, String password) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Attempting the connection with " + host + " and " + username);
    	
    	updateConnections(uuid, Connection.CONNECTING);
    	
        try {
        	Bundle bundle = new Bundle();
        	bundle.putLong("uuid",uuid);
        	bundle.putString("host",host);
        	bundle.putInt("port",22);
        	bundle.putString("username",username);
        	bundle.putString("password",password);
            Message msg = Message.obtain(null, ConnectionService.MSG_CONNECTION_INITIATE);
            if (msg != null) {
                msg.setData(bundle);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we can count on soon being
            // disconnected (and then reconnected if it can be restarted)
            // so there is no need to do anything here.
        }
    }

    public void initiateDisconnection(long uuid) {
        if (MainActivity.isDebuggable) Log.i(TAG, "Attempting to diconnect from " + Long.toString(uuid));

        updateConnections(uuid, Connection.DISCONNECTING);

        try {
            Bundle bundle = new Bundle();
            bundle.putLong("uuid", uuid);
            Message msg = Message.obtain(null, ConnectionService.MSG_CONNECTION_TERMINATE);
            if (msg != null) {
                msg.setData(bundle);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we can count on soon being
            // disconnected (and then reconnected if it can be restarted)
            // so there is no need to do anything here.
        }
    }

    public void connectionEstablished(long uuid) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Creating a new fragment for this connection with tag (" + Long.toString(uuid) + ")");
    	
    	updateConnections(uuid, Connection.CONNECTED);
    	
    	// Open the Connection Activity
    	Intent intent = new Intent(this, ConnectionActivity.class);
    	intent.putExtra("uuid", uuid);
    	startActivity(intent);
    }

    public void connectionDestroyed(long uuid) {
        if (MainActivity.isDebuggable) Log.i(TAG, "Destroying a fragment for this connection with tag (" + Long.toString(uuid) + ")");

        updateConnections(uuid, Connection.NOT_CONNECTED);

        // TODO: Make sure the fragment for this connection is cleared away.
        // Open the Connection Activity
//        Intent intent = new Intent(this, ConnectionActivity.class);
//        intent.putExtra("uuid", uuid);
//        startActivity(intent);
    }

    
    
    
    
    
    
    static class IncomingHandler extends Handler {
    	private final WeakReference<MainActivity> mActivity;
    	
    	IncomingHandler(MainActivity activity) {
    		mActivity = new WeakReference<MainActivity>(activity);
    	}

    	@Override
        public void handleMessage(Message msg) {
    		MainActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case ConnectionService.MSG_CONNECTION_ESTABLISHED:
                        // Do something become the connection was complete
                        if (MainActivity.isDebuggable) Log.i(activity.TAG, "Service connection to server - confirmed");
                        Bundle bundle = msg.getData();
                        if (bundle != null) {
                            activity.connectionEstablished(bundle.getLong("uuid"));
                        }
                        break;
                    case ConnectionService.MSG_CONNECTION_DESTROYED:
                        // Do something become the connection was cut off
                        if (MainActivity.isDebuggable) Log.i(activity.TAG, "Service connection to server - closed");
                        Bundle destroyedBundle = msg.getData();
                        if (destroyedBundle != null) {
                            activity.connectionDestroyed(destroyedBundle.getLong("uuid"));
                        }
                        break;
                    case ConnectionService.MSG_CONNECTION_CHECKED:
                        Bundle checkedBundle = msg.getData();
                        if (checkedBundle != null) {
                            boolean hasConnection = checkedBundle.getBoolean("hasConnection");
                            long uuid = checkedBundle.getLong("uuid");
                            activity.handleConnectionCheck(uuid, checkedBundle.getString("host"), checkedBundle.getString("username"), hasConnection);
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
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
            initiateQueuedDisconnections();
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