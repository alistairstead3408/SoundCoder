package stead.alistair.com.soundcoder;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

public class GridSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {
	Context mContext;

	private GridControl thread;

	public GridSurfaceView(Context context, AttributeSet attrs,
			Handler handlerInput, int rowsInput, int colsInput,
			int tileWidthInput, int tileHeightInput, int xGridOffsetInput,
			int yGridOffsetInput) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		mContext = context;

		// create thread only; it's started in surfaceCreated()
		thread = new GridControl(rowsInput, colsInput, tileWidthInput,
				tileHeightInput, xGridOffsetInput, yGridOffsetInput, holder,
				context, handlerInput);

		setFocusable(true); // make sure we get key events
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		thread.start();
		/*
		thread.set(4, 1, new GridObjectView(mContext,
				R.drawable.tile_blob_containment, 1, 100, 0.5f, 0.5f, 50, 0.7f,
				0.7f));
		thread.set(4, 2, new GridObjectView(mContext,
				R.drawable.tile_blob_entrance, 1, 100, 0.5f, 0.5f, 50, 0.7f,
				0.7f));

		thread.set(4, 3, new GridObjectView(mContext,
				R.drawable.tile_property_frequency, 3));

		thread.set(4, 4, new GridObjectView(mContext, R.drawable.tile_modifier_value, 5,
				0.5f));
		thread.set(4, 4, new GridObjectView(mContext, R.drawable.tile_synth, 6,
				1));
		thread.set(4, 5, new GridObjectView(mContext, R.drawable.tile_modifier_attack, 0.5f, 0.5f));
	*/
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}

	}

	public GridControl getThread() {
		return thread;
	}

	class GridControl extends Thread {

		/** Handler we can use to send messages back to the UI thread */
		private Handler mHandler;

		/** Indicate whether the surface has been created & is ready to draw */
		private boolean mRun = false;

		/** Tag which is printed on debug statements */
		private static final String TAG = "GridControl";

		/** Geometry Variables */
		private int tileWidth;
		private int tileHeight;

		/** The position the grid is in (non mutable) */
		private float xGridOffset;
		private float yGridOffset;

		/** Offsets which will never change */
		private final float XHARDOFFSET;
		private final float YHARDOFFSET;

		/** The number of fixed rows and cols given to the grid */
		private int rows;
		private int cols;

		/** Handle to the surface manager object we interact with */
		private SurfaceHolder mSurfaceHolder;

		/** Context we can use to load resources */
		private Context mContext;

		/** Generic rectangle to use for bounds */
		Rect r = new Rect(0, 0, 0, 0);

		/** Set of generic paints we can use without adding objects */
		Paint mPaintWhite = new Paint();
		Paint paintBlack = new Paint();
		Paint paintYellow = new Paint();
		Paint paintRed = new Paint();
		Paint paintYellowThin = new Paint();
		Paint paintGreyFilled = new Paint();

		/** The main grid object we'll use to store objects */
		public ArrayList<ArrayList<GridObjectView>> gridMatrix = null;

		/** Grid drag variable */
		private Coordinate gridDragDown = null;

		/** Touch Variables */
		private Coordinate lastDownTouchCoordinate = new Coordinate(-1, -1);
		private GridObjectView lastDownTouchObject = null;
		private long lastDownTouchTime = 0;
		public Coordinate currentMovePos = new Coordinate(-1, -1);
		public boolean currentlyMoving = false;
		private Coordinate popupSelection = null;

		public final static int HANDLER_PALETTE_DROP_TILE = 0;
		public final static int HANDLER_TILE_STARTDRAG = 1;
		public final static int HANDLER_TILE_DRAG = 2;
		public final static int HANDLER_TILE_DROP = 3;
		public final static int HANDLER_PALETTE_DROP_CANCEL = 4;
		public final static int HANDLER_DOUBLETAP = 5;
		public final static int HANDLER_PALETTE_TILE_DRAG = 6;
		public final static int HANDLER_DELETE_SELECTED_TILE = 7;
		public final static int HANDLER_VALIDATE = 8;

		/** Reusable component, not used for persistence */
		Coordinate gridPoint = new Coordinate(0, 0);
		
		/** 400ms is required before the "Drag" command is sent to the grid */
		private boolean passedDownTimeRequirement = false;
		private long mDownTime = 0;

		public GridControl(int rowsInput, int colsInput, int tileWidthInput,
				int tileHeightInput, int xGridOffsetInput,
				int yGridOffsetInput, SurfaceHolder surfaceHolder,
				Context context, Handler mHandlerInput) {
			mSurfaceHolder = surfaceHolder;
			mContext = context;
			mHandler = mHandlerInput;
			tileWidth = tileWidthInput;
			tileHeight = tileHeightInput;
			xGridOffset = xGridOffsetInput;
			yGridOffset = yGridOffsetInput;
			XHARDOFFSET = xGridOffsetInput;
			YHARDOFFSET = yGridOffsetInput;
			rows = rowsInput;
			cols = colsInput;
			mPaintWhite.setColor(Color.WHITE);
			paintBlack.setColor(Color.GRAY);
			paintRed.setColor(Color.RED);
			paintRed.setStrokeWidth(4);
			paintRed.setStyle(Style.STROKE);
			paintYellow.setColor(Color.YELLOW);
			paintYellowThin.setColor(Color.YELLOW);
			paintYellowThin.setStrokeWidth(4);
			paintYellowThin.setStyle(Style.STROKE);
			paintGreyFilled.setColor(Color.rgb(90, 90, 90));
			

			
			populateMatrix();

			// **********************
		}

		@Override
		public void run() {
			while (mRun) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (ProgramActivity.drawingMutex) {
						doDraw(c);
					}

				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}

			}
		}

		public void setRunning(boolean b) {
			mRun = b;
		}

		public void pause() {
			synchronized (ProgramActivity.drawingMutex) {
				mRun = false;
			}
		}

		private void populateMatrix() {
			synchronized (ProgramActivity.drawingMutex) {
				if(PersistantObjects.getGrid() == null){
					gridMatrix = new ArrayList<ArrayList<GridObjectView>>();
					for (int i = 0; i < cols; i++) {
						ArrayList<GridObjectView> temp = new ArrayList<GridObjectView>();
						for (int j = 0; j < rows; j++) {
							temp.add(new GridObjectView(mContext));
						}
						gridMatrix.add(temp);
					}
				}
				else{
					/** We can re-initiate previously saved grids */
					gridMatrix = PersistantObjects.getGrid();
				}
			}
		}

		public void set(int row, int col, GridObjectView object) {
			synchronized (ProgramActivity.drawingMutex) {
				if (gridMatrix.size() >= row && gridMatrix.get(0).size() >= col) {
					if(object != null)
						gridMatrix.get(row).set(col, object);
					else
						Log.e(TAG, "Failed to set null grid object!");
					
					GridValidator.validate(gridMatrix, mHandler);
				}
			}
		}
		
		public boolean isValid(){
			return GridValidator.validate(gridMatrix, mHandler);
		}
		
		public SystemState getSystemState(float [] blobData){
			SystemState mSystemState = GridValidator.getSystemState(gridMatrix, mHandler, blobData);
			return mSystemState;
		}

		public void setPopUpSelection(Coordinate inputCoordinate) {
			synchronized (ProgramActivity.drawingMutex) {
				popupSelection = inputCoordinate;
			}
		}

		public void resetPopupSelection() {
			synchronized (ProgramActivity.drawingMutex) {
				popupSelection = null;
			}
		}

		public synchronized GridObjectView get(int row, int col) {
			synchronized (ProgramActivity.drawingMutex) {
				return gridMatrix.get(row).get(col);
			}
		}

		/**
		 * Returns the coordinate on the grid, taking into account the offset
		 * and the local offset around the finger
		 * 
		 * @param rawX
		 * @param rawY
		 * @return
		 */
		public Coordinate getGridPoint(float rawX, float rawY) {
			synchronized (ProgramActivity.drawingMutex) {
				rawX -= xGridOffset;
				rawY -= yGridOffset;
				Coordinate temp;
				temp = new Coordinate((int) (rawX / tileWidth),
						(int) (rawY / tileHeight));
				return temp;
			}
		}

		/**
		 * This method requests an offset change. If the offset hits a border of
		 * the grid the change is rejected in those directions. (Changes local
		 * variables xGridOffset and yGridOffset).
		 * 
		 * @param rawX
		 * @param rawY
		 */
		private void requestOffsetChange(float rawX, float rawY) {
			synchronized (ProgramActivity.drawingMutex) {
				if (rawX < XHARDOFFSET
						&& rawX > -1 * ((cols * tileWidth) - 800)) {
					xGridOffset = rawX;
				}

				if (rawY < YHARDOFFSET
						&& rawY > -1 * ((rows * tileHeight) - 480)) {
					yGridOffset = rawY;
				}
			}
		}

		/**
		 * This completely ignores palettes (sorted out by ProgramActivity) and
		 * just returns info about the underlying grid object
		 * 
		 * @param rawX
		 * @param rawY
		 * @return
		 */
		public/* synchronized */int getGridDragTypeAtPos(float rawX, float rawY) {
			synchronized (ProgramActivity.drawingMutex) {
				// Already uses getGridPoint which takes offsets into
				// consideration

				if (rawX > xGridOffset) {
					Coordinate co = getGridPoint(rawX, rawY);
					if (get((int) co.x, (int) co.y).getType() == TileReference.TYPE_EMPTY) {
						return ProgramActivity.DRAG_GRID;
					} else
						return ProgramActivity.DRAG_TILE;
				} else {
					return ProgramActivity.DRAG_NOTHING;
				}
			}

		}

		public Handler getHandler() {
			return mGridHandler;
		}

		private Handler mGridHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case GridControl.HANDLER_PALETTE_DROP_TILE:
					gridPoint = GridControl.this.getGridPoint(msg.arg1,msg.arg2);
					if(get((int)gridPoint.x, (int) gridPoint.y).getType() == TileReference.TYPE_EMPTY)
					{
						set((int) gridPoint.x, (int) gridPoint.y,(GridObjectView) msg.obj);
						currentlyMoving = false;
						Coordinate newPoint = getGridPoint((int) msg.arg1,	(int) msg.arg2);
						popupSelection = newPoint;
					}
					break;
				/**
				 * Regardless of grid or gridobject used to drag
				 */
				case GridControl.HANDLER_TILE_STARTDRAG:
					int gridPosType = GridControl.this.getGridDragTypeAtPos(
							msg.arg1, msg.arg2);
					if (gridPosType == ProgramActivity.DRAG_GRID) {
						gridDragDown = new Coordinate((msg.arg1 - xGridOffset),
								(msg.arg2 - yGridOffset));
					} else if (gridPosType == ProgramActivity.DRAG_TILE) {
						// Setting gridDragDown helps differentiate from
						// grid/tile event later
						gridDragDown = null;
					
						gridPoint = getGridPoint(msg.arg1, msg.arg2);
						lastDownTouchObject = get((int) gridPoint.x,
								(int) gridPoint.y);
						lastDownTouchCoordinate = gridPoint;
						popupSelection = null;
						mDownTime = SystemClock.elapsedRealtime();
						passedDownTimeRequirement = false;
					}
					break;
				/** Has to be different from below due to the timing issues */	
				case GridControl.HANDLER_PALETTE_TILE_DRAG:
					
					currentlyMoving = true;
					
					currentMovePos = new Coordinate(msg.arg1, msg.arg2);
					popupSelection = currentMovePos;
					// Method to allow grid movement after certain points on
					// the canvas
					if (msg.arg1 > 700)
						requestOffsetChange(xGridOffset - 5, yGridOffset);
					else if (msg.arg1 < 100)
						requestOffsetChange(xGridOffset + 5, yGridOffset);
					if (msg.arg2 > 380)
						requestOffsetChange(xGridOffset, yGridOffset - 5);
					else if (msg.arg2 < 100)
						requestOffsetChange(xGridOffset, yGridOffset + 5);
					break;
				/**
				 * Just updates dragging position
				 */
				case GridControl.HANDLER_TILE_DRAG:
					if (gridDragDown != null) { // Grid drag
						requestOffsetChange((msg.arg1 - gridDragDown.x),
								(msg.arg2 - gridDragDown.y));
					} else { // Tile drag
						if((SystemClock.elapsedRealtime() - mDownTime) > 400 || getGridPoint(msg.arg1, msg.arg2).equals(lastDownTouchCoordinate) == false){
							if(passedDownTimeRequirement == false){
								set((int) lastDownTouchCoordinate.x,(int) lastDownTouchCoordinate.y,new GridObjectView(mContext));
								passedDownTimeRequirement = true;
							}
							currentlyMoving = true;
							
							currentMovePos = new Coordinate(msg.arg1, msg.arg2);
							popupSelection = currentMovePos;
							// Method to allow grid movement after certain points on
							// the canvas
							if (msg.arg1 > 700)
								requestOffsetChange(xGridOffset - 5, yGridOffset);
							else if (msg.arg1 < 100)
								requestOffsetChange(xGridOffset + 5, yGridOffset);
							if (msg.arg2 > 380)
								requestOffsetChange(xGridOffset, yGridOffset - 5);
							else if (msg.arg2 < 100)
								requestOffsetChange(xGridOffset, yGridOffset + 5);
						}
					}
					break;
		
				/**
				 * Drops the object comfortably :)
				 */
				case GridControl.HANDLER_TILE_DROP:
					if (gridDragDown != null) { // gridDrag
						// Do nothing
						popupSelection = null;
					} else {
						
						currentlyMoving = false;
						Coordinate newPoint = getGridPoint((int) msg.arg1,	(int) msg.arg2);
						popupSelection = newPoint;
						Log.e(TAG, "Grid Set : " + newPoint.toString());
						if (get((int) newPoint.x, (int) newPoint.y).getType() == TileReference.TYPE_EMPTY){
							set((int) newPoint.x, (int) newPoint.y,	lastDownTouchObject);
						}
						else{
							set((int) lastDownTouchCoordinate.x,(int) lastDownTouchCoordinate.y,lastDownTouchObject);
							popupSelection = lastDownTouchCoordinate;
						}
						lastDownTouchObject = null;
						Log.e(TAG, "" + GridValidator.validate(gridMatrix, mHandler));
					}
					break;
				case GridControl.HANDLER_PALETTE_DROP_CANCEL:
					currentlyMoving = false;
					popupSelection = null;
					currentMovePos.x = -1;
					currentMovePos.y = -1;
					lastDownTouchObject = null;
					break;
				case GridControl.HANDLER_DOUBLETAP:
					gridPoint = GridControl.this.getGridPoint(msg.arg1, msg.arg2);
					GridObjectView temp = get((int) gridPoint.x, (int)gridPoint.y);
					if(temp.getType() != TileReference.TYPE_EMPTY)
						mHandler.obtainMessage(ProgramActivity.HANDLER_SETTINGS_OPEN, temp).sendToTarget();
					break;
				case GridControl.HANDLER_DELETE_SELECTED_TILE:
					set((int) popupSelection.x, (int)popupSelection.y, new GridObjectView(mContext));
					popupSelection = null;
					break;
				case GridControl.HANDLER_VALIDATE:
					GridValidator.validate(gridMatrix, mHandler);
				}// switch
			}
		};

		/**
		 * This is the main drawing method. Draws tiles, then grid. Uses
		 * CurrentMovePos (Coordinate) and currentlymoving (boolean) to mark
		 * where the user is moving their finger
		 * 
		 * @param c
		 *            (Canvas)
		 */
		public void doDraw(Canvas c) {

			// *********Processing*******************************
			if (c == null)
				Log.e(TAG, "C NULL");

			c.drawPaint(paintBlack);

			// ***************DRAW*PRESENT*TILES******************
			synchronized (ProgramActivity.drawingMutex) {
				for (int i = 0; i < gridMatrix.size(); i++) {
					for (int j = 0; j < gridMatrix.get(i).size(); j++) {

						r.left = (int) ((i * tileWidth) + xGridOffset);
						r.right = r.left + tileWidth;
						r.top = (int) ((j * tileHeight) + yGridOffset);
						r.bottom = r.top + tileHeight;
						gridMatrix.get(i).get(j).DrawInBounds(c, r);

					}
				}
			}

			int touchHoverX = -2, touchHoverY = -2;

			// Sets up the visibility for the yellow grid box
			if (currentlyMoving) {
				touchHoverX = (int) ((currentMovePos.x - xGridOffset) / tileWidth);
				touchHoverY = (int) ((currentMovePos.y - yGridOffset) / tileHeight);

				

			}

			// ***************DRAW*GRID***************************
			for (int i = 0; i < gridMatrix.size(); i++) {
				for (int j = 0; j < gridMatrix.get(i).size(); j++) {
					if (currentlyMoving
							&& (touchHoverX == i)
							&& (touchHoverY == j)
							&& (get((int) touchHoverX, (int) touchHoverY)
									.getType() == TileReference.TYPE_EMPTY)) {
						r.left = (int) ((i * tileWidth) + xGridOffset);
						r.top = (int) ((j * tileHeight) + yGridOffset);
						r.right = (int) (((i + 1) * tileWidth) + xGridOffset);
						r.bottom = (int) (((j + 1) * tileHeight) + yGridOffset);
						synchronized (ProgramActivity.drawingMutex) {
							c.drawRect(r, paintYellow);
						}
					} else {
						synchronized (ProgramActivity.drawingMutex) {
							// Draw Horizontal Lines
							c.drawLine((i * tileWidth) + xGridOffset,
									(j * tileHeight) + yGridOffset,
									((i + 1) * tileWidth) + xGridOffset,
									(j * tileHeight) + yGridOffset, mPaintWhite);
							// Draw Vertical Lines
							c.drawLine((i * tileWidth) + xGridOffset,
									(j * tileHeight) + yGridOffset,
									(i * tileWidth) + xGridOffset,
									((j + 1) * tileHeight) + yGridOffset,
									mPaintWhite);
						}
					}
					// This is to replace the surrounding rectangle with red to
					// show the current selection
					if (popupSelection != null && i > 0 && j > 0) {
						if (popupSelection.x == i-1 && popupSelection.y == j-1) {
							synchronized (ProgramActivity.drawingMutex) {
								/** Post-drawing because otherwise grid draws over selection rectangle*/
								c.drawRect(
										new Rect(
												(int) (((i-1) * tileWidth) + xGridOffset),
												(int) (((j-1 )* tileHeight) + yGridOffset),
												(int) (((i) * tileWidth) + xGridOffset),
												(int) (((j) * tileHeight) + yGridOffset)),
												paintYellowThin);
							}
						}

					}
					
					//This is to highlight incorrect tiles where the debug boolean has been set to true
					if(gridMatrix.get(i).get(j).getCompletionHighlight()){
						synchronized (ProgramActivity.drawingMutex) {
							Drawable drawable;
							/** Post-drawing because otherwise grid draws over selection rectangle*/
							if(!(currentlyMoving&& (touchHoverX == (i-1))&& (touchHoverY == (j-1))))
								drawable = this.mContext.getResources().getDrawable(R.drawable.complete_background);
							else
								drawable = this.mContext.getResources().getDrawable(R.drawable.complete);
							r = new Rect((int) (((i-1) * tileWidth) + xGridOffset), (int) (((j-1) * tileHeight) + yGridOffset), (int) (((i) * tileWidth) + xGridOffset), (int) (((j) * tileHeight) + yGridOffset));
							drawable.setBounds(r);
							drawable.draw(c);
						}
					}
					
					if(gridMatrix.get(i).get(j).getSemanticDebugHighlight()){
						synchronized (ProgramActivity.drawingMutex) {
							c.drawRect(
									new Rect(
											(int) (((i-1) * tileWidth) + xGridOffset),
											(int) (((j-1 )* tileHeight) + yGridOffset),
											(int) (((i) * tileWidth) + xGridOffset),
											(int) (((j) * tileHeight) + yGridOffset)),
											paintRed);
						}
					}
				}
			}// for
			
			if(currentlyMoving){
				
				Coordinate tileOffset = addOrientationOffset(currentMovePos.x,
						currentMovePos.y);
				if (lastDownTouchObject != null) {
					synchronized (ProgramActivity.drawingMutex) {
						Rect bounds = new Rect((int) tileOffset.x,
								(int) tileOffset.y, (int) tileOffset.x
										+ tileWidth, (int) tileOffset.y
										+ tileHeight);
						lastDownTouchObject.DrawInBounds(c, bounds);
					}
				}
			}

			// ***************************************************

		}

		/**
		 * This is a temporary method
		 * 
		 * @param x
		 * @param y
		 * @return
		 */
		private Coordinate addOrientationOffset(float x, float y) {
			synchronized (ProgramActivity.drawingMutex) {
				x -= (1 * tileWidth);
				y -= (1 * tileHeight);
				Coordinate temp = new Coordinate(x, y);
				return temp;
			}
		}
	}

}
