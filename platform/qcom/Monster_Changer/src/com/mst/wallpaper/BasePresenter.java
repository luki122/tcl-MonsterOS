package com.mst.wallpaper;

public interface BasePresenter {

	void start();
	
	void onStart();
	
	void onFinish(Object... results);
}
