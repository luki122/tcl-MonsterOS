package com.monster.appmanager.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.monster.appmanager.MainActivity;
import com.monster.appmanager.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.monster.launchericon.utils.IconGetterManager;
import com.monster.launchericon.utils.IIconGetter;

public class HorizontalListViewAdapter extends BaseAdapter{
	public static int EMPTY_SIZE = 3;
	private ArrayList<AppEntry> entries;
	private Context mContext;
	private LayoutInflater mInflater;
	private int selectIndex = -1;
	private int itemId;
	private PackageManager pm;
	private Drawable defaultDrawable;
	private static IconGetterManager iconGetter;
	public HorizontalListViewAdapter(Context context, ArrayList<AppEntry> entries, int itemId, PackageManager pm){
		this.mContext = context;
		this.entries = entries;
		this.itemId = itemId;
		this.pm = pm;

		iconGetter = IconGetterManager.getInstance(mContext, false,false);
		mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
		defaultDrawable = mContext.getResources().getDrawable(R.drawable.empty, MainActivity.mainActivity.getTheme());	
		defaultDrawable = resizeBitmap(defaultDrawable, mContext.getResources().getDimensionPixelSize(R.dimen.thumnail_default_width), mContext.getResources());
	}
	@Override
	public int getCount() {
		return entries.size()+EMPTY_SIZE*2;
	}
	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView==null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(itemId, parent, false);
			holder.mImage=(ImageView)convertView;
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder)convertView.getTag();
		}
		
		if(position == selectIndex){
			convertView.setSelected(true);
		}else{
			convertView.setSelected(false);
		}
		Drawable drawable = null;
		if(position < EMPTY_SIZE || position> (entries.size()+EMPTY_SIZE-1)){
			drawable = defaultDrawable;
		}else{
			drawable = getApplicationIcon(entries.get(position-EMPTY_SIZE).info, pm);
		}
		holder.mImage.setImageDrawable(drawable);

		return convertView;
	}

	public static Drawable getApplicationIcon(ApplicationInfo info, PackageManager pm){
		Activity activity = MainActivity.mainActivity;
		if(iconGetter == null){
			iconGetter = IconGetterManager.getInstance( activity, false,  false);
		}
		Drawable drawable = allDrawables.get(info.packageName);
		if(drawable == null){
			Drawable iconTmp = null;
			try{
				iconTmp = iconGetter.getIconDrawable(info.packageName);
			}catch(Exception e){
				e.printStackTrace();
			}			
			if(iconTmp == null){
				drawable = pm.getApplicationIcon(info);
			}else{
				drawable = iconTmp;
			}
			drawable = resizeBitmap(drawable, activity.getResources().getDimensionPixelSize(R.dimen.thumnail_default_width), activity.getResources());
			allDrawables.put(info.packageName, drawable);
		}
		return drawable;
	}
	private static Map<String, Drawable> allDrawables = new HashMap<>();
	public static final float SCAN_MAX = 0.15f;
	/**
	 * @param bitmap
	 * @param newWidth
	 * @param resources 
	 * @return
	 */
	public static Drawable resizeBitmap(Drawable drawable, int newWidth, Resources resources) {
		Bitmap.Config config =  drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.ARGB_8888;
		Bitmap bitmap = Bitmap.createBitmap(newWidth,newWidth,config);
		//注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
		Canvas canvas = new Canvas(bitmap);   
		int offset = (int)(newWidth*SCAN_MAX);
		drawable.setBounds(offset, offset, newWidth-offset, newWidth-offset);   
		drawable.draw(canvas);
		return new BitmapDrawable(resources, bitmap);
	}

	private static class ViewHolder {
		private ImageView mImage;
	}
	public void setSelectIndex(int i){
		selectIndex = i;
	}
}