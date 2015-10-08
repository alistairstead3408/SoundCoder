package stead.alistair.com.tiles;

import stead.alistair.com.soundcoder.ProgramActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public abstract class Tile extends View{

	private int mType;
	private int iconID;
	private Rect r;
	private Paint paint = null;
	private final String TAG = "GridObjectView";
	private boolean completionHighlight = false;
	private boolean semanticDebugHighlight = false;
	
	protected Context mContext;
	
	

	public Tile(Context context, int iconIDInput, int type, int tileWidth, int tileHeight){
		super(context);
		iconID = iconIDInput;
		mContext = context;
		mType = type;
		r = new Rect(0, 0, tileWidth, tileHeight);
	}
	
	public int getTileWidth(){
		return r.width();
	}
	
	public int getTileHeight(){
		return r.height();
	}

	
	public void setCompletionHighlight(boolean bool){
		completionHighlight = bool;
	}
	
	public boolean getCompletionHighlight(){
		return completionHighlight;
	}
	
	public void setSemanticDebugHighlight(boolean bool){
		semanticDebugHighlight = bool;
	}
	
	public boolean getSemanticDebugHighlight(){
		return semanticDebugHighlight;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		return false;
	}

	
	public void DrawInBounds(Canvas canvas, Rect bounds){
		synchronized (ProgramActivity.drawingMutex) {
			Drawable drawable = this.getResources().getDrawable(getIconID());
			drawable.setBounds(bounds);
			drawable.draw(canvas);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.e(TAG, "onDraw");
		super.onDraw(canvas);
		DrawInBounds(canvas, r);
	}

	public int getIconID() {
		if (mType != TileReference.TYPE_EMPTY) {
			return iconID;
		} else
			return 0;
	}

	public int getType() {
		return mType;
	}



	
	public abstract Tile getClone();
	

	
	@Override
	public String toString(){
		String tag = TileReference.getTag(iconID);
		
		return tag;
	}
	
	public abstract boolean equals(Object other);
	


}
