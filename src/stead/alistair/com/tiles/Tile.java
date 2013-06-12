package stead.alistair.com.tiles;

import stead.alistair.com.soundcoder.TileReference;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public abstract class Tile extends View {
	
	public final static int TYPE_BLOB_ONE = 0, TYPE_BLOB_TWO = 1, TYPE_VAR = 2, TYPE_MUSIC = 3, TYPE_SYNTHID = 4,  TYPE_BLOB_PROPERTY = 5, TYPE_EMPTY = 6, TYPE_ATTACK = 7;
	
	Context mContext;
	final int mType;
	
	public Tile(Context context, int type) {
		super(context);
		mContext = context;
		mType = type;
	}
	
	public void DrawInBounds(Canvas canvas, Rect bounds){
		//Do nothing
	}
	
	public int getType(){
		return mType;
	}
	
	
	
	

}
