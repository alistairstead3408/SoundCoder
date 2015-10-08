package stead.alistair.com.tiles;

import stead.alistair.com.soundcoder.ProgramActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Var tile 
 * @author Alistair Stead
 *
 */
public class TileSynth extends Tile
{
	private static final String TAG = TileSynth.class.getSimpleName();

	/** Synth Number represents the ID of a given synth on the screen */
	private int mSynthNo;
	
	// Synth tile
	public TileSynth(Context context, int iconIDInput, int synthNo, int tileWidth, int tileHeight){
		super(context, iconIDInput, TileReference.TYPE_SYNTHID, tileWidth, tileHeight);
		setWillNotDraw(false);
		mSynthNo = synthNo;
	}
	
	
	public int getSynthNo(){
		return mSynthNo;
	}
	
	public void setSynthNo(int synthNo){
		mSynthNo = synthNo;
	}
	
	@Override
	public void DrawInBounds(Canvas canvas, Rect bounds){
		synchronized (ProgramActivity.drawingMutex) {
			Drawable drawable = this.getResources().getDrawable(getIconID());
			drawable.setBounds(bounds);
			drawable.draw(canvas);
			Paint character = new Paint();
			character.setColor(Color.BLACK);
			character.setTextSize(200);
			character.setShadowLayer(3, 5, 5, Color.GRAY);
			canvas.drawText(TileReference.getSynthString(mSynthNo), bounds.left + 30, bounds.bottom - 40, character);
		}
	}


	@Override
	public Tile getClone()
	{
		return new TileSynth(mContext, getIconID(), mSynthNo, getTileWidth(), getTileHeight());
	}


	@Override
	public boolean equals(Object other)
	{
		TileSynth otherGov = (TileSynth) other;
		if(this.mSynthNo == otherGov.mSynthNo)
			return true;
		return false;
	}
}
