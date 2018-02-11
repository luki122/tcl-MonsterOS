package com.mst.wallpaper;

import android.content.Context;
/**
 * 
 *Base on MVP,we  declare this class to represent
 *V 
 * @param <D> data need to load or handle
 * @param <S> status when handle datas
 * @param <P> subPresenter for V
 */
public interface AbsView <D,S,P>{


	/**
	 * When data was handled ,call this method to update
	 * V's UI
	 * @param data
	 * @param status
	 */
	public void updateView(D data,S status);
	
	/**
	 * If take long time to handle data,show Progress Here
	 * @param progress
	 */
	public void updateProgress(S progress);
	
	/**
	 * If Presenter need Context,call this method to get it,suggest
	 * set ApplicationContext,instead of Activity
	 * @return
	 */
	public Context getViewContext();
	
}
