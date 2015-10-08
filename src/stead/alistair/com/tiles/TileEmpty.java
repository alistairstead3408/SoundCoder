package stead.alistair.com.tiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Music tile 
 * @author Alistair Stead
 *
 */
public class TileEmpty extends Tile
{
	private static final String TAG = TileEmpty.class.getSimpleName();
	
	// blank tile
	public TileEmpty(Context context, int tileWidth, int tileHeight) {
		super(context, 0, TileReference.TYPE_EMPTY, tileWidth, tileHeight);
	}
	
	
	@Override
	public void DrawInBounds(Canvas canvas, Rect bounds){

	}


	@Override
	public Tile getClone()
	{
		return new TileEmpty(mContext, getTileWidth(), getTileHeight());
	}


	@Override
	public boolean equals(Object other)
	{
		return true;
	}
}
