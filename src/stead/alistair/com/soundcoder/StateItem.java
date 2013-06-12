package stead.alistair.com.soundcoder;

import android.util.Log;

public class StateItem {
	
	private static final String TAG = "StateItem";
	private final int mColourClassification;
	private boolean mEnter = false;
	private boolean mSee = false;
	private boolean mExit = false;
	private float mArea = 0;
	private float mMinSeenArea = 0;
	private float mMaxSeenArea = 0;
	private int mCount = 0;
	
	/** The mGov is final after being construected */
	public StateItem(int colourClassification){
		mColourClassification = colourClassification;
	}
	
	@Override
	public String toString(){
		return "StateString: Color: " + mColourClassification + ", enter: " + mEnter + ", exit: " + mExit + 
		", see: " + mSee + ", area: " + mArea + ", maxArea: " + mMaxSeenArea + ", minArea: " + mMinSeenArea; 
	}
	
	
	/** Getters and setters*/
	public boolean getEnter(){
		return mEnter;
	}
	
	public void setEnter(boolean val){
		mEnter = val;
	}
	
	public boolean getExit(){
		return mExit;
	}
	
	public void setExit(boolean val){
		mExit = val;
	}
	
	public boolean getSee(){
		return mSee;
	}
	
	public void setSee(boolean val){
		mSee = val;
	}
	
	public void setArea(float val){
		mArea = val;
		if(mArea > mMaxSeenArea )
			mMaxSeenArea = mArea;
		if(mArea < mMinSeenArea || mMinSeenArea == 0)
			mMinSeenArea = mArea;
	}
	
	public float getArea(){
		return mArea;
	}
	
	public float getAreaPercentage(){
		/* - calculates percentage of area seen. Need a consistent relative area
		if(mArea > 0 && mMaxSeenArea > 0){
			float result = (float) (mArea - mMinSeenArea) / (mMaxSeenArea - mMinSeenArea);
			Log.e(TAG, "Area: " + mArea + " Max: " + mMaxSeenArea + " Min:" + mMinSeenArea + ", result: " + result);
			return (float) result;
		}
		else
			return 0;
			*/
		float result = mArea / 75000;
		Log.e(TAG, "PerArea: " + result);
		return result;
	}
	
	public int getCount(){
		return mCount;
	}
	
	public void setCount(int newCount){
		mCount = newCount;
	}
	
	public void resetNonAreaVals(){
		mEnter = false;
		mExit = false;
		mSee = false;
		mCount = 0;
		mArea = 0;
	}
	

}
