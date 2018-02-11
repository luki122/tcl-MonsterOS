package com.mst.wallpaper.presenter;

import java.util.List;

import com.mst.wallpaper.DatabaseView;
import com.mst.wallpaper.object.Wallpaper;

public interface WallpaperDatabaseContract {

	public interface View extends DatabaseView<Presenter, Wallpaper>{
		
		public void updateView(List<Wallpaper> wallpaperList);
		
	}
	
	public interface Presenter extends DatabasePresenter<Wallpaper>{
		
	}
}
