package com.mst;

import com.android.incallui.InCallApp;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import com.android.contacts.common.util.PermissionsUtil;

public class MstPhoneUtils {
	private final static String TAG = "MstPhoneUtils";
	public static boolean isSimulate() {
		// SharedPreferences sP = null;
		// sP = InCallApp.getInstance().getSharedPreferences(
		// "com.android.phone_preferences", Context.MODE_PRIVATE);
		// return sP != null && sP.getBoolean("simulate_switch", false);
		return true;
	}

	/**
	 * Given a bitmap, returns a drawable that is configured to display the
	 * bitmap based on the specified request.
	 */
	public static Drawable getDrawableForBitmap(Drawable d) {
		Resources resources = InCallApp.getInstance().getResources();
		Bitmap bitmap = drawableToBitamp(d);
		final RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory
				.create(resources, bitmap);
		drawable.setAntiAlias(true);
		drawable.setCornerRadius(bitmap.getHeight() / 2);
		return drawable;

	}

	private static Bitmap drawableToBitamp(Drawable drawable) {
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();
		Log.i(TAG, " drawableToBitamp w = " + w);
		Log.i(TAG, "drawableToBitamp  h = " + h);
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		drawable.draw(canvas);
		return bitmap;
	}
	
	 public static boolean isShowFullScreenWhenRinging() {
	        boolean isShowFullScreen = false;
	        try {  		   		  
	 		    ActivityManager am = (ActivityManager) InCallApp.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
	 		    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
	 	        Log.i("isShowFullScreenWhenRinging", "topActiviy = " +  cn.getClassName());
	 	        if(cn.getClassName().contains("InCallActivity") 
	 	        		|| cn.getClassName().contains("com.monster.launcher")
//	 	        		|| cn.getClassName().contains("com.android.contacts")
	 	        		|| cn.getClassName().contains("com.android.camera")) {
	 	        	isShowFullScreen = true;
	 	        }
	 	    } catch (Exception e) {
	 		   e.printStackTrace();
	 	    }
	        
	        KeyguardManager keyguardManager = (KeyguardManager) InCallApp.getInstance().getSystemService(Context.KEYGUARD_SERVICE);
	        if(keyguardManager.isKeyguardLocked()) {
		        	isShowFullScreen = true;
		     }
	        String WINDOW_PERMISSION = "android.permission.SYSTEM_ALERT_WINDOW";
			 boolean hasWindowPermission = PermissionsUtil.hasPermission(InCallApp.getInstance(),WINDOW_PERMISSION);
	        Log.i(TAG, "isShowFullScreenWhenRinging = " + isShowFullScreen );
	        Log.i(TAG, "hasWindowPermission = " + hasWindowPermission );
	        return isShowFullScreen || !hasWindowPermission;
	    }
}