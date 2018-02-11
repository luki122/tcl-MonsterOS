package com.mst.wallpaper.activity;

import java.util.ArrayList;

import mst.app.dialog.AlertDialog;
import mst.utils.DisplayUtils;
import mst.widget.PagerAdapter;
import mst.widget.ViewPager;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.mst.wallpaper.ActivityPresenter;
import com.mst.wallpaper.MainWorker.OnRequestListener;
import com.mst.wallpaper.R;
import com.mst.wallpaper.adapter.WallpaperPreviewAdapter;
import com.mst.wallpaper.fragment.DesktopWallpaperPreviewFragment;
import com.mst.wallpaper.fragment.KeyguardWallpaperPreviewFragment;
import com.mst.wallpaper.fragment.PreviewFragment;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.presenter.PreviewContract;
import com.mst.wallpaper.presenter.PreviewContract.Presenter;
import com.mst.wallpaper.presenter.PreviewPresenter;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.ToastUtils;
import com.mst.wallpaper.utils.WallpaperManager;
import com.mst.wallpaper.utils.loader.IconPreviewLoader;
public class WallpaperPreviewActivity extends Activity implements OnMenuItemClickListener
,OnRequestListener<Object, Wallpaper>{
	private static final String TAG = "WallpaperPreview";
	private PreviewFragment mFragment;
	private Toolbar mToolbar;
	private WallpaperManager mWallpaperManager;
	
	private Wallpaper mCurrentWallpaper;
	
	private int mCurrentPosition = -1;
	
	private int mWallpaperType = Wallpaper.TYPE_OTHER;
	
	
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {
			case Config.SetWallpaperStatus.STATUS_FAILED:
				ToastUtils.showLongToast(WallpaperPreviewActivity.this, getResources().getString(R.string.tip_apply_wallpaper_failed));
				break;
			case Config.SetWallpaperStatus.STATUS_SUCCESS:
				ToastUtils.showLongToast(WallpaperPreviewActivity.this, getResources().getString(R.string.tip_apply_wallpaper_success));
				WallpaperPreviewActivity.this.finish();
				break;
			case Config.SetWallpaperStatus.STATUS_WALLPAPER_APPLIED:
				ToastUtils.showLongToast(WallpaperPreviewActivity.this, getResources().getString(R.string.tip_current_wallpaper_is_applied));
				break;
			case Config.SetWallpaperStatus.STATUS_UNKOWNE_WALLPAPER_TYPE:
				ToastUtils.showLongToast(WallpaperPreviewActivity.this, getResources().getString(R.string.tip_unknown_wallpaper));
				break;
			}
			
		};
		
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mWallpaperType = getIntent().getIntExtra(Config.Action.KEY_WALLPAPER_PREVIEW_TYPE, Wallpaper.TYPE_OTHER);
			getWindow().getDecorView().setSystemUiVisibility(
	                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
	                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
			getWindow().setNavigationBarColor(Color.TRANSPARENT);
		
		super.onCreate(savedInstanceState);
		mWallpaperManager = WallpaperManager.getInstance();
		mWallpaperManager.addWallpaperHandleListener(this.getClass().getName(), this);
		setContentView(R.layout.desktop_wallpaper_preview_title);
		mToolbar = (Toolbar)findViewById(R.id.toolbar);
		
		mToolbar.setOnMenuItemClickListener(this);
		
		if(mWallpaperType == Wallpaper.TYPE_DESKTOP){
			mFragment = new DesktopWallpaperPreviewFragment();
		}else if(mWallpaperType == Wallpaper.TYPE_KEYGUARD ){
			mFragment = new KeyguardWallpaperPreviewFragment();
		}else{
			finish();
		}
		mToolbar.setNavigationOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
		if(mFragment != null){
			mFragment.setToolbar(mToolbar);
			getFragmentManager().beginTransaction()
			.replace(R.id.content, mFragment)
			.commit();
			mFragment.postIntent();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mWallpaperManager.removeWallpaperHandleListener(this.getClass().getName(), this);
	}
	
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == R.id.wallpaper_set_done){
			if(mCurrentWallpaper != null){
			if(mWallpaperType == Wallpaper.TYPE_DESKTOP ){
				mCurrentWallpaper.type = Wallpaper.TYPE_DESKTOP;
			}else if(mWallpaperType == Wallpaper.TYPE_KEYGUARD){
				mCurrentWallpaper.type = Wallpaper.TYPE_KEYGUARD;
			}
			mWallpaperManager.applyWallpaper(mCurrentWallpaper, this, this.getClass().getName(), mCurrentPosition);
				
			}
		}
		return false;
	}
	
	
	/**
	 * 设置当前预览的壁纸
	 * @param wallpaper  当前预览的壁纸
	 * @param currentPosition  当前预览壁纸的位置
	 */
	public void setCurrentWallpaper(Wallpaper wallpaper,int currentPosition){
		this.mCurrentWallpaper = wallpaper;
		mCurrentPosition = currentPosition;
	}


	@Override
	public void onSuccess(Wallpaper wallpaper, Object obj,
			int statusCode) {
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(statusCode);
		
	}

	@Override
	public void onStartRequest(Wallpaper wallpaper, int statusCode) {
		
		
	}





	
}
