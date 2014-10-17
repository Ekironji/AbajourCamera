package com.ekirei.abajourcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.ekirei.abajourcamera.net.UDPSendCommandThread;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;

public class MainActivity extends Activity{
	
//	private final static String TAG = "MainActivity";
	
	private final static String MY_PREFERENCES = "MyPref";
	private final static String IP_ADDRESS_KEY = "IpAddress";
	private final static String LASTCOLOR   = "lastColor";
	
	private static SharedPreferences prefs;
	
	private static UDPSendCommandThread sendCommandThread = null;
	
	private ColorPicker picker;
	
	private String ipAddress = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
				
		ipAddress = prefs.getString(IP_ADDRESS_KEY, null);					
		sendCommandThread = new UDPSendCommandThread(ipAddress);
		
		picker = (ColorPicker) findViewById(R.id.color_picker);
		
		int lastColor = prefs.getInt(LASTCOLOR, 0xffffffff);
		
		//To set the old selected color u can do it like this
		picker.setOldCenterColor(lastColor);
		picker.setColor(lastColor);
		sendCommandThread.setRed(Color.red(lastColor));
		sendCommandThread.setGreen(Color.green(lastColor));
		sendCommandThread.setBlue(Color.blue(lastColor));
		
		picker.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int eventaction = event.getAction();
				switch (eventaction) {
			        case MotionEvent.ACTION_DOWN:
			        	if (ipAddress != null) {
			        		if (sendCommandThread != null){
				    			sendCommandThread.stopRunning();
				    			try {
				    				sendCommandThread.join();
				    			} catch (InterruptedException e) {
				    				// TODO Auto-generated catch block
				    				e.printStackTrace();
				    			} 
				    		}
			    			sendCommandThread = new UDPSendCommandThread(ipAddress);
			    			sendCommandThread.start();
			    		} else {
			    			//toast o qualcosa che ti dice di settare ip
			    		}
			            break;
	
			        case MotionEvent.ACTION_MOVE:
			            break;
	
			        case MotionEvent.ACTION_UP:   
			    		if (sendCommandThread != null){
			    			sendCommandThread.stopRunning();
			    			try {
			    				sendCommandThread.join();
			    			} catch (InterruptedException e) {
			    				// TODO Auto-generated catch block
			    				e.printStackTrace();
			    			} 
			    		}
			    		picker.setOldCenterColor(picker.getColor());
			            break;
				}
				return false;
			}
		});
		// Get notified when the user changes the color
		picker.setOnColorChangedListener(new OnColorChangedListener() {
			
			@Override
			public void onColorChanged(int color) {		
								
				sendCommandThread.setRed(Color.red(color));
				sendCommandThread.setGreen(Color.green(color));
				sendCommandThread.setBlue(Color.blue(color));
				
//				picker.setOldCenterColor(picker.getColor());
			}
		});
	
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (sendCommandThread == null){
			ipAddress = prefs.getString(IP_ADDRESS_KEY, null);					
			sendCommandThread = new UDPSendCommandThread(ipAddress);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();	
		if (prefs != null && picker != null) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(LASTCOLOR, picker.getColor());
            editor.commit();
		}
		if (sendCommandThread != null){
			sendCommandThread.stopRunning();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (sendCommandThread != null){
			sendCommandThread.stopRunning();
			try {
				sendCommandThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			sendCommandThread = null;
		}
	}


}
