package com.mst.thememanager.ui.fragment.themedetail;

import java.util.ArrayList;

import mst.widget.ViewPager;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.gallery3d.util.ImageWorker.ImageLoaderCallback;
import com.mst.thememanager.R;
import com.mst.thememanager.entities.PreviewTransitionInfo;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.ui.fragment.AbsThemeFragment;
import com.mst.thememanager.utils.CommonUtil;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.DialogUtils;
import com.mst.thememanager.utils.StringUtils;
import com.mst.thememanager.views.ThemePreviewDonwloadButton;
public class ThemePkgDetailFragment extends AbsThemeFragment implements ThemePkgDetailMVPView
,OnClickListener{
	
	private Theme mCurrentTheme;
	private LinearLayout mPreviewScroller;
	private ThemePkgDetailPresenter mPresenter;
	private TextView mDesigner;
	private TextView mThemeSize;
	private TextView mDescription;
	private View mContentView;
	private int mPreviewImageWidth;
	private int mPreviewImageHeight;
	private int mImageLeftMargin;
	private ThemePreviewDonwloadButton mOptionBtn;
	
	/**
	 * Share drawable for preview ,it must set to null 
	 * when this fragment finished.
	 */
	private static  Drawable[] sPreviewDrawable;
	private  ArrayList<PreviewTransitionInfo> mInfos;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle args = getBundle();
		if(args != null){
			mCurrentTheme = args.getParcelable(Config.BUNDLE_KEY.KEY_THEME_PKG_DETAIL);
		}
		
		
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContentView = inflater.inflate(R.layout.theme_pkg_detail_layout, container,false);
		mPresenter = new ThemePkgDetailPresenter(getActivity(), mCurrentTheme);
		mPresenter.attachView(this);
		initView();
		mPreviewImageWidth = getResources().getDimensionPixelSize(R.dimen.theme_detail_preview_img_width);
		mPreviewImageHeight = getResources().getDimensionPixelSize(R.dimen.theme_detail_preview_img_height);
		mImageLeftMargin = getResources().getDimensionPixelSize(R.dimen.theme_detail_preview_img_margin_left);
		return mContentView;
	}
	
	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		mPreviewScroller = (LinearLayout)mContentView.findViewById(R.id.theme_pkg_detail_preview_scroller);
		mDesigner = (TextView)mContentView.findViewById(R.id.theme_detail_designer);
		mThemeSize = (TextView)mContentView.findViewById(R.id.theme_detail_size);
		mDescription = (TextView)mContentView.findViewById(R.id.theme_detail_description);
		mOptionBtn = (ThemePreviewDonwloadButton)mContentView.findViewById(R.id.theme_detail_option_btn);
		mOptionBtn.setTheme(mCurrentTheme);
		new Handler().post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mPresenter.loadThemePreview();
			}
		});
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mPresenter.updateThemeInfo();
	}


	@Override
	public void updatePreview(Bitmap previewBitmap) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateThemeInfo(Theme theme) {
		// TODO Auto-generated method stub
		if(theme != null){
			mDesigner.setText(getString(R.string.theme_detail_designer,theme.designer));
			mThemeSize.setText(getString(R.string.theme_detail_size,theme.size));
			mDescription.setText(getString(R.string.theme_detail_description,theme.description));
			createPreview(theme);
		}
	}


	@Override
	public void updateTheme(Theme theme) {
		// TODO Auto-generated method stub
		
	}



	private void createPreview(Theme theme){
		
		int imagesCount = theme.previewArrays.size();
		final boolean isDefaultTheme = theme.id == Config.DEFAULT_THEME_ID;
		if(isDefaultTheme){
			imagesCount = Config.DEFAUTL_THEME_PREVIEWS.length;
		}
		Log.d("preview", "imagesCount-->"+imagesCount);
		if(imagesCount == 0){
			return;
		}
		mPreviewScroller.removeAllViews();
		for(int i = 0;i < imagesCount;i++){
			final ImageView image = new ImageView(getActivity());
			image.setScaleType(ScaleType.CENTER_CROP);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mPreviewImageWidth, mPreviewImageHeight);
			if(i >0){
				params.leftMargin = mImageLeftMargin;
			}
			mPreviewScroller.addView(image, params);
			image.setOnClickListener(this);
			image.setId(i);
			if(isDefaultTheme){
				CommonUtil.getDrawableFromAssets(getImageResizer(), Config.DEFAUTL_THEME_PREVIEWS[i], image);
			}else{
				Log.d("preview", ""+theme.previewArrays.get(i));
				getImageResizer().loadImage(theme.previewArrays.get(i), image);
			}
			
		}
		
	}

	public static Drawable[] getPreviewDrawables(){
		return sPreviewDrawable;
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int previewCount = mPreviewScroller.getChildCount();
		sPreviewDrawable = new Drawable[previewCount];
		mInfos = new ArrayList<PreviewTransitionInfo>();
		for(int i = 0;i < previewCount ;i++){
			ImageView image = (ImageView) mPreviewScroller.getChildAt(i);
			sPreviewDrawable[i] = image.getDrawable();
			PreviewTransitionInfo info = new PreviewTransitionInfo();
			info.index = i;
			final View imageView = mPreviewScroller.getChildAt(i);
			int[] position = imageView.getLocationOnScreen();
			info.x = position[0];
			info.y = position[1];
			mInfos.add(info);
		}
		
		 Intent intent = new Intent(getActivity(), ThemePreviewActivity.class);
	     intent.putExtra(PreviewTransitionInfo.KEY_ID,v.getId());
	     intent.putParcelableArrayListExtra(PreviewTransitionInfo.KEY_INFO, mInfos);
	     Log.d("preview", "startPreview");
	     startActivity(intent);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(sPreviewDrawable != null){
			int count = sPreviewDrawable.length;
			for(int i = 0 ;i < count;i++){
				sPreviewDrawable[i] = null;
			}
			sPreviewDrawable = null;
		}
	}
	
	

	
	
	

}
