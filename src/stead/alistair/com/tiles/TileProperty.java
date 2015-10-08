package stead.alistair.com.tiles;

import stead.alistair.com.soundcoder.ProgramActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Var tile 
 * @author Alistair Stead
 *
 */
public class TileProperty extends Tile
{
	private static final String TAG = TileProperty.class.getSimpleName();

	private Rect r;
	private int iconID;
	private float defaultValue;
	private float value = -1;
	
	public TileProperty(Context context, int iconIDInput, float defaultValueInput, int tileWidth, int tileHeight) {
		super(context, iconIDInput, TileReference.TYPE_VAR, tileWidth, tileHeight);
		setWillNotDraw(false);
		r = new Rect(0, 0, 0, 0);
		value = defaultValueInput;
		defaultValue = defaultValueInput;
	}
	
	public void setValue(float valueInput) {
		Log.e(TAG, "Value Updated!");
		value = valueInput;
	}
	public float getValue() {
		return value;
	}
	
	public float getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void DrawInBounds(Canvas canvas, Rect bounds){
		synchronized (ProgramActivity.drawingMutex) {
			Drawable drawable = this.getResources().getDrawable(iconID);
			drawable.setBounds(bounds);
			drawable.draw(canvas);
			Paint blackPaint = new Paint(0);
			blackPaint.setColor(Color.BLACK);
			
			/** Draw the straight line guage */ 
			r.left = (bounds.left + (bounds.width()) / 2);
			r.top = bounds.top + 12;
			r.right = (bounds.left + (bounds.width() / 2) + 5);
			r.bottom = bounds.bottom - 10;
			canvas.drawRect(r, blackPaint);
			
			/**
			 * Calculate the offset to show the user what the current level is (negative offset)
			 */
			if(value == -1)
				Log.e(TAG, "VALUE has not been initialised");
			else{
			  int verticalOffset =  (int) (value * (r.top - r.bottom + 10));
			  //Log.e(TAG, "Vertical Offset " + verticalOffset);
			  /** Draw the level */
			  r.left = bounds.left + (bounds.width() / 2) - 15;
			  r.right = (bounds.left + (bounds.width() / 2) + 20);
			  r.top = r.bottom + verticalOffset - 10;
		      
			  r.bottom = r.top + 10;
			  canvas.drawRect(r, blackPaint);
			}
		}
	}

	@Override
	public TileProperty getClone()
	{
		return new TileProperty(mContext, iconID, defaultValue, getTileWidth(), getTileHeight());
	}

	@Override
	public boolean equals(Object other)
	{
		TileProperty otherGov = (TileProperty) other;
		if(this.value == otherGov.value)
			return true;
		return false;
	}
}
