package stead.alistair.com.soundcoder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SoundCoderActivity extends Activity {

	private Button bTraining;
	private Button bProgram;
	private Button bPerformance;
	private Button bQuit;

	private String TAG = "SoundCoderActivity";

	public final static int TRAINING_SUCCESS = 1, TRAINING_FAILED = 2,
			PROGRAM = 3, CAPTURE = 4;

	private long[] blobData = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.soundcoderactivity);

		// Initialise UI Components
		bTraining = (Button) findViewById(R.id.bTraining);
		bProgram = (Button) findViewById(R.id.bProgram);
		bPerformance = (Button) findViewById(R.id.bCapture);
		bQuit = (Button) findViewById(R.id.bQuit);

		bTraining.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
//				Intent intent = new Intent(SoundCoderActivity.this,
//						TrainingActivity.class);
//				if (SoundCoderActivity.this.blobData != null) {
//					intent = new Intent(SoundCoderActivity.this,
//							TrainingActivity.class);
//					Bundle bundle = new Bundle();
//					bundle.putLongArray("bundleBlobData",SoundCoderActivity.this.blobData);
//					intent.putExtra("blobData", bundle);
//				}
//				
//				startActivityForResult(intent,
//						SoundCoderActivity.TRAINING_FAILED);
				
				SoundCoderActivity.this.startActivityForResult(new Intent(SoundCoderActivity.this, ActivityCamera.class), SoundCoderActivity.TRAINING_FAILED);

			}
		});
		
		
		

		bProgram.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if ( SoundCoderActivity.this.blobData != null ) { 
																			
					Intent intent = new Intent(SoundCoderActivity.this,
							ProgramActivity.class);
					Bundle bundle = new Bundle();
					bundle.putLongArray("bundleBlobData",
							SoundCoderActivity.this.blobData);
					intent.putExtra("blobData", bundle);
					startActivityForResult(intent, SoundCoderActivity.PROGRAM);
				} else{
					Intent intent = new Intent(SoundCoderActivity.this,
							ProgramActivity.class);
					startActivityForResult(intent, SoundCoderActivity.PROGRAM);
				}
					//Log.e(TAG, "Cannot start Program as no BlobData present");

			}
		});

		bPerformance.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// startActivityForResult(new Intent(SoundCoderActivity.this,
				// PerformanceActivity.class), SoundCoderActivity.CAPTURE);

				if (SoundCoderActivity.this.blobData != null) {
					Intent intent = new Intent(SoundCoderActivity.this,
							PerformanceActivity.class);
					Bundle bundle = new Bundle();
					bundle.putLongArray("bundleBlobData",
							SoundCoderActivity.this.blobData);
					intent.putExtra("blobData", bundle);
					startActivityForResult(intent, SoundCoderActivity.CAPTURE);
				} else
					Log.e(TAG, "Cannot start Program as no BlobData present");
			}
		});
		
		bQuit.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				SoundCoderActivity.this.finish();
			}
		});

		bProgram.setEnabled(true);
		bPerformance.setEnabled(false);
		/*
		OSCHandler oschandler = new OSCHandler(); 
		oschandler.start();
		Log.e(TAG, "OSC Started");
		for(int i = 0; i < 10000; i++){
			if(i % 3 == 0){
				oschandler.addVolumeEnvelopeMessage(1, 0.5f,99);
				oschandler.addFrequencyEnvelopeMessage(1, 0.5f,  "B5");
			}
			else if(i % 3 == 1){
				oschandler.addVolumeEnvelopeMessage(1, 0.5f,10);
				oschandler.addFrequencyEnvelopeMessage(1, 0.5f,  "E5");
			}
			else if(i % 3 == 2){
				oschandler.addVolumeEnvelopeMessage(1, 0.5f,99);
				oschandler.addFrequencyEnvelopeMessage(1, 0.5f,  "F5");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
*/
	}

	// Listen for results.
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// See which child activity is calling us back.
		switch (resultCode) {
		case SoundCoderActivity.TRAINING_FAILED:
			//bPerformance.setEnabled(true);
			//bTraining.setText("Re-train");
			break;
		case SoundCoderActivity.TRAINING_SUCCESS:
			//bPerformance.setEnabled(true);
			bProgram.setEnabled(true);
			bTraining.setText("Re-train");
			Bundle bundle = data.getBundleExtra("bundleData");
			long[] blobBundleData = bundle.getLongArray("BlobData");
			for (int i = 0; i < blobBundleData.length; i++) {
				Log.e(TAG, "" + blobBundleData[i]);
			}
			blobData = blobBundleData;
			break;
		case SoundCoderActivity.PROGRAM:
			bPerformance.setEnabled(true);
			break;
		case SoundCoderActivity.CAPTURE:
			Log.e(TAG, "Capture returned");
			break;
		}
	}

}