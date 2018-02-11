package com.mst.wallpaper.adapter;

import android.util.Log;
import android.view.View;
import mst.widget.PagerAdapter;
import mst.widget.toolbar.Toolbar;

public abstract class AbsViewPagerAdapter extends PagerAdapter implements View.OnClickListener{

	private static final int DURATION = 200;
	protected Toolbar toolbar;
	private boolean mShowToolbar = true;
	public void setToolbar(Toolbar toolbar){
		this.toolbar = toolbar;
	}
	
	public void showToolbar(){
		mShowToolbar = !mShowToolbar;
//		toolbar.setVisibility(mShowToolbar?View.VISIBLE:View.GONE);
		int height = toolbar.getHeight();
		if(mShowToolbar){
			toolbar.animate().translationY(0).setDuration(DURATION).start();
		}else{
			toolbar.animate().translationY(-height).setDuration(DURATION).start();
		}
		
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		showToolbar();
	}
	
}
