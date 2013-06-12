package stead.alistair.com.soundcoder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class GridObjectView extends View{

	private int mType;
	private int iconID;
	private float h, s, v;
	private float h2, s2, v2;
	private Rect r;
	private Paint paint = null;
	private Paint paint2 = null;
	private float value = -1;
	private float defaultValue;
	private float mTime;
	private float mDefaultTime;
	private final String TAG = "GridObjectView";
	/** Synth Number represents the ID of a given synth on the screen */
	private int mSynthNo;
	private boolean completionHighlight = false;
	private boolean semanticDebugHighlight = false;
	
	private Context mContext;
	
	private int mColourID = -1;
	private int mColourID2 = -1;
	

	// Tile that's connected with a blob
	public GridObjectView(Context context, int iconIDInput, float hInput, float sInput, float vInput,int colourID, int type) {
		super(context);
		mContext = context;
		setWillNotDraw(false);
		iconID = iconIDInput;
		mType = type;
		h = hInput;
		s = sInput;
		v = vInput;
		r = new Rect(0, 0, 0, 0);

		paint = new Paint(0);
		paint.setColor(Color.rgb((int)h, (int)s, (int)v));
		mColourID = colourID;
	}

	// This is for tiles with two colours
	public GridObjectView(Context context, int iconIDInput,
			float hInput, float sInput, float vInput, int colourID, float hInput2,
			float sInput2, float vInput2, int colourID2) {
		super(context);
		mContext = context;
		setWillNotDraw(false);
		iconID = iconIDInput;
		mType = TileReference.TYPE_BLOB_TWO;
		h = hInput;
		s = sInput;
		v = vInput;
		h2 = hInput2;
		s2 = sInput2;
		v2 = vInput2;
		r = new Rect(0, 0, 0, 0);
		
		paint = new Paint(0);
		paint.setColor(Color.rgb((int)h, (int)s, (int)v));
		mColourID = colourID;
		paint2 = new Paint(0);
		paint2.setColor(Color.rgb((int)h2, (int)s2, (int)v2));
		mColourID2 = colourID2;
	}

	// Var tile
	public GridObjectView(Context context, int iconIDInput,
			float defaultValueInput) {
		super(context);
		mContext = context;
		setWillNotDraw(false);
		iconID = iconIDInput;
		r = new Rect(0, 0, 0, 0);
		mType = TileReference.TYPE_VAR;
		value = defaultValueInput;
		defaultValue = defaultValueInput;
	}

	// Music tile
	public GridObjectView(Context context, int iconIDInput) {
		super(context);
		mContext = context;
		setWillNotDraw(false);
		iconID = iconIDInput;
		r = new Rect(0, 0, 0, 0);
		mType = TileReference.TYPE_MUSIC;
	}
	
	public GridObjectView(Context context, int iconIDInput, float timeInMilliseconds, float finalValue) {
		super(context);
		mContext = context;
		setWillNotDraw(false);
		iconID = iconIDInput;
		r = new Rect(0, 0, 0, 0);
		mType = TileReference.TYPE_ATTACK;
		mTime = timeInMilliseconds;
		mDefaultTime = mTime;
		value = finalValue;
		defaultValue = finalValue;
	}
	
	// Synth tile
	public GridObjectView(Context context, int iconIDInput, int synthNo){
		super(context);
		mContext = context;
		setWillNotDraw(false);
		iconID = iconIDInput;
		r = new Rect(0, 0, 0, 0);
		mType = TileReference.TYPE_SYNTHID;
		mSynthNo = synthNo;
	}
	
	// blank tile
	public GridObjectView(Context context) {
		super(context);
		mContext = context;
		mType = TileReference.TYPE_EMPTY;
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

	/**
	 * Returns the first color (only color if mType = blob_one)
	 * 
	 * @return
	 */
	public int getColor1() {
		if (paint != null)
			return paint.getColor();
		else
			return 0;
	}

	/**
	 * Returns the second color (only if mType = blob_two)
	 * 
	 * @return
	 */
	public int getColor2() {
		if (paint2 != null)
			return paint2.getColor();
		else
			return 0;
	}
	
	
	public void setTime(float newTime){
		mTime = newTime;
	}

	/**
	 * Sets the first color (only color if mType = blob_one)
	 * 
	 * @return
	 */
	public void setColor1(int newColor) {
		if (paint != null){
			paint.setColor(newColor);
		}
	}

	/**
	 * Sets the second color (only color if mType = blob_two)
	 * 
	 * @return
	 */
	public void setColor2(int newColor) {
		if (paint != null){
			paint2.setColor(newColor);
		}
	}



	public float getValue() {
		return value;
	}
	
	
	public int getSynthNo(){
		if(mType == TileReference.TYPE_SYNTHID)
			return mSynthNo;
		else
			return 0;
	}
	
	public void setSynthNo(int synthNo){
		if(mType == TileReference.TYPE_SYNTHID){
			mSynthNo = synthNo;
		}
	}

	public void setValue(float valueInput) {
		Log.e(TAG, "Value Updated!");
		value = valueInput;
	}
	
	public float getDefaultTime(){
		return mDefaultTime;
	}
	
	public float getTime(){
		return mTime;
	}

	protected void DrawInBounds(Canvas canvas, Rect bounds) {
		Drawable drawable;

		// Blob Tiles ***************************************
		switch (iconID) {
		case R.drawable.tile_blob_entrance:
		case R.drawable.tile_modifier_area:
		case R.drawable.tile_blob_see:
		case R.drawable.tile_blob_exit:
		case R.drawable.tile_blob_true:
			synchronized (ProgramActivity.drawingMutex) {
				drawable = this.getResources().getDrawable(iconID);
				drawable.setBounds(bounds);
				canvas.drawRect(bounds, paint);
				drawable.draw(canvas);
			}
			break;
		case R.drawable.tile_blob_containment:
			synchronized (ProgramActivity.drawingMutex) {
				drawable = this.getResources().getDrawable(iconID);
				drawable.setBounds(bounds);
				canvas.drawRect(bounds, paint);
				canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(),
						15, paint2);
				drawable.draw(canvas);
			}
			break;
		
		case R.drawable.tile_blob_next:
			
			synchronized (ProgramActivity.drawingMutex) {
				drawable = this.getResources().getDrawable(iconID);
				drawable.setBounds(bounds);
				// 1st Rectangle
				canvas.drawRect(bounds, paint);
				// 2nd Rectangle
				r.left = bounds.left + (bounds.width() / 2);
				r.top = bounds.top;
				r.right = bounds.right;
				r.bottom = bounds.bottom;
				canvas.drawRect(r, paint2);
				drawable.draw(canvas);
			}
			
			break;
		case R.drawable.tile_blob_nextto:
			
			synchronized (ProgramActivity.drawingMutex) {
				drawable = this.getResources().getDrawable(iconID);
				drawable.setBounds(bounds);
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
			break;

		// Value Tile ***************************************
		case R.drawable.tile_modifier_value:
			synchronized (ProgramActivity.drawingMutex) {
				drawable = this.getResources().getDrawable(iconID);
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
			break;

		// Music Tiles ***************************************
		case R.drawable.tile_property_frequency:
		case R.drawable.tile_modifier_attack:
		case R.drawable.tile_property_volume:
			synchronized (ProgramActivity.drawingMutex) {
				drawable = this.getResources().getDrawable(iconID);
				drawable.setBounds(bounds);
				drawable.draw(canvas);
			}
			break;
			
		case R.drawable.tile_synth:
			drawable = this.getResources().getDrawable(iconID);
			drawable.setBounds(bounds);
			drawable.draw(canvas);
			Paint character = new Paint();
			character.setColor(Color.BLACK);
			character.setTextSize(70);
			character.setShadowLayer(3, 5, 5, Color.GRAY);
			canvas.drawText(TileReference.getSynthString(mSynthNo), bounds.left + 15, bounds.bottom - 20, character);
			break;
		}
		
		

	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.e(TAG, "onDraw");
		super.onDraw(canvas);
		DrawInBounds(canvas, new Rect(0, 0, 80, 80));
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

	public static float[] getHSV(float hInput, float sInput, float vInput) {
		float[] temp = new float[3];
		temp[0] = hInput;
		temp[1] = sInput;
		temp[2] = vInput;
		return temp;
	}

	public float getDefaultValue() {
		return defaultValue;
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
	
	public GridObjectView getClone(){
		GridObjectView temp;
		switch(mType){
		case TileReference.TYPE_BLOB_PROPERTY:
		case TileReference.TYPE_BLOB_ONE: //Context context, int iconIDInput, int blobIDInput, float hInput, float sInput, float vInput
			temp = new GridObjectView(mContext, iconID, h, s, v, mColourID, mType);
			break;
		case TileReference.TYPE_BLOB_TWO: // Context context, int iconIDInput, int blobIDInput, float hInput, float sInput, float vInput, float hInput2, float sInput2, float vInput2
			temp = new GridObjectView(mContext, iconID, h, s, v, mColourID, h2, s2, v2, mColourID2);
			break;
		case TileReference.TYPE_EMPTY:
			temp = new GridObjectView(mContext);
			break;
		case TileReference.TYPE_MUSIC: // Context context, int iconIDInput, int blobIDInput
			temp = new GridObjectView(mContext, iconID);
			break;
		case TileReference.TYPE_SYNTHID: //Context context, int iconIDInput, int blobIDInput, int synthNo
			temp = new GridObjectView(mContext, iconID, mSynthNo);
			break;
		case TileReference.TYPE_VAR://Context context, int iconIDInput, int blobIDInput, float defaultValueInput
			temp = new GridObjectView(mContext, iconID, defaultValue);
			break;
		case TileReference.TYPE_ATTACK:
			temp = new GridObjectView(mContext, iconID, mTime, defaultValue);
			break;
		default:
			temp = new GridObjectView(mContext);
			break;
		}
		return temp;
		
	}
	
	@Override
	public String toString(){
		String tag = TileReference.getTag(iconID);
		
		return tag;
	}
	
	@Override
	public boolean equals(Object other){
		if(other.getClass() == GridObjectView.class){
			GridObjectView otherGov = (GridObjectView) other;
			if(otherGov.getType() == this.getType() && this.getIconID() == otherGov.getIconID())
			{
				switch(this.getType()){
				case TileReference.TYPE_BLOB_PROPERTY:
				case TileReference.TYPE_BLOB_ONE:
					if(this.getColor1() == otherGov.getColor1())
						return true;
					break;
				case TileReference.TYPE_BLOB_TWO:
					if(this.getColor1() == otherGov.getColor1() && this.getColor2() == otherGov.getColor2())
						return true;
					break;
				case TileReference.TYPE_EMPTY:
					return true;
				case TileReference.TYPE_MUSIC:
					return true;
				case TileReference.TYPE_SYNTHID:
					if(this.mSynthNo == otherGov.mSynthNo)
						return true;
					break;
				case TileReference.TYPE_VAR:
					if(this.value == otherGov.value)
						return true;
				}
				
				return false;
			}
			else
				return false;
		}
		else
			return false;
	}


}
