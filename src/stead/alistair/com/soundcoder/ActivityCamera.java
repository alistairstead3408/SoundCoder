package stead.alistair.com.soundcoder;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class ActivityCamera extends Activity implements CvCameraViewListener, PictureCallback, ShutterCallback{

	public static final String OVERLAY_ENABLED = null;
	public static final int OVERLAY_IMAGE_TAG = 0;

	/** UI Variables **/
	private String TAG = "cameraController";
	private Vibrator mVibrator = null;
	private ImageView mCameraButtonIV = null;

	
	/** Add a progressdialog to show messages **/
	ProgressDialog progressDialog;


	/** Any image data we get **/
	private Bitmap mImageBitmap = null;
	private String siteId = null;


	
	private int mMode = Mode.MODE_RUNNING;
	
	public class Mode{
		public static final int MODE_RUNNING = 1;
		public static final int MODE_CAPTURED = 2;
	}
	
	private Mat mRgba;
	private JavaCameraView mOpenCvCameraView;
	

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	    @Override
	    public void onManagerConnected(int status) {
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {	
	                Log.i(TAG, "OpenCV loaded successfully");
	
				//Load native library after(!) OpenCV initialization
			    System.loadLibrary("native_skin");
				mOpenCvCameraView.enableView();
				} break;
		        default:
		        {
		            super.onManagerConnected(status);
		        } break;
	        }
	    }
	};



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/** Set the params of the screen **/
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_camera);
		
			
		mOpenCvCameraView = (JavaCameraView) findViewById(R.id.camera_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
		
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		progressDialog = new ProgressDialog(this);

		/** Look for site info if it's there **/
		
//		Bundle mExtras = this.getIntent().getExtras();
//		if (mExtras != null) {
//			siteId =  (String) mExtras.get(stead.alistair.com.soundcoder.Utils.BundleFlags.FLAG_SITE_INDEX);
//		}
//		else{
//			Toast.makeText(this, "Lost track of person and site", Toast.LENGTH_LONG).show();
//			this.finish();
//		}
		
		
		

		mCameraButtonIV = (ImageView) this.findViewById(R.id.camera_take);
		mCameraButtonIV.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				stead.alistair.com.soundcoder.Utils.debug(ActivityCamera.class,TAG, "CameraButton");
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mCameraButtonIV.setImageResource(R.drawable.camera_button_down);
					break;
				case MotionEvent.ACTION_UP:
					mCameraButtonIV
							.setImageResource(R.drawable.camera_button);
					takePicture();
					break;
				}
				return true;
			}
		});
	}


	/**
	 * This method inserts image details in the database and stores the new
	 * image in the correct folder
	 */
	public void takePicture() {
		mOpenCvCameraView.takePicture(this, this);
	}
	

	public Mat onCameraFrame(Mat inputFrame) {
        inputFrame.copyTo(mRgba);
        EdgeDetector(mRgba.getNativeObjAddr());
        Log.d(TAG, "width" + mRgba.width() + ", h" + mRgba.height());
        return mRgba;
	}




	@Override
	protected void onPause() {
		if (mOpenCvCameraView != null && mMode == Mode.MODE_RUNNING)
            mOpenCvCameraView.disableView();
		super.onPause();
	}


	@Override
	protected void onResume() {
		super.onResume();
		if(mMode == Mode.MODE_RUNNING){
			mImageBitmap = null;
			//OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
			OpenCVLoader.initDebug(); //uses static libraries rather than OpenCV Manager
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        if (mOpenCvCameraView != null && mMode == Mode.MODE_RUNNING)
            mOpenCvCameraView.disableView();
	}
	

    


	public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
	}


	public void onCameraViewStopped() {
		mRgba.release();
		
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		if(requestCode == Utils.IntentFlags.INTENT_CAMERA_CONFIRMATION){
			switch(resultCode){
			case Activity.RESULT_OK:
				mMode = Mode.MODE_RUNNING;
				break;
			case Activity.RESULT_CANCELED:
				Log.e(TAG, "Attempting to finish()");

				mMode = Mode.MODE_RUNNING;
				
				//ActivityCamera.this.finish(); this causes an error
				break;
			}
				
		}
	}
	
	
	public native void FindFeatures(long matAddrGr, long matAddrRgba);
    
    public native void EdgeDetector(long matAddrRgba);
    
    public native boolean QALighting(long matAddrRgba);
    public native boolean QABlur(long matAddrRgba);



	public void onShutter() {
		// TODO Auto-generated method stub
		Log.e(TAG, "Shutter!");
	}



	public void onPictureTaken(byte[] jpeg, Camera c) {
		mRgba.release();
		mMode = Mode.MODE_CAPTURED;
		mImageBitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
		stead.alistair.com.soundcoder.Utils.cameraBitmap = mImageBitmap;
		stead.alistair.com.soundcoder.Utils.cameraMat = new Mat();
		org.opencv.android.Utils.bitmapToMat(mImageBitmap, Utils.cameraMat);
//		Intent qualityIntent = new Intent(ActivityCamera.this, ActivityQualityAssessment.class);
//		qualityIntent.putExtras(this.getIntent().getExtras());
//		this.startActivity(qualityIntent);
	}





}
