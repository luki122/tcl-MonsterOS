package com.monster.appmanager;

import mst.app.MstActivity;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullActivityBase extends MstActivity{
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setTitle(getTitle());
		showBackIcon(!(this instanceof MainActivity));
		getWindow().getDecorView().setBackgroundColor(0xFFFFFFFF);
//		getToolbar().setTitleTextAppearance(this, R.style.ToolBarTitleTextAppearance);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
	public void setContentView(int layoutResID) {
		super.setMstContentView(layoutResID);
	}	
	
	public void onNavigationClicked(View view){
		if(!(this instanceof MainActivity)) {
			finish();
		}
	}
}
