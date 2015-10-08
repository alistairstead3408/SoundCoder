package stead.alistair.com.soundcoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import stead.alistair.com.osc.OSCHandler;
import stead.alistair.com.tiles.Tile;
import stead.alistair.com.tiles.TileAttack;
import stead.alistair.com.tiles.TileOneBlob;
import stead.alistair.com.tiles.TileProperty;
import stead.alistair.com.tiles.TileReference;
import stead.alistair.com.tiles.TileSynth;
import stead.alistair.com.tiles.TileTwoBlob;
import android.util.Log;

public class SystemState extends Thread{
	
	HashMap<Integer, ArrayList<Tile>> mRules;
	private final static String TAG = "SystemState";
	
	//data structure for system state, just guards, still in gridobjectview
	ArrayList<StateItem> mStateItemList;
	
	/** Allows us to send OSC messages over wirless/wired on UDP */
	OSCHandler oscHandler;
	
	/** Storing stateChanges to be processed*/
	ArrayList<Object> stateChangeStack;
	
	public boolean running = true;
	public boolean started = false;
	
	
	public SystemState(HashMap<Integer, ArrayList<Tile>> sentences, float [] blobData){
		stateChangeStack = new ArrayList<Object>();
		//Initialise stateItems from info when training
		mStateItemList = new ArrayList<StateItem>();
		StateItem tempStateItem;
		if(blobData.length %3 == 0){
			for(int i = 0; i < blobData.length; i++){
				if( i % 3 == 0){
					tempStateItem = new StateItem(i/3);
					mStateItemList.add(tempStateItem);
					Log.e(TAG, tempStateItem.toString());
				}
			}
		}
		Log.e(TAG, "mStateItemListSize: " + mStateItemList.size());
		
		mRules = sentences; 
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(){
		while(running){
			
			if(stateChangeStack.size() > 0){
				processStateChange((ArrayList<ArrayList<Long>>) stateChangeStack.get(0));
				stateChangeStack.remove(0);
				inferMessages(mStateItemList, mRules);
			}
			try {
				Thread.sleep(200);
				Log.e(TAG, "Sleep");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	




	public boolean initialiseConnection(){
		if(oscHandler != null)
			oscHandler.running = true;
		else{
			oscHandler = new OSCHandler();
			oscHandler.start();
		}
		if(oscHandler.obtainedPort)
			return true;
		else
			return false;
	}
	
	
	
	
	
	public void printSystemState(){
		for(int i = 0; i < mStateItemList.size(); i++){
			Log.e(TAG, mStateItemList.get(i).toString());
		}
	}
	
	
	
	public synchronized void postStateChange(ArrayList<ArrayList<Long>> incomingStateChanges){
		stateChangeStack.add(incomingStateChanges);
	}
	
	public synchronized void processStateChange(ArrayList<ArrayList<Long>> incomingStateChanges){
		for(int i = 0; i < mStateItemList.size(); i++){
			mStateItemList.get(i).resetNonAreaVals();
		}
		
		// <Blob ID> <Enter(1)/Exit(2)/See(3)> <Area> <others?..> <Serparator>
		for(int i = 0; i < incomingStateChanges.size(); i++){
			int id = incomingStateChanges.get(i).get(0).intValue();
			int action = incomingStateChanges.get(i).get(1).intValue(); 
			float area = 0;
			if(incomingStateChanges.get(i).size() >= 3) 
				area = incomingStateChanges.get(i).get(2); //area
			
			switch(action){
			case 1:
				mStateItemList.get(id).setEnter(true);
			case 2:
				mStateItemList.get(id).setExit(true);
			case 3:
				mStateItemList.get(id).setSee(true);
			}
			
			//increment count
			mStateItemList.get(id).setCount(mStateItemList.get(id).getCount() + 1);
			
			if(area != 0)
				mStateItemList.get(id).setArea(mStateItemList.get(id).getArea() + area);
			Log.e(TAG, mStateItemList.get(id).toString());
		}
		
		
	}
	
	private synchronized void inferMessages(ArrayList<StateItem> stateItemList,  HashMap<Integer, ArrayList<Tile>> rules) {
		Long startTime = System.currentTimeMillis();
		Iterator<Integer> it = mRules.keySet().iterator();
		while(it.hasNext()){
			Object nextKey = it.next();
			ArrayList<Tile> sentence = (ArrayList<Tile>) mRules.get(nextKey);
			
			boolean triggeredSentence = true;
			int identifier = -1;
			float time = 0.0f;
			String valueStr = "";
			int valueInt = 0;
			Tile currentObject;
			//Go through each guard in the sentence and check it is valid
			for(int i = 0; i < sentence.size(); i++){
				currentObject = sentence.get(i);
				//Check the guards
				if(TileReference.getCatagory(currentObject.getIconID()) == TileReference.CATAGORY_GUARD){
					switch(sentence.get(i).getIconID()){
					case R.drawable.tile_blob_entrance:
						if(!stateItemList.get(((TileOneBlob)currentObject).getColourID()).getEnter())
							triggeredSentence = false;
						break;
					case R.drawable.tile_blob_see:
						if(!stateItemList.get(((TileOneBlob)currentObject).getColourID()).getSee())
							triggeredSentence = false;
						break;
						
					case R.drawable.tile_blob_exit:
						if(!stateItemList.get(((TileOneBlob)currentObject).getColourID()).getExit())
							triggeredSentence = false;
						break;
					case R.drawable.tile_blob_containment:
						//if(!stateItemList.get(currentObject.getColourID()).getEnter())
							triggeredSentence = false;
						break;
						
					case R.drawable.tile_blob_next:
						TileTwoBlob twoBlobTile = (TileTwoBlob) currentObject;
						if(!stateItemList.get(twoBlobTile.getColourID()).getSee() || !stateItemList.get(twoBlobTile.getColourID2()).getSee()){
							if(twoBlobTile.getColourID() == (twoBlobTile.getColourID2()))
							{
								if(stateItemList.get(((TileTwoBlob)currentObject).getColourID()).getCount() <= 1)
									triggeredSentence = false;
							}
							else
								triggeredSentence = false;
						}
						break;
					case R.drawable.tile_blob_true:
						break;						
					case R.drawable.tile_blob_nextto:
						//if(!stateItemList.get(currentObject.getColourID()).getEnter())
							triggeredSentence = false;
						break;
					}
				}
				
				//Get the identifier
				if(TileReference.getCatagory(currentObject.getIconID()) == TileReference.CATAGORY_IDENTIFIER){
					identifier = ((TileSynth)currentObject).getSynthNo();
				}
				
				//Get the value
				if(TileReference.getCatagory(currentObject.getIconID()) == TileReference.CATAGORY_MODIFIER){
					switch(currentObject.getIconID()){
					case R.drawable.tile_modifier_value:
						valueInt = (int) (((TileProperty)currentObject).getValue() * 100);
						valueStr = MidiReference.getNote((int)(valueInt));
						time = 0.0f;
						break;
					case R.drawable.tile_modifier_attack:
						valueInt = (int) (((TileProperty)currentObject).getValue() * 100);
						valueStr = MidiReference.getNote((int)(valueInt));
						time = ((TileAttack)currentObject).getTime();
						break;
					case R.drawable.tile_modifier_area:
						valueInt = (int) (stateItemList.get(((TileOneBlob)currentObject).getColourID()).getAreaPercentage() * 100);
						Log.e(TAG, "AreaVal: " + valueInt);
						valueStr = MidiReference.getNote((int)(valueInt));
						time = 0.0f;
						break;
					}
				}
				
			}//for
			
			if(triggeredSentence){
				//find the properties and send the OSCMessages
				for(int i = 0; i < sentence.size(); i++){
					//Check the guards
					if(TileReference.getCatagory(sentence.get(i).getIconID()) == TileReference.CATAGORY_PROPERTY){
						switch(sentence.get(i).getIconID()){
						case R.drawable.tile_property_frequency:
							oscHandler.addFrequencyEnvelopeMessage(identifier, time,  valueStr);
							break;
						case R.drawable.tile_property_volume:
							oscHandler.addVolumeEnvelopeMessage(identifier, time, valueInt);
							break;
						}
					}
					
					
				}
			}
			

		}//while
		
		
		Log.e(TAG, "SystemState Infer takes: " + (System.currentTimeMillis() - startTime)  );
	
		
		
	}
	


}
