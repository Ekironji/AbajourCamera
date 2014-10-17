package com.ekirei.abajourcamera.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

public class UDPSendCommandThread extends Thread {
	
	private final String TAG = "UDPSendCommandThread";
	
	//UDP variables
	int port = 9002;
	byte[] outgoingBytes = new byte[4];
	DatagramSocket datagramSocket;
	DatagramPacket datagramPacket;
	InetAddress udooAddr;
	
	private boolean running = true;
	
	private int red 	= 0;
	private int green 	= 0;
	private int blue 	= 0;

	public UDPSendCommandThread(String ipIn)
	{
	    try {
	    	 udooAddr = InetAddress.getByName(ipIn);
	    } catch (UnknownHostException e) {
	    	 e.printStackTrace();
	    }

		try {
			datagramSocket = new DatagramSocket();
		} catch (SocketException e) {

			e.printStackTrace();
		}

		Log.i("UDP Constructor: ", "UDP thread got this IP: " + ipIn);
	}


	public void startRunning(){
	     running = true;
	}
	
	public void stopRunning(){
		running = false;
	}

	@Override
	public void run() {
		Log.i(TAG, "start Thread");
		try {	
			while (running) {	
				
//				Log.i(TAG, "outgoingBytes: " + red + " " + green + " " + blue);
				outgoingBytes[0] = (byte) 0x20;
				outgoingBytes[3] = (byte) red;
				outgoingBytes[2] = (byte) green;	
				outgoingBytes[1] = (byte) blue;
				
//				Log.i(TAG, "outgoingBytes: " + outgoingBytes[0] + " " + outgoingBytes[1] + " " + outgoingBytes[2]);
	
				datagramPacket = new DatagramPacket(outgoingBytes, outgoingBytes.length, udooAddr, port);
	
				datagramSocket.send(datagramPacket);
	
				Thread.sleep(15);
	
			}
			datagramSocket.close();
			Log.i(TAG, "stop Thread");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (datagramSocket != null) {
                datagramSocket.close();
        	}
		}
	}

	public void setRed(int red) {
		this.red = red;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}
}
