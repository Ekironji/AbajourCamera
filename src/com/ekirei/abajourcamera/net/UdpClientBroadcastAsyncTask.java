package com.ekirei.abajourcamera.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

public class UdpClientBroadcastAsyncTask extends AsyncTask<Void, Void, String> {

	private final String TAG = "UdpClientBroadcastAsyncTask";
	
	Context appContext;
	//UDP variables
	int port = 9002;
	byte[] outgoingBytes = new byte[4];
	DatagramSocket datagramSocket;
	InetAddress serverAddr;
	private static final int TIMEOUT_MS = 4000;
	
	ProgressDialog dialog;
	String progressDialogMessage = "Wait until server is found...";
	
	private boolean running = true;
	
	IPAddressServerListener ipAddressServerListener;
	
	/*
	 * UdpClientBroadcastAsyncTask costructor
	 * params
	 * 		Context context: the context of the Activity 
	 */
	public UdpClientBroadcastAsyncTask(Context context) {
		this.appContext = context;
		dialog = new ProgressDialog(appContext);
		try {			
			serverAddr = getBroadcastAddress(appContext);
			datagramSocket = new DatagramSocket();
			datagramSocket.setBroadcast(true);
			datagramSocket.setSoTimeout(TIMEOUT_MS);
//			TODO: prende messaggio già formattato da inviare in broadcast da libreria
			outgoingBytes = "ciao".getBytes();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void onPreExecute() {		
		showProgressDialog();
	}
	
    protected String doInBackground(Void... params) {
    		
		running = true;
		int count = 0;
		try {
			while (running && count < 4) {
				Log.i(TAG, "datagramsocket: " + datagramSocket.toString());
				datagramSocket.setSoTimeout(TIMEOUT_MS);
				DatagramPacket datagramSendPacket = new DatagramPacket(outgoingBytes, outgoingBytes.length, serverAddr, port);
				datagramSocket.send(datagramSendPacket);
				Thread.sleep(30);
				datagramSocket.send(datagramSendPacket);
				Thread.sleep(30);
				datagramSocket.send(datagramSendPacket);
				Thread.sleep(30);
				
				try {
					while (running) {
						byte[] buf = new byte[15];
						DatagramPacket datagramReceivePacket = new DatagramPacket(buf, buf.length);
						datagramSocket.receive(datagramReceivePacket);
						String s = new String(datagramReceivePacket.getData(), 0, datagramReceivePacket.getLength());
						Log.i(TAG, "Received response " + s);
						if (validateIPstring(s)){
							running = false;
							return s;
						}
					}
				} catch (SocketTimeoutException e) {
					Log.i(TAG, "Receive timed out");
				}	
				count ++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (datagramSocket != null) {
                datagramSocket.close();
        	}
		}			
		Log.i(TAG, "stop Thread");
		datagramSocket.close();
		
        return null; 
    }

    protected void onPostExecute(String address) {
    	hideProgressDialog();
    	if (datagramSocket != null) {
            datagramSocket.close();
    	}
    	
    	if (address != null) { 		
    		ipAddressServerListener.IPAddressServerFounded(address);
    	} else {
    		ipAddressServerListener.IPAddressServerFailed();
    	}
    }
    
    
	private InetAddress getBroadcastAddress(Context context) throws IOException {			
	    WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);	    
	    DhcpInfo dhcp = wifi.getDhcpInfo();
	    // handle null somehow

	    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    return InetAddress.getByAddress(quads);	
	}
	
	private boolean validateIPstring(final String ip){          

	      Pattern pattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
								  	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
									        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
									        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	      Matcher matcher = pattern.matcher(ip);
	      return matcher.matches();             
	}
	
	/*
	 * setProgressDialogMessage
	 * params:
	 * 	String message: the message of the progress dialog
	 */
	public void setProgressDialogMessage(String message){
		this.progressDialogMessage = message;
	}
	
	public void showProgressDialog(){
		dialog.setMessage(progressDialogMessage);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();		
	}
	
	public void hideProgressDialog(){
		dialog.dismiss();		
	}
	
	public void setIPAddressServerListener(IPAddressServerListener ipAddressServerListener) {
        this.ipAddressServerListener = ipAddressServerListener;
    }
	
	public static interface IPAddressServerListener {
        void IPAddressServerFounded(String address);
        void IPAddressServerFailed();
    }
}
