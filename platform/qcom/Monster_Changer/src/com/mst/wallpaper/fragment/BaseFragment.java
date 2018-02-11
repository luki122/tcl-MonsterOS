package com.mst.wallpaper.fragment;

import mst.widget.ActionModeListener;

import com.mst.wallpaper.BasePresenter;
import com.mst.wallpaper.BaseView;
import com.mst.wallpaper.adapter.WallpaperAdapter;

import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;

public abstract class BaseFragment extends Fragment implements ActionModeListener{
	
	protected static final int DIALOG_ID_DELETE_DESKTOP_WALLPAPER = 0 ;
	protected static final int DIALOG_ID_DELETE_KEYGUARD_WALLPAPER = 1;

	/**
	 * Current Mode is Edit or not,Edit means user can delete
	 * the Wallpaper selected
	 */
	private boolean mEditMode = false;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	
	
	
	public boolean isEditMode(){
		return mEditMode;
	}
	
	public void setEditMode(boolean editmode){
		mEditMode = editmode;
	}
	
	/**
	 * Enter Edit Mode,this situation will show Select UI
	 * @param position
	 */
	public abstract void enterEditMode(int position);
	
	/**
	 * Exit Edit Mode ,and hide Select UI
	 */
	public abstract void exitEditMode();

	public boolean onKeyDown() {
		// TODO Auto-generated method stub
		if(mEditMode){
			exitEditMode();
			return false;
		}
		return true;
	}
	
	
	/**
	 * Show a Dialog By ID
	 * @param id
	 */
	protected void showDialog(int id){
		Dialog dialog = onCreateDialog(id);
		if(dialog != null){
			dialog.show();
		}
	}
	
	protected void dismissDialog(int id){
		Dialog dialog = onCreateDialog(id);
		if(dialog != null){
			dialog.dismiss();
		}
	}
	
	/**
	 * Create a Dialog By ID,this Dialog will show by {@link #showDialog(int id)}
	 * @param id
	 * @return
	 */
	protected  Dialog onCreateDialog(int id){
		return null;
	}
	
	
	protected abstract void deleteWallpaper();


	
}
