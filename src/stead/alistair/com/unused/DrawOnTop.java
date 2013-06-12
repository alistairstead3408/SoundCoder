package stead.alistair.com.unused;

import java.util.ArrayList;

import stead.alistair.com.soundcoder.Coordinate;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * This is a class that extends a typical view in android
 * and allows lines to be drawn using touch events
 * 
 * Currently only supports two lines - Positive and Negative
 * but could be extended to support infinite numbers
 * @author Alistair Graham Stead
 *
 */

public class DrawOnTop extends View { 
	
	private ArrayList<Coordinate> positiveLineCoordinates = null;
	private ArrayList<Coordinate> negativeLineCoordinates = null;
	
    public DrawOnTop(Context context) { 
            super(context); 
            
    } 
    @Override 
    protected void onDraw(Canvas canvas) { 
    	if(positiveLineCoordinates != null)
    	{
    		Paint paint = new Paint(); 
            paint.setStyle(Paint.Style.FILL); 
            paint.setColor(Color.WHITE); 
            paint.setStrokeWidth(10);
            canvas.drawLines(getFloatValues(positiveLineCoordinates), paint);
    	}
    	if(negativeLineCoordinates != null)
    	{
    		Paint paint = new Paint(); 
            paint.setStyle(Paint.Style.FILL); 
            paint.setColor(Color.BLACK); 
            paint.setStrokeWidth(10);
            canvas.drawLines(getFloatValues(negativeLineCoordinates), paint);
    	}
    	super.onDraw(canvas); 
    } 
    
    public void start(float x, float y, boolean type){
    	//initialise and add the first coordinate..enabling the rest
    	if(type){
    		if(positiveLineCoordinates == null)
    		{
    			positiveLineCoordinates = new ArrayList<Coordinate>();
    			positiveLineCoordinates.add(new Coordinate(x, y));
    		}
    		else//make a new line
    		{
    			//TODO Add ability to create more than one line
    			// do this by messing with the way the getFloats() method works
    		}
    	}
    	else{
    		if(negativeLineCoordinates == null){
	    		negativeLineCoordinates = new ArrayList<Coordinate>();
	    		negativeLineCoordinates.add(new Coordinate(x, y));
    		}
    		else
    		{
    			//TODO Add ability to create more than one line
    		}
    	}
    	
    }
    
    public void move(float x, float y, boolean type){
    	if(type){
    		positiveLineCoordinates.add(new Coordinate(x, y));
    	}
    	else{
    		negativeLineCoordinates.add(new Coordinate(x, y));
    	}
    }
    
    //Method to return the points in a drawable format
    public static float[] getFloatValues(ArrayList<Coordinate> givenCoordinates){
    	if(givenCoordinates != null){
    		if(givenCoordinates.size() >=2)
    		{
		    	float[] tempFloat = new float[((2 * givenCoordinates.size())-1) * 2];
		    	int floatCount = 6;
		    	tempFloat[0] = givenCoordinates.get(0).x;
		    	tempFloat[1] = givenCoordinates.get(0).y;
		    	tempFloat[2] = givenCoordinates.get(1).x;
		    	tempFloat[3] = givenCoordinates.get(1).y;
		    	// X0, Y0, X1, Y1, || X2, Y2, X3, Y3
		    	// X0, Y0, X1, Y1, || X1, Y1, X2, Y2 || X2, Y2, X3, Y3
		    	
		    	for(int i = 2; i < givenCoordinates.size(); i++){
		    		tempFloat[floatCount-2] = tempFloat[floatCount - 4];
		    		tempFloat[floatCount -1] =  tempFloat[floatCount - 3];
		    		tempFloat[floatCount] =  givenCoordinates.get(i).x;
		    		tempFloat[floatCount + 1] =  givenCoordinates.get(i).y;
		    		floatCount+=4;
		    	}
		    	return tempFloat;
		    	//0 -> 0, 1, 2, 3
		    	//1 -> 4, 5, 6, 7
    		}
    		else
    			return null;
    	}
    	else
    		return null;
    }
    
    
    public ArrayList<Coordinate> getPositiveLineCoordinates(){
    	return positiveLineCoordinates;
    }
    
    public ArrayList<Coordinate> getNegativeLineCoordinates(){
    	return negativeLineCoordinates;
    }
    

     
    
} 