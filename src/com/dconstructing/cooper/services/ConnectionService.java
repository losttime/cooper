package com.dconstructing.cooper.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

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
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ConnectionService extends Service {

	public final String TAG = getClass().getSimpleName();
	
	public static final int MSG_CONNECTION_INITIATE = 101;
	public static final int MSG_CONNECTION_ESTABLISHED = 102;
	public static final int MSG_CONNECTION_CHECK = 103;
	public static final int MSG_CONNECTION_CHECKED = 104;
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

    public void sendResponse(long uuid, String command, Object response, Messenger reply) {
    	try {
    		Bundle bundle = new Bundle();
        	bundle.putLong("uuid", uuid);
        	bundle.putString("command", command);
        	if (response instanceof String) {
        		bundle.putString("response", (String)response);
        	} else if (response instanceof HashMap) {
        		for (Map.Entry<String, ArrayList<String>> entry : ((HashMap<String, ArrayList<String>>) response).entrySet()) {
        			bundle.putStringArrayList(entry.getKey(), entry.getValue());
        		}
        	}
        	Message msg = Message.obtain(null, ConnectionService.MSG_COMMAND_RETURN);
    		msg.setData(bundle);
    		msg.replyTo = mMessenger;
    		reply.send(msg);
    	} catch (RemoteException e) {
    		
    	}
    }
    
    public boolean checkForConnection(long uuid) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Checking connection boolean");
    	boolean hasConnection = false;
    	Connection connection = mConnections.get(uuid);
    	if (connection != null) {
    		hasConnection = true;
    	}
    	return hasConnection;
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
    	
    	//sendCommand(uuid, "pwd", null, reply);
    }
    
    public synchronized void connectionFailed(Long uuid) {
    	if (MainActivity.isDebuggable) Log.e(TAG, "connection failed " + Long.toString(uuid));
    }
    
    public synchronized void commandResponse(long uuid, String command, String path, Object response, Messenger reply) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Got response from command");
    	if (command.indexOf("ls") == 0) {
    		if (response instanceof Vector) {
    			HashMap<String, ArrayList<String>> contents = new HashMap<String, ArrayList<String>>();
    			
    			ArrayList<String> files = new ArrayList<String>();
    			ArrayList<String> directories = new ArrayList<String>();
				for(ChannelSftp.LsEntry item : (Vector<ChannelSftp.LsEntry>)response) {
					//if (MainActivity.isDebuggable) Log.i(TAG, "Adding " + item.getFilename());
					if (item.getAttrs().isDir()) {
						directories.add(item.getFilename());
					} else {
						files.add(item.getFilename());
					}
				}
				
				contents.put("files", files);
				contents.put("directories", directories);

    			sendResponse(uuid, command, contents, reply);
    		}
    	} else if (command.indexOf("pwd") == 0) {
    		// Response is the connection path. Set it.
    		Connection connection = mConnections.get(uuid);
    		connection.path = ((String)response).replaceAll("\n", "");
    		mConnections.put(uuid, connection);
    		sendCommand(uuid, "ls", null, reply);
    	} else if (command.indexOf("vi") == 0) {
    		// Opening the file, just send the response to the user
    		sendResponse(uuid, command, (String)response, reply);
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
	            case MSG_CONNECTION_CHECK:
	            	if (MainActivity.isDebuggable) Log.i(TAG, "Checking for existing connection");
	            	Bundle checkBundle = msg.getData();
	            	try {
	            		checkBundle.putBoolean("hasConnection", service.checkForConnection(checkBundle.getLong("uuid")));
	                	Message outgoingMsg = Message.obtain(null, ConnectionService.MSG_CONNECTION_CHECKED);
	                	outgoingMsg.setData(checkBundle);
	                	outgoingMsg.replyTo = mMessenger;
	            		msg.replyTo.send(outgoingMsg);
	            	} catch (RemoteException e) {
	            		
	            	}
	            	break;
	            default:
	                super.handleMessage(msg);
            }
        }
    }
    
    
    private class CommandThread extends Thread {
    	
    	private final long tUuid;
    	private final Session tSession;
    	private String tCommand;
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
        		// An exec channel seems to give the response I want - scratch that
        		//ChannelExec channel = (ChannelExec)tSession.openChannel("exec");
            	//channel.setCommand((tCommand + " " + tPath).getBytes());
            	//Channel channel = tSession.openChannel("shell");
        		// An SFTP channel seems to give the response I want
            	ChannelSftp channel = (ChannelSftp)tSession.openChannel("sftp");
            	channel.connect();
				
				try {
					Object response = null;
					
					if (tPath == null) {
						if (MainActivity.isDebuggable) Log.i(TAG, "Updating command to pwd");
						tCommand = "pwd";
					}
					
					if (tCommand.equals("ls")) {
						Vector<ChannelSftp.LsEntry> list = channel.ls(tPath);
						response = list;
					} else if (tCommand.equals("pwd")) {
						String pwd = channel.pwd();
						response = pwd;
					} else if (tCommand.equals("vi")) {
						InputStream inputStream = channel.get(tPath);
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

						StringBuilder stringBuilder = new StringBuilder();
					    String line = null;

					    while ((line = bufferedReader.readLine()) != null) {
					        stringBuilder.append(line + "\n");
					    }

					    inputStream.close();
						response = stringBuilder.toString();
					}
					commandResponse(tUuid, tCommand, tPath, response, tReply);
				} catch (SftpException e) {
					e.printStackTrace();
				}
								
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
