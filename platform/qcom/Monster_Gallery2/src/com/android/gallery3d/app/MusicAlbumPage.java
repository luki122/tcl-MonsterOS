package com.android.gallery3d.app;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.android.gallery3d.app.PhotoPage.PhotoPageRootPane;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.glrenderer.FadeTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.MusicAlbumSlotRenderer;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SlotView;
import com.android.gallery3d.util.GalleryUtils;

class MusicAlbumInfo {
	public ArrayList<String> mSubPicture;
	public String mMainPic;
	public String mTitle;
}

public class MusicAlbumPage extends ActivityState implements
        SelectionManager.SelectionListener, GalleryActionBar.ClusterRunner,
        EyePosition.EyePositionListener, MediaSet.SyncListener {
	private boolean mIsActive = false;
	private int mMusicAlbumItemCnt;
	private GLView mRootPane;
	private MusicAlbumSlotRenderer mMusicAlbumView;
	private ArrayList<MusicAlbumInfo> mMusicDataItem = new ArrayList<MusicAlbumInfo>();

	 public class MusicAlbumRootPane extends GLView {
	        @Override
	        protected void onLayout(
	                boolean changed, int left, int top, int right, int bottom) {
	        			mMusicAlbumView.layout(0, 0, right - left, bottom - top);
	        }
	    }
	
	
	@Override
    public void onCreate(Bundle data, Bundle restoreState) {
		super.onCreate(data, restoreState);
		initializeViews();
		initializeDatas();
		Context context = mActivity.getAndroidContext();
//		startupSlidingPageView();
	}
	
	private void initializeViews() {
		mRootPane = new MusicAlbumRootPane();
		mMusicAlbumView = new MusicAlbumSlotRenderer(mActivity);
		

		mRootPane.addComponent(mMusicAlbumView);
	}
	
	private void initializeDatas() {
		int index;
		for (index = 0;index < 100;index++) {
			MusicAlbumInfo item = new  MusicAlbumInfo();
			item.mTitle = "My Music Album Title";
			item.mMainPic = "/sdcard/Pictures/Screenshots/111.png";
			mMusicDataItem.add(item);
		}
	}

	 @Override
	 public void onPause() {
		 super.onPause();
	      mIsActive = false;
	 }
	
	
	 @Override
	 public void onResume() {
		 super.onResume();
		 mIsActive = true;
		 
		 setContentPane(mRootPane);
	 }
	
	 public void getMusicAlbumData() {
		 
		 mMusicAlbumItemCnt = 10;
	 }
	 
	 private void startupSlidingPageView() {		 
		 Path path;
		 DataManager manager = mActivity.getDataManager();;
         path = Path.fromString(
                     manager.getTopSetPath(DataManager.INCLUDE_IMAGE));

         Bundle data = new Bundle();
         data.putString(SlideshowPage.KEY_SET_PATH, path.toString());
         data.putBoolean(SlideshowPage.KEY_RANDOM_ORDER, true);
         data.putBoolean(SlideshowPage.KEY_REPEAT, true);
         data.putBoolean(SlideshowPage.KEY_DREAM, true);
         mActivity.getStateManager().startState(SlideshowPage.class, data);         
	 }
	 
	@Override
	public void onMstToolbarNavigationClicked(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMstToolbarMenuItemClicked(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onSyncDone(MediaSet mediaSet, int resultCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEyePositionChanged(float x, float y, float z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doCluster(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectionModeChange(int mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectionChange(Path path, boolean selected) {
		// TODO Auto-generated method stub
		
	}
	
}