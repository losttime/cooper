package com.dconstructing.cooper;

import android.app.Activity;
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

import com.dconstructing.cooper.services.ConnectionService;


public class MainActivity extends Activity {
	
	public final String TAG = getClass().getSimpleName();
    public static boolean isDebuggable = false;
    
    public final static String EXTRA_MESSAGE = "com.dconstructing.cooper.MESSAGE";
    
    Messenger mService = null;
    
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.isDebuggable = (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
        
        if (MainActivity.isDebuggable) {
        	Log.i(TAG, "Debuggable");
        } else {
        	Log.i(TAG, "Not Debuggable");
        }
        
        try {
        	getApplicationContext().bindService(new Intent(this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
        } catch (SecurityException e) {
        	Log.e(TAG, "Could not bind to service", e);
        }
    }
    
    
    public void sendCommand() {
        try {
            Message reply = Message.obtain(null, ConnectionService.MSG_COMMAND_DISPATCH, this.hashCode(), 0);
        	mService.send(reply);
        } catch (RemoteException e) {
        	
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

            // We want to monitor the service for as long as we are
            // connected to it.
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