package stead.alistair.com.tiles;

import stead.alistair.com.soundcoder.ProgramActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Music tile 
 * @author Alistair Stead
 *
 */
public class TileOneBlob extends Tile
{
	private static final String TAG = TileOneBlob.class.getSimpleName();
	private float h, s, v;
	private Paint paint = null;
	private int mColourID = -1;

	private Rect r;
	private int iconID;
	
	// Tile that's connected with a blob
	public TileOneBlob(Context context, int iconIDInput, float hInput, float sInput, float vInput,int colourID, int tileWidth, int tileHeight) {
		super(context, iconIDInput, TileReference.TYPE_BLOB_ONE, tileWidth, tileHeight);
		setWillNotDraw(false);
		iconID = iconIDInput;
		h = hInput;
		s = sInput;
		v = vInput;

		paint = new Paint(0);
		paint.setColor(Color.rgb((int)h, (int)s, (int)v));
		mColourID = colourID;
	}
	
	
	public int getColor() {
		if (paint != null)
			return paint.getColor();
		else
			return 0;
	}
	

	public void setColor(int newColor) {
		if (paint != null){
			paint.setColor(newColor);
		}
	}
	
	public int getColourID(){
		return mColourID;
	}
	
	public void setColourID(int newID){
		mColourID = newID;
	}
	
	public static float[] getHSV(float hInput, float sInput, float vInput) {
		float[] temp = new float[3];
		temp[0] = hInput;
		temp[1] = sInput;
		temp[2] = vInput;
		return temp;
	}

	
	@Override
	public void DrawInBounds(Canvas canvas, Rect bounds){
		synchronized (ProgramActivity.drawingMutex) {
			Drawable drawable = this.getResources().getDrawable(iconID);
			drawable.setBounds(bounds);
			canvas.drawRect(bounds, paint);
			drawable.draw(canvas);
		}
	}
	
	public TileOneBlob getClone(){
		return new TileOneBlob(mContext, iconID, h, s, v, mColourID, getTileWidth(), getTileHeight());
	}


	@Override
	public boolean equals(Object other)
	{
		TileOneBlob otherGov = (TileOneBlob) other;
		if(this.getColor() == otherGov.getColor())
			return true;
		return false;
	}
}
