package com.mst.thememanager.ui.fragment;

import mst.utils.DisplayUtils;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.android.gallery3d.util.ImageResizer;
import com.android.gallery3d.util.ImageWorker.ImageLoaderCallback;
import com.mst.thememanager.ui.MainActivity;

public abstract class AbsThemeFragment extends Fragment {
	
	
    private static final String TAG = "ThemeFragment";
    
    private Bundle mStartArgs;
    
    private ImageResizer mImageResizer;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	mStartArgs = getActivity().getIntent().getBundleExtra(MainActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS);
    }
    
    public Bundle getBundle(){
    	return mStartArgs;
    }
    
    public ImageResizer getImageResizer(){
    	if(mImageResizer == null){
    		final int height = DisplayUtils.getHeightPixels(getActivity());
    		final int width = DisplayUtils.getWidthPixels(getActivity());
    		mImageResizer = new ImageResizer(getContext(), width/2, height/2);
    		mImageResizer.addImageCache(getActivity(), "detail_images");
    	}
        return mImageResizer;
    }
    
    protected abstract void initView();

	public boolean startFragment(Fragment caller, String fragmentClass,boolean addToBackStack, int titleRes,
            int requestCode, Bundle extras) {
       return startFragment(caller, fragmentClass, addToBackStack, getResources().getString(titleRes), requestCode, extras);
    }
	
	public boolean startFragment(Fragment caller, String fragmentClass,boolean addToBackStack, CharSequence title,
            int requestCode, Bundle extras) {
        final Activity activity = getActivity();
        if (activity instanceof MainActivity) {
        	MainActivity ma = (MainActivity) activity;
            ma.startThemePanel(fragmentClass, extras,addToBackStack, title,  caller, requestCode);
            return true;
        } else {
            Log.w(TAG,
                    "Parent isn't MainActivity , thus there's no way to "
                    + "launch the given Fragment (name: " + fragmentClass
                    + ", requestCode: " + requestCode + ")");
            return false;
        }
    }

}
