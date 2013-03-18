package com.dconstructing.cooper.services;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.dconstructing.cooper.MainActivity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectionService extends Service {

	public final String TAG = getClass().getSimpleName();
	
	public static final int MSG_START_CONNECTION = 100;
	public static final int MSG_SEND_COMMAND = 200;
	
	protected Map<String,Session> mConnections = new HashMap<String,Session>();
	
	protected final Handler mHandler = new IncomingHandler(this);
	final Messenger mMessenger = new Messenger(mHandler);
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mMessenger.getBinder();
	}
	
	
	public void establishConnection(String uuid) {
		ConnectThread mConnectThread = new ConnectThread(uuid, "192.168.0.1", 22 , "username", "password");
	}

	
	
	
	
	
	
    public synchronized void connected(String uuid, Session session) {
    	mConnections.put(uuid, session);
    }
    
    public synchronized void connectionFailed(String uuid) {
    	if (MainActivity.isDebuggable) Log.e(TAG, "connection failed " + uuid);
    }

    
    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
    	private final WeakReference<ConnectionService> mService;
    	
    	IncomingHandler(ConnectionService service) {
    		mService = new WeakReference<ConnectionService>(service);
    	}
    	
        @Override
        public void handleMessage(Message msg) {
        	if (MainActivity.isDebuggable) Log.i(TAG, "Message received");
        	ConnectionService service = mService.get();
        	
            switch (msg.what) {
	            case MSG_START_CONNECTION:
	            	if (MainActivity.isDebuggable) Log.i(TAG, "Start Connection Message");
	            	service.establishConnection("UUID");
	                break;
	            case MSG_SEND_COMMAND:
	            	if (MainActivity.isDebuggable) Log.i(TAG, "Send Command Message");
	                break;
	            default:
	                super.handleMessage(msg);
            }
        }
    }
    
    
    private class ConnectThread extends Thread {
    	private final String tUuid;
    	private final String tHost;
    	private final int tPort;
    	private final String tUsername;
    	private final String tPassword;

        public ConnectThread(String uuid, String host, int port, String username, String password) {
        	tUuid = uuid;
            tHost = host;
            tPort = port;
            tUsername = username;
            tPassword = password;
        }

        public void run() {
            // Make a connection to remote server
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("compression.s2c", "zlib,none");
            config.put("compression.c2s", "zlib,none");

            JSch jsch=new JSch();
            try {
            	Session session=jsch.getSession(tUsername, tHost, tPort);
                session.setConfig(config);
                session.setPassword(tPassword);
                session.connect();
                Log.i(TAG, "Connected");
                connected(tUuid, session);
            } catch (JSchException e) {
            	connectionFailed(tUuid);
			}
        }
    }
    
}
