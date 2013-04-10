package com.dconstructing.cooper.services;

import java.io.IOException;
import java.io.InputStream;
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
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectionService extends Service {

	public final String TAG = getClass().getSimpleName();
	
	public static final int MSG_CONNECTION_INITIATE = 101;
	public static final int MSG_CONNECTION_ESTABLISHED = 102;
	public static final int MSG_COMMAND_DISPATCH = 201;
	public static final int MSG_COMMAND_RETURN = 202;
	
	protected Map<Long,Session> mConnections = new HashMap<Long,Session>();
	
	protected final Handler mHandler = new IncomingHandler(this);
	final Messenger mMessenger = new Messenger(mHandler);
	
	@Override
	public IBinder onBind(Intent intent) {
		if (MainActivity.isDebuggable) Log.i(TAG, "Returning the binder");
		return mMessenger.getBinder();
	}
	
	
	public void establishConnection(Long uuid, String host, int port, String username, String password, Messenger reply) {
		ConnectThread thread = new ConnectThread(uuid, host, port, username, password, reply);
		thread.start();
	}
	
	public void sendCommand(Long uuid, String command, Messenger reply) {
		Session session = mConnections.get(uuid);
		CommandThread thread = new CommandThread(uuid, session, command, reply);
		thread.start();
	}

	
	
	
	
	
	
    public synchronized void connected(Long uuid, Session session, Messenger reply) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Connection successful " + uuid);
    	mConnections.put(uuid, session);
    	try {
    		Bundle bundle = new Bundle();
        	bundle.putLong("uuid",uuid);
    		Message msg = Message.obtain(null, ConnectionService.MSG_CONNECTION_ESTABLISHED);
    		msg.setData(bundle);
    		msg.replyTo = mMessenger;
    		reply.send(msg);
    	} catch (RemoteException e) {
    		
    	}
    }
    
    public synchronized void connectionFailed(Long uuid) {
    	if (MainActivity.isDebuggable) Log.e(TAG, "connection failed " + Long.toString(uuid));
    }
    
    public synchronized void commandResponse(long uuid, String response, Messenger reply) {
    	//if (MainActivity.isDebuggable) Log.i(TAG, "Command Response: " + response);
    	try {
    		Bundle bundle = new Bundle();
        	bundle.putLong("uuid", uuid);
        	bundle.putString("response", response);
    		Message msg = Message.obtain(null, ConnectionService.MSG_COMMAND_RETURN);
    		msg.setData(bundle);
    		msg.replyTo = mMessenger;
    		reply.send(msg);
    	} catch (RemoteException e) {
    		
    	}
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
	            	service.establishConnection(bundle.getLong("uuid"), bundle.getString("host"), bundle.getInt("port"), bundle.getString("username"), bundle.getString("password"), msg.replyTo);
	                break;
	            case MSG_COMMAND_DISPATCH:
	            	if (MainActivity.isDebuggable) Log.i(TAG, "Send Command Message");
	            	Bundle cmdBundle = msg.getData();
	            	service.sendCommand(cmdBundle.getLong("uuid"), cmdBundle.getString("command"), msg.replyTo);
	                break;
	            default:
	                super.handleMessage(msg);
            }
        }
    }
    
    
    private class CommandThread extends Thread {
    	
    	private final long tUuid;
    	private final Session tSession;
    	private final String tCommand;
    	private Messenger tReply;

        public CommandThread(long uuid, Session session, String command, Messenger reply) {
        	tUuid = uuid;
        	tSession = session;
            tCommand = command;
            tReply = reply;
        }

        public void run() {
        	if (MainActivity.isDebuggable) Log.i(TAG, "Sending command: " + tCommand);
        	
        	try {
        		// An exec channel seems to give the response I want
        		ChannelExec channel = (ChannelExec)tSession.openChannel("exec");
            	channel.setCommand(tCommand.getBytes());
            	//Channel channel = tSession.openChannel("shell");
            	
				InputStream inputStream = channel.getInputStream();
				//OutputStream outputStream = channel.getOutputStream();
				
				channel.connect();
				
				// Send the command to the remote server
				//outputStream.write((tCommand+"\n").getBytes());
				//outputStream.flush();
				
				// Read the response from the remote server
				byte[] tmp = new byte[1024];
				String response = "";
				while(true) {
					while(inputStream.available() > 0) {
						int i = inputStream.read(tmp, 0, 1024);
						if(i < 0) break;
						response += new String(tmp, 0, i);
					}
					if (MainActivity.isDebuggable) Log.i(TAG, "reply: " + response);
					if(channel.isClosed()) {
						if (MainActivity.isDebuggable) Log.i(TAG, "exit-status: " + channel.getExitStatus());
						break;
					}
					try{
						Thread.sleep(1000);
					}
					catch(Exception ee){}
				}
				commandResponse(tUuid, response, tReply);
				channel.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSchException e) {
				e.printStackTrace();
			}
        }
    }
    
    
    private class ConnectThread extends Thread {
    	private final Long tUuid;
    	private final String tHost;
    	private final int tPort;
    	private final String tUsername;
    	private final String tPassword;
    	private Messenger tReply;

        public ConnectThread(Long uuid, String host, int port, String username, String password, Messenger reply) {
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
