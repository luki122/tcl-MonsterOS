package com.mst.wallpaper.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.imageutils.ImageWorker;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.Config;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import mst.widget.PagerAdapter;
import mst.widget.ViewPager;
import mst.widget.toolbar.Toolbar;

import com.mst.wallpaper.R;
public class WallpaperPreviewAdapter extends AbsViewPagerAdapter{

    private static final String TAG = "WallpaperPreviewAdapter";
    private ViewPager mViewPager;
    private Context mContext;
    private List<View> mViews;
    private LayoutInflater mInflater;
    private int mWallpaperType;
    private ArrayList<Wallpaper> mWallpaper;
    private ImageResizer mImageResizer;
    public WallpaperPreviewAdapter(Context context, ArrayList<Wallpaper>  wallpapers, int type) {
        mContext = context;
        mWallpaperType = type;
        mWallpaper = wallpapers;
        mViews = initLayout(wallpapers);
    }

    
    @Override
    public int getCount() {
        if (mViews != null) {
            return mViews.size();
        }
        return 0;
    }
    

    @Override
    public Object instantiateItem(View container, int position) {
        ViewPager viewPager = ((ViewPager) container);
        viewPager.addView(mViews.get(position));
        refreshData(position);
        return mViews.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager = (ViewPager) container;
        ImageView mImageView = (ImageView) mViews.get(position).findViewById(R.id.wallpaper_preview_item_background);
        if (mImageView != null) {
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
        viewPager.removeView(mViews.get(position));
    }

    public Wallpaper getWallpaper(int position){
    	return mWallpaper != null?mWallpaper.get(position):null;
    }
    

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    public List<View> initLayout(ArrayList<Wallpaper> wallpapers) {
        int size = wallpapers.size();
        List<View> views = new ArrayList<View>();
        for (int i = 0; i < size; i++) {
            View view = View.inflate(mContext, R.layout.desktop_wallpaper_preview_item, null);
            views.add(view);
        }
        return views;
    }
    

    public void refreshData(int position) {
        View view = mViews.get(position);
        ImageView iv = (ImageView) view.findViewById(R.id.wallpaper_preview_item_background);     
        iv.setOnClickListener(this);
	   mImageResizer.loadImage(mWallpaper.get(position).getPathByKey(0), iv,position);
    }
    
    
    class ViewHolder {
        ImageView mBackground;
        ImageView mForeground;
    }

    public void clearData(){
//        mImageLoader.clearCache();
        if (mViews != null) {
            mViews.clear();
            mViews = null;
        }
        if (mWallpaper != null) {
        	mWallpaper.clear();
        	mWallpaper = null;
        }
        if (mImageResizer != null) {
            mImageResizer.clearCache();
            //mImageResizer.clearMemoryCache();
            mImageResizer.closeCache();
        }

    }
    
    public void onPause(){
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(true);
            mImageResizer.flushCache();
        }
    }
    
    public void onResume(){
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(false);
            notifyDataSetChanged();
        }
    }
    
    public void setImageResizer(ImageResizer imageResizer){
        mImageResizer = imageResizer;
    }
    
    public ImageResizer getImageResizer(){
    	return mImageResizer;
    }
    
    public ImageView getItemImageView(int position){
    	View view = mViews.get(position);
    	return (ImageView)view.findViewById(R.id.wallpaper_preview_item_background);
    	
    }

    
    
}
