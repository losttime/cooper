package com.dconstructing.cooper;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class MainActivity extends Activity {
	
	public final String TAG = getClass().getSimpleName();
    public static boolean isDebuggable = false;
    
    public final static String EXTRA_MESSAGE = "com.dconstructing.cooper.MESSAGE";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.isDebuggable = (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
        
        // TODO: Instead of executing the AsyncTask, bind to the service.
        new RemoteTestTask().execute();
    }

    
    
    
    private class RemoteTestTask extends AsyncTask<String, Void, Void> {
    	@Override
    	protected Void doInBackground(String... address) {
    		// do a thing
            Properties props = new Properties(); 
            props.put("StrictHostKeyChecking", "no");

            String host="192.168.0.1";
            String user ="username";
            String pwd = "password";
            int port = 22;

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("compression.s2c", "zlib,none");
            config.put("compression.c2s", "zlib,none");

            JSch jsch=new JSch();
            try {
            	Session session=jsch.getSession(user, host, port);
                session.setConfig(config);
                session.setPassword(pwd);
                session.connect();
                Log.i(TAG, "Connected");

                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                channel.setInputStream(null);
                channel.setOutputStream(baos);
                channel.setErrStream(System.err);
                
                channel.setCommand("ls");
                channel.connect();
                channel.disconnect();
                Log.i(TAG, baos.toString());
                session.connect();
            } catch (JSchException e) {
            	Log.e(TAG, "Exception");	
            }

            return null;
    	}

    	protected void onPostExecute() {
    		// do a thing
    		Toast.makeText(getApplicationContext(), "All done", Toast.LENGTH_SHORT).show();
    		Log.i(TAG, "All Done");
    	}
    }
}