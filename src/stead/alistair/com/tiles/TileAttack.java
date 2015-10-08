package stead.alistair.com.tiles;

import android.content.Context;

/**
 * Music tile 
 * @author Alistair Stead
 *
 */
public class TileAttack extends Tile
{
	private static final String TAG = TileAttack.class.getSimpleName();

	private float mTime;
	private float mDefaultTime;
	float value;
	float defaultValue;
	
	public TileAttack(Context context, int iconIDInput, float timeInMilliseconds, float finalValue, int tileWidth, int tileHeight) {
		super(context, iconIDInput, TileReference.TYPE_ATTACK, tileWidth, tileHeight);
		setWillNotDraw(false);
		mTime = timeInMilliseconds;
		mDefaultTime = mTime;
		value = finalValue;
		defaultValue = finalValue;
		
	}
	
	public float getValue(){
		return value;
	}
	
	public float getDefaultTime(){
		return mDefaultTime;
	}
	
	public float getTime(){
		return mTime;
	}
	
	public void setTime(float newTime){
		mTime = newTime;
	}

	@Override
	public Tile getClone()
	{
		return new TileAttack(mContext, getIconID(), mTime, defaultValue, getTileWidth(), getTileHeight());
	}

	@Override
	public boolean equals(Object other)
	{
		return false;
	}

}
