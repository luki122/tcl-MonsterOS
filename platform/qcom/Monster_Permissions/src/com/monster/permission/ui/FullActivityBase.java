package com.monster.permission.ui;

import mst.app.MstActivity;
import android.annotation.TargetApi;
import android.app.Activity;
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
		showBackIcon(true);
	}
	
	@Override
	public void setContentView(int layoutResID) {
		super.setMstContentView(layoutResID);
	}	
	
	public void onNavigationClicked(View view){
		finish();
	}
}
