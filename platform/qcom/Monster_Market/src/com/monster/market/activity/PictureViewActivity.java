package com.monster.market.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.monster.market.R;
import com.monster.market.fragment.PicViewFragment;
import com.monster.market.utils.PicBrowserUtil;
import com.monster.market.views.PicViewPager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.List;

public class PictureViewActivity extends FragmentActivity implements
		OnClickListener {
	public int position = 0;
    
	private Bitmap mCurBitmap;
	
	private String[] m_shootdata;
	private int picSum = 0;
	private ImagePagerAdapter picPagerAdapter;

	// 屏幕宽度
	public static int screenWidth;
	// 屏幕高度
	public static int screenHeight;
	private PicViewPager vp;

	// 图片加载工具
	public ImageLoader imageLoader = ImageLoader.getInstance();
	public DisplayImageOptions options;

	// 控件隐藏状态
	private boolean isVisible = true;
	// 记录上一个页面的位置，用以判断viewpager的滑动方向
	private int lastPosition;
	// 常量标示
	private static final int UPDATE_UI = 0;
	private static final int SET_CONTENT = 1;
	// 是否正在请求数据
	boolean isRequestData = false;
	// 进度提示
	private LinearLayout bannerDot;
	private FrameLayout.LayoutParams vpParams;
	private RelativeLayout.LayoutParams dotParams;
	private List<ImageView> dotList;
	// 消息处理类
	private Handler picViewHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATE_UI:
				// 更新界面
				picPagerAdapter.changeData(m_shootdata);
				isRequestData = false;
				break;

			case SET_CONTENT:
				// 设置控件内容
				setAdapter();
				isRequestData = false;
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_picture);
		Intent intent = getIntent();

		position = intent.getIntExtra("index", 0);
		PicBrowserUtil.setDefaultPicIndex(position);

		m_shootdata = intent.getStringArrayExtra("content");
		// 获取屏幕尺寸
		getScreenSize();
		// 初始化控件
		initViews();
		// 初始化图片加载类
		initImageLoader();
		// 获取传递的数据
		getData();

		// 注册监听器
		setListener();
	}

	private void initImageLoader() {
		options = new DisplayImageOptions.Builder()
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.cacheInMemory(true).cacheOnDisc(true)
				.bitmapConfig(Config.RGB_565).build();
	}

	/**
	 * 注册监听器
	 */
	private void setListener() {
		vp.setOnPageChangeListener(pageChangeListener);
	}

	/**
	 * 为ViewPager添加适配器
	 */
	private void setAdapter() {
		dotList = new ArrayList<ImageView>();
		picPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(),
				m_shootdata);
		vp.setAdapter(picPagerAdapter);
		vp.setOffscreenPageLimit(6);
		vp.setCurrentItem(position, false);
		
		bannerDot.removeAllViews();
		ImageView dotIv = null;
		LinearLayout.LayoutParams paramsMargin = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		int marginRight = getResources().getDimensionPixelSize(
				R.dimen.homepage_banner_dot_margin_rigth);
		paramsMargin.setMargins(0, 0, marginRight, 10);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 10);
		for (int i = 0; i < m_shootdata.length; i++) {
			dotIv = new ImageView(this);
			// 默认第一个为开
			if (i == position) {
				dotIv.setImageResource(R.drawable.banner_dot_on);
			} else {
				dotIv.setImageResource(R.drawable.banner_dot_off);
			}
			if (i ==  m_shootdata.length - 1) {
				dotIv.setLayoutParams(params);
			} else {
				dotIv.setLayoutParams(paramsMargin);
			}
			dotList.add(dotIv);
			bannerDot.addView(dotIv);
		}
		
	}

	
	/**
	 * 获取传递的数据
	 */
	private void getData() {
	
		requestDatas();
	}

	private void requestDatas() {
		isRequestData = true;

		new Thread() {
			@Override
			public void run() {

				picSum = m_shootdata.length;

				picViewHandler.sendEmptyMessage(SET_CONTENT);

			}
		}.start();

	}

	/**
	 * 初始化组件
	 */
	private void initViews() {
		vp = (PicViewPager) findViewById(R.id.pic_view_viewpager);
		bannerDot = (LinearLayout)findViewById(R.id.bannerDot);

		setProgress(position);
	}

	/**
	 * 获取屏幕尺寸
	 */
	private void getScreenSize() {
		DisplayMetrics metrics = new DisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
	}

	/**
	 * ViewPager适配器
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private String[] data;

		public ImagePagerAdapter(FragmentManager fm, String[] data) {
			super(fm);
			this.data = data;
		}

		public void changeData(String[] data) {
			this.data = data;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return data.length;
		}

		@Override
		public Fragment getItem(int position) {
			String url = null;
			try {
				url = data[position];
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		    return PicViewFragment.newInstance(url, position);
			 
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			super.destroyItem(container, position, object);
		}

	}

	@Override
	public void onClick(View v) {

	}

	// 翻页监听
	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {

			position = arg0;

			for (int i = 0; i < dotList.size(); i++) {
				if (arg0 == i) {
					dotList.get(i).setImageResource(R.drawable.banner_dot_on);
				} else {
					dotList.get(i).setImageResource(R.drawable.banner_dot_off);
				}
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	};

}