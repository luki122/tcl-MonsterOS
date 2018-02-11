package com.mst.wallpaper;

public interface ActivityView<T> {
	
	public void updateView();
	
	public void setPresenter(T presenter);

}
