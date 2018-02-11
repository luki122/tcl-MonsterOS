package com.mst.wallpaper.utils.loader;

import com.monster.launchericon.utils.IconGetterManager;
import com.monster.launchericon.utils.PKGIcongetter;
import com.mst.wallpaper.utils.Config;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mst.wallpaper.R;

public class IconPreviewLoader {

	private Context mContext;

	private PackageManager mPm;

	private LayoutInflater mInflater;

	private boolean mSmall = false;

	
	private IconGetterManager mIconManager;

	private LinearLayout mTopParent;

	public IconPreviewLoader(Context context) {
		this(context, false);
	}

	public IconPreviewLoader(Context context, boolean samllIcon) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mSmall = samllIcon;
		mPm = mContext.getPackageManager();
		mIconManager = IconGetterManager.getInstance(mContext, false,false);
	}

	public void setupWallPaperPreviewBottomIcons(LinearLayout bottomParent) {
		int size = Config.WALLPAPER_PREVIEW_BOTTOM_ICONS.length;
		for (int i = 0; i < size; i++) {
			String pkg = Config.WALLPAPER_PREVIEW_BOTTOM_ICONS[i];
			Drawable iconDrawable = getPackageIcon(pkg);
			if (iconDrawable != null) {
				View iconView = mInflater.inflate(
						mSmall ? R.layout.wallpaper_icon_item_bottom_small
								: R.layout.wallpaper_icon_item_bottom, null);
				ImageView icon = (ImageView) iconView
						.findViewById(R.id.icon_bottom);
				if(i == 0){
					LinearLayout.LayoutParams p = (LinearLayout.LayoutParams)icon.getLayoutParams();
					p.gravity = Gravity.LEFT;
				}else if(i == 1){
					LinearLayout.LayoutParams p = (LinearLayout.LayoutParams)icon.getLayoutParams();
					p.gravity = Gravity.CENTER_HORIZONTAL;
				}else{
					LinearLayout.LayoutParams p = (LinearLayout.LayoutParams)icon.getLayoutParams();
					p.gravity = Gravity.RIGHT;
				}
				icon.setImageDrawable(iconDrawable);

				bottomParent.addView(iconView, createParams());
			}
		}
	}

	private LinearLayout.LayoutParams createParams() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		return params;
	}

	public void setupWallpaperPreviewTopIcons(LinearLayout topParent) {
		int size = Config.WALLPAPER_PREVIEW_TOP_ICONS.length;
		for (int i = 0; i < size / 2; i++) {
			addTopIcons(i, topParent, 0);
		}

		for (int i = size / 2; i < size; i++) {
			addTopIcons(i, topParent, 1);
		}

	}

	private void addTopIcons(int index, LinearLayout topParent, int row) {
		String pkg = Config.WALLPAPER_PREVIEW_TOP_ICONS[index];
		Drawable iconDrawable = getPackageIcon(pkg);
		mTopParent = topParent;
		if (iconDrawable != null) {
			View iconView = mInflater.inflate(
					mSmall ? R.layout.wallpaper_icon_item_top_small
							: R.layout.wallpaper_icon_item_top, null);
			ImageView icon = (ImageView) iconView.findViewById(R.id.icon_img);
			icon.setImageDrawable(iconDrawable);
			TextView iconName = (TextView) iconView
					.findViewById(R.id.icon_name);
			String appName = getAppNameByPkgName(pkg);
			iconName.setText(appName);
			LinearLayout parent = (LinearLayout) topParent.getChildAt(row);
			parent.addView(iconView, createParams());
		}
	}

	private String getAppNameByPkgName(String pkgName) {
		PackageManager pm = mContext.getPackageManager();
		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(pkgName,
					PackageManager.GET_META_DATA);
			if (appInfo != null) {
				CharSequence appName = pm.getApplicationLabel(appInfo);
				return appName.toString();
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	public void setIconNameColor(int color) {
		if (mTopParent != null) {
			int rowCount = mTopParent.getChildCount();
			LinearLayout firstRow = (LinearLayout) mTopParent.getChildAt(0);
			LinearLayout secondRow = (LinearLayout) mTopParent.getChildAt(1);
			int firstRowIconCount = firstRow.getChildCount();
			int secondeRowIconCount = secondRow.getChildCount();
			for (int i = 0; i < firstRowIconCount; i++) {
				View iconView = firstRow.getChildAt(i);
				TextView iconName = (TextView) iconView
						.findViewById(R.id.icon_name);
				iconName.setTextColor(color);
			}

			for (int i = 0; i < secondeRowIconCount; i++) {
				View iconView = secondRow.getChildAt(i);
				TextView iconName = (TextView) iconView
						.findViewById(R.id.icon_name);
				iconName.setTextColor(color);
			}
		}
	}

	private Drawable getPackageIcon(String packageName) {
		try {
			if (mIconManager != null) {
				return mIconManager.getIconDrawable(packageName);
			} else {
				return mPm.getApplicationIcon(packageName);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return mContext.getDrawable(R.drawable.icon);
	}

}
