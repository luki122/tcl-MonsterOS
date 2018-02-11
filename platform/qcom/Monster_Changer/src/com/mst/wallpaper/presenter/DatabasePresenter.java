package com.mst.wallpaper.presenter;

import java.util.List;

public interface DatabasePresenter<T> {
	
	public List<T> queryAll();
	
	public T queryById(int id);

	public T queryByName(String name);
	
	public boolean insert(T data);
	
	public void update(T data);
	
	public void delete(T data);
	
	
}
