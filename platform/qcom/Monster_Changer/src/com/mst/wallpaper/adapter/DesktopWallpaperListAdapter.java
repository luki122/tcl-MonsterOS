package com.mst.wallpaper.adapter;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import mst.widget.recycleview.RecyclerView.ViewHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.mst.wallpaper.R;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.WallpaperManager;

public class DesktopWallpaperListAdapter extends WallpaperAdapter {
	
	private ImageResizer mImageResizer;
	private WallpaperManager mWallpaperManager;
	private SparseArray<WeakReference<ImageView>> mItemViews = new SparseArray<WeakReference<ImageView>>();
	public DesktopWallpaperListAdapter(Context context) {
		super(context);
		mWallpaperManager = WallpaperManager.getInstance();
	}

	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = getInflater().inflate(R.layout.desktop_wallpaper_list_item, parent,false);
	
		return new Holder(itemView,this);
	}

	public ImageView getItemView(int position){
		WeakReference<ImageView> item = mItemViews.get(position);
		if(item != null && item.get() != null){
			return item.get();
		}
		return null;
	}
	
	
	
	@Override
	public void onBindViewHolder(ViewHolder  holder, int position) {
		// TODO Auto-generated method stub
		Holder holderInternal = (Holder)holder;
		
		holderInternal.bindItemData(holder.itemView, position);
		
	}
	
	private  class Holder extends WallpaperAdapter.RecylerHolder{

		private ImageView wallpaperView;
		private CheckBox mDeleteCheck;
		private ImageView mAppliedStatusView;
		public Holder(View itemView,WallpaperAdapter adapter) {
			super(itemView,adapter);
			// TODO Auto-generated constructor stub
			wallpaperView = (ImageView)itemView.findViewById(R.id.wallpaper_item);
			mDeleteCheck = (CheckBox)itemView.findViewById(R.id.wallpaper_item_select_delete);
			mAppliedStatusView = (ImageView)itemView.findViewById(R.id.wallpaper_item_applied);
		}

		@Override
		protected void bindItemData(final View itemView, final int position) {
			mItemViews.put(position, new WeakReference<ImageView>(wallpaperView));
			itemView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(DesktopWallpaperListAdapter.this.getItemClickListener() != null){
						DesktopWallpaperListAdapter.this.getItemClickListener().onItemClicked(itemView, position);
					}
				}
			});
			itemView.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					if(DesktopWallpaperListAdapter.this.getItemClickListener() != null){
						DesktopWallpaperListAdapter.this.getItemClickListener().onItemLongClicked(itemView, position);
					}
					return true;
				}
			});
			
			Wallpaper wallpaper = getAdapter().getWallpaper(position);
			String path = wallpaper.getPathByKey(0);
			if(!TextUtils.isEmpty(path)){
				mImageResizer.loadImage(path, wallpaperView);
			}
			Boolean selected = mMapForSelect.get(position);
			mDeleteCheck.setVisibility(getAdapter().getDeleteMode()?View.VISIBLE:View.GONE);
			mDeleteCheck.setChecked((selected==null)?false:selected);
			mAppliedStatusView.setVisibility(wallpaper.applied?View.VISIBLE:View.GONE);
		}
		
	}

	public void setImageResizer(ImageResizer resizer) {
		// TODO Auto-generated method stub
		mImageResizer = resizer;
	}





	

}
