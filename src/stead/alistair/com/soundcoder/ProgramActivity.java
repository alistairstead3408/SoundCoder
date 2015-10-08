package stead.alistair.com.soundcoder;

import stead.alistair.com.soundcoder.GridSurfaceView.GridControl;
import stead.alistair.com.tiles.Tile;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;


public class ProgramActivity extends Activity {

	/** Local copy of the blob data gathered from training (hsv values) */
	private float[] blobData = null;

	/** Useful constants */
	private final String TAG = "ProgramActivity";
	private final int tileWidth = 180;
	private final int tileHeight = 180;

	/** Grid Components - the most important variables here */
	private GridSurfaceView mGridSurfaceView;
	private GridControl mGridThread;
	private Handler mGridThreadHandler;

	/** This is the beast that controls drawing exclusivity */
	public static Object drawingMutex = new Object();

	/** Variables which identify the section that is being selected */
	public enum DragType{
		DRAG_NOTHING,
		DRAG_GRID,
		DRAG_TILE
	}

	/** Used to let the UI thread know to open or close the settings view */
	public enum HandlerMessage{
		HANDLER_SETTINGS_OPEN,
		HANDLER_SETTINGS_CLOSE,
		HANDLER_SEEKBAR,
		HANDLER_SEEKBAR_REMOVE,
		HANDLER_TILE_STARTDRAG,
		HANDLER_TILE_DRAG,
		HANDLER_TILE_DROP,
		HANDLER_TOAST,
		HANDLER_SEEKBAR2,
	}

	public FrameLayout					mFl						= null;
	public RelativeLayout				mRl						= null;
	public RelativeLayout.LayoutParams	mParams					= null;
	public RelativeLayout.LayoutParams	mPaletteParams			= null;
	private SettingsView				mSettingsView			= null;
	private RelativeLayout.LayoutParams mSettingsViewParams = null;

	/** local copy of the mPalette */
	private PaletteView mPalette;

	/** Sensor Information */
	SensorManager mSensorManager;
	private OrientationType lastKnownOrientation;
	public enum OrientationType{
		ORIENTATION_NORMAL, 
		ORIENTATION_90CLOCK, 
		ORIENTATION_90ANTI,
		ORIENTATION_180;
	}


	/**
	 * Used by the variable view to change the value - best slider component
	 * there is atm
	 */
	SeekBar mSeekBar;
	SeekBar mSeekBar2;
	Button mConfirmButton;
	Button mDeleteButton;

	/** Floating tile used for dragging from mPalette */
	Tile mFloatingTile;
	RelativeLayout.LayoutParams mFloatingTileParams;

	GestureDetector mGestureDetector;
	Vibrator vibe;
	
	Toast toast;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.programactivity);
		if(this.getIntent().getBundleExtra("blobData")!= null){
			Log.e(TAG, "GOT SOME DATA");
			long [] longBlobData = this.getIntent().getBundleExtra("blobData").getLongArray("bundleBlobData" );
			
			// remove the first two values, we don't need them
			blobData = new float[(int) (longBlobData[0] -2)];
			Log.e(TAG, "" + blobData[0]);
			int blobDataCounter = -1;
			for(int i = 2; i < longBlobData.length-2; i+=3){
						blobData[++blobDataCounter] = longBlobData[i];
						blobData[++blobDataCounter] = longBlobData[i+1];
						blobData[++blobDataCounter] = longBlobData[i+2];
			}
				 
			
		}
		else{
			
			// ***FAKE BLOB DATA***

			blobData = new float[18]; // 2 blobs with 3 hsv
			blobData[0] = 100;
			blobData[1] = 0.5f;
			blobData[2] = 0.5f;

			blobData[3] = 50;
			blobData[4] = 0.7f;
			blobData[5] = 0.7f;

			blobData[6] = 0;
			blobData[7] = 0.7f;
			blobData[8] = 0.7f;

			blobData[9] = 150;
			blobData[10] = 0.7f;
			blobData[11] = 0.7f;

			blobData[12] = 125;
			blobData[13] = 0.5f;
			blobData[14] = 0.5f;

			blobData[15] = 255;
			blobData[16] = 0.7f;
			blobData[17] = 0.7f;

			// ********************
			
		}

		vibe = (Vibrator) getSystemService (Context.VIBRATOR_SERVICE);
		mFl = (FrameLayout) findViewById(R.id.fl);
		mRl = new RelativeLayout(mFl.getContext());
		mFl.addView(mRl);
		
		



		synchronized (ProgramActivity.drawingMutex) {
			try {
				mPalette = new PaletteView(mRl.getContext(), blobData,mHandler, tileWidth, tileHeight);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mGridSurfaceView = new GridSurfaceView(this, null, mHandler, 20, 20, tileWidth, tileHeight, 40, 0);
		mRl.addView(mGridSurfaceView);
		mGridThread = mGridSurfaceView.getThread();
		mGridThreadHandler = mGridThread.getHandler();

		mPaletteParams = new RelativeLayout.LayoutParams(tileWidth + 60, getWindowManager().getDefaultDisplay().getHeight());
		mPaletteParams.leftMargin = -1 * (tileWidth + 20);
		mPaletteParams.topMargin = 0;
		mRl.addView(mPalette, mPaletteParams);
		mPalette.bringToFront();
		
		mGestureDetector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

					
					@Override
					public boolean onDoubleTap(MotionEvent ev){
						mGridThreadHandler.obtainMessage(GridControl.HANDLER_DOUBLETAP, (int) ev.getX(), (int) ev.getY()).sendToTarget();
						return true;
						
					}
					
					//If we don't override it, it returns true and messes things up
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
						System.out.println("fling");
						return false;
						
					}

			
				});
				

		mGridSurfaceView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent me) {

				/** Do tests to see if we should reject the touch */
				if (mSettingsView != null) {
					return false;
				}

				/** Send every touch event here to the mPalette */
				if (me.getX() <= (mPaletteParams.leftMargin + mPaletteParams.width)
						&& me.getX() > (mPaletteParams.leftMargin + mPaletteParams.width - 40)) {
					return false;
				}

				if (mGestureDetector.onTouchEvent(me))
					return true;
				else {

					/**
					 * Handle grid-based events by sending them to the grid
					 * handler
					 */
					switch (me.getAction()) {
					case MotionEvent.ACTION_DOWN:
							ProgramActivity.this.mGridThreadHandler.obtainMessage(GridControl.HANDLER_TILE_STARTDRAG,(int) me.getRawX(), (int) me.getRawY()).sendToTarget();
						break;
					case MotionEvent.ACTION_MOVE:
						ProgramActivity.this.mGridThreadHandler.obtainMessage(GridControl.HANDLER_TILE_DRAG,(int) me.getRawX(), (int) me.getRawY()).sendToTarget();
						break;
					case MotionEvent.ACTION_UP:
						
						ProgramActivity.this.mGridThreadHandler.obtainMessage(GridControl.HANDLER_TILE_DROP,(int) me.getRawX(), (int) me.getRawY()).sendToTarget();
						break;

					}
					return true;
				}
			}

		});
		
		lastKnownOrientation = ProgramActivity.OrientationType.ORIENTATION_180;

		
	}

	/**
	 * This method changes the tiles position depending on where we think the
	 * humans hand is (down, right)
	 */
	private PointF addOrientationOffset(float x, float y) {

		PointF temp;
		switch(lastKnownOrientation){
		case ORIENTATION_180:
			x -= (1 * tileWidth);
			y -= (1 * tileHeight);
			temp = new PointF(x, y);
			return temp;
		case ORIENTATION_90ANTI:
			x += (0.6 * tileWidth);
			y -= (1.2 * tileHeight);
			temp = new PointF(x, y);
			return temp;
		case ORIENTATION_90CLOCK:
			x -= (1.2 * tileWidth);
			y += (0.6 * tileHeight);
			temp = new PointF(x, y);
			return temp;
		case ORIENTATION_NORMAL:
			x += (1 * tileWidth);
			y += (1 * tileHeight);
			temp = new PointF(x, y);
			return temp;
		default:
			x -= (1.2 * tileWidth);
			y -= (1.2 * tileHeight);
			temp = new PointF(x, y);
			return temp;
		
		}
	}

	/** Handler used throughout the program */
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (HandlerMessage.values()[msg.what]) {
			/**
			 * This is called from GridControl to open the settings view when
			 * the appropriate clicks have been made
			 */
			case HANDLER_SETTINGS_OPEN:
				synchronized (drawingMutex) {
					Log.e(TAG, "Handler Settings");
					mSettingsView = new SettingsView((Tile) msg.obj,
							mRl.getContext(), blobData, this);
					if (mSettingsViewParams == null)
						mSettingsViewParams = new RelativeLayout.LayoutParams(
								getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
					
					
					mSettingsViewParams.leftMargin = 0;
					mSettingsViewParams.topMargin = 0;
					mRl.addView(mSettingsView, mSettingsViewParams);
					mDeleteButton = new Button(mRl.getContext());
					mDeleteButton.setText("Delete");
					mDeleteButton.setOnClickListener(new OnClickListener(){

						public void onClick(View v) {
							mGridThreadHandler.obtainMessage(GridControl.HANDLER_DELETE_SELECTED_TILE).sendToTarget();
							mRl.removeView(mSettingsView);
							mSettingsView = null;
							mRl.removeView(mDeleteButton);
							mRl.removeView(mConfirmButton);
							mRl.removeView(mSeekBar);
							mRl.removeView(mSeekBar2);
							mGridThreadHandler.obtainMessage(GridControl.HANDLER_VALIDATE).sendToTarget();
						}
						
					});
					RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(110, 60);
					buttonParams.leftMargin = 140;
					buttonParams.topMargin = 300;
					mRl.addView(mDeleteButton, buttonParams);
				}
				break;
			/**
			 * Called from GridControl to close the settings view
			 */
			case HANDLER_SETTINGS_CLOSE:
				synchronized (drawingMutex) {
					mRl.removeView(mSettingsView);
					mSettingsView = null;
					mRl.removeView(mDeleteButton);
				}
				break;

			/**
			 * Signals that the user wants to drag a tile from the mPalette to
			 * the grid
			 */
			case HANDLER_TILE_STARTDRAG:
				synchronized (drawingMutex) {
					Log.e(TAG, "Start Drag");
					Tile temp = (Tile) msg.obj;
					mFloatingTile = temp;
					mFloatingTileParams = new RelativeLayout.LayoutParams(tileWidth, tileHeight);
					if (msg.arg1 < mPalette.getLeft() + mPalette.getWidth()) {
						mFloatingTileParams.leftMargin = msg.arg1 - (tileWidth / 2);
						mFloatingTileParams.topMargin = msg.arg2 - (tileHeight / 2);
					} else {
						PointF newCo = addOrientationOffset(msg.arg1, msg.arg2);
						mFloatingTileParams.leftMargin = (int) newCo.x;
						mFloatingTileParams.topMargin = (int) newCo.y;
					}
					mRl.addView(mFloatingTile, mFloatingTileParams);
				}
				break;
			/**
			 * Signals that the user wants to drag a tile from the mPalette to
			 * the grid
			 */
			case HANDLER_TILE_DRAG:
				synchronized (drawingMutex) {
					Log.e(TAG, "Continue Drag");
					/** Over the palette don't add any offset, keep it in place */
					if (msg.arg1 < mPalette.getLeft() + mPalette.getWidth()) {
						mFloatingTileParams.leftMargin = msg.arg1 - (tileWidth / 2);
						mFloatingTileParams.topMargin = msg.arg2 - (tileHeight / 2);
					} else {
					/** Over the grid add the usual offset */
						PointF newCo = addOrientationOffset(msg.arg1,msg.arg2);
						mFloatingTileParams.leftMargin = (int) newCo.x;
						mFloatingTileParams.topMargin = (int) newCo.y;
					}
					mRl.updateViewLayout(mFloatingTile, mFloatingTileParams);
				}
				// Throw the drag to the grid so it reacts as it would to
				// "native" tiles
				mGridThreadHandler.obtainMessage(GridControl.HANDLER_PALETTE_TILE_DRAG, msg.arg1, msg.arg2).sendToTarget();
				break;
			/**
			 * Signals that the user wants to drag a tile from the mPalette to
			 * the grid
			 */
			case HANDLER_TILE_DROP:
				synchronized (drawingMutex) {
					mRl.removeView(mFloatingTile);
					if (msg.arg1 > mPalette.getLeft() + mPalette.getWidth()) {
						mGridThreadHandler.obtainMessage(GridControl.HANDLER_PALETTE_DROP_TILE,msg.arg1, msg.arg2, mFloatingTile.getClone()).sendToTarget();
					} else {
						mGridThreadHandler.obtainMessage(GridControl.HANDLER_PALETTE_DROP_CANCEL).sendToTarget();
					}
					mFloatingTile = null;
				}
				break;

			/**
			 * The mSettingsView needs the seekbar when in VAR mode
			 */
			case HANDLER_SEEKBAR:

				break;
			case HANDLER_SEEKBAR2:

				break;
				
				
				
			case HANDLER_TOAST:
				toast = Toast.makeText(ProgramActivity.this, (String) msg.obj, Toast.LENGTH_SHORT);
				toast.show();
				
				break;

			/**
			 * When mSettingsView is finished, release seekmode and default
			 * button
			 */
			case HANDLER_SEEKBAR_REMOVE:
				mRl.removeView(mSeekBar);
				mSeekBar = null;
				mRl.removeView(mSeekBar2);
				mSeekBar2 = null;
				mRl.removeView(mConfirmButton);
				mConfirmButton = null;
				break;
				
			
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		mGridSurfaceView.getThread().pause();
		PersistantObjects.setGrid(mGridThread.gridMatrix);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		mGridSurfaceView.getThread().resume();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
	}
	
	@Override
	public void onBackPressed() {
		//This needs to be conditional on whether or not the configuration is valid
		if(mGridSurfaceView.getThread().isValid()){
			PersistantObjects.setGrid(mGridThread.gridMatrix);
			PersistantObjects.setSystemState(mGridThread.getSystemState(blobData));
			Intent intent = new Intent(ProgramActivity.this,
					SoundCoderActivity.class);
			ProgramActivity.this.setResult(
					SoundCoderActivity.PROGRAM, intent);
			ProgramActivity.this.finish();
			return;
		}
		else{
			//show a message about it not being valid
			Toast.makeText(ProgramActivity.this, "Please make sure your grid is valid!", Toast.LENGTH_SHORT).show();
		}
		
	}

}
