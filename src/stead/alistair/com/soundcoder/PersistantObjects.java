package stead.alistair.com.soundcoder;

import java.util.ArrayList;

import stead.alistair.com.tiles.Tile;

public class PersistantObjects {
	
	private static ArrayList<ArrayList<Tile>> mGrid = null;
	private static SystemState mState = null;
	
	public static void setGrid(ArrayList<ArrayList<Tile>> grid){
		mGrid = grid; 
	}
	
	public static ArrayList<ArrayList<Tile>> getGrid(){
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
