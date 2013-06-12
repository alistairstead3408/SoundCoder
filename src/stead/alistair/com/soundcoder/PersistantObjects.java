package stead.alistair.com.soundcoder;

import java.util.ArrayList;

public class PersistantObjects {
	
	private static ArrayList<ArrayList<GridObjectView>> mGrid = null;
	private static SystemState mState = null;
	
	public static void setGrid(ArrayList<ArrayList<GridObjectView>> grid){
		mGrid = grid; 
	}
	
	public static ArrayList<ArrayList<GridObjectView>> getGrid(){
		return mGrid;
	}

	public static void setSystemState(SystemState state){
		mState = state;
	}
	
	public static SystemState getSystemState(){
		return mState;
	}
	
	public static void removeGrid(){
		mGrid = null;
	}
	
	public static void deleteSystemState(){
		mState = null;
	}
}
