package com.mst.wallpaper;

public interface DatabaseView<P,D> {

	public void updateView(D data);
	
	public void setPresenter(P presenter);
	
}
