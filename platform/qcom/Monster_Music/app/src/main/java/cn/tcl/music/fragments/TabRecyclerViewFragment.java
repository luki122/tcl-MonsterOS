package cn.tcl.music.fragments;

import android.app.Activity;
import android.content.UriMatcher;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.tcl.framework.log.NLog;

import cn.tcl.music.R;
import cn.tcl.music.database.MusicMediaDatabaseHelper;

public abstract class TabRecyclerViewFragment extends AbsImageFetcherRecyclerView {
	
	private static String TAG = TabRecyclerViewFragment.class.getSimpleName();
    public static int SPAN_SIZE = 0;
	public  enum AdapterType{
		NORMAL,
		SIMPLE,
		PICKER
	}
	
	
	protected AdapterType mAdapterType = AdapterType.NORMAL;


	private static final UriMatcher sUriMatcher;

	static {
		sUriMatcher = new UriMatcher (UriMatcher.NO_MATCH);
	}
    
	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static TabRecyclerViewFragment newInstance(Object data) {
		TabRecyclerViewFragment fragment = null;
		return fragment;
	}

    /**
      * Returns a new instance of this fragment for the given section number.
      */
    public static TabRecyclerViewFragment newMutliSelectedInstance(Object data, int firstSongId) {
      TabRecyclerViewFragment fragment = null;
      return fragment;
    }

	public void setSimpleAdapter(boolean isSimple)
	{
		if (isSimple)
			mAdapterType = AdapterType.SIMPLE;
		else
			mAdapterType = AdapterType.NORMAL;
	}
	
	public void setAdapterType(AdapterType newAdapterType)
	{
		mAdapterType = newAdapterType;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(mAdapterType == AdapterType.NORMAL);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
	protected void switchToList() {
		LinearLayoutManager llm = new LinearLayoutManager(getActivity());
		changeRecyclerLayoutManager(llm);
		
		mImageFetcher.setEmptyImageRes(R.drawable.default_cover_list);
		mImageFetcher.setImageSize(getResources().getDimensionPixelSize(R.dimen.image_artwork_row_size));
	}

	protected void switchToGridLayout() {
		mImageFetcher.setEmptyImageRes(R.drawable.default_cover_list);
		mImageFetcher.setImageSize(getResources().getDimensionPixelSize(R.dimen.image_artwork_case_size));
	}
	

	

	@Override
	protected void onRecyclerViewCreated(RecyclerView rv) {
		
	}
}
