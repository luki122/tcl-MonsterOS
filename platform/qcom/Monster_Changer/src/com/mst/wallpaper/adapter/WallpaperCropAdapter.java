package com.mst.wallpaper.adapter;

import java.util.ArrayList;
import java.util.List;

import com.mst.wallpaper.activity.SetKeyguardWallpaperActivity;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.imageutils.ImageWorker;
import com.mst.wallpaper.utils.BitmapUtils;
import com.mst.wallpaper.widget.CropImageView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import mst.widget.PagerAdapter;
import mst.widget.ViewPager;

import com.mst.wallpaper.R;

public class WallpaperCropAdapter extends PagerAdapter{

    private static final String TAG = "WallpaperCropAdapter";
    private Context mContext;
    private List<String> mImageList;
    private List<View> mViews;
    private ImageResizer mImageResizer;
    private OnItemClickedListener mListener;
    public interface OnItemClickedListener{
    	public void onItemClicked(View view);
    }

    public WallpaperCropAdapter(Context context, List<String> imageList) {
        mContext = context;
        if (imageList == null) {
			mImageList = new ArrayList<String>();
		}else {
			mImageList = imageList;
		}
        initLayout();
    }

    @Override
    public int getCount() {
        if (null != mImageList) {
            return mImageList.size();
        }
        return 0;
    }
    
    public ImageView getItemImageView(int position){
    	View view = mViews.get(position);
		ImageView iv = (ImageView) view
				.findViewById(R.id.wallpaper_crop_item);
		return iv;
    }
    
	

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewPager viewPager = ((ViewPager) container);
        viewPager.addView(mViews.get(position));
        refreshData(position);
        return mViews.get(position);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager = (ViewPager) container;
        CropImageView iv = (CropImageView) mViews.get(position).findViewById(R.id.wallpaper_crop_item);
        if (iv != null) {
            ImageWorker.cancelWork(iv);
            iv.setImageDrawable(null);
        }
        viewPager.removeView(mViews.get(position));
    }

    private void initLayout() {
        int size = mImageList.size();
        mViews = new ArrayList<View>(size);
        for (int i = 0; i < size; i++) {
            View view = View.inflate(mContext, R.layout.wallpaper_crop_item, null);
            CropImageView iv = (CropImageView) view.findViewById(R.id.wallpaper_crop_item);
            mViews.add(view);
        }
    }


    public void setImageResizer(ImageResizer imageResizer) {
        mImageResizer = imageResizer;
    }

    public void refreshData(int position) {
        if (mViews == null || mViews.size() <= 0) {
            return;
        }
        View view = mViews.get(position);
        CropImageView iv = (CropImageView) view.findViewById(R.id.wallpaper_crop_item);
        iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d("click", "onClick:"+v);
				if(mListener != null){
					mListener.onItemClicked(v);
				}
			}
		});
        mImageResizer.loadImage(mImageList.get(position), iv, position);
    }

    public List<View> getItemViews(){
        return mViews;
    }
    
    public void onResume() {
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(false);
            notifyDataSetChanged();
        }
        
        //It may be no safe, if this problem happens again, please check here
        refreshData(((SetKeyguardWallpaperActivity) mContext).getCurrentItem());
    }

    public void onPause() {
    	
    }

    public void clearData() {
        if (mViews != null) {
            mViews.clear();
            mViews = null;
        }
        if (mImageList != null) {
        	mImageList.clear();
        	mImageList = null;
        }
        if (mImageResizer != null) {
            // mImageResizer.clearCache();
            // mImageResizer.clearMemoryCache();
            mImageResizer.closeCache();
        }
    }

	public void setOnItemClickListener(OnItemClickedListener listener) {
		// TODO Auto-generated method stub
		mListener = listener;
	}
}
