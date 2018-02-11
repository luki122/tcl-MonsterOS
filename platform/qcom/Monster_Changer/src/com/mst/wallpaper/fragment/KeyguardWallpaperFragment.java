package com.mst.wallpaper.fragment;

import java.util.ArrayList;
import java.util.List;

import mst.app.dialog.AlertDialog;
import mst.utils.DisplayUtils;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.ActionMode;
import mst.widget.ActionMode.Item;
import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerViewItemEmptySpace;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;

import com.mst.wallpaper.BasePresenter;
import com.mst.wallpaper.activity.SetDesktopWallpaperActivity;
import com.mst.wallpaper.activity.SetKeyguardWallpaperActivity;
import com.mst.wallpaper.activity.WallPaperListActivity;
import com.mst.wallpaper.adapter.KeyguardWallpaperListAdapter;
import com.mst.wallpaper.adapter.WallpaperAdapter.OnItemClickListener;
import com.mst.wallpaper.adapter.WallpaperAdapter.OnSliderDeleteListener;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.IntentUtils;
import com.mst.wallpaper.utils.ToastUtils;
import com.mst.wallpaper.utils.WallpaperManager;
import com.mst.wallpaper.widget.RecycleViewDivider;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mst.wallpaper.R;
public class KeyguardWallpaperFragment extends BaseFragment implements OnMenuItemClickListener
,OnSliderDeleteListener,OnItemClickListener{

	
	private WallPaperListActivity mActivity;
	
	private KeyguardWallpaperListAdapter mAdapter;
	
	private RecyclerView mList;
	
	private View mContentView;
	
	private ImageResizer mImageResizer;
	
	private Toolbar mToolbar;
	
	private BottomNavigationView mBottomBar;
	
	private WallpaperManager mWallpaperManager;
	
	private boolean mUpdateFromPicker;
	
	private boolean mDeleteFromSlider = false;
	
	private boolean mSelectAll = false;
	
	private int mDeletePositionFromSlider = -1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mWallpaperManager = WallpaperManager.getInstance();
		mContentView = inflater.inflate(R.layout.keyguard_wallpaper_list_fragment, container,false);
		initImageCache();
		return mContentView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mActivity = (WallPaperListActivity) getActivity();
		mList = (RecyclerView)mContentView.findViewById(R.id.recycler_view);
	    mList.getItemAnimator().setSupportsChangeAnimations(false);
		mList.setLayoutManager(new LinearLayoutManager(getActivity()	, LinearLayoutManager.VERTICAL, false));
		mList.addItemDecoration(new RecyclerViewItemEmptySpace(getResources().getDimensionPixelSize(R.dimen.keyguard_wallpaper_list_item_spacer)));
		mBottomBar = (BottomNavigationView)mContentView.findViewById(R.id.bottom_navigation);
		mBottomBar.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
			
			@Override
			public boolean onNavigationItemSelected(MenuItem item) {
				if(item.getItemId() == R.id.menu_item_delete_wallpaper){
					if(isEditMode()){
						List<Wallpaper> deleteDatas = mAdapter.getSelectedItems();
						if( deleteDatas.size() > 0){
							showDialog(DIALOG_ID_DELETE_KEYGUARD_WALLPAPER);
						}else{
							ToastUtils.showShortToast(getContext(), getResources().getString(R.string.tip_select_wallpaper));
						}
						
					}
				}
				return true;
			}
		});
		mActivity.setTitle(getString(R.string.keyguard_wallpaper));
		mToolbar = mActivity.getToolbar();
		mToolbar.setOnMenuItemClickListener(this);
		mAdapter = new KeyguardWallpaperListAdapter(getActivity(),mWallpaperManager);
		mAdapter.setSlideDeleteListener(this);
		mAdapter.setOnItemClickListener(this);
		mAdapter.setImageResizer(mImageResizer);
		mList.setAdapter(mAdapter);
	}
	
    private void initImageCache() {
        final int height = DisplayUtils.getHeightPixels(getContext());
        final int width = DisplayUtils.getWidthPixels(getContext());
        mImageResizer = new ImageResizer(getContext(), width / 2, height / 2, true);
        mImageResizer.addImageCache(getActivity(), Config.KEYGUARD_WALLPAPER_CACHE_DIR);
        mImageResizer.setLoadingImage(R.drawable.img_loading);
    }

    
	
	@Override
	protected Dialog onCreateDialog(int id) {
		int deleteCount =mAdapter.getSelectedItems().size();
		String msg;
		if(mDeleteFromSlider){
			msg = getResources().getString(R.string.delete_keyguard_current_wallpaper_msg);
		}else{
			msg = getResources().getString(R.string.delete_keyguard_wallpaper_msg, mAdapter.getSelectedItems().size());
		}
		if(id == DIALOG_ID_DELETE_KEYGUARD_WALLPAPER){
			AlertDialog alert = new AlertDialog.Builder(getContext())
			.setTitle(R.string.delete_dialog_title)
			.setMessage(msg)
			.setPositiveButton(R.string.confirm_ok,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(mDeleteFromSlider){
						deleteWallpaperBySlide();
					}else{
						deleteWallpaper();
					}
					
					dialog.dismiss();
				}
			}).setNegativeButton(R.string.confirm_cancel,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					exitEditMode();
					dialog.dismiss();
				}
			}).create();
			return alert;
		}
		return null;
	}
	
	@Override
	protected void deleteWallpaper() {
		// TODO Auto-generated method stub
		List<Wallpaper> deleteDatas = mAdapter.getSelectedItems();
			for(Wallpaper wallpaper:deleteDatas){
				mAdapter.removeWallPaper(wallpaper);
			}
			mWallpaperManager.deleteKeyguardWallpaperByName(deleteDatas);
			exitEditModeWithoutData();
			
	}
	
	private void deleteWallpaperBySlide( ){
		if(mDeletePositionFromSlider == -1){
			return;
		}
		Wallpaper w = mAdapter.getWallpaper(mDeletePositionFromSlider);
		ArrayList<Wallpaper> list = new ArrayList<Wallpaper>();
		if(w != null){
			list.add(w);
			mAdapter.removeWallPaper(w);
			mWallpaperManager.deleteKeyguardWallpaperByName(list);
		}
		
	}
    
    @Override
    public void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mAdapter.onDestory();
    }
    
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == R.id.menu_item_add_wallpaper){
			startActivityForResult(IntentUtils.buildPickerKeyguardWallpaperIntent(), IntentUtils.REQUEST_PICKER_KEYGUARD_WALLPAPER);
		}
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK
                && (requestCode == IntentUtils.REQUEST_PICKER_KEYGUARD_WALLPAPER)) {
//			Log.d("pick", "has Data-->"+(data != null && data.getParcelableArrayListExtra("data") != null));
			if (data != null) {
				Intent request = new Intent(getContext(), SetKeyguardWallpaperActivity.class);
	           
				 mUpdateFromPicker = true;
				if(data.getData() != null){
					Uri uri = data.getData();
					request.putExtra(Config.WallpaperStored.KEYGUARD_WALLPAPER_CROP_TYPE, "single");
					request.setDataAndType(uri, IntentUtils.IMAGE_TYPE)
					 .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
				}else if(data.getParcelableArrayListExtra("data") != null){
					ArrayList<Uri> list = data.getParcelableArrayListExtra("data");
					request.putExtra(Config.WallpaperStored.KEYGUARD_WALLPAPER_CROP_TYPE, "multiple");
					request.setType(IntentUtils.IMAGE_TYPE).addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
					request.putParcelableArrayListExtra("images", list);
				}
				 startActivity(request);
			}
		}else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onSlideDeleteSelect(int position) {
		// TODO Auto-generated method stub
		mDeleteFromSlider = true;
		mDeletePositionFromSlider = position;
		showDialog(DIALOG_ID_DELETE_KEYGUARD_WALLPAPER);
	}

	@Override
	public void onItemClicked(View view, int position) {
		// TODO Auto-generated method stub
		if(isEditMode()){
			Wallpaper w = mAdapter.getWallpaper(position);
			if(isSystemWallpaper(w)){
				ToastUtils.showShortToast(getContext(), getResources().getString(R.string.tip_system_wallpaper_not_delete));
				return;
			}else if(isApplied(w)){
				
				ToastUtils.showShortToast(getContext(), getResources().getString(R.string.tip_applied_wallpaper_not_delete));
				return;
			}
			mAdapter.selectedItemOrNot(position);
		}
	}

	@Override
	public void onItemLongClicked(View view, int position) {
		// TODO Auto-generated method stub
		enterEditMode(position);
	}

	private boolean isApplied(Wallpaper w){
		return w.id == mWallpaperManager.getAppliedKeyguardWallpaperId(getActivity());
	}
	@Override
	public  void enterEditMode(int position){
		setEditMode(true);
		mActivity.showActionMode(true);
		mDeleteFromSlider = false;
		mDeletePositionFromSlider = -1;
		mSelectAll = false;
		String positiveText = getResources().getString(R.string.select_all);
		mActivity.getActionMode().setPositiveText(positiveText);
		Wallpaper w = mAdapter.getWallpaper(position);
		if(!(isSystemWallpaper(w) ||isApplied(w))){
			mAdapter.selectedItemOrNot(position);
		}
		mBottomBar.setVisibility(View.VISIBLE);
		mAdapter.enterDeleteMode(true);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter.onResume(mUpdateFromPicker);
	}
	
	private boolean isSystemWallpaper(Wallpaper w){
		
		return w.systemFlag == Wallpaper.FLAG_SYSTEM;
	}

	/**
	 * When Delete Button is not Clicked,call this method to 
	 * exit edit mode
	 */
	@Override
	public void exitEditMode(){
		exitEditModeWithoutData();
	}
	
	/**
	 * When Delete Button is Clicked,call this method
	 * to exit edit mode
	 */
	private void exitEditModeWithoutData(){
		setEditMode(false);
		mActivity.showActionMode(false);
		mAdapter.enterDeleteMode(false);
		mBottomBar.setVisibility(View.GONE);
	}

	@Override
	public void onActionItemClicked(Item item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == ActionMode.NAGATIVE_BUTTON){
			exitEditMode();
		}else if(item.getItemId() == ActionMode.POSITIVE_BUTTON){
			String positiveText = getResources().getString(mSelectAll?R.string.select_all:R.string.cancel_select_all);
			mActivity.getActionMode().setPositiveText(positiveText);
			mSelectAll = !mSelectAll;
			mAdapter.selectAll(mSelectAll);
		}
	}

	@Override
	public void onActionModeDismiss(ActionMode actionMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActionModeShow(ActionMode actionMode) {
		// TODO Auto-generated method stub
		
	}
	
}
