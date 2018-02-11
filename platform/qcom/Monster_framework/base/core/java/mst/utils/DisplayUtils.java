package mst.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class DisplayUtils {
	
	private DisplayUtils(){};
	
	/**
	 * Get current Screen width pixels
	 * @param context
	 * @return
	 */
	public static int getWidthPixels(Context context){
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	
	/**
	 * Get current Screen height pixels
	 * @param context
	 * @return
	 */
	public static int getHeightPixels(Context context){
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	
	/**
	 * Get the logical density of the display
	 * @param context
	 * @return
	 */
	public static float getDensity(Context context){
		return context.getResources().getDisplayMetrics().density;
	}
	
	/**
     * The screen density expressed as dots-per-inch.  May be either
     * 
	 * @param context
	 * @return {@link #DENSITY_LOW}, {@link #DENSITY_MEDIUM}, or {@link #DENSITY_HIGH}.
	 */
	public static float getDensityDpi(Context context){
		return context.getResources().getDisplayMetrics().densityDpi;
	}
	
	/**
	 * Transparent system statusbar
	 * @param activity
	 */
	public static void transparentStatusBar(Activity activity){
		Window window = activity.getWindow();  
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS  
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);  
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  
        window.setStatusBarColor(Color.TRANSPARENT);  
	}

}
