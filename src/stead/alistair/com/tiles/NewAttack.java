package stead.alistair.com.tiles;

import stead.alistair.com.soundcoder.TileReference;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;

public class NewAttack extends Tile{
	
	private float mTime;
	private float mFinalValue;

	public NewAttack(Context context, float time, float finalValue) {
		super(context, TileReference.TYPE_ATTACK);
		mTime = time;
		mFinalValue = finalValue;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void DrawInBounds(Canvas canvas, Rect bounds){
		//Do nothing
	}

}
