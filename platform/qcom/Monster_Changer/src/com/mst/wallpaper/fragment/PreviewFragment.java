package com.mst.wallpaper.fragment;

import mst.widget.toolbar.Toolbar;
import android.app.Fragment;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

import com.mst.wallpaper.activity.WallpaperPreviewActivity;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.CommonUtil;
import com.mst.wallpaper.utils.Config;

public abstract class PreviewFragment extends Fragment {
	
	private Toolbar mToolbar;
	public abstract void postIntent();
	public void setToolbar(Toolbar toolbar){
		this.mToolbar = toolbar;
	}
	
	public Toolbar getToolbar(){
		return mToolbar;
	}
	
	
	protected WallpaperPreviewActivity getWallpaperActivity(){
		return ((WallpaperPreviewActivity)getActivity());
	}
	
	public abstract  Wallpaper getWallpaper(int position);
	
	protected void updateNavigationBar(int color){
		Window window = getActivity().getWindow();
		View decorView = window.getDecorView();
		CommonUtil.lightNavigationBar(window, decorView, color != Config.Color.COLOR_WHITE);
	}
	
	
	
	
}
