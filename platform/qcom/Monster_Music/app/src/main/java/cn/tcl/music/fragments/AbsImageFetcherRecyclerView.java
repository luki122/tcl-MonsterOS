/* ----------|----------------------|---------------------|-------------------*/
/* 07/05/2015|xiaolong.zhang        |PR986319             |queue's cover missed after lock*/
/* ----------|----------------------|---------------------|-------------------*/
package cn.tcl.music.fragments;

import android.os.Bundle;
import android.view.View;

import cn.tcl.music.R;
import cn.tcl.music.view.image.ImageFetcher;

public abstract class AbsImageFetcherRecyclerView extends RecyclerViewFragment {

    protected ImageFetcher mImageFetcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (getActivity() == null)
    		return;

    	int artworkSize = getResources().getDimensionPixelSize(R.dimen.image_artwork_row_size);
        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), artworkSize, R.drawable.default_cover_list);

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
    	super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        //[BUGFIX]-MOD-BEGIN by binbin.chang for 2237011 on 2016/6/2
        try {
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        //[BUGFIX]-MOD-END by binbin.chang for 2237011 on 2016/6/2
    }

    public ImageFetcher getImageFetcher()
    {
    	return mImageFetcher;
    }

    @Override
    public void onPause() {
    	super.onPause();
        mImageFetcher.setExitTasksEarly(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //When QueueFragment create will create QueueAdapter(Context context, Cursor c, ImageFetcher imageFetcher)
        //so the adapter has the mImageFetcher A,A controll load image by the value mExitTasksEarly
        //And QueueFragment secondly create,the adapter doesn't change,then load imge also use the mImageFetcher A,not the new ImageFetcher
        mImageFetcher = null;
        mAdapter = null;
    }

}
