package com.mst.thememanager.state;

import android.content.Context;

import com.mst.thememanager.entities.Theme;

public interface DonwloadOption {
	public  void start();

	public  void pause();

	public  void stop();

	public  void resume();

	public  void apply();

	public  void update();
	
	public void setTheme(Theme theme);

	public void showDialog(Context context,int dialogId);
	
	public void dismissDialog();
	
}
