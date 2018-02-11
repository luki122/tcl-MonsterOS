package com.monster.market.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.monster.market.R;
import com.monster.market.activity.AppDetailActivity;
import com.monster.market.bean.AppDetailAnimInfo;
import com.monster.market.bean.BannerInfo;
import com.monster.market.constants.HttpConstant;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.ScreenUtil;
import com.monster.market.utils.SettingUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.monster.market.utils.DensityUtil.dip2px;

/**
 * @ClassName: FrameBannerView
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author billy
 * @date 2014-5-23 下午5:17:43
 *
 */
public class FrameBannerView extends FrameLayout {

	private static final String TAG = "FrameBannerView";
	private static final int HANDLE_CHANGE_INDEX = 100;

	private static final int TURN_TIME = 3500;

	public LoopViewPager bannerViewPager;
	private LinearLayout bannerDot;
	private List<ImageView> dotList;
	private List<BannerInfo> bannerInfos = new ArrayList<BannerInfo>();
	private int index;
	private int size;
	private FrameHandler mHandler;

	private int bannerViewPagerHeight = 0;
	private int marginBottom = 0;
	// 0-图片大于等于3张 1-图片等于2张 2-图片等于1张
	private int pic_dotype = 2;

	private int old_trans_y = 0;
	private int old_index = 0;

	private boolean isRunning = false;

	// 上一次点击item项的时间
	private long lastClickItemTime = 0;

	public FrameBannerView(Context context) {
		super(context);
		initView();
	}

	public FrameBannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	private void initView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.view_framebanner, this);
		bannerViewPager = (LoopViewPager) view
				.findViewById(R.id.bannerViewPager);
		bannerViewPager.removeAllViews();
		bannerViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				switch (action) {
					case MotionEvent.ACTION_DOWN:
						if (isRunning()) {
							stop();
						}
						break;
					case MotionEvent.ACTION_MOVE:
						if (isRunning()) {
							stop();
						}
						break;
					case MotionEvent.ACTION_UP:
						if (!isRunning()) {
							start();
						}
						break;
					case MotionEvent.ACTION_CANCEL:
						if (!isRunning()) {
							start();
						}
						break;
				}
				return false;
			}
		});

		bannerDot = (LinearLayout) view.findViewById(R.id.bannerDot);
		// alpha = view.findViewById(R.id.bannerAlpha);

		ViewPagerScroller scroller = new ViewPagerScroller(getContext());
		scroller.initViewPagerScroll(bannerViewPager);
	}

	/**
	 * @Title: setImages
	 * @Description: 设置显示资源
	 * @param @param video_item
	 * @return void
	 * @throws
	 */
	public void setImages(List<BannerInfo> bannerInfos) {

		this.bannerInfos = bannerInfos;
		initData();
	}

	private void initData() {
		 if (bannerInfos != null && bannerInfos.size() > 0) {
			size = bannerInfos.size();
			initPagerData(bannerInfos.size());
		}
	}

	private void initPagerData(int size) {
		List<View> advPics = new ArrayList<View>();
		//ImageView imageView = null;
		int dot_size = 0;
		if (size == 2) {
			pic_dotype = 1;
			size = 4;
			dot_size = 2;
		} else if (size == 1) {
			pic_dotype = 2;
			dot_size = 0;
		} else {
			pic_dotype = 0;
			dot_size = size;
		}

		for (int i = 0; i < size; i++) {
			View view = LayoutInflater.from(getContext()).inflate(R.layout.view_banner, null);

			advPics.add(view);
		}
		bannerViewPager.setAdapter(new BannerAdapter(advPics));
		dotList = new ArrayList<ImageView>();
		bannerDot.removeAllViews();
		ImageView dotIv = null;
		LinearLayout.LayoutParams paramsMargin = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		int marginRight = getResources().getDimensionPixelSize(
				R.dimen.homepage_banner_dot_margin_rigth);
		paramsMargin.setMargins(0, 0, marginRight, 0);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 0);
		for (int i = 0; i < dot_size; i++) {
			dotIv = new ImageView(getContext());
			// 默认第一个为开
			if (i == 0) {
				dotIv.setImageResource(R.drawable.banner_dot_on);
			} else {
				dotIv.setImageResource(R.drawable.banner_dot_off);
			}
			if (i == dot_size - 1) {
				dotIv.setLayoutParams(params);
			} else {
				dotIv.setLayoutParams(paramsMargin);
			}
			dotList.add(dotIv);
			bannerDot.addView(dotIv);
		}
		bannerViewPager.setOnPageChangeListener(new BannerPageChangeListener(
				advPics));
		bannerViewPager.setCurrentItem(0);
		if (pic_dotype != 2)
			start();
	}

	private class BannerAdapter extends PagerAdapter {

		private List<View> views = null;
		private DisplayImageOptions optionsImage;
		private ImageLoader imageLoader = ImageLoader.getInstance();
		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

		public BannerAdapter(List<View> views) {
			this.views = views;

			optionsImage = new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.banner_loading_default)
					.showImageForEmptyUri(R.drawable.banner_loading_default)
					.showImageOnFail(R.drawable.banner_loading_default)
					.cacheInMemory(true).cacheOnDisk(true).build();
		}

		@Override
		public void destroyItem(View parent, int position, Object obj) {

		}

		public List<View> getViews() {
			return views;
		}

		@Override
		public int getCount() {
			return views.size();// Integer.MAX_VALUE;
		}

		@Override
		public Object instantiateItem(View parent, int position) {
			ImageView iv = (ImageView) views.get(position).findViewById(R.id.imageView);// (ImageView)
			ImageView iv_crop = (ImageView) views.get(position).findViewById(R.id.crop_imageView);
			if (bannerInfos != null && bannerInfos.size() > 0) {
				// iv.setImageResource(images[position]);
				// 开始头像图片异步加载
				if (SettingUtil.isLoadingImage(getContext())) {
					if (pic_dotype == 1) {
						if (position == 2)
							position = 0;
						else if (position == 3)
							position = 1;
					}
					imageLoader.displayImage(bannerInfos.get(position)
									.getIconPath(), new ImageViewAware(iv), optionsImage,
							animateFirstListener);
				}
				else
				{
					iv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.banner_loading_default));
				}
			}

			try {
				((ViewPager) parent).addView(views.get(position));
			} catch (Exception e) {

			}
			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					if (System.currentTimeMillis() - lastClickItemTime > 1000) {
						String bannerType;
						String bannerUrl;

						if(pic_dotype == 1)			// 图片等于2张
						{
							bannerType = bannerInfos.get(
									bannerViewPager.getCurrentItem() % 2)
									.getBannerType();
							bannerUrl = bannerInfos.get(
									bannerViewPager.getCurrentItem() % 2)
									.getBannerUrl();
						}
						else
						{
							bannerType = bannerInfos.get(
									bannerViewPager.getCurrentItem() % views.size())
									.getBannerType();
							bannerUrl = bannerInfos.get(
									bannerViewPager.getCurrentItem() % views.size())
									.getBannerUrl();
						}

						if (bannerType.equals(HttpConstant.AD_TYPE_APP)) {
							Intent detail = new Intent(mContext, AppDetailActivity.class);
							detail.putExtra(AppDetailActivity.PACKAGE_NAME, bannerUrl);
							detail.putExtra(AppDetailActivity.ANIM_PARAMS, getBannerAnimInfo(view));
							mContext.startActivity(detail);
							if (mContext instanceof Activity) {
								((Activity) mContext).overridePendingTransition(0,0);
							}
						}

						lastClickItemTime = System.currentTimeMillis();
					}
				}
			});
			return views.get(position);
		}

		private AppDetailAnimInfo getBannerAnimInfo(View view) {
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			Point point = new Point(0, ScreenUtil.getScreenHeight(mContext) - dip2px(mContext, 151));

			AppDetailAnimInfo animInfo = new AppDetailAnimInfo();
			animInfo.setLayoutInitHeight(dip2px(mContext, 151))
					.setLayoutMarginTop(dip2px(mContext, 156))
					.setIconMarginLeft(dip2px(mContext, 0))
					.setIconMarginTop(dip2px(mContext, 0))
					.setInitIconSize(dip2px(mContext, 0))
					.setFinalIconSize(dip2px(mContext, 0))
					.setCoordinate(point)
					.setType(AppDetailAnimInfo.TYPE_BOTTOM_SLIDE_IN);
			return animInfo;
		}

		private class AnimateFirstDisplayListener extends
				SimpleImageLoadingListener {

			private List<String> displayedImages = Collections
					.synchronizedList(new LinkedList<String>());

			@Override
			public void onLoadingComplete(String imageUri, View view,
										  Bitmap loadedImage) {
				if (loadedImage != null) {
					ImageView imageView = (ImageView) view;

					boolean firstDisplay = !displayedImages.contains(imageUri);
					if (firstDisplay) {
						FadeInBitmapDisplayer.animate(imageView, 500);
						displayedImages.add(imageUri);
					}
				}
			}
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

	}

	private class BannerPageChangeListener implements OnPageChangeListener {

		private List<View> views = null;

		public BannerPageChangeListener(List<View> views) {
			this.views = views;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			if (dotList.size() == 0) {
				return;
			}
			int mPosition = position % dotList.size();
			for (int i = 0; i < dotList.size(); i++) {
				if (mPosition == i) {
					dotList.get(i).setImageResource(R.drawable.banner_dot_on);
				} else {
					dotList.get(i).setImageResource(R.drawable.banner_dot_off);
				}
			}
			index = position;
		}

	}

	/**
	 * @Title: start
	 * @Description: 开始切换动画
	 * @param
	 * @return void
	 * @throws
	 */
	public void start() {
		LogUtil.i(TAG, "FrameBannerView start()");
		if (mHandler == null) {
			mHandler = new FrameHandler(this);
		}
		isRunning = true;
		mHandler.removeMessages(HANDLE_CHANGE_INDEX);
		mHandler.sendEmptyMessageDelayed(HANDLE_CHANGE_INDEX, TURN_TIME);
	}

	/**
	 * @Title: stop
	 * @Description: 停止切换动画
	 * @param
	 * @return void
	 * @throws
	 */
	public void stop() {
		LogUtil.i(TAG, "FrameBannerView stop()");
		if (mHandler != null) {
			mHandler.removeMessages(HANDLE_CHANGE_INDEX);
		}
		isRunning = false;
	}

	/**
	 * @Title: exit
	 * @Description: 退出
	 * @param
	 * @return void
	 * @throws
	 */
	public void exit() {
		if (mHandler != null) {
			mHandler.removeMessages(HANDLE_CHANGE_INDEX);
			mHandler = null;
		}
	}

	private static class FrameHandler extends Handler {

		private WeakReference<FrameBannerView> frame;

		public FrameHandler(FrameBannerView m_frame) {
			frame = new WeakReference<FrameBannerView>(m_frame);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HANDLE_CHANGE_INDEX:
					FrameBannerView vm = frame.get();
					if (vm != null && vm.isRunning()) {
						vm.index++;
//						vm.bannerViewPager.setCurrentItem(vm.index);

						vm.bannerViewPager.setCurrentItem(vm.index, true);
						sendEmptyMessageDelayed(HANDLE_CHANGE_INDEX, TURN_TIME);
					}
					break;
			}
		}

	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getPic_dotype() {
		return pic_dotype;
	}

	public int getLoopBannerHeight() {
		if (bannerViewPager != null) {
			return bannerViewPager.getHeight();
		}
		return 0;
	}

	/**
	 * ViewPager 滚动速度设置
	 *
	 */
	public class ViewPagerScroller extends Scroller {

		private int mScrollDuration = 700;             // 滑动速度

		/**
		 * 设置速度速度
		 * @param duration
		 */
		public void setScrollDuration(int duration){
			this.mScrollDuration = duration;
		}

		public ViewPagerScroller(Context context) {
			super(context);
		}

		public ViewPagerScroller(Context context, Interpolator interpolator) {
			super(context, interpolator);
		}

		public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
			super(context, interpolator, flywheel);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy, int duration) {
			super.startScroll(startX, startY, dx, dy, mScrollDuration);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy) {
			super.startScroll(startX, startY, dx, dy, mScrollDuration);
		}

		public void initViewPagerScroll(ViewPager viewPager) {
			try {
				Field mScroller = ViewPager.class.getDeclaredField("mScroller");
				mScroller.setAccessible(true);
				mScroller.set(viewPager, this);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
