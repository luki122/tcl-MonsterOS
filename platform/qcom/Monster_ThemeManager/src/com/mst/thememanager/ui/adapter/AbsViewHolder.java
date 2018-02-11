package com.mst.thememanager.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.mst.thememanager.entities.Theme;

public abstract class AbsViewHolder<T extends Theme> {
	private Context mContext;
	private AbsBaseAdapter<T> mAdapter;
	public AbsViewHolder(Context context,AbsBaseAdapter<T> adapter){
		mContext = context;
		mAdapter = adapter;
	}
	
	public abstract void holdConvertView(View convertView);

	public abstract void bindDatas(int position,List<T> themes);
	
	public Context getContext(){
		return mContext;
	}
	
	public  AbsBaseAdapter<T> getAdapter(){
		return mAdapter;
	}
	
}
