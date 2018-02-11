package com.mst.thememanager.ui.adapter;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import mst.utils.DisplayUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.gallery3d.util.ImageResizer;
import com.android.gallery3d.util.ImageWorker.ImageLoaderCallback;
import com.mst.thememanager.R;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.utils.CommonUtil;
import com.mst.thememanager.utils.Config;

public class LocalThemeListAdapter extends AbsBaseAdapter<Theme> {

	
	
	
	public LocalThemeListAdapter(Context context) {
		super(context);
	}
	

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView == null){
			convertView = inflate(R.layout.local_theme_pkg_list_item, null);
			holder = new ViewHolder(getContext(),this);
			holder.holdConvertView(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.bindDatas(position, getThemes());
		return convertView;
	}

	static class ViewHolder extends AbsViewHolder<Theme>{
		
		private AbsBaseAdapter<Theme> adapter;
		public ViewHolder(Context context,AbsBaseAdapter<Theme> adapter) {
			super(context,adapter);
			// TODO Auto-generated constructor stub
			this.adapter = adapter;
		}

		ImageView image;
		TextView title;
		ImageView applyStatus;
		@Override
		public void bindDatas(int position, List<Theme> themes) {
			Theme theme = themes.get(position);
			
			if(theme != null){
				title.setText(theme.name);
			}
			String coverPath = theme.loadedPath+File.separatorChar+Config.LOCAL_THEME_PREVIEW_DIR_NAME;
			File coverFile = new File(coverPath);
			if(coverFile.exists()){
					String[] childrenFiles = coverFile.list();
					if(childrenFiles != null && childrenFiles.length > 0){
						getAdapter().getImageResizer().loadImage(coverPath+childrenFiles[0], image);
					}else{
						image.setImageResource(R.drawable.ic_launcher);
					}
					
			}else{
				if(theme.id == Config.DEFAULT_THEME_ID){
					CommonUtil.getDrawableFromAssets(getAdapter().getImageResizer(),Config.DEFAULT_THEME_COVER,image);
				}else{
					image.setImageResource(R.drawable.ic_launcher);
				}
				
			}
			applyStatus.setVisibility(adapter.themeApplied(theme)?View.VISIBLE:View.GONE);
		}

		@Override
		public void holdConvertView(View convertView) {
			// TODO Auto-generated method stub
			image = (ImageView)convertView.findViewById(R.id.theme_list_item_image);
			title = (TextView)convertView.findViewById(R.id.theme_list_item_title);
			applyStatus = (ImageView)convertView.findViewById(R.id.theme_list_item_apply_status);
		}

	}


}
