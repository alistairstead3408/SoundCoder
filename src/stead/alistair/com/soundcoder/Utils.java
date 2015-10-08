package stead.alistair.com.soundcoder;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.opencv.core.Mat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class Utils {

	public static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
	
	public static Bitmap cameraBitmap = null;
	public static Mat cameraMat = null;

	/** This is the directory used to store or search for images **/

	public class BundleFlags{
		public static final String FLAG_SITE_INDEX = "siteIndex";
		public static final String FLAG_PERSON_INDEX = "personIndex";
		public static final String FLAG_IMAGE_DATA = "imagedata";
		public static final String FLAG_QUALITY_FOCUS = "qualityfocus";
		public static final String FLAG_QUALITY_BRIGHTNESS = "qualitybrightness";
		public static final String FLAG_QUALITY_SIZE = "qualitysize";
		public static final String FLAG_USERNAME = "username";
		public static final String FLAG_PASSWORD = "password";
		public static final String FLAG_ERROR = "error";
		public static final String FLAG_JSON = "json";
	}
	public class IntentFlags{
		public static final int INTENT_CAMERA = 1;
		public static final int INTENT_CAMERA_CONFIRMATION = 2;
	}
	
	@SuppressWarnings("unused")
	private static final String TAG = "Utils";
	public static boolean firstRun = false;


	public static void showPopup(Context ctx, String message) {
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(ctx);
		TextView myMsg = new TextView(ctx);
		myMsg.setText(message);
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		popupBuilder.setView(myMsg);
	}



	/**
	 * Controls how log messages are shown in production vs. debug
	 * 
	 * @param myClass
	 * @param tag
	 * @param msg
	 */
	@SuppressWarnings("rawtypes")
	public static void debug(final Class myClass, final String tag,
			final String msg) {
		if (Log.isLoggable(tag, Log.DEBUG)) {
			Log.d(tag, myClass.getSimpleName().concat(":").concat(msg));
		}
	}
	
	public static void debugToast(Context ctx, final String tag, final String toastString, final int timeLength){
		if (Log.isLoggable(tag, Log.DEBUG)) {
			Toast.makeText(ctx, toastString, timeLength).show();
		}
	}

	// this is used to save the final bitmap. Due to orientation
	// if it's saved without rotation, it's incorrect
	public static Bitmap rotateBitmap(Bitmap b, float degrees) {
		Matrix m = new Matrix();
		if (degrees != 0) {
			// clockwise
			m.postRotate(degrees, (float) b.getWidth() / 2,
					(float) b.getHeight() / 2);
		}

		try {
			Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
					b.getHeight(), m, true);
			if (b != b2) {
				//b.recycle();
				b = b2;
			}
		} catch (OutOfMemoryError ex) {
			// We have no memory to rotate. Return the original bitmap.
		}
		return b;
	}

	// returns ratio as width/height
	public static double getImageRatio(Bitmap image) {
		return (double) image.getWidth() / (double) image.getHeight();
	}

	public static int dpToPixels(WeakReference<Context> c, int dp) {
		final float scale = c.get().getResources().getDisplayMetrics().density;
		int px = (int) (dp * scale + 0.5f);
		return px;
	}

	public static void broadcastGalleryUpdate(WeakReference<Context> c) {
		c.get().sendBroadcast(
				new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
						+ Environment.getExternalStorageDirectory())));
	}

	public static String getDayString(long timestamp) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(timestamp);
		String returnString = "" + cal.get(Calendar.DATE);
		return returnString;
	}

	public static String getTimeString(long timestamp) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(timestamp);
		String minute = "" + cal.get(Calendar.MINUTE);
		if (minute.length() == 1)
			minute = "0" + minute;
		String returnString = "" + cal.get(Calendar.HOUR_OF_DAY) + ":" + minute;
		return returnString;
	}

	/** This isn't a conventional date **/
	public static String getDateString(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		String returnVal = "";
		switch (cal.get(Calendar.MONTH)) {
		case Calendar.JANUARY:
			returnVal += "Jan ";
			break;
		case Calendar.FEBRUARY:
			returnVal += "Feb ";
			break;
		case Calendar.MARCH:
			returnVal += "Mar ";
			break;
		case Calendar.APRIL:
			returnVal += "Apr ";
			break;
		case Calendar.MAY:
			returnVal += "May ";
			break;
		case Calendar.JUNE:
			returnVal += "Jun ";
			break;
		case Calendar.JULY:
			returnVal += "Jul ";
			break;
		case Calendar.AUGUST:
			returnVal += "Aug ";
			break;
		case Calendar.SEPTEMBER:
			returnVal += "Sep ";
			break;
		case Calendar.OCTOBER:
			returnVal += "Oct ";
			break;
		case Calendar.NOVEMBER:
			returnVal += "Nov ";
			break;
		case Calendar.DECEMBER:
			returnVal += "Dec ";
			break;
		}
		String year = "" + cal.get(Calendar.YEAR);
		year = "'" + year.substring(year.length() - 2, year.length());
		returnVal += year;
		return returnVal;
	}

	public static String getFullDateAndTime(long timestamp) {
		return getTimeString(timestamp) + " " + getDayString(timestamp) + " "
				+ getDateString(timestamp);
	}



	/** recursively remove views. **/
	public static void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}
	
	
	
	
	
	public static class FlurryKeys{
		public final static String FLURRY_MOLE_ADD = "MoleAdd";
		public final static String FLURRY_IMAGE_ADD_WITH_OVERLAY = "ImageAddWithOverlay";
		public final static String FLURRY_IMAGE_ADD_NO_OVERLAY = "ImageAddNoOverlay";
		public final static String FLURRY_OPEN_CAMERA_FROM_GALLERY = "OpenCameraFromGallery";
		public final static String FLURRY_OPEN_CAMERA_FROM_HOME = "OpenCameraFromHome";
		public final static String FLURRY_OPEN_CAMERA_FROM_ALERT_NOTIFY = "OpenCameraFromAlertNotify";
	}
	
	public static boolean isNetworkAvailable(Context ctx) {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}

}
