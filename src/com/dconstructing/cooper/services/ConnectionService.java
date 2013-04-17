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
import com.dconstructing.cooper.objects.Connection;
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
	
	protected Map<Long,Connection> mConnections = new HashMap<Long,Connection>();
	
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
	
	public void sendCommand(Long uuid, String command, String pathChange, Messenger reply) {
		Connection connection = mConnections.get(uuid);
		if (pathChange != null) {
			connection.updatePath(pathChange);
		}
		CommandThread thread = new CommandThread(uuid, connection.session, command, connection.path, reply);
		thread.start();
	}

    public void sendResponse(long uuid, String command, String response, Messenger reply) {
    	try {
    		Bundle bundle = new Bundle();
        	bundle.putLong("uuid", uuid);
        	bundle.putString("command", command);
        	bundle.putString("response", response);
    		Message msg = Message.obtain(null, ConnectionService.MSG_COMMAND_RETURN);
    		msg.setData(bundle);
    		msg.replyTo = mMessenger;
    		reply.send(msg);
    	} catch (RemoteException e) {
    		
    	}
    }

    
    
    
	
	
	
	
	
    public synchronized void connected(Long uuid, Session session, Messenger reply) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Connection successful " + uuid);
    	Connection connection = new Connection();
    	connection.session = session;
    	mConnections.put(uuid, connection);
    	
    	try {
    		Bundle bundle = new Bundle();
        	bundle.putLong("uuid",uuid);
    		Message msg = Message.obtain(null, ConnectionService.MSG_CONNECTION_ESTABLISHED);
    		msg.setData(bundle);
    		msg.replyTo = mMessenger;
    		reply.send(msg);
    	} catch (RemoteException e) {
    		
    	}
    	
    	sendCommand(uuid, "pwd", null, reply);
    }
    
    public synchronized void connectionFailed(Long uuid) {
    	if (MainActivity.isDebuggable) Log.e(TAG, "connection failed " + Long.toString(uuid));
    }
    
    public synchronized void commandResponse(long uuid, String command, String path, String response, Messenger reply) {
    	if (command.indexOf("ls") == 0) {
    		if (response.equals(path)) {
    			// It's actually a file. Open it
    			sendCommand(uuid, "vi", path, reply);
    		} else {
    			// It's the contents of a directory. Send them to the user
    			sendResponse(uuid, command, response, reply);
    		}
    	} else if (command.indexOf("pwd") == 0) {
    		// Response is the connection path. Set it.
    		Connection connection = mConnections.get(uuid);
    		connection.path = response.replaceAll("\n", "");
    		mConnections.put(uuid, connection);
    		sendCommand(uuid, "ls", null, reply);
    	} else if (command.indexOf("vi") == 0) {
    		// Opening the file, just send the response to the user
    		sendResponse(uuid, command, response, reply);
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
	            	service.sendCommand(cmdBundle.getLong("uuid"), cmdBundle.getString("command"), cmdBundle.getString("pathChange"), msg.replyTo);
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
    	private final String tPath;
    	private Messenger tReply;

        public CommandThread(long uuid, Session session, String command, String path, Messenger reply) {
        	tUuid = uuid;
        	tSession = session;
            tCommand = command;
            tPath = path;
            tReply = reply;
        }

        public void run() {
        	if (MainActivity.isDebuggable) Log.i(TAG, "Sending command: " + tCommand + " " + tPath);
        	
        	try {
        		// An exec channel seems to give the response I want
        		ChannelExec channel = (ChannelExec)tSession.openChannel("exec");
            	channel.setCommand((tCommand + " " + tPath).getBytes());
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
				commandResponse(tUuid, tCommand, tPath, response, tReply);
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
