package com.mst.thememanager.ui.fragment.themedetail;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.mst.thememanager.BasePresenter;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.job.loader.LocalThemeLoader;
import com.mst.thememanager.job.loader.ThemeLoader;
import com.mst.thememanager.listener.OnThemeLoadedListener;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.TLog;

public class ThemePkgDetailPresenter extends BasePresenter<ThemePkgDetailMVPView> {

	private static final String TAG = "ThemeDetail";
	
	private Theme mCurrentTheme;
	
	private Context mContext;
	
	private String mPreviewDir;
	
	public ThemePkgDetailPresenter(Context context,Theme theme){
		mContext = context;
		mCurrentTheme = theme;
		initPreview();
	}
	
	private void initPreview(){
		StringBuilder builder = new StringBuilder();
		builder.append(mCurrentTheme.loadedPath);
		builder.append(File.separatorChar);
		builder.append(Config.LOCAL_THEME_PREVIEW_DIR_NAME);
		mPreviewDir = builder.toString();
	}
	
	public void loadThemePreview(){
		mCurrentTheme.previewArrays.clear();
		final String previewPath = mPreviewDir;
		File file = new File(previewPath);
		if(file.exists()){
			String[] images = file.list();
			if(images != null){
				for(String s:images){
					mCurrentTheme.previewArrays.add(previewPath+s);
				}
			}
		}
		getMvpView().updateThemeInfo(mCurrentTheme);
	}
	
	public void updateTheme(){
		
	}
	
	
	
	
	public void updateThemeInfo(){
		getMvpView().updateThemeInfo(mCurrentTheme);
	}
	
	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
		
	}
	
	
}
