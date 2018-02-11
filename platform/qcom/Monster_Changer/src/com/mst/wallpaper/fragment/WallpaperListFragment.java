package com.mst.wallpaper.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import mst.app.dialog.AlertDialog;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.view.menu.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import mst.widget.ActionMode;
import mst.widget.ActionMode.Item;
import mst.widget.recycleview.GridLayoutManager;
import mst.widget.recycleview.GridLayoutManager.SpanSizeLookup;
import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerViewGridItemSpace;
import mst.widget.recycleview.StaggeredGridLayoutManager;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;

import com.mst.wallpaper.BasePresenter;
import com.mst.wallpaper.MainWorker.OnRequestListener;
import com.mst.wallpaper.activity.SetDesktopWallpaperActivity;
import com.mst.wallpaper.activity.WallPaperListActivity;
import com.mst.wallpaper.activity.WallpaperPreviewActivity;
import com.mst.wallpaper.adapter.DesktopWallpaperListAdapter;
import com.mst.wallpaper.adapter.WallpaperAdapter.OnItemClickListener;
import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.presenter.PresenterBridge;
import com.mst.wallpaper.utils.BitmapLoader;
import com.mst.wallpaper.utils.BitmapUtils;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.IntentUtils;
import com.mst.wallpaper.utils.ToastUtils;
import com.mst.wallpaper.utils.WallpaperManager;
import com.mst.wallpaper.utils.loader.WallpaperLoader;
import com.mst.wallpaper.widget.RecycleViewDivider;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mst.wallpaper.R;
public class WallpaperListFragment extends BaseFragment implements OnItemClickListener
,OnMenuItemClickListener,PresenterBridge.WallpaperView{
	private static final String TAG = "WallpaperList";
	private static final int LIST_SPAN_COUNT = 3;
	private static final int MSG_LOAD_WALLPAPER_SUCCESS = 100;
	private static final int IMAGE_LOAD_SIZE = 119;
	private WallPaperListActivity mActivity;
	private RecyclerView mList;
	private DesktopWallpaperListAdapter mAdapter;
	private View mContentView;
	private List<Wallpaper> mWallpaper;
	private ImageResizer mImageResizer;
	private Toolbar mToolbar;
	private BottomNavigationView mBottomBar;
	private WallpaperLoader mWallpaperLoader;
	private WallpaperManager mWallpaperManager;
	private boolean mSelectAll = false;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if (msg.what == MSG_LOAD_WALLPAPER_SUCCESS) {
				ArrayList<Wallpaper> result = (ArrayList<Wallpaper>) msg.obj;
				if (result.size() > 0) {
					for (Wallpaper w : result) {
						w.type = Wallpaper.TYPE_DESKTOP;
						mAdapter.addWallPaper(w);
					}

				}
			}
		};
		
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mWallpaperLoader = new WallpaperLoader(this);
		mWallpaperManager = WallpaperManager.getInstance();
		mContentView = inflater.inflate(R.layout.desktop_wallpaper_list_fragment, container,false);
		mList = (RecyclerView)mContentView.findViewById(R.id.recycler_view);
		mBottomBar = (BottomNavigationView)mContentView.findViewById(R.id.bottom_navigation);
		mBottomBar.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
			
			@Override
			public boolean onNavigationItemSelected(MenuItem item) {
				// TODO Auto-generated method stub
				if(item.getItemId() == R.id.menu_item_delete_wallpaper){
					if(isEditMode()){
						List<Wallpaper> deleteDatas = mAdapter.getSelectedItems();
						if(deleteDatas.size() < 1){
							ToastUtils.showShortToast(getContext(), getResources().getString(R.string.tip_select_wallpaper));
						}else{
							showDialog(DIALOG_ID_DELETE_DESKTOP_WALLPAPER);
						}
						
					}
				}
				return true;
			}
		});
		return mContentView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		initialView();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		int deleteCount =mAdapter.getSelectedItems().size();
		
		if(id == DIALOG_ID_DELETE_DESKTOP_WALLPAPER){
			AlertDialog alert = new AlertDialog.Builder(getContext())
			.setTitle(R.string.delete_dialog_title)
			.setMessage(getResources().getString(R.string.delete_desktop_wallpaper_msg, mAdapter.getSelectedItems().size()))
			.setPositiveButton(R.string.confirm_ok,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					deleteWallpaper();
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
	protected void deleteWallpaper(){
		List<Wallpaper> deleteDatas = mAdapter.getSelectedItems();
			for(Wallpaper wallpaper:deleteDatas){
				mAdapter.removeWallPaper(wallpaper);
				
			}
			mWallpaperManager.deleteWallpaper(deleteDatas);
			exitEditModeWithoutData();
	}

	
	private void initialView(){
		mActivity = (WallPaperListActivity) getActivity();
		mActivity.setTitle(getString(R.string.wallpaper));
		mToolbar = mActivity.getToolbar();
		mToolbar.setOnMenuItemClickListener(this);
		mToolbar.inflateMenu(R.menu.wallpaper_add_item);
		initImageCache();
		mList.setHasFixedSize(true);
		GridLayoutManager layoutManager = new GridLayoutManager(mActivity,LIST_SPAN_COUNT);
		mList.setLayoutManager(layoutManager);
		mList.addItemDecoration(new RecyclerViewGridItemSpace(getResources().getDimensionPixelSize(R.dimen.space_desktop_wallpaper_item),LIST_SPAN_COUNT));
		mAdapter = new DesktopWallpaperListAdapter(mActivity);
		mAdapter.setOnItemClickListener(this);
		mAdapter.setImageResizer(mImageResizer);
		mList.setAdapter(mAdapter);
		
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter.clearWallPapers();
		mWallpaperLoader.start();
		int appliedPosition = mWallpaperManager.getAppliedWallpaperPosition(getContext());
		if(mAdapter.getItemCount() > 0){
		if(appliedPosition != -1){
			for(Wallpaper w:mAdapter.getWallpapers()){
				if(w.applied){
					int oldPosition = mAdapter.getWallpapers().indexOf(w);
					mAdapter.getWallpaper(oldPosition).applied = false;
					mAdapter.notifyItemChanged(oldPosition);
					break;
				}
			}
			mAdapter.getWallpaper(appliedPosition).applied = true;
			mAdapter.notifyItemChanged(appliedPosition);
		}
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mWallpaperLoader.onDestory();
	}
	
	
	

    private void initImageCache() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int longest = ( int ) (IMAGE_LOAD_SIZE * displayMetrics.scaledDensity);
        mImageResizer = new ImageResizer(getActivity(), longest,true);
        mImageResizer.setLoadingImage(R.drawable.item_wallpaper_default);
        mImageResizer.addImageCache(getActivity(), Config.WALLPAPER_THUMB_CACHE);
        
    }

    
	@Override
	public void onItemClicked(View view, final int position) {
		// TODO Auto-generated method stub
		if(Config.DEBUG){
			Log.d(TAG, "onItemClicked  position-->"+position);
		}
		if(isEditMode()){
			Wallpaper w = mAdapter.getWallpaper(position);
			if(isSystemWallpaper(w)){
				ToastUtils.showShortToast(getContext(), getResources().getString(R.string.tip_system_wallpaper_not_delete));
				return;
			}else if(w.applied){
				ToastUtils.showShortToast(getContext(), getResources().getString(R.string.tip_applied_wallpaper_not_delete));
				return;
			}
			mAdapter.selectedItemOrNot(position);
		}else{
			new Handler().post(new Runnable(){
				@Override
				public void run() {
					startActivity(buildPreviewIntent(position));
				};
			});
			
		}
	}

	private boolean isSystemWallpaper(Wallpaper w){
		
		return w.systemFlag == Wallpaper.FLAG_SYSTEM;
	}
	@Override
	public void onItemLongClicked(View view, int position) {
		// TODO Auto-generated method stub
		if(Config.DEBUG){
			Log.d(TAG, "onItemLongClicked  position-->"+position);
		}
		enterEditMode(position);
	}
    
	
	
    private Intent buildPreviewIntent(int position){
    	Intent intent = IntentUtils.buildWallpaperPreviewIntent(position, Wallpaper.TYPE_DESKTOP,mAdapter.getWallpapers());
    	ImageView thumb = mAdapter.getItemView(position);
    	if(thumb != null){
    		int color = BitmapUtils.calcTextColor(BitmapUtils.drawable2bitmap(thumb.getDrawable()));
    		intent.putExtra(Config.Action.KEY_WALLPAPER_PREVIEW_WIDGET_INIT_COLOR, color);
    	}
    	intent.setClass(getActivity(),WallpaperPreviewActivity.class);
    	return intent;
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		startActivityForResult(IntentUtils.buildPickerDesktopWallpaperIntent(), 
				IntentUtils.REQUEST_PICK_DESKTOP_WALLPAPER_CODE);
		return true;
	}

	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK
                && (requestCode == IntentUtils.REQUEST_PICK_DESKTOP_WALLPAPER_CODE)) {
			if (data != null && data.getData() != null) {
				Uri uri = data.getData();
				Intent request = new Intent(getContext(), SetDesktopWallpaperActivity.class);
	            request.setDataAndType(uri, IntentUtils.IMAGE_TYPE)
	            .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
	            startActivity(request);
			}
		}else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	



	@Override
	public void updateView(List<Wallpaper> data, Integer status) {
		// TODO Auto-generated method stub
		Message msg = new Message();
		msg.obj = data;
		
		msg.what = MSG_LOAD_WALLPAPER_SUCCESS;
		mHandler.sendMessage(msg);
	}

	@Override
	public void updateProgress(Integer progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Context getViewContext() {
		// TODO Auto-generated method stub
		return getActivity().getApplicationContext();
	}

	@Override
	public void enterEditMode(int position){
		setEditMode(true);
		mSelectAll = false;
		Wallpaper w = mAdapter.getWallpaper(position);
		String positiveText = getResources().getString(R.string.select_all);
		mActivity.getActionMode().setPositiveText(positiveText);
		if(!(isSystemWallpaper(w) || w.applied)){
			mAdapter.selectedItemOrNot(position);
		}
		mBottomBar.setVisibility(View.VISIBLE);
		mAdapter.enterDeleteMode(true);
		mActivity.showActionMode(true);
		
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
