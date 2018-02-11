package com.mst.wallpaper.presenter;

import java.util.ArrayList;

import mst.utils.DisplayUtils;
import mst.widget.PagerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mst.wallpaper.adapter.WallpaperPreviewAdapter;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.Config;

public class PreviewPresenter implements PreviewContract.Presenter {

	private static final String TAG = "Preview";
	private PreviewContract.View mView;
	private Intent mIntent;

	private ArrayList<Wallpaper> mWallpaper;

	private WallpaperPreviewAdapter mAdapter;

	private int mWallpaperType;

	private int mCurrentPosition;

	private ImageResizer mImageResizer;

	private Context mContext;
	
	public PreviewPresenter(PreviewContract.View view, Intent intent) {
		mView = view;
		mIntent = intent;
		mView.setPresenter(this);
		mContext = mView.getViewContext();
		initImageCache(null);
	}

	private void initImageCache(String updatePath) {
		final int height = DisplayUtils.getHeightPixels(mContext);
		final int width = DisplayUtils.getWidthPixels(mContext);
		mImageResizer = new ImageResizer(mContext, width / 2, height / 2);
		mImageResizer.addImageCache((Activity)mContext,
				Config.WALLPAPER_PREVIEW_IMAGE_CACHE_DIR, updatePath);
	}

	@Override
	public void handleIntent() {
		// TODO Auto-generated method stub
		mWallpaperType = mIntent.getIntExtra(
				Config.Action.KEY_WALLPAPER_PREVIEW_TYPE, Wallpaper.TYPE_OTHER);
		mCurrentPosition = mIntent.getIntExtra(
				Config.Action.KEY_WALLPAPER_PREVIEW_POSITION, 0);
		mWallpaper = mIntent
				.getParcelableArrayListExtra(Config.Action.KEY_WALLPAPER_PREVIEW_DATA_LIST);
		if (Config.DEBUG) {
			Log.d(TAG, "position-->" + mCurrentPosition + "  type-->"
					+ mWallpaperType + "  list size-->"
					+ (mWallpaper != null ? mWallpaper.size() : 0));
		}
		if (mWallpaper != null) {
			mAdapter = new WallpaperPreviewAdapter(mView.getViewContext(),
					mWallpaper, mWallpaperType);
			mAdapter.setImageResizer(mImageResizer);
			mView.setCurrentItem(mCurrentPosition);
			mView.updateView(mAdapter);
		}
		

	}

}
