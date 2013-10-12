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
package com.dconstructing.cooper.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    public static final int MSG_CONNECTION_TERMINATE = 111;
    public static final int MSG_CONNECTION_DESTROYED = 112;
	public static final int MSG_COMMAND_DISPATCH = 201;
	public static final int MSG_COMMAND_RETURN = 202;
	public static final int MSG_FILE_SAVE = 301;
	public static final int MSG_FILE_SAVED = 302;
	
	public static final int CMD_WHERE_AM_I = 1001;
	public static final int CMD_CHANGE_LOCATION = 1001;
	public static final int CMD_DIR_READ = 2001;
	public static final int CMD_DIR_MAKE = 2003;
	public static final int CMD_DIR_DEL = 2004;
	public static final int CMD_FILE_READ = 3001;
	public static final int CMD_FILE_WRITE = 3002;
	public static final int CMD_FILE_MAKE = 3003;
	public static final int CMD_FILE_DELETE = 3004;
	
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

    public void destroyConnection(Long uuid, Messenger reply) {
        Connection connection = mConnections.get(uuid);
        Session session = connection.session;
        session.disconnect();

        mConnections.remove(uuid);

        try {
            Bundle bundle = new Bundle();
            bundle.putLong("uuid",uuid);
            Message msg = Message.obtain(null, ConnectionService.MSG_CONNECTION_DESTROYED);
            if (msg != null) {
                msg.setData(bundle);
                msg.replyTo = mMessenger;
                reply.send(msg);
            }
        } catch (RemoteException e) {
            if (MainActivity.isDebuggable) Log.e(TAG, "Error replying to disconnect request", e);
        }
    }

	public void sendCommand(Long uuid, int command, String parameter, String content, Messenger reply) {
		Connection connection = mConnections.get(uuid);
		CommandThread thread = new CommandThread(uuid, connection.session, command, parameter, content, reply);
		thread.start();
	}

    public void sendResponse(long uuid, int command, String parameter, Object response, Messenger reply) {
    	try {
    		Bundle bundle = new Bundle();
        	bundle.putLong("uuid", uuid);
        	bundle.putInt("command", command);
        	if (response instanceof String) {
        		bundle.putString("response", (String)response);
        		bundle.putString("parameter", parameter);
        	} else if (response instanceof HashMap) {
        		for (Map.Entry<String, ArrayList<String>> entry : ((HashMap<String, ArrayList<String>>) response).entrySet()) {
        			bundle.putStringArrayList(entry.getKey(), entry.getValue());
        		}
        	}
        	Message msg = Message.obtain(null, ConnectionService.MSG_COMMAND_RETURN);
            if (msg != null) {
                msg.setData(bundle);
                msg.replyTo = mMessenger;
                reply.send(msg);
            }
    	} catch (RemoteException e) {
            if (MainActivity.isDebuggable) Log.e(TAG, "Error replying to command request", e);
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
    
    public void saveFile(Long uuid, String parameter, String content, Messenger reply) {
		Connection connection = mConnections.get(uuid);
		CommandThread thread = new CommandThread(uuid, connection.session, CMD_FILE_WRITE, parameter, content, reply);
		thread.start();
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
            if (msg != null) {
                msg.setData(bundle);
                msg.replyTo = mMessenger;
                reply.send(msg);
            }
    	} catch (RemoteException e) {
            if (MainActivity.isDebuggable) Log.e(TAG, "Error replying to connect request", e);
    	}
    	
    	//sendCommand(uuid, "pwd", null, reply);
    }
    
    public synchronized void connectionFailed(Long uuid) {
    	if (MainActivity.isDebuggable) Log.e(TAG, "connection failed " + Long.toString(uuid));
    }
    
    public synchronized void commandResponse(long uuid, int command, String parameter, Object response, Messenger reply) {
    	if (MainActivity.isDebuggable) Log.i(TAG, "Got response from command");
    	if (command == CMD_WHERE_AM_I) {
    		// Response is the connection path. Set it.
    		Connection connection = mConnections.get(uuid);
    		connection.path = ((String)response).replaceAll("\n", "");
    		mConnections.put(uuid, connection);
    		sendCommand(uuid, CMD_DIR_READ, connection.path, null, reply);
    	} else if (command == CMD_CHANGE_LOCATION) {
			Connection connection = mConnections.get(uuid);
			connection.updatePath(parameter);
			sendCommand(uuid, CMD_DIR_READ, connection.path, null, reply);
    	} else if (command == CMD_DIR_READ) {
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

    			sendResponse(uuid, command, parameter, contents, reply);
    		}
    	} else if (command == CMD_FILE_READ) {
    		// Opening the file, just send the response to the user
    		sendResponse(uuid, command, parameter, (String)response, reply);
    	} else if (command == CMD_FILE_WRITE) {
    		sendResponse(uuid, command, parameter, (String)response, reply);
    	}
    }
    
    
    
    
    

    
    static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
    	private final WeakReference<ConnectionService> mService;
    	
    	IncomingHandler(ConnectionService service) {
    		mService = new WeakReference<ConnectionService>(service);
    	}
    	
        @Override
        public void handleMessage(Message msg) {
        	ConnectionService service = mService.get();
            if (service != null) {
                if (MainActivity.isDebuggable) Log.i(service.TAG, "Message received");

                switch (msg.what) {
                    case MSG_CONNECTION_INITIATE:
                        if (MainActivity.isDebuggable) Log.i(service.TAG, "Initiate Connection");
                        Bundle bundle = msg.getData();
                        if (bundle != null) {
                            service.establishConnection(bundle.getLong("uuid"), bundle.getString("host"), bundle.getInt("port"), bundle.getString("username"), bundle.getString("password"), msg.replyTo);
                        }
                        break;
                    case MSG_CONNECTION_TERMINATE:
                        if (MainActivity.isDebuggable) Log.i(service.TAG, "Terminate Connection");
                        Bundle terminateBundle = msg.getData();
                        if (terminateBundle != null) {
                            service.destroyConnection(terminateBundle.getLong("uuid"), msg.replyTo);
                        }
                        break;
                    case MSG_CONNECTION_CHECK:
                        if (MainActivity.isDebuggable) Log.i(service.TAG, "Checking for existing connection");
                        Bundle checkBundle = msg.getData();
                        try {
                            checkBundle.putBoolean("hasConnection", service.checkForConnection(checkBundle.getLong("uuid")));
                            Message outgoingMsg = Message.obtain(null, ConnectionService.MSG_CONNECTION_CHECKED);
                            if (outgoingMsg != null) {
                                outgoingMsg.setData(checkBundle);
                                outgoingMsg.replyTo = service.mMessenger;
                                msg.replyTo.send(outgoingMsg);
                            }
                        } catch (RemoteException e) {
                            if (MainActivity.isDebuggable) Log.e("ConnectionService Handler", "Error replying to connection check", e);
                        }
                        break;
                    case MSG_COMMAND_DISPATCH:
                        if (MainActivity.isDebuggable) Log.i(service.TAG, "Send Command Message");
                        Bundle cmdBundle = msg.getData();
                        if (cmdBundle != null) {
                            service.sendCommand(cmdBundle.getLong("uuid"), cmdBundle.getInt("command"), cmdBundle.getString("parameter"), cmdBundle.getString("content"), msg.replyTo);
                        }
                        break;
                    case MSG_FILE_SAVE:
                        if (MainActivity.isDebuggable) Log.i(service.TAG, "Saving file service");
                        Bundle saveBundle = msg.getData();
                        if (saveBundle != null) {
                            service.saveFile(saveBundle.getLong("uuid"), saveBundle.getString("parameter"), saveBundle.getString("content"), msg.replyTo);
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }
    
    
    private class CommandThread extends Thread {
    	
    	private final long tUuid;
    	private final Session tSession;
    	private int tCommand;
    	private final String tParameter;
    	private final String tContent;
    	private Messenger tReply;

        public CommandThread(long uuid, Session session, int command, String parameter, String content, Messenger reply) {
        	tUuid = uuid;
        	tSession = session;
            tCommand = command;
            tParameter = parameter;
            tContent = content;
            tReply = reply;
        }

        public void run() {
        	if (MainActivity.isDebuggable) Log.i(TAG, "Sending command: " + tCommand);
        	
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
					
					if (tParameter == null) {
						if (MainActivity.isDebuggable) Log.i(TAG, "Updating command to pwd");
						tCommand = CMD_WHERE_AM_I;
					}
					
					if (tCommand == CMD_WHERE_AM_I) {
						String pwd = channel.pwd();
						response = channel.pwd();
					} else if (tCommand == CMD_CHANGE_LOCATION) {
						channel.cd(tParameter);
					} else if (tCommand == CMD_DIR_READ) {
						Vector<ChannelSftp.LsEntry> list = channel.ls(tParameter);
						response = list;
					} else if (tCommand == CMD_FILE_READ) {
						InputStream inputStream = channel.get(tParameter);
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

						StringBuilder stringBuilder = new StringBuilder();
					    String line = null;

					    while ((line = bufferedReader.readLine()) != null) {
					        stringBuilder.append(line + "\n");
					    }

					    inputStream.close();
						response = stringBuilder.toString();
					} else if (tCommand == CMD_FILE_WRITE) {
						OutputStream outputStream = channel.put(tParameter);
						outputStream.write(tContent.getBytes());
						outputStream.close();
						response = tContent;
					}
					commandResponse(tUuid, tCommand, tParameter, response, tReply);
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
