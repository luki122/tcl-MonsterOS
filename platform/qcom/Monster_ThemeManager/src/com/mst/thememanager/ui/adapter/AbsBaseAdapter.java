package com.mst.thememanager.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import mst.utils.DisplayUtils;

import com.android.gallery3d.util.ImageResizer;
import com.android.gallery3d.util.ImageWorker.ImageLoaderCallback;
import com.mst.thememanager.ThemeManager;
import com.mst.thememanager.ThemeManagerImpl;
import com.mst.thememanager.entities.Theme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class AbsBaseAdapter<T extends Theme> extends BaseAdapter {

	private List<T> mThemes = new ArrayList<T>();
	private Context mContext;
	private LayoutInflater mInflater;
	private ImageResizer mResizer;
	private ThemeManager mThemeManager;
	public AbsBaseAdapter(Context context){
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mThemeManager = ThemeManagerImpl.getInstance(context);
	}
	
	
	
	public void setImageResizer(ImageResizer resizer){
		mResizer = resizer;
	}
	
	public ImageResizer getImageResizer(){
		return mResizer;
	}
	
	public void addTheme(T theme){
		if(!mThemes.contains(theme)){
			mThemes.add(theme);
			notifyDataSetChanged();
		}
	}
	
	public void removeTheme(int position){
		mThemes.remove(position);
		notifyDataSetChanged();
	}
	
	public void removeTheme(T theme){
		if(mThemes.contains(theme)){
			mThemes.remove(theme);
			notifyDataSetChanged();
		}
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mThemes.size();
	}
	
	public T getTheme(int position){
		return getItem(position);
	}
	
	public boolean themeApplied(Theme theme){
		return mThemeManager.themeApplied(theme);
	}
	
	public Context getContext(){
		return mContext;
	}
	
	public List<T> getThemes(){
		return mThemes;
	}
	
	public LayoutInflater getInflater(){
		return mInflater;
	}
	
	public View inflate(int resource,ViewGroup root){
		return getInflater().inflate(resource, root);
	}
	
	@Override
	public T getItem(int position) {
		// TODO Auto-generated method stub
		return mThemes.get(position);
	}
	
	

}
