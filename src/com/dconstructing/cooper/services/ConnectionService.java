package com.dconstructing.cooper.services;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.dconstructing.cooper.MainActivity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectionService extends Service {

	public final String TAG = getClass().getSimpleName();
	
	public static final int MSG_CONNECTION_INITIATE = 101;
	public static final int MSG_CONNECTION_ESTABLISHED = 102;
	public static final int MSG_COMMAND_DISPATCH = 201;
	
	protected Map<String,Session> mConnections = new HashMap<String,Session>();
	
	protected final Handler mHandler = new IncomingHandler(this);
	final Messenger mMessenger = new Messenger(mHandler);
	
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	
	
	public void establishConnection(String uuid, String host, int port, String username, String password, Messenger reply) {
		ConnectThread thread = new ConnectThread(uuid, host, port, username, password, reply);
		thread.start();
	}

	
	
	
	
	
	
    public synchronized void connected(String uuid, Session session, Messenger reply) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Connection successful " + uuid);
    	mConnections.put(uuid, session);
    	try {
    		Message msg = Message.obtain(null, ConnectionService.MSG_CONNECTION_ESTABLISHED);
    		msg.replyTo = mMessenger;
    		reply.send(msg);
    	} catch (RemoteException e) {
    		
    	}
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
	            case MSG_CONNECTION_INITIATE:
	            	if (MainActivity.isDebuggable) Log.i(TAG, "Initiate Connection");
	            	Bundle bundle = msg.getData();
	            	service.establishConnection(bundle.getString("uuid"), bundle.getString("host"), bundle.getInt("port"), bundle.getString("username"), bundle.getString("password"), msg.replyTo);
	                break;
	            case MSG_COMMAND_DISPATCH:
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
    	private Messenger tReply;

        public ConnectThread(String uuid, String host, int port, String username, String password, Messenger reply) {
        	tUuid = uuid;
            tHost = host;
            tPort = port;
            tUsername = username;
            tPassword = password;
            tReply = reply;
        }

        public void run() {
        	if (MainActivity.isDebuggable) Log.i(TAG, "Initiating Connection to " + tHost);
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
                connected(tUuid, session, tReply);
            } catch (JSchException e) {
            	connectionFailed(tUuid);
			}
        }
    }
    
}
