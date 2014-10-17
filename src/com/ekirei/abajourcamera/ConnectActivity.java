package com.ekirei.abajourcamera;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ekirei.abajourcamera.net.UdpClientBroadcastAsyncTask;
import com.ekirei.abajourcamera.net.UdpClientBroadcastAsyncTask.IPAddressServerListener;

public class ConnectActivity extends Activity {

	private Button connectButton;
	private EditText ip;
	
	private final static String MY_PREFERENCES = "MyPref";
	private final static String IP_ADDRESS_KEY = "IpAddress";   
	
	static SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_connect);
		connectButton = (Button) findViewById(R.id.connectButton);
		ip = (EditText) findViewById(R.id.IPEditText);
		
		prefs  = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
		
		String ipAddress = prefs.getString(IP_ADDRESS_KEY, null);
		if (ipAddress != null) {
			ip.setText(ipAddress);
		}
	
		connectButton.setOnClickListener(new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
				String ipString = ip.getText().toString();
				if (validateIPstring(ip.getText().toString())){
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(IP_ADDRESS_KEY, ipString);
	                editor.commit();
					
					Intent colorIntent = new Intent(ConnectActivity.this, MainActivity.class);
					startActivity(colorIntent);

					finish();
				} else {
					Toast.makeText(getApplicationContext(), "Invalid IP address!!",
							   Toast.LENGTH_SHORT).show();
				}
			}
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_discover:
            	UdpClientBroadcastAsyncTask task = new UdpClientBroadcastAsyncTask(this);
        		task.setIPAddressServerListener(new IPAddressServerListener() {
        			
        			@Override
        			public void IPAddressServerFounded(String address) {
        				Toast.makeText(getApplicationContext(), "Abajour found!! ip: " + address, Toast.LENGTH_SHORT).show();
        				
        				ip.setText(address);  
        				
        				SharedPreferences.Editor editor = prefs.edit();
    					editor.putString(IP_ADDRESS_KEY, address);
    	                editor.commit();    	                   	                      				
        			}
        			
        			@Override
        			public void IPAddressServerFailed() {
        				Toast.makeText(getApplicationContext(), "Abajour not found :-(", Toast.LENGTH_SHORT).show();
        			}
        		});
        		task.setProgressDialogMessage("Wait until abajour is found...");
        		task.execute();
                break;
        }
        return false;
    }
	
	public static boolean validateIPstring(final String ip){          

	      Pattern pattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
								  	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
									        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
									        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	      Matcher matcher = pattern.matcher(ip);
	      return matcher.matches();             
	}



}
