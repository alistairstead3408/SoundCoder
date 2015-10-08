package stead.alistair.com.soundcoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import stead.alistair.com.tiles.Tile;
import stead.alistair.com.tiles.TileReference;

import android.os.Handler;
import android.util.Log;

public class GridValidator {
	public static String TAG = "GridValidator";
	
	
	/** This method evalutes the soundness of the given rules so that they can be used later 
	 * @param grid - a matrix of tile objects
	 * */
	public static boolean validate(ArrayList<ArrayList<Tile>> grid, Handler mHandler){
		//Assign a number to each statement, regardless of synth no
		int [] [] catagoryMatrix = new int [grid.size()] [grid.get(0).size()]; // x, y
		
		int rows = grid.get(0).size();
		int cols = grid.size();
		
		/** Load all the types into an array */
		for(int i = 0; i <  rows; i++){
			for(int j = 0; j < cols; j++){
				catagoryMatrix[i][j] = TileReference.getCatagory(grid.get(j).get(i).getIconID());
			}
		}
		
		//Verification part - We need a location of the offending incorrect tiles=======================
		
		/* Verifies syntactically that everything is correct (i.e. no instances of diagonal touching) 
		 * |---|   |     |   |___| 
		 * |   |---|     |---|   | these are the two incorrect possibilities 
		 */
		int empty = -1;
		//Reset all the squares 
		for(int i = 0; i < rows-1; i++){
			for(int j = 0; j < cols-1; j++){
				grid.get(j).get(i).setCompletionHighlight(false);
				grid.get(j).get(i).setSemanticDebugHighlight(false);
			}
		}
		boolean syntaxError = false;
		//Because the loop is incrementing by 1, can't reset during loop, hence pre-reset.
		for(int i = 0; i < rows-1; i++){
			for(int j = 0; j < cols-1; j++){
				if(catagoryMatrix[i][j] != empty && catagoryMatrix[i+1][j] == empty && catagoryMatrix[i][j+1] == empty && catagoryMatrix[i+1][j+1] != empty){
					//case 1
					grid.get(j+1).get(i+2).setCompletionHighlight(true);
					grid.get(j+2).get(i+1).setCompletionHighlight(true);
					syntaxError = true;
				}
				if(catagoryMatrix[i][j] == empty && catagoryMatrix[i+1][j] != empty && catagoryMatrix[i][j+1] != empty && catagoryMatrix[i+1][j+1] == empty){
					//case 2
					grid.get(j+1).get(i+1).setCompletionHighlight(true);
					grid.get(j+2).get(i+2).setCompletionHighlight(true);
					syntaxError = true;
				}
			}
		}
		
		
		/** get a labelled version to filter the sentences */
		int [] [] labelMatrix = getLabelMatrix(rows, cols, catagoryMatrix);
		
		if(!(semanticValidation(grid, mHandler, rows, cols, labelMatrix, catagoryMatrix)))
				return false;
		
		if(syntaxError) return false;
		//if we've passed all the tests, return valid
		return true; 
	}
	
	/** get a labelled version to filter the sentences */
	private static int [][] getLabelMatrix(int rows, int cols, int [][] catagoryMatrix){
		int labelCounter = 0;
		int [] [] labelMatrix = new int [cols][rows];
		// initialise the matrix
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				labelMatrix[i][j] = 0;
			}
		}
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				if(labelMatrix[i][j] == 0 && catagoryMatrix[i][j] != -1){
					//always start doing down
					floodFill(i, j,rows, cols, catagoryMatrix, labelMatrix, 3, ++labelCounter);
				}
			}
		}
		//printMatrix(labelMatrix, rows, cols);
		return labelMatrix;
	}
	
	private static boolean semanticValidation(ArrayList<ArrayList<Tile>> grid, Handler mHandler, int rows, int cols, int [][] labelMatrix, int [][] catagoryMatrix){
		/* Does validation semantically - i.e. checks the number of types in each sentence */
		/* A neighbour is defined as being any tile connected with any other on the 4 sides */ 
		HashMap<Integer, ArrayList<Tile>> sentences = new HashMap<Integer, ArrayList<Tile>>();
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				// if we care about the value add it to the appropriate sentence arraylist
				if(labelMatrix[i][j] != 0){
					//if there's already an entry for this sentence, add to it
					if(sentences.containsKey(labelMatrix[i][j])){
						ArrayList<Tile> temp = (ArrayList<Tile>) sentences.get(labelMatrix[i][j]);
						Tile tempGridObject = grid.get(j).get(i);
						temp.add(tempGridObject);
						
					}
					else{
						Tile tempGridObject = grid.get(j).get(i);
						ArrayList<Tile> tempArrayList = new ArrayList<Tile>();
						tempArrayList.add(tempGridObject);
						sentences.put(labelMatrix[i][j], tempArrayList);
					}
						
				}
			}
		}
		//Now that we have a map of sentences, send each one to be validated
		Iterator it = sentences.keySet().iterator();
		while(it.hasNext()){
			Object nextKey = it.next();
			Object objSentence =  sentences.get(nextKey);
				ArrayList<Tile> sentence = (ArrayList<Tile>) objSentence;
				Log.e(TAG, "sentence size: " + sentence.size());
				printSentence(sentence);
				if(!validateSentence(sentence, mHandler)){
					//Highlight all instances of modifiers 
					Log.e(TAG, "Invalid sentence label:" + (Integer) nextKey);
					for(int i = 0; i < rows; i++){
						for(int j = 0; j < cols; j++){
							if(labelMatrix[i][j] == (Integer) nextKey && catagoryMatrix[i][j] == TileReference.CATAGORY_MODIFIER){
								grid.get(j+1).get(i+1).setSemanticDebugHighlight(true);
								//This needs to be changed to a red highlight
								Log.e(TAG, "setHighlight");
							}
						}
					}
					
					return false;
				}
		}
		
		return true;
		
	}
	
	/** This method runs on the assumption that this grid has already been validated */
	public static SystemState getSystemState(ArrayList<ArrayList<Tile>> grid, Handler mHandler, float [] blobData){

		int [] [] catagoryMatrix = new int [grid.size()] [grid.get(0).size()]; // x, y
		
		int rows = grid.get(0).size();
		int cols = grid.size();
		
		/** Load all the types into an array */
		for(int i = 0; i <  rows; i++){
			for(int j = 0; j < cols; j++){
				catagoryMatrix[i][j] = TileReference.getCatagory(grid.get(j).get(i).getIconID());
			}
		}
		
		int [][] labelMatrix = getLabelMatrix(rows, cols, catagoryMatrix);
		
		HashMap<Integer, ArrayList<Tile>> sentences = new HashMap<Integer, ArrayList<Tile>>();
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				// if we care about the value add it to the appropriate sentence arraylist
				if(labelMatrix[i][j] != 0){
					//if there's already an entry for this sentence, add to it
					if(sentences.containsKey(labelMatrix[i][j])){
						ArrayList<Tile> temp = (ArrayList<Tile>) sentences.get(labelMatrix[i][j]);
						Tile tempGridObject = grid.get(j).get(i);
						temp.add(tempGridObject);
						
					}
					else{
						Tile tempGridObject = grid.get(j).get(i);
						ArrayList<Tile> tempArrayList = new ArrayList<Tile>();
						tempArrayList.add(tempGridObject);
						sentences.put(labelMatrix[i][j], tempArrayList);
					}
						
				}
			}
		}
		
		return new SystemState(sentences, blobData);
			
	}
	
	/** Solves the labelling problem */
	private static void floodFill(int i, int j, int rows, int cols, int [][] catagoryMatrix, int [][] labelMatrix, int direction, int label){
	
		labelMatrix[i][j] = label;
		//up
		if(i-1 >= 0 && direction != 3){
			if(labelMatrix[i-1][j] == 0 && catagoryMatrix[i-1][j] != -1 && catagoryMatrix[i][j] != -1)
				floodFill(i-1, j,rows, cols, catagoryMatrix, labelMatrix, 0, label);
		}
		//left
		if(j-1 >= 0 && direction != 2){
			if(labelMatrix[i][j-1] == 0 && catagoryMatrix[i][j-1] != -1 && catagoryMatrix[i][j] != -1)
				floodFill(i, j-1,rows, cols,  catagoryMatrix, labelMatrix, 1, label);
		}
		//right
		if(j < cols-1 && direction != 1){
			if(labelMatrix[i][j+1] == 0 && catagoryMatrix[i][j+1] != -1 && catagoryMatrix[i][j] != -1)
				floodFill(i, j+1,rows, cols,  catagoryMatrix, labelMatrix, 2, label);
		}
		//down
		if(i < rows-1 && direction != 0){
			if(labelMatrix[i+1][j] == 0 && catagoryMatrix[i+1][j] != -1 && catagoryMatrix[i][j] != -1)
				floodFill(i+1, j,rows, cols,  catagoryMatrix, labelMatrix, 3, label);
		}
	}
	
	
	/** Requirements are <Synth>(1*) <Guard> (1*) <Modifier> (1) <Property> (1*) in any order*/
	private static boolean validateSentence(ArrayList<Tile> catagoryArray, Handler mHandler){
		int identifiers = 0, guards = 0, modifiers = 0, properties = 0, anomolies = 0;
		for(int i = 0; i < catagoryArray.size(); i++){
			switch(TileReference.getCatagory(catagoryArray.get(i).getIconID())){
			case TileReference.CATAGORY_GUARD:
				guards++;
				break;
			case TileReference.CATAGORY_IDENTIFIER:
				identifiers++;
				break;
			case TileReference.CATAGORY_MODIFIER:
				modifiers++;
				if(modifiers > 1)
					return false;
				break;
			case TileReference.CATAGORY_PROPERTY:
				properties++;
				break;
			default:
				anomolies++;
				
			}
		}
		System.out.println("Guards: " + guards + ", Identifiers: " + identifiers + ", Modifiers: " + modifiers + ", Properties: " + properties + ", Anomolies: " + anomolies);
		if(guards > 0 && identifiers > 0 && properties > 0 && modifiers > 0)
			return true;
		else{
			if(modifiers > 0){
				//mHandler.obtainMessage(ProgramActivity.HANDLER_TOAST, "Too many modifiers!").sendToTarget();
			}
			else{
				//mHandler.obtainMessage(ProgramActivity.HANDLER_TOAST, "Not enough tiles").sendToTarget();
			}
			return false;
		}
	}
	

	
	public static void printSentence(ArrayList<Tile> sentence){
		for(int i = 0; i < sentence.size(); i++){
			Log.e(TAG, sentence.get(i).toString());
		}
	}
	
	public static void printMatrix(int [][] matrix, int rows, int cols){
		System.out.println();
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				System.out.print("" + matrix[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		
	}
	

}
