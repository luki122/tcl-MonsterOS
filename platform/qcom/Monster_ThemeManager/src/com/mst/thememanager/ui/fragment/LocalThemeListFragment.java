package com.mst.thememanager.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.ui.adapter.LocalThemeListAdapter;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.TLog;
import com.mst.thememanager.views.ThemePreviewDonwloadButton;
import com.mst.thememanager.ui.fragment.themedetail.ThemePkgDetailFragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.mst.thememanager.R;
public class LocalThemeListFragment<ThemeListPresenter> extends AbsThemeFragment implements ThemeListMVPView,
OnItemClickListener{
	private static final String TAG = "ThemeList";
	private static final int MSG_ADD_NEW_THEME = 0;
	private GridView mContentView;
	private LocalThemeListPresenter mThemeListPresenter;
	private LocalThemeListAdapter mAdapter;
	private Handler mHandler = new Handler(){
	
		public void handleMessage(android.os.Message msg) {
			if(msg.what == MSG_ADD_NEW_THEME){
				Theme t = (Theme) msg.obj;
				mAdapter.addTheme(t);
			}
		};
	
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mThemeListPresenter = new LocalThemeListPresenter(getContext());
		mThemeListPresenter.attachView(this);
		mContentView = (GridView)inflater.inflate(R.layout.local_theme_pkg_layout, container,false);
		mAdapter = new LocalThemeListAdapter(getContext());
		mContentView.setAdapter(mAdapter);
		mThemeListPresenter.loadTheme();
		mContentView.setOnItemClickListener(this);
		return mContentView;
	}
	
	
	
	
	
	@Override
	public void updateThemeList(Theme theme) {
		// TODO Auto-generated method stub
		
		if(theme != null){
			if(theme.id == Config.DEFAULT_THEME_ID){
				theme.name = getResources().getString(R.string.default_theme_name);
				theme.description = getResources().getString(R.string.default_theme_description);
			}
			Message msg = new Message();
			msg.what = MSG_ADD_NEW_THEME;
			msg.obj = theme;
			mHandler.sendMessage(msg);
		}
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Theme theme = mAdapter.getTheme(position);
		if(theme != null){
			Bundle args = new Bundle();
			args.putParcelable(Config.BUNDLE_KEY.KEY_THEME_PKG_DETAIL, theme);
			startFragment(this, ThemePkgDetailFragment.class.getName(), true, theme.name, 0, args);
		}
	}
	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter.notifyDataSetChanged();
	}
	
	
	

}
