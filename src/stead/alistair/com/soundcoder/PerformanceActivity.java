package stead.alistair.com.soundcoder;

import stead.alistair.com.soundcoder.jni.Processor;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// ----------------------------------------------------------------------

public class PerformanceActivity extends Activity {

	private LinearLayout linearLayout = null;
	private FrameLayout frameLayout = null;
	private ImageView imageView = null;
	private String TAG = "TrainingActivity";
	private TextView tv;
	
	private int mState;
	public final static int STARTING = 0, RUNNING = 1, PAUSE = 2;
	
	/** Used to store the classifier */
	private long[] blobData = null;

	/** These thresholds can be changed if they really need to be */
	private int satThreshold = 60;
	private int valThreshold = 60;

//	private NativePreviewer mPreview;

	static final int BEGIN_CAPTURE = 0;

	
	public boolean captureLineHueValues = false;
	
	/** The processor used to process each frame*/
//	public PerformanceProcessor captureProcessor;
	
	/** The system state we will use when each rule is fired */
	SystemState mSystemState = null;
	
	
	void toasts(int id) {
		switch (id) {
		case BEGIN_CAPTURE:
			Toast.makeText(this, "Begin Capture", Toast.LENGTH_LONG).show();
			break;

		default:
			break;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		blobData = this.getIntent().getBundleExtra("blobData")
				.getLongArray("bundleBlobData");

		// Set up the relative layout so we can overlay the crayon
//		mPreview = new NativePreviewer(getApplication(), 640, 480);
//		frameLayout = new FrameLayout(this);
		// This is actually a surface view
		
		//Initialise SystemState
		if(PersistantObjects.getSystemState() != null){
			mSystemState = PersistantObjects.getSystemState();
			if(!mSystemState.started){
				mSystemState.start();
				mSystemState.started = true;
			}
			else{
				Log.e(TAG, "Reset Running");
				mSystemState.running = true;
			}

			boolean connected = mSystemState.initialiseConnection();
			if(!connected){
				Log.e(TAG, "Could get connection!");
			}
		}
		//if we can't initialise, we assume testing atm
		
		

		frameLayout.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent me) {
				switch (me.getAction()) {
				case MotionEvent.ACTION_UP:
					switch (mState) {
					// This is the initial state
					case PerformanceActivity.STARTING:
//						LinkedList<PoolCallback> defaultcallbackstack = new LinkedList<PoolCallback>();
//						defaultcallbackstack.addFirst(glview.getDrawCallback());
//						captureProcessor = new PerformanceProcessor();
//						defaultcallbackstack.addFirst(captureProcessor);
//						mPreview.addCallbackStack(defaultcallbackstack);
						mState = PerformanceActivity.RUNNING;
						break;
					case PerformanceActivity.RUNNING: // -> Pause
						// Show pause icon on screen
						mState = PerformanceActivity.PAUSE;
						break;
					case PerformanceActivity.PAUSE: // -> Run
						// reinitialise processors etc.
						mState = PerformanceActivity.RUNNING;
						break;
					}
					tvHandler.obtainMessage(mState).sendToTarget();
					break;
				}// switch
				return true;
			}
		});// OnTouchListener

		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.height = getWindowManager().getDefaultDisplay().getHeight();
		params.width = (int) (params.height * 4.0 / 2.88);

		tv = new TextView(this);
		tv.setTextSize(30);
		tv.setTextColor(Color.RED);

		LinearLayout vidlay = new LinearLayout(getApplication());

		vidlay.setGravity(Gravity.CENTER);
//		vidlay.addView(mPreview, params);
		frameLayout.addView(vidlay);

		// make the glview overlay ontop of video preview
//		mPreview.setZOrderMediaOverlay(false);

//		glview = new GL2CameraViewer(getApplication(), false, 0, 0);
//		glview.setZOrderMediaOverlay(true);
//		glview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
//				LayoutParams.FILL_PARENT));

		// Add the TextView via a LinearLayout
		linearLayout = new LinearLayout(getApplicationContext());
		linearLayout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		linearLayout.addView(tv);

//		frameLayout.addView(glview);
		frameLayout.addView(linearLayout);

		setContentView(frameLayout);
		imageView = new ImageView(this);
		frameLayout.addView(imageView);

		mState = PerformanceActivity.STARTING;
		tvHandler.obtainMessage(mState).sendToTarget();

	}

	private final Handler tvHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.e(TAG, "HANDLER");
			switch (msg.what) {
			case PerformanceActivity.STARTING:
				fade(true);
				break;
			case PerformanceActivity.RUNNING:
				fade(false);
				// mPreview.refreshSurface();
				break;
			case PerformanceActivity.PAUSE:
				fade(false);
				// mPreview.pauseSurface();
				break;
			}
		}
	};

	public void fade(boolean running) {
		if (running) {
			Drawable d = this.getResources().getDrawable(R.drawable.playicon);
			frameLayout.removeView(imageView);
			imageView.setMaxHeight(256);
			imageView.setMaxWidth(256);
			imageView.setImageDrawable(d);
			frameLayout.addView(imageView);

			Animation myFadeInAnimation = AnimationUtils.loadAnimation(this,
					R.anim.fadein);
			imageView.startAnimation(myFadeInAnimation); // Set animation to
															// your ImageView
		} else {
			frameLayout.removeView(imageView);
		}

	}

	// final processor so that these processor callbacks can access it
	final Processor processor = new Processor();
//	private GL2CameraViewer glview;

//	class PerformanceProcessor implements NativeProcessor.PoolCallback {
//
//		Long blobControlPointer = null;
//
//		public void process(int idx, image_pool pool, long timestamp,
//				NativeProcessor nativeProcessor) {
//
//			if (blobData == null)
//				Log.e(TAG, "BLOB DATA NULL***");
//			// Log.e(TAG, "knn address: " + blobData[1]);
//			if (blobControlPointer == null && !(blobData == null))
//				blobControlPointer = processor.getBlobControl(blobData[1]);// knn
//
//			//resultReference is a pointer to a list of results which we can loop through
//			long resultReference = processor.detectNewBlobs(blobControlPointer,
//					idx, pool, satThreshold, valThreshold);
//
//			/** Critical Area for detecting the blobs
//			*   Here we will be obtaining updates about the state of the system
//			*   We will need to update SystemState and then trigger the messages to be sent
//			*/
//			//<Blob ID> <Enter/Exit/See> <Blob Area> <others...> <Separator (-1)>
//			long currentValue;
//			int resultLength = processor.getLongLength(resultReference);
//			//This list stores all the system state changes
//			ArrayList<ArrayList<Long>> results = new ArrayList<ArrayList<Long>>();
//			//This list stores the current state change
//			ArrayList<Long> tempList = new ArrayList<Long>();
//			for(int i = 1; i < resultLength; i++){
//				currentValue = processor.getLongVal(resultReference, i);
//				if(currentValue == -1){ //it's a seperator
//					results.add(tempList);
//					tempList = new ArrayList<Long>();
//				}
//				else{
//					tempList.add(currentValue);
//				}
//			}
//			
//			//Just print a subset of what's going on for reference!
//			//Log.e(TAG, "Feedback Size: " + resultLength);
//			for(int i = 0; i < results.size(); i++){
//				String str = "BlobID: " + results.get(i).get(0) + ", Action: " + results.get(i).get(1);
//				if(results.get(i).size() >= 3) //area
//					str += ", " + results.get(i).get(2);
//				if(results.get(i).size() >= 4)
//					str += ", " + results.get(i).get(3);
//				Log.e(TAG, str);
//			}
//			Log.e(TAG, "-----------");
//			
//			
//			
//			if(mSystemState != null){
//				mSystemState.postStateChange(results);
//			}
//			
//			
//			
//			
//			
//			// mPreview.pauseSurface(); not sure why this is here
//		}
//	}

	@Override
	protected void onPause() {
		super.onPause();
		// clears the callback stack
//		mPreview.onPause();
//		glview.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();
//		glview.onResume();

		// add an initiall callback stack to the preview on resume...
		// this one will just draw the frames to opengl
//		LinkedList<NativeProcessor.PoolCallback> cbstack = new LinkedList<PoolCallback>();
//		cbstack.add(glview.getDrawCallback());
//		mPreview.addCallbackStack(cbstack);
//		mPreview.onResume();

	}
	
	@Override
	public void onBackPressed() {
		mSystemState.running = false;
		mSystemState.oscHandler.running = false;
		PersistantObjects.setSystemState(mSystemState);
		mSystemState.oscHandler.sendStop();
		processor.delete();
		PerformanceActivity.this.finish();
		return;

		
	}

}
