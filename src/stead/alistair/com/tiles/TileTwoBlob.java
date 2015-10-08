package stead.alistair.com.tiles;

import stead.alistair.com.soundcoder.ProgramActivity;
import stead.alistair.com.soundcoder.R;
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
public class TileTwoBlob extends Tile
{
	private static final String TAG = TileTwoBlob.class.getSimpleName();
	private float h, s, v;
	private float h2, s2, v2;
	private Paint paint = null;
	private int mColourID = -1;
	private int mColourID2 = -1;
	private Paint paint2 = null;
	
	boolean isSideBySide;

	private Rect r;
	private int iconID;
	
	// This is for tiles with two colours
	public TileTwoBlob(Context context, int iconIDInput,
			float hInput, float sInput, float vInput, int colourID, float hInput2,
			float sInput2, float vInput2, int colourID2, int tileWidth, int tileHeight) {
		super(context, iconIDInput, TileReference.TYPE_BLOB_TWO, tileWidth, tileHeight);
		setWillNotDraw(false);
		iconID = iconIDInput;
		h = hInput;
		s = sInput;
		v = vInput;
		h2 = hInput2;
		s2 = sInput2;
		v2 = vInput2;
		r = new Rect(0, 0, 0, 0);
		
		if(iconIDInput == R.drawable.tile_blob_containment)
			isSideBySide = true;
		
		paint = new Paint(0);
		paint.setColor(Color.rgb((int)h, (int)s, (int)v));
		mColourID = colourID;
		paint2 = new Paint(0);
		paint2.setColor(Color.rgb((int)h2, (int)s2, (int)v2));
		mColourID2 = colourID2;
	}
	

	public int getColor1() {
		if (paint != null)
			return paint.getColor();
		else
			return 0;
	}


	public int getColor2() {
		if (paint2 != null)
			return paint2.getColor();
		else
			return 0;
	}
	

	public void setColor1(int newColor) {
		if (paint != null){
			paint.setColor(newColor);
		}
	}


	public void setColor2(int newColor) {
		if (paint != null){
			paint2.setColor(newColor);
		}
	}
	
	public int getColourID(){
		return mColourID;
	}
	
	public void setColourID(int newID){
		mColourID = newID;
	}
	
	public void setColourID2(int newID){
		mColourID2 = newID;
	}
	
	public int getColourID2(){
		return mColourID2;
	}

	
	@Override
	public void DrawInBounds(Canvas canvas, Rect bounds){
		synchronized (ProgramActivity.drawingMutex) {
			Drawable drawable = this.getResources().getDrawable(iconID);
			drawable.setBounds(bounds);
			if(isSideBySide){
				// 1st Rectangle
				canvas.drawRect(bounds, paint);
				// 2nd Rectangle
				r.left = bounds.left + (bounds.width() / 2) + 3;
				r.top = bounds.top;
				r.right = bounds.right;
				r.bottom = bounds.bottom;
				canvas.drawRect(r, paint2);
				drawable.draw(canvas);
			}
			else{
				canvas.drawRect(bounds, paint);
				canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(),
						15, paint2);
				drawable.draw(canvas);
			}
		}
	}
	
	public TileTwoBlob getClone(){
		return new TileTwoBlob(mContext, iconID, h, s, v, mColourID, h2, s2, v2, mColourID2, getTileWidth(), getTileHeight());
	}


	@Override
	public boolean equals(Object other)
	{
		TileTwoBlob otherGov = (TileTwoBlob) other;
		if(this.getColor1() == otherGov.getColor1() && this.getColor2() == otherGov.getColor2())
			return true;
		return false;
	}
}
