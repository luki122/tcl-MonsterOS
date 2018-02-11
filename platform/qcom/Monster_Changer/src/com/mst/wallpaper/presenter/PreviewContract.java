package com.mst.wallpaper.presenter;

import mst.widget.PagerAdapter;
import android.content.Context;

import com.mst.wallpaper.ActivityPresenter;
import com.mst.wallpaper.ActivityView;

public interface PreviewContract {
	
	public interface View extends ActivityView<Presenter>{
		public void updateView(PagerAdapter adapter);
		
		public void setCurrentItem(int currentItem);
		
		
		public Context getViewContext();
	}
	
	public interface Presenter extends ActivityPresenter{
		
	}

}
