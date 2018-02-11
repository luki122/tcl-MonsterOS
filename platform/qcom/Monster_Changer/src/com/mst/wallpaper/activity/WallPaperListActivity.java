package com.mst.wallpaper.activity;

import mst.app.MstActivity;
import mst.widget.toolbar.Toolbar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.mst.wallpaper.R;
import com.mst.wallpaper.fragment.BaseFragment;
import com.mst.wallpaper.fragment.KeyguardWallpaperFragment;
import com.mst.wallpaper.fragment.WallpaperListFragment;
import com.mst.wallpaper.utils.Config;
public class WallPaperListActivity extends MstActivity {

	private Toolbar mToolbar;
	private FragmentManager mFragManager;
	private FragmentTransaction mTransation;
	private BaseFragment fragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		setMstContentView(R.layout.wallpaper_list_activity);
		mToolbar = getToolbar();
		mFragManager = getFragmentManager();
		mTransation = mFragManager.beginTransaction();
		Intent intent = getIntent();
		if(Config.Action.ACTION_DESKTOP_WALLPAPER_LIST.equals(intent.getAction())){
			fragment =new WallpaperListFragment();
		}else{
			fragment =new KeyguardWallpaperFragment();
		}
		setActionModeListener(fragment);
		mTransation.replace(com.mst.R.id.content, fragment);
		mTransation.commit();
	}
	
	@Override
	protected void initialUI(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.initialUI(savedInstanceState);
	}
	
	@Override
	public void onNavigationClicked(View view) {
		// TODO Auto-generated method stub
		onBackPressed();
	}
	
	
	
	
	public void setTitle(CharSequence title){
		if(!TextUtils.isEmpty(title)){
			mToolbar.setTitle(title);
		}
	}
	
	public Toolbar getToolbar(){
		
		return super.getToolbar();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean finish = true;
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish = fragment.onKeyDown();
		}
		return super.onKeyDown(keyCode, event) && finish;
	}
	
	
}
