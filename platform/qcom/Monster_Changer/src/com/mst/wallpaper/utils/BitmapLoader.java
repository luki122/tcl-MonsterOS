package com.mst.wallpaper.utils;

import com.mst.wallpaper.BasePresenter;

public interface BitmapLoader {
	
	public void loadBitmap(String path);
	
	public void setPresenter(BasePresenter presenter);

}
