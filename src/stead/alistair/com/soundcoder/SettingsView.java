package stead.alistair.com.soundcoder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SettingsView extends View implements OnSeekBarChangeListener {

	/** This is our local copy of the tile we'll be looking at */
	GridObjectView tile;

	/** Some reusable variables */
	Drawable drawable;
	Rect bounds;
	Paint blackPaintTitle = new Paint();
	Paint blackPaintNormal = new Paint();
	Paint highlightPaint = new Paint();

	/** Used for opening and closing UI components in the main thread */
	Handler mHandler;

	/** Some useful variables for referring to the type of view we'll show */
	private final String TAG = "SettingsView";

	private int type;

	/** local copy of blob data (allows us to show colour picker) */
	float[] mBlobData;

	/** Records the status of the seekbar for the VAR type */
	private boolean seekbar = false;
	private boolean seekbar2 = false;
	
	private Context mContext;

	public SettingsView(GridObjectView tileInput, Context context,
			float[] blobData, Handler mHandlerInput) {
		super(context);
		mContext = context;
		setWillNotDraw(false);
		mBlobData = blobData;
		highlightPaint.setColor(Color.RED);
		highlightPaint.setStyle(Style.STROKE);
		highlightPaint.setStrokeWidth(3);
		tile = tileInput;
		bounds = new Rect(0, 0, 0, 0);
		mHandler = mHandlerInput;
		blackPaintTitle.setColor(Color.BLACK);
		blackPaintTitle.setTextSize(36);
		Typeface tf = Typeface.DEFAULT_BOLD;

		blackPaintTitle.setTypeface(tf);
		type = TileReference.getType(tileInput.getIconID());

	}


	@Override
	protected void onDraw(Canvas canvas) {
		synchronized(ProgramActivity.drawingMutex){
			super.onDraw(canvas);
			Log.e(TAG, "Settings Re-draw");
			// Draw the palette background **********
			drawable = this.getResources().getDrawable(R.drawable.popup_toolbar);
			bounds.left = 0;
			bounds.top = 0;
			bounds.right = 800;
			bounds.bottom = 480;
			drawable.setBounds(bounds);
			drawable.draw(canvas);
	
			// Add some text at the top to make it look nice
			canvas.drawText(TileReference.getTag(tile.getIconID()) + " Settings ",
					155, 145, blackPaintTitle);
	
			drawTilePreview(canvas);
	
			switch (type) {
			case TileReference.TYPE_BLOB_PROPERTY:
			case TileReference.TYPE_BLOB_ONE:
				drawBlobOne(canvas);
				break;
			case TileReference.TYPE_BLOB_TWO:
				drawBlobTwo(canvas, tile.getIconID());
				break;
			case TileReference.TYPE_MUSIC:
				drawMusic(canvas);
				break;
			case TileReference.TYPE_VAR:
				drawVar(canvas);
				break;
			case TileReference.TYPE_SYNTHID:
				drawSynthID(canvas);
				break;
			case TileReference.TYPE_ATTACK:
				Log.e(TAG, "TYPE_ATTACK");
				drawAttack(canvas);
				break;
			}
		}

	}
	
	private void drawTilePreview(Canvas c){
		// Draw the icon of the gridObject*********
		bounds.left = 160;
		bounds.right = bounds.left + 80;
		bounds.top = 200;
		bounds.bottom = bounds.top + 80;
		tile.DrawInBounds(c, bounds);
		c.drawLine(260, 175, 260, 350, blackPaintNormal);
		blackPaintNormal.setColor(Color.BLACK);
		blackPaintNormal.setTextSize(28);
		c.drawText("Preview", 150, 190, blackPaintNormal);
	}
	
	

	public void drawBlobOne(Canvas canvas) {
		Log.e(TAG, "drawBlobOne" + tile.getColor1());
		Paint tempPaint = new Paint();
		Rect mRect = new Rect(0, 0, 0, 0);
		/** Draw colour picker grid - selection highlighted in red rectangle */
		for (int i = 0; i < mBlobData.length && i < 18; i += 3) {
			if ((i / 3) % 2 == 0) {
				mRect.top = 175;
				mRect.bottom = 225;
			} else {
				mRect.top = 235;
				mRect.bottom = 285;
			}
			mRect.left = 280 + (i / 6 * 60);
			mRect.right = mRect.left + 50;
			float[] temp = { mBlobData[i], mBlobData[i + 1], mBlobData[i + 2] };
			tempPaint.setColor(Color.rgb((int)mBlobData[i], (int)mBlobData[i + 1],(int) mBlobData[i + 2]));
			canvas.drawRect(mRect, tempPaint);
			/** Show the user which colour the tile is currently based on */
			if (tempPaint.getColor() == tile.getColor1())
				canvas.drawRect(mRect, highlightPaint);

		}

	}
	
	public void drawAttack(Canvas canvas){
		if (!seekbar2 && type == TileReference.TYPE_ATTACK) {
			mHandler.obtainMessage(ProgramActivity.HANDLER_SEEKBAR2).sendToTarget();
			
			seekbar2 = true;
		}
		
			synchronized(ProgramActivity.drawingMutex){
				String str = "Final Value: " + MidiReference.getNote((int)( tile.getValue() * 100)) + "/" + (int) (tile.getValue() * 100);
				Paint character = new Paint();
				character.setColor(Color.BLACK);
				character.setTextSize(40);
				canvas.drawText(str,280, 240, character);
				str = "Time: " + tile.getTime() + "s";
				canvas.drawText(str,280, 330, character);
			}
		
	}


	public void touchBlobOne(MotionEvent me) {
		Log.e(TAG, "TouchBlobOne " + tile.getColor1());
		
		Rect mRect = new Rect(0, 0, 0, 0);
		/**
		 * Use exactly the same method to get rectangles as drawBlobOne, then
		 * change tile to appropriate color
		 */
		for (int i = 0; i < mBlobData.length && i < 18; i += 3) {
			if ((i / 3) % 2 == 0) {
				mRect.top = 175;
				mRect.bottom = 225;
			} else {
				mRect.top = 235;
				mRect.bottom = 285;
			}
			mRect.left = 280 + (i / 6 * 60);
			mRect.right = mRect.left + 50;

			if (mRect.contains((int) me.getX(), (int) me.getY())) {
				Log.e(TAG, "CONTAINS*******");
				float[] temp = { mBlobData[i], mBlobData[i + 1],
						mBlobData[i + 2] };
				tile.setColourID(i/3);
				tile.setColor1(Color.rgb((int)mBlobData[i], (int)mBlobData[i + 1],(int) mBlobData[i + 2]));
				tile.invalidate();
				this.invalidate();
			}

		}
		
	}

	public void drawBlobTwo(Canvas canvas, int iconid) {
		String src1 = "Outer";
		String src2 = "Inner";
		switch(iconid){
		case R.drawable.tile_blob_next:
		case R.drawable.tile_blob_nextto:
			src1 = "First";
			src2 = "Second";
		}
		blackPaintNormal.setColor(Color.BLACK);
		blackPaintNormal.setTextSize(28);
		Paint tempPaint = new Paint();
		Rect mRect = new Rect(0, 0, 0, 0);

		canvas.drawText(src1, 280, 200, blackPaintNormal);
		/** Draw colour picker grid - selection highlighted in red rectangle */
		for (int i = 0; i < mBlobData.length-2 && i < 18; i += 3) {
			if ((i / 3) % 2 == 0) {
				mRect.top = 225;
				mRect.bottom = 275;
			} else {
				mRect.top = 285;
				mRect.bottom = 335;
			}
			mRect.left = 280 + (i / 6 * 60);
			mRect.right = mRect.left + 50;
			float[] temp = { mBlobData[i], mBlobData[i + 1], mBlobData[i + 2] };
			tempPaint.setColor(Color.rgb((int)mBlobData[i], (int)mBlobData[i + 1],(int) mBlobData[i + 2]));
			canvas.drawRect(mRect, tempPaint);
			/** Show the user which colour the tile is currently based on */
			if (tempPaint.getColor() == tile.getColor1())
				canvas.drawRect(mRect, highlightPaint);

		}
		canvas.drawLine(470, 175, 470, 350, blackPaintNormal);

		canvas.drawText(src2, 480, 200, blackPaintNormal);
		for (int i = 0; i < mBlobData.length-2 && i < 18; i += 3) {
			if ((i / 3) % 2 == 0) {
				mRect.top = 225;
				mRect.bottom = 275;
			} else {
				mRect.top = 285;
				mRect.bottom = 335;
			}
			mRect.left = 480 + (i / 6 * 60);
			mRect.right = mRect.left + 50;
			float[] temp = { mBlobData[i], mBlobData[i + 1], mBlobData[i + 2] };
			tempPaint.setColor(Color.rgb((int)mBlobData[i], (int)mBlobData[i + 1],(int) mBlobData[i + 2]));
			canvas.drawRect(mRect, tempPaint);
			/** Show the user which colour the tile is currently based on */
			if (tempPaint.getColor() == tile.getColor2())
				canvas.drawRect(mRect, highlightPaint);

		}

	}

	/**
	 * Uses the same methods as drawBlobTwo to detect whether the event happens within
	 * a drawn rectangle. The tile paint is then changed to reflect that given color.
	 * @param me
	 */
	public void touchBlobTwo(MotionEvent me) {
		Rect mRect = new Rect(0, 0, 0, 0);
		/** Draw colour picker grid - selection highlighted in red rectangle */ 
		for (int i = 0; i < mBlobData.length && i < 18; i += 3) {
			if ((i / 3) % 2 == 0) {
				mRect.top = 225;
				mRect.bottom = 275;
			} else {
				mRect.top = 285;
				mRect.bottom = 335;
			}
			mRect.left = 280 + (i / 6 * 60);
			mRect.right = mRect.left + 50;
			if (mRect.contains((int) me.getX(), (int) me.getY())) {
				float[] temp = { mBlobData[i], mBlobData[i + 1], mBlobData[i + 2] };
				tile.setColourID(i/3);
				Log.e(TAG, "TouchBlob1: " + (i/3));
				tile.setColor1(Color.rgb((int)mBlobData[i], (int)mBlobData[i + 1],(int) mBlobData[i + 2]));
				tile.invalidate();
				this.invalidate();
			}
		}
		/** Do detection for the second set of rectangles */
		for (int i = 0; i < mBlobData.length && i < 18; i += 3) {
			if ((i / 3) % 2 == 0) {
				mRect.top = 225;
				mRect.bottom = 275;
			} else {
				mRect.top = 285;
				mRect.bottom = 335;
			}
			mRect.left = 480 + (i / 6 * 60);
			mRect.right = mRect.left + 50;
			if (mRect.contains((int) me.getX(), (int) me.getY())) {
				float[] temp = { mBlobData[i], mBlobData[i + 1], mBlobData[i + 2] };
				Log.e(TAG, "TouchBlob2: " + (i/3));
				tile.setColourID2(i/3);
				tile.setColor2(Color.rgb((int)mBlobData[i], (int)mBlobData[i + 1],(int) mBlobData[i + 2]));
				tile.invalidate();
				this.invalidate();
			}
		}
	}

	public void drawMusic(Canvas canvas) {
		blackPaintNormal.setColor(Color.BLACK);
		blackPaintNormal.setTextSize(28);
		canvas.drawText("No settings for this tile", 280, 240, blackPaintNormal);
	}

	public void touchMusic(MotionEvent me) {

	}

	public void drawVar(Canvas canvas) {
		if (!seekbar && type == TileReference.TYPE_VAR) {
			mHandler.obtainMessage(ProgramActivity.HANDLER_SEEKBAR)
					.sendToTarget();
			
			seekbar = true;
		}
		
			synchronized(ProgramActivity.drawingMutex){
				String str = MidiReference.getNote((int)( tile.getValue() * 100)) + "/" + (int) (tile.getValue() * 100);
				Paint character = new Paint();
				character.setColor(Color.BLACK);
				character.setTextSize(40);
				character.setShadowLayer(3, 5, 5, Color.GRAY);
				canvas.drawText(str,300, 320, character);
			}
		

	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {

		switch (me.getAction()) {
		case MotionEvent.ACTION_UP:
			Log.e(TAG, "" + me.getX() + ", " + me.getY());
			/** if the up event happens on the Exit Button */
			if (me.getX() > 630 && me.getX() < 695 && me.getY() > 60 && me.getY() < 130) { 
				Log.e(TAG, "Close Settings");
				if (seekbar) {
					mHandler.obtainMessage(
							ProgramActivity.HANDLER_SEEKBAR_REMOVE)
							.sendToTarget();
					seekbar = false;
					
				}
				if(seekbar2){
					mHandler.obtainMessage(
							ProgramActivity.HANDLER_SEEKBAR_REMOVE)
							.sendToTarget();
					seekbar2 = false;
				}

				mHandler.obtainMessage(ProgramActivity.HANDLER_SETTINGS_CLOSE)
						.sendToTarget();
			} 
			else 
			{
				switch (type) {
				case TileReference.TYPE_BLOB_PROPERTY:
				case TileReference.TYPE_BLOB_ONE:
					touchBlobOne(me);
					break;
				case TileReference.TYPE_BLOB_TWO:
					touchBlobTwo(me);
					break;
				case TileReference.TYPE_MUSIC:
					touchMusic(me);
					break;
				case TileReference.TYPE_VAR:
					// Touch is already handled by the SeekBar in the UI thread
					break;
				case TileReference.TYPE_SYNTHID:
					touchSynthID(me);
					break;
				}
			}

			break;
		}

		return true;

	}
	
	
	public void drawSynthID(Canvas canvas){
		//Draw minus button
		drawable = mContext.getResources().getDrawable(R.drawable.button_minus);
		bounds.left = 300;
		bounds.right = bounds.left + 50;
		bounds.top = 250;
		bounds.bottom = bounds.top + 50;
		drawable.setBounds(bounds);
		drawable.draw(canvas);
		
		//Draw text preview in between
		String str = TileReference.getSynthString(tile.getSynthNo());
		Paint character = new Paint();
		character.setColor(Color.BLACK);
		character.setTextSize(100);
		character.setShadowLayer(3, 5, 5, Color.GRAY);
		canvas.drawText(str,400, 300, character);
		
		//Draw plus button
		drawable = mContext.getResources().getDrawable(R.drawable.button_plus);
		bounds.left = 525;
		bounds.right = bounds.left + 50;
		bounds.top = 250;
		bounds.bottom = bounds.top + 50;
		drawable.setBounds(bounds);
		drawable.draw(canvas);
		
	}
	
	public void touchSynthID(MotionEvent me){
		
		//Touch minus button
		bounds.left = 300;
		bounds.right = bounds.left + 50;
		bounds.top = 250;
		bounds.bottom = bounds.top + 50;
		
		if(bounds.contains((int)me.getRawX(), (int)me.getRawY())){
			int tempNo;
			if((tempNo = tile.getSynthNo()) > 1){
				tile.setSynthNo(tempNo - 1);
			}
		}
		
		//Touch plus button
		bounds.left = 525;
		bounds.right = bounds.left + 50;
		bounds.top = 250;
		bounds.bottom = bounds.top + 50;
		
		if(bounds.contains((int)me.getRawX(), (int)me.getRawY())){
			int tempNo;
			if((tempNo = tile.getSynthNo()) < 26){
				tile.setSynthNo(tempNo + 1);
			}
		}
		
		tile.invalidate();
		this.invalidate();
		
	}
	
	
	

	public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
		//The Id's have been set so value is 1 and time is 2
		if(sb.getId() == 1){
			tile.setValue((float) ((float) progress / (float) sb.getMax()));
		}
		else if(sb.getId() == 2){
			tile.setTime((float) ((float) progress / 10f));
		}
		Log.e(TAG, "" + tile.getValue());
		tile.invalidate();
		this.invalidate();
		
	}

	public void onStartTrackingTouch(SeekBar arg0) {

	}

	public void onStopTrackingTouch(SeekBar arg0) {

	}

}
