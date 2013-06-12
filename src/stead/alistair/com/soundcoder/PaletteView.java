package stead.alistair.com.soundcoder;

import java.util.ArrayList;

import stead.alistair.com.tiles.NewAttack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PaletteView extends ImageView {

	private static final String TAG = "PaletteView";
	ArrayList<GridObjectView> paletteTiles;

	/** fixed tile parameters */
	private final int tileWidth = 80;
	private final int tileHeight = 80;

	/** Offsets for tiles */
	private final int yInitialOffset = 65;
	private final int xInitialOffset = 10;

	/** the space between each visible tile */
	private final int spacerOffset = 10;

	/** The variable offset that changes with touch */
	private int yOffset = 0;

	/** Reusable components */
	private Drawable drawable;
	private Rect bounds;

	/** Stores the tile selected on the down touch */
	private int currentlySelected = -1;

	/** Stores the xOffset for the currently selected tile */
	private int dragXOffset = 0;

	/** Used to caculate the yOffset value */
	private Coordinate lastTouchPos = null;

	/**
	 * This is telling the touch handler that the down click was on the handle,
	 * not the tiles
	 */
	private boolean handle = false;

	/** Local storage of the handler used to update the UI thread */
	private Handler mHandler;

	public PaletteView(Context context, float[] blobDataInput, Handler handler)
			throws Exception {
		super(context);
		mHandler = handler;
		bounds = new Rect(0, 0, 0, 0);
		setBackgroundColor(Color.WHITE);

		lastTouchPos = new Coordinate(0, 0);

		paletteTiles = new ArrayList<GridObjectView>();

		if (blobDataInput.length < 3) {
			throw new Exception("No Blob Data Given!");
		}

		/** The Synth tile is also a special tile*/
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_synth, 1));
		
		/** Add initil blob-based tiles */
		paletteTiles.add(new GridObjectView(context,
				R.drawable.tile_blob_entrance, blobDataInput[0],
				blobDataInput[1], blobDataInput[2], 0, TileReference.TYPE_BLOB_ONE));
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_blob_exit, blobDataInput[0], blobDataInput[1],
				blobDataInput[2], 0, TileReference.TYPE_BLOB_ONE));
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_blob_see, blobDataInput[0], blobDataInput[1],
				blobDataInput[2], 0, TileReference.TYPE_BLOB_ONE));
		//paletteTiles.add(new GridObjectView(context,
		//		R.drawable.tile_blob_containment,
		//		blobDataInput[0], blobDataInput[1], blobDataInput[2], 0,
		//		blobDataInput[0], blobDataInput[1], blobDataInput[2], 0));
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_blob_next, blobDataInput[0], blobDataInput[1],
				blobDataInput[2],0, blobDataInput[0], blobDataInput[1],
				blobDataInput[2],0));
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_blob_true, blobDataInput[0],
				blobDataInput[1], blobDataInput[2], 0, TileReference.TYPE_BLOB_ONE));


		/** The Value tile is a special tile */
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_modifier_value, 0.5f));
		
		/** These are changes that should be made to Music tiles (Not the same thing!)*/
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_modifier_attack,
				0.5f, 0.5f));
		
		
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_modifier_area, blobDataInput[0], blobDataInput[1],
				blobDataInput[2], 0, TileReference.TYPE_BLOB_PROPERTY));
		
		
		
		/** Add General Music Tiles */
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_property_frequency));
		paletteTiles.add(new GridObjectView(context, R.drawable.tile_property_volume));
		

		
		
	}

	public PaletteView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PaletteView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		synchronized (ProgramActivity.drawingMutex) {
			super.onDraw(canvas);
			// Draw the palette background **********
			drawable = this.getResources().getDrawable(
					R.drawable.expansion_list);
			bounds.left = 0;
			bounds.top = 0;
			bounds.right = 140;
			bounds.bottom = 480;
			drawable.setBounds(bounds);
			drawable.draw(canvas);

			for (int i = 0; i < paletteTiles.size(); i++) {

				int offsetY = i * (tileHeight + spacerOffset) + yOffset;
				bounds.left = xInitialOffset;
				bounds.right = (xInitialOffset + tileWidth);
				/**
				 * We won't draw the tile because it will be replaced by an
				 * ImageView in the UI
				 */
				if (currentlySelected != i) {
					// bounds.left += dragXOffset;
					// bounds.right += dragXOffset;

					bounds.top = yInitialOffset + offsetY;
					bounds.bottom = (yInitialOffset + tileHeight + offsetY);
					if (bounds.bottom <= 480 && bounds.top > 0)
						paletteTiles.get(i).DrawInBounds(canvas, bounds);
				} else {
					// Send the xOffset to the handler
				}
			}
			
			//Finally draw the faded overlap to imply there were more in the list
			drawable = this.getResources().getDrawable(R.drawable.palette_overlay);
			bounds.left = 0;
			bounds.top = 0;
			bounds.right = 100;
			bounds.bottom = 480;
			drawable.setBounds(bounds);
			drawable.draw(canvas);
		}

	}

	public void setYOffset(int y) {
		if (y < 0
				&& y > -1
						* ((paletteTiles.size() - 1) * (tileHeight + spacerOffset))) {
			yOffset = y;
		}
	}

	public int getYOffset() {
		return yOffset;
	}

	// This is the xOffset which will occur when the user drags the icon
	// slightly
	// it's mostly for effect and to encourage dragging onto the grid
	public void setDragXOffset(int offset) {
		dragXOffset = offset;
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		synchronized (ProgramActivity.drawingMutex) {
			switch (me.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//Log.e(TAG,
				//		"left + width: " + (this.getLeft() + this.getWidth())
				//				+ ", x: " + me.getX());
				if (me.getX() <= this.getWidth()
						&& me.getX() > (this.getWidth() - 40)) { // Handle
					handle = true;
					//Log.e(TAG, "Handle = true");
				} else // Everything but the handle
				{
					//Log.e(TAG, "Everything but the handle");
					handle = false;
					lastTouchPos.x = me.getX();
					lastTouchPos.y = me.getY();
					/*
					 * if(lastTouchPos.x > (this.getLeft() + this.getWidth() -
					 * 40)){ return false; }
					 */
					// set currentlySelected here
					currentlySelected = getObjectNumAtPos(me.getX(), me.getY());
					if (currentlySelected != -1) {

						mHandler.obtainMessage(
								ProgramActivity.HANDLER_TILE_STARTDRAG,(int) me.getRawX(), (int) me.getRawY(),paletteTiles.get(currentlySelected)).sendToTarget();
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (handle) {
					int newXPos = (int) (me.getRawX() - this.getWidth()) + 20;
					if (newXPos >= (40 - this.getWidth()) && newXPos <= 0) {
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(140, 480);
						params.leftMargin = newXPos;
						params.rightMargin = params.leftMargin+ this.getWidth();
						this.setLayoutParams(params);
						//Log.e(TAG, "Move over Palette");
					}
				} else {

					int paletteYOffset = getYOffset();
					int difference = (int) (lastTouchPos.y - me.getY());
					setYOffset((int) (paletteYOffset - difference));
					this.invalidate();
					lastTouchPos.x = me.getX();
					lastTouchPos.y = me.getY();
					dragXOffset = (int) me.getX();
					if (currentlySelected != -1) {
						//Log.e(TAG, "Getting Move Events!");
						mHandler.obtainMessage(ProgramActivity.HANDLER_TILE_DRAG,(int) me.getRawX(), (int) me.getRawY()).sendToTarget();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if (handle) {
					if (this.getLeft() < ((40 - this.getWidth()) / 2)) {
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
								140, 480);
						params.leftMargin = (40 - this.getWidth());
						params.rightMargin = params.leftMargin
								+ this.getWidth();
						this.setLayoutParams(params);

					} else {
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
								140, 480);
						params.leftMargin = 0;
						params.rightMargin = params.leftMargin
								+ this.getWidth();
						this.setLayoutParams(params);
					}
				} else {
					if (currentlySelected != -1) {
						mHandler.obtainMessage(
								ProgramActivity.HANDLER_TILE_DROP,(int) me.getRawX(), (int) me.getRawY()).sendToTarget();
					}
					currentlySelected = -1;
				}
				break;

			}
		}
		// We return false so that the other levels can know
		return true;

	}

	// Assume that the tray is fully extended
	public int getObjectNumAtPos(float x, float y) {
		int blobNumberThatContainsPost = -1;

		for (int i = 0; i < paletteTiles.size(); i++) {

			int offsetY = i * (tileHeight + spacerOffset) + yOffset;
			bounds.left = xInitialOffset;
			bounds.right = (xInitialOffset + tileWidth);
			if (currentlySelected == i) {
				bounds.left += dragXOffset;
				bounds.right += dragXOffset;
			}
			bounds.top = yInitialOffset + offsetY;
			bounds.bottom = (yInitialOffset + tileHeight + offsetY);
			if (bounds.bottom <= 480 && bounds.top > 0) {
				if (bounds.contains((int) x, (int) y))
					blobNumberThatContainsPost = i;
			}
		}

		return blobNumberThatContainsPost;
	}

}
