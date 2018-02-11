package com.mst.wallpaper.presenter;

import java.util.List;

import android.graphics.drawable.Drawable;

import com.mst.wallpaper.AbsView;
import com.mst.wallpaper.Presenter;
import com.mst.wallpaper.object.Wallpaper;

public interface PresenterBridge {
	
	public interface DrawableView extends AbsView<Drawable, Integer,DrawablePresenter>{
		
	}
	
	
	public interface DrawablePresenter extends Presenter{
		
		
	}
	
	public interface WallpaperView extends AbsView<List<Wallpaper>, Integer, WallpaperPresenter>{
		
	}
	
	public interface   WallpaperPresenter extends Presenter{
		
	}

}
