package stead.alistair.com.osc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import android.util.Log;


public class OSCHandler extends Thread {
	
	private static final String TAG = "OSCHandler";

	/** Allows us to turn off the threads actions*/
	public boolean running = true;
	
	/** The list we'll use to allow non-instant processing of OSC messages*/
	ArrayList<OSCMessage> OSCPool; 
	
	/** our port to send OSC messages*/
	OSCPortOut sender;
	
	public boolean obtainedPort;
	
	public boolean initialise(){
		
		OSCPool = new ArrayList<OSCMessage>();
		try {
			sender = new OSCPortOut(InetAddress.getByName("192.168.14.237")); 
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	
	public OSCHandler(){
		obtainedPort = initialise();
	}
	
	@Override
	public void run(){
		
		if(obtainedPort){
			while(running){
				if(OSCPool.size() > 0){
					try {
						sender.send(OSCPool.get(0));
						Log.e(TAG, "Message sent");
					} catch (IOException e) {
						Log.e(TAG, "Failed to send OSC Message!");
						e.printStackTrace();
					}
					OSCPool.remove(0);
				}
				//Log.e(TAG,"Running OSC Handler");
				try {Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}//while
		}
		
	}
	
	public void setRunning(boolean bool){
		running = bool;
	}
	
	public void addVolumeMessage(int synthID, int percentage){
		Object args[] = new Object[2];
		args[0] = new Integer(synthID);
		args[1] = new Float(0.0);
		args[1] = new Integer(percentage);
		OSCMessage msg = new OSCMessage("/set-volume", args);
		OSCPool.add(msg);
	}
	
	public void addVolumeEnvelopeMessage(int synthID, float timeLength, int percentage){
		Object args[] = new Object[3];
		args[0] = new Integer(synthID);
		args[1] = new Float(timeLength);
		args[2] = new Integer(percentage);
		OSCMessage msg = new OSCMessage("/set-volume", args);
		OSCPool.add(msg);
	}
	
	public void addFrequencyEnvelopeMessage(int synthID, float timeLength, String frequency){
		Object args[] = new Object[3];
		args[0] = new Integer(synthID);
		args[1] = new Float(timeLength);
		args[2] = new String(frequency);
		OSCMessage msg = new OSCMessage("/set-frequency", args);
		OSCPool.add(msg);
	}
	
	public void addFrequencyMessage(int synthID, String frequency){
		Object args[] = new Object[3];
		args[0] = new Integer(synthID);
		args[1] = new Float(0);
		args[2] = new String(frequency);
		OSCMessage msg = new OSCMessage("/set-frequency", args);
		OSCPool.add(msg);
	}
	
	public void sendStop(){
		OSCMessage msg = new OSCMessage("/start", null);
	}

	
	
	

	
	public boolean sendOSCMessage(){
		return false;
	}

}
