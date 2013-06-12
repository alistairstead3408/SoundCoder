package stead.alistair.com.soundcoder;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opencv.camera.NativePreviewer;
import com.opencv.camera.NativeProcessor;
import com.opencv.camera.NativeProcessor.PoolCallback;
import com.opencv.jni.Mat;
import com.opencv.jni.PtrMat;
import com.opencv.jni.image_pool;
import com.opencv.opengl.GL2CameraViewer;

import stead.alistair.com.soundcoder.jni.cvcamera;
import stead.alistair.com.soundcoder.jni.Processor;

// ----------------------------------------------------------------------

public class TrainingActivity extends Activity {

	private LinearLayout linearLayout;
	private FrameLayout frameLayout;
	private String TAG = "TrainingActivity";
	private TextView tv;
	private int mState;
	private final static int STARTING = 0, FOCUS = 1, PICTURE_COMPLETED = 2,
			RETURN_DATA = 3;

	private NativePreviewer mPreview;
	private int satThreshold = 60; //40
	private int valThreshold = 60;

	static final int DIALOG_CALIBRATING = 0;
	static final int DIALOG_CALIBRATION_FILE = 1;
	private static final int DIALOG_OPENING_TUTORIAL = 2;
	private static final int DIALOG_TUTORIAL_FAST = 3;
	private static final int DIALOG_TUTORIAL_BLOBS = 4;
	public boolean captureLineHueValues = false;
	public TrainingProcessor trainingProcessor;
	long oldknnVal = 0;

	void toasts(int id) {
		switch (id) {
		case DIALOG_OPENING_TUTORIAL:
			Toast.makeText(this, "Try clicking the menu for CV options.",
					Toast.LENGTH_LONG).show();
			break;
		case DIALOG_TUTORIAL_FAST:
			Toast.makeText(this, "Detecting and Displaying FAST features",
					Toast.LENGTH_LONG).show();
			break;
		case DIALOG_TUTORIAL_BLOBS:
			Toast.makeText(this, "Attempting to draw labels", Toast.LENGTH_LONG)
					.show();
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
		
		if(this.getIntent().getBundleExtra("blobData")!= null){
			long [] longBlobData = this.getIntent().getBundleExtra("blobData").getLongArray("bundleBlobData");
			if(longBlobData.length > 1)
			    oldknnVal = longBlobData[1]; //this is the knn
		}

		// Set up the relative layout so we can overlay the crayon
		mPreview = new NativePreviewer(getApplication(), 640, 480);
		frameLayout = new FrameLayout(this);
		// This is actually a surface view

		mState = TrainingActivity.STARTING;
		tvHandler.obtainMessage(mState).sendToTarget();

		frameLayout.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent me) {
				switch (me.getAction()) {
				case MotionEvent.ACTION_DOWN:
					switch (mState) {
					case TrainingActivity.FOCUS:
						mPreview.postautofocus(0);
						break;
					}
					break;
				case MotionEvent.ACTION_MOVE:
					switch (mState) {

					}
					break;
				case MotionEvent.ACTION_UP:
					switch (mState) {
					// This is the initial state
					case TrainingActivity.STARTING:
						LinkedList<PoolCallback> defaultcallbackstack = new LinkedList<PoolCallback>();
						defaultcallbackstack.addFirst(glview.getDrawCallback());
						trainingProcessor = new TrainingProcessor();
						defaultcallbackstack.addFirst(trainingProcessor);
						toasts(DIALOG_TUTORIAL_BLOBS);
						mPreview.addCallbackStack(defaultcallbackstack);
						break;
					// This is where the user is taking the proper picture.
					case TrainingActivity.FOCUS:
						// This is where the blob tracking and classifier
						// training should take place
						captureLineHueValues = true;
						// TODO stop autofocus on camera
						break;
					case TrainingActivity.PICTURE_COMPLETED:
						// show the user the number of blobs
						while (!trainingProcessor.finishedProcessing) {
						}
						tvHandler.obtainMessage(TrainingActivity.RETURN_DATA,
								trainingProcessor.finalBlobData).sendToTarget();
						break;
					case MotionEvent.ACTION_POINTER_DOWN:

						Log.e(TAG, "ACTION_POINTER_DOWN");
						break;
					}

					if (mState < TrainingActivity.PICTURE_COMPLETED)
						mState++;
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
		vidlay.addView(mPreview, params);
		frameLayout.addView(vidlay);

		// make the glview overlay ontop of video preview
		mPreview.setZOrderMediaOverlay(false);

		glview = new GL2CameraViewer(getApplication(), false, 0, 0);
		glview.setZOrderMediaOverlay(true);
		glview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		// Add the TextView via a LinearLayout
		linearLayout = new LinearLayout(getApplicationContext());
		linearLayout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		linearLayout.addView(tv);

		frameLayout.addView(glview);
		frameLayout.addView(linearLayout);
		frameLayout.setKeepScreenOn(true);
		setContentView(frameLayout);
		toasts(DIALOG_OPENING_TUTORIAL);
		// DrawOnTop gives us the ability to draw lines over the NativePreviewer
		// dOT = new DrawOnTop(this);
		// addContentView(dOT, new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT));

	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (event.getX() != 0) {
				if (event.getX() > 0) {
					if (satThreshold < 255)
						satThreshold++;
				} else {
					if (satThreshold > 0)
						satThreshold--;
				}
			} else if (event.getY() != 0) {
				if (event.getY() > 0) {
					if (valThreshold < 255)
						valThreshold++;
				} else {
					if (valThreshold > 0)
						valThreshold--;
				}
			} else
				break;
		}
		Log.e(TAG, "Sat:" + satThreshold + ", Val:" + valThreshold);

		return true;
	}

	private final Handler tvHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.e(TAG, "HANDLER");
			switch (msg.what) {
			case TrainingActivity.STARTING:
				tv.setText("Draw one blob per colour and focus the camera on them. Tap to continue.");
				break;
			case TrainingActivity.FOCUS:
				tv.setText("Tap on the screen when only blobs are visible.");
				break;
			case TrainingActivity.PICTURE_COMPLETED:
				tv.setText("Thank you! Tap for the main menu!");
				break;
			case TrainingActivity.RETURN_DATA:
				Log.e(TAG, "Return Data");
				Long[] result = (Long[]) msg.obj;
				Bundle bundle = new Bundle();
				long[] longResult = new long[result.length];
				for (int i = 0; i < result.length; i++)
					longResult[i] = result[i];
				bundle.putLongArray("BlobData", longResult);
				Intent intent = new Intent(TrainingActivity.this,
						SoundCoderActivity.class);
				intent.putExtra("bundleData", bundle);
				TrainingActivity.this.setResult(
						SoundCoderActivity.TRAINING_SUCCESS, intent);
				processor.delete();
				TrainingActivity.this.finish();
			}
		}
	};

	// final processor so that these processor callbacks can access it
	final Processor processor = new Processor();
	private GL2CameraViewer glview;

	class TrainingProcessor implements NativeProcessor.PoolCallback {

		public boolean finishedProcessing = false;
		public Long[] finalBlobData = null;

		public void process(int idx, image_pool pool, long timestamp,
				NativeProcessor nativeProcessor) {
			if(oldknnVal != 0){
				processor.deleteObject(oldknnVal);
				oldknnVal = 0;
			}
			if (!captureLineHueValues) // Make sure this is !
			{
				processor.filterBackground(idx, pool, satThreshold,
						valThreshold);
			} else {
				Log.e(TAG, "Doing blob stuff");
				long blobDataPointer = processor.getBlobLabels(idx, pool,satThreshold, valThreshold);

				int length = processor.getLongLength(blobDataPointer);
				Long[] blobData = new Long[length]; // all-in length

				for (int i = 0; i < length; i++) {
					blobData[i] = processor.getLongVal(blobDataPointer, i);
				}

				// now we've got the appropriate data back from JNI, return it
				// to SoundCoderActivity by sending it to the handler
				finalBlobData = blobData;

				// Long temp[] = new Long[1];
				// temp[0] = blobDataPointer;
				// finalBlobData = temp;
				// captureLineHueValues = false;

				mPreview.pauseSurface();
				finishedProcessing = true;
				
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		// clears the callback stack
		mPreview.onPause();
		glview.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();
		glview.onResume();

		// add an initiall callback stack to the preview on resume...
		// this one will just draw the frames to opengl
		LinkedList<NativeProcessor.PoolCallback> cbstack = new LinkedList<PoolCallback>();
		cbstack.add(glview.getDrawCallback());
		mPreview.addCallbackStack(cbstack);
		mPreview.onResume();

	}

}
