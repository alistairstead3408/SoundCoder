package stead.alistair.com.tiles;

import stead.alistair.com.soundcoder.ProgramActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Music tile 
 * @author Alistair Stead
 *
 */
public class TileMusic extends Tile
{
	private static final String TAG = TileMusic.class.getSimpleName();

	private Rect r;
	
	public TileMusic(Context context, int iconIDInput, int tileWidth, int tileHeight) {
		super(context, iconIDInput, TileReference.TYPE_MUSIC, tileWidth, tileHeight);
		setWillNotDraw(false);
		r = new Rect(0, 0, 0, 0);
	}
	
	
	@Override
	public void DrawInBounds(Canvas canvas, Rect bounds){
		synchronized (ProgramActivity.drawingMutex) {
			Drawable drawable = this.getResources().getDrawable(getIconID());
			drawable.setBounds(bounds);
			drawable.draw(canvas);
		}
	}


	@Override
	public TileMusic getClone()
	{
		return new TileMusic(mContext, getIconID(), getTileWidth(), getTileHeight());
	}


	@Override
	public boolean equals(Object other)
	{
		return true;
	}
}
