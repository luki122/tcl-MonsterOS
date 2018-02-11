package cn.tcl.music.view;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import mst.widget.PagerAdapter;
import mst.widget.ViewPager;
import mst.widget.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.ScenesDetailActivity;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.live.LiveMusicSceneListBean;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.Util;

public class BannerFlipView_v1 extends FrameLayout {
    private static final String TAG = BannerFlipView_v1.class.getSimpleName();
    private Context mContext;
    private ViewPager mViewPager;
    private FlipAdapter mflipAdapter;
    private LinearLayout mIndicator;
    private ImageView mIndexItem;
    private List<LiveMusicSceneListBean> mList = new ArrayList<LiveMusicSceneListBean>();
    private Timer mTimer;
    private boolean mIsRun = false;
    private int mCircleRes[] = new int[]{R.drawable.page_on, R.drawable.page_off};


    public BannerFlipView_v1(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    private BannerFlipView_v1(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }


    /**
     * set display imgs
     *
     * @param list picture url
     */
    public void setDisplayImgs(List<LiveMusicSceneListBean> list) {
        if (list != null && mflipAdapter != null) {
            mList.clear();
            mList.addAll(list);
            mViewPager.setAdapter(mflipAdapter);
            mflipAdapter.notifyDataSetChanged();

            addIndicator();
        }
    }


    /**
     * init view
     */
    private void initView() {
        //show img
        mViewPager = new ViewPager(mContext);
        mViewPager.setOffscreenPageLimit(4);
        mflipAdapter = new FlipAdapter(mContext, mViewPager, mList);
        mViewPager.setAdapter(mflipAdapter);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        addView(mViewPager, params);

        mIsRun = false;
    }

    /**
     * add indicator
     */
    private void addIndicator() {
        int childCount = getChildCount();

        if (childCount > 1) {
            removeViewAt(1);
        }

        mIndicator = new LinearLayout(mContext);
        int value = getResources().getDimensionPixelSize(R.dimen.dp_4);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = Util.dip2px(mContext, 6);
        mIndicator.setLayoutParams(params);
        mIndicator.setOrientation(LinearLayout.HORIZONTAL);
        mIndicator.setGravity(Gravity.CENTER_VERTICAL);

        if (mList != null) {
            int size = 4;
            LinearLayout.LayoutParams paramItem
                    = new LinearLayout.LayoutParams(value, value);

            for (int i = 0; i < size; i++) {
                mIndexItem = new ImageView(mContext);
                if (i > 0) {
                    paramItem.leftMargin = Util.dip2px(mContext, 6);
                    mIndexItem.setImageResource(mCircleRes[1]);
                } else {
                    mIndexItem.setImageResource(mCircleRes[0]);
                }

                mIndexItem.setLayoutParams(paramItem);
                mIndicator.addView(mIndexItem);
                mIndexItem = null;
            }
        }

        addView(mIndicator);
    }

    /**
     * set crt indicator
     */
    private void setCrtIndicator(int pos) {
        if (pos < 0) {
            return;
        }

        if (mIndicator != null) {
            int count = mIndicator.getChildCount();

            try {
                pos = pos % mList.size();

                if (pos < 0) {
                    pos = pos + mList.size();
                }

                for (int i = 0; i < count; i++) {
                    if (pos == i) {
                        ((ImageView) mIndicator.getChildAt(i)).setImageResource(mCircleRes[0]);
                    } else {
                        ((ImageView) mIndicator.getChildAt(i)).setImageResource(mCircleRes[1]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * flip adapter
     */
    class FlipAdapter extends PagerAdapter implements OnPageChangeListener {
        private Context mContext;
        private List<LiveMusicSceneListBean> mList;
        private ViewPager pager;

        public FlipAdapter(Context context, ViewPager pager, List<LiveMusicSceneListBean> list) {
            mContext = context;
            mList = list;
            this.pager = pager;

            if (pager != null) {
                pager.setOnPageChangeListener(this);
            }
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object != null) {
                try {
                    container.removeView((View) object);
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                }
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            if (container == null) {
                return null;
            }

            ImageView img = new ImageView(mContext);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            img.setLayoutParams(params);
            img.setScaleType(ScaleType.CENTER_CROP);

            if (mList == null || mList.size() < 1) {
                img.setImageResource(R.drawable.default_banner);
                container.addView(img);
                return img;
            }
            final int pos;

            if (position < 0) {
                pos = mList.size() + position;
            } else {
                pos = position % mList.size();
            }

            final String picUrl = mList.get(pos).logo;//logo
            if (!MusicApplication.getApp().isDataSaver()) {
                Glide.with(getContext())
                        .load(picUrl)
                        .placeholder(R.drawable.default_banner)
                        .into(img);
            } else {
                Glide.with(getContext()).load("").placeholder(R.drawable.default_banner).into(img);
            }

            img.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    //MobclickAgent.onEvent(getContext(), MobConfig.MAIN_BANNER+pos);
//                    final MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(mContext);
//                    if (null != dialog && dialog.showWrapper()) {
//                        return;
//                    }
                    gotoDetail(mList.get(pos),position);
                }
            });


            container.addView(img);
            return img;
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {


        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int pos) {
            setCrtIndicator(pos);
        }

    }


    /**
     * start flip
     */
    public void startFlip() {
        if (mList == null || mList.size() < 1) {
            return;
        }

        if (mIsRun) {
            return;
        }

        mIsRun = true;
        mTimer = new Timer();

        TimerTask mTimerTask = new TimerTask() {

            @Override
            public void run() {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            int crtPage = mViewPager.getCurrentItem();
                            ++crtPage;
                            mViewPager.setCurrentItem(crtPage);
                        } catch (Exception e) {
                            LogUtil.e(TAG, e.toString());
                        }
                    }
                });
            }
        };

        mTimer.schedule(mTimerTask, 3000, 6000);
    }

    /**
     * stop flip
     */
    public void stopFlip() {
        mIsRun = false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;

            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
        }

    }

    /**
     * go to detail
     */
    private void gotoDetail(LiveMusicSceneListBean bean, int position) {
        ScenesDetailActivity.launch((Activity)mContext, bean);
    }

    /**
     * mHandler
     */
    private Handler mHandler = new Handler();

    private boolean isNetWorkConnect(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                LogUtil.e(TAG, String.valueOf(networkInfo.isConnected()));
                return networkInfo.isConnected();
            }
        }
        return false;
    }
}
