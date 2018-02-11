package cn.tcl.music.view;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import mst.widget.PagerAdapter;
import mst.widget.ViewPager;
import mst.widget.ViewPager.OnPageChangeListener;

import android.text.TextUtils;
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
import cn.tcl.music.activities.live.AlbumDetailActivity;
import cn.tcl.music.activities.live.OnlinePlayListDetailActivity;
import cn.tcl.music.activities.live.SingerDetailActivity;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.live.LiveMusicBannerItem;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.Util;

public class BannerFlipView extends FrameLayout {
    private static final String TAG = BannerFlipView.class.getSimpleName();
    private Context mContext;
    private ViewPager mViewPager;
    private FlipAdapter mflipAdapter;
    private LinearLayout mIndicator;
    private ImageView mIndexItem;
    private List<LiveMusicBannerItem> mResUrls = new ArrayList<LiveMusicBannerItem>();
    private Timer mTimer;
    private boolean mIsRun;
    private int mCircleRes[] = new int[]{R.drawable.page_on, R.drawable.page_off};

    public BannerFlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    private BannerFlipView(Context context) {
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
     * @param urls picture url
     */
    public void setDisplayImgs(List<LiveMusicBannerItem> urls) {
        if (urls != null && mflipAdapter != null) {
            mResUrls.clear();
            mResUrls.addAll(urls);
            mViewPager.setAdapter(mflipAdapter);
            mflipAdapter.notifyDataSetChanged();
            addIndicator();
        }
    }


    /**
     * init view
     */
    private void initView() {
        mViewPager = new ViewPager(mContext);
        mViewPager.setOffscreenPageLimit(5);
        mflipAdapter = new FlipAdapter(mContext, mViewPager, mResUrls);
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

        if (mResUrls != null) {
            int size = mResUrls.size();
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
                pos = pos % mResUrls.size();

                if (pos < 0) {
                    pos = pos + mResUrls.size();
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
        private List<LiveMusicBannerItem> mUrls;
        private ViewPager pager;

        public FlipAdapter(Context context, ViewPager pager, List<LiveMusicBannerItem> urls) {
            mContext = context;
            mUrls = urls;
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
        public Object instantiateItem(ViewGroup container, int position) {
            if (container == null) {
                return null;
            }

            ImageView img = new ImageView(mContext);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            img.setLayoutParams(params);
            img.setScaleType(ScaleType.CENTER_CROP);

            if (mUrls == null || mUrls.size() < 1) {
                img.setImageResource(R.drawable.default_banner);
                container.addView(img);
                return img;
            }
            final int pos;

            if (position < 0) {
                pos = mUrls.size() + position;
            } else {
                pos = position % mUrls.size();
            }

            final String picUrl = mUrls.get(pos).pic_url_yasha;
            final String content = mUrls.get(pos).url;
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
                    gotoDetail(picUrl, content, pos);
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
        if (mResUrls == null || mResUrls.size() < 1) {
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
    private void gotoDetail(String picUrl, String content, int position) {

        if (TextUtils.isEmpty(picUrl) || TextUtils.isEmpty(content)) {
            return;
        }

        String contentType = "";
        String id = "";

        try {
            contentType = content.substring(0, content.indexOf(":"));
            int size = content.length();
            id = content.substring(content.indexOf(":") + 1, size);
        } catch (Exception e) {
            e.printStackTrace();
            //说明服务器出现异常了或者传递数据有问题
            return;
        }

        //精选集
        if ("collect".equals(contentType)) {
            OnlinePlayListDetailActivity.launch((Activity) mContext, id, null, 0);
        }
        //专辑
        else if ("album".equals(contentType)) {
            AlbumDetailActivity.launch((Activity) mContext, id, "", 0);
        }
        //歌手
        else if ("artist".equals(contentType)) {
            SingerDetailActivity.launch((Activity) mContext, id, null, 0, 0);
        }
        //歌曲
        else if ("song".equals(contentType)) {
            new LiveMusicPlayTask(getContext()).playBySongId(id);
        }
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
