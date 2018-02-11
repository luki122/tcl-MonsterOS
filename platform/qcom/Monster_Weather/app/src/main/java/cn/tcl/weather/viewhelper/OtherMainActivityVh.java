package cn.tcl.weather.viewhelper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.leon.tools.view.AndroidUtils;
import com.leon.tools.view.UiController;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.weather.ActivityFactory;
import cn.tcl.weather.MainActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.adapter.MainPagerAdapter;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.service.ICityManager;
import cn.tcl.weather.service.ICityManagerSupporter;
import cn.tcl.weather.service.UpdateService;
import cn.tcl.weather.utils.IBoardcaster;
import cn.tcl.weather.utils.LogUtils;
import cn.tcl.weather.utils.ToastUtils;
import cn.tcl.weather.view.CustomSwipeRefreshLayout;
import cn.tcl.weather.view.PagerView;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-22.
 * $desc
 */
public class OtherMainActivityVh extends UiController implements MainActivity.IMainVh, View.OnClickListener, CustomSwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = OtherMainActivityVh.class.getName();

    public final static String SWITCH_PAGE_ACTION = "main.switch.page";

    /*view in other_main_activity.xml*/
    private CustomSwipeRefreshLayout mRefreshLayout;
    private ImageView mIvMenu;
    private PagerView mViewPager;
    private int mCurrentPosition;

    private MainPagerAdapter mAdapter;

    private List<City> mCityList = new ArrayList<>(10);


    private MyIndictor mIndictor;


    private MainActivity mActivity;

    private UpdateService mUpdateService;

    OtherMainActivityVh(MainActivity activity) {
        super(activity, R.layout.other_main_activity);
        mActivity = activity;
    }

    public void init() {
        initView();
        setViewPagerAdapter();
    }

    public void setUpdateService(UpdateService updateService) {
        mUpdateService = updateService;
        initService();
        WeatherCNApplication.getWeatherCnApplication().regiestOnReceiver(VerticalScrollBehavior.ACTION, mStatausReceiver);
        WeatherCNApplication.getWeatherCnApplication().regiestOnReceiver(VerticalScrollBehavior.ACTION_MOVING, mIndictor);
        WeatherCNApplication.getWeatherCnApplication().regiestOnReceiver(SWITCH_PAGE_ACTION, mReceiver);
    }


    private void initService() {
        mUpdateService.addCityObserver(mCityObserver);
        mCityList = mUpdateService.listAllCity();
        mAdapter.setCityLists(mCityList);
        mViewPager.scrollToPage(mCurrentPosition);
    }

    public void recycle() {
        WeatherCNApplication.getWeatherCnApplication().unregiestOnReceiver(SWITCH_PAGE_ACTION, mReceiver);
        WeatherCNApplication.getWeatherCnApplication().unregiestOnReceiver(VerticalScrollBehavior.ACTION_MOVING, mIndictor);
        WeatherCNApplication.getWeatherCnApplication().unregiestOnReceiver(VerticalScrollBehavior.ACTION, mStatausReceiver);

        if (null != mUpdateService) {
            mUpdateService.removeCityObserver(mCityObserver);
        }
    }

    private void setViewPagerAdapter() {
        mAdapter = new MainPagerAdapter(mActivity, this, mViewPager);
        mViewPager.setPagerAdapter(mAdapter);
    }

    public void resume() {
        if (null != mAdapter) {
            mAdapter.resume();
        }
    }

    public void pause() {
        if (null != mAdapter) {
            mAdapter.pause();
        }
    }

    @Override
    public void onRefresh() {
        LogUtils.i(TAG, "onRefresh ... ");
        City currentCity = mAdapter.getCurrentCity();
        mUpdateService.requestRefreshCity(currentCity, new ICityManagerSupporter.OnRequestRefreshListener() {
            @Override
            public void onRefreshing(City city, int state) {
                if (state != ICityManagerSupporter.OnRequestRefreshListener.REFRESH_STATE_START &&
                        state != ICityManagerSupporter.OnRequestRefreshListener.REFRESH_STATE_REFRESHING &&
                        state != ICityManagerSupporter.OnRequestRefreshListener.REFRESH_STATE_RECYCLED) {
                    mRefreshLayout.setRefreshing(false);
                    if (state == ICityManagerSupporter.OnRequestRefreshListener.REFRESH_STATE_SUCCEED) {
                        ToastUtils.show(mActivity, mActivity.getResources().getString(R.string.update_result_suc), Toast.LENGTH_SHORT);
                    } else if (state == ICityManagerSupporter.OnRequestRefreshListener.REFRESH_STATE_FAILED) {
                        ToastUtils.show(mActivity, mActivity.getResources().getString(R.string.update_result_fail), Toast.LENGTH_SHORT);
                    }
                    mCityList = mUpdateService.listAllCity();
                    mAdapter.setCityLists(mCityList);
                    mViewPager.scrollToPage(mCurrentPosition);
                }
            }
        });
    }

    /**
     * set page index for pager view
     *
     * @param index
     */
    public void setCurrentPosition(int index) {
        mCurrentPosition = index;
        AbsMainCityWeatherVh vh = mAdapter.getCurrentVh();
        if (null != vh)// set current ignoreView
            mRefreshLayout.setIgnoreView(vh.getIgnoreView());
    }


    private void initView() {
        mRefreshLayout = findViewById(R.id.swipe_refresh);
        mRefreshLayout.setOnRefreshListener(this);

        mIvMenu = findViewById(R.id.iv_menu);
        mIvMenu.setOnClickListener(this);

        mViewPager = findViewById(R.id.viewpager);

        mIndictor = new MyIndictor();
        mViewPager.setIndictor(mIndictor);

        //adapt the UI
        //Bitmap bitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.test_detail);
        //Bitmap mBitmap = Bitmap.createBitmap(bitmap, 0, 0, 1080 * 3, 1776 * 3, null, false);
        //BitmapDrawable bitmapDrawable = new BitmapDrawable(mActivity.getResources(), mBitmap);
        //findViewById(R.id.main_activity_layout).setBackground(bitmapDrawable);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_menu:
                //Intent cityManageIntent = new Intent(mActivity, TclWeatherManagerActivity.class);
                //mActivity.startActivity(cityManageIntent);
                ActivityFactory.jumpToActivity(ActivityFactory.WEAHTER_MANAGER_ACTIVITY, mActivity, null);
                break;
            default:
                break;
        }
    }


    private ICityManager.CityObserver mCityObserver = new ICityManager.CityObserver() {


        @Override
        protected void onCityListChanged() {
            LogUtils.i(TAG, "onCityListChanged...");
            mCityList = mUpdateService.listAllCity();
            mAdapter.setCityLists(mCityList);
            mViewPager.scrollToPage(mCurrentPosition);
            if (mCityList.size() == 0) {
                mRefreshLayout.setEnabled(false);
            } else {
                mRefreshLayout.setEnabled(true);
            }
        }

    };


    private IBoardcaster.Receiver mReceiver = new IBoardcaster.Receiver() {

        @Override
        public void onReceived(String action, Message msg) {
            LogUtils.i(TAG, "onReceived:" + action);
            mCityList = mUpdateService.listAllCity();
            mAdapter.setCityLists(mCityList);
            City city = (City) msg.obj;
            mViewPager.scrollToPage(mAdapter.indexOfCity(city));
//            mViewPager.setToPage(mAdapter.indexOfCity(city));
        }
    };

    private IBoardcaster.Receiver mStatausReceiver = new IBoardcaster.Receiver() {
        @Override
        public void onReceived(String action, Message msg) {
            if (msg.what == VerticalScrollBehavior.MESSAGE_WHAT_SHOW) {
                mRefreshLayout.setEnabled(true);
            } else {
                mRefreshLayout.setEnabled(false);
            }
        }
    };


    private class MyIndictor extends PagerView.Indictor implements IBoardcaster.Receiver {

        private final static float POINT_HEIGHT = 412f;
        private final static float ITEM_WIDTH = 3f;
        private final static float SKIP_WIDTH = 7f;
        private final static float LOCATE_POINT_WIDTH = 7f;

        private final float SQRT_2 = (float) (Math.sqrt(2) / 2);

        private Paint mPointPaint;


        private int mItemWidth;
        private int mSkipWidth;
        private int mPointWidth;

        private Path mLocatedPath = new Path();


        private float mOffsetY;

        public MyIndictor() {
            initPaint();
        }

        private void initPaint() {
            mPointPaint = new Paint();
            mPointPaint.setStyle(Paint.Style.FILL);
            mPointPaint.setAntiAlias(true);
        }


        @Override
        protected void layout(int parentW, int parentH, int childCounts) {
            mOffsetY = AndroidUtils.dip2px(mActivity, POINT_HEIGHT);
            if (mTop == 0) {
                mTop = mOffsetY;
            }
            mItemWidth = (int) AndroidUtils.dip2px(mActivity, ITEM_WIDTH);
            mSkipWidth = (int) AndroidUtils.dip2px(mActivity, SKIP_WIDTH);
            int totalWidth = childCounts * (mItemWidth + mSkipWidth) - mSkipWidth;
            /*because the point is align the right of the rect , we add 12 to layout in center*/
            mLeft = (parentW - totalWidth) / 2;
            mDrawingRect.set(0, 0, totalWidth, mSkipWidth + mItemWidth);


            mPointWidth = (int) AndroidUtils.dip2px(mActivity, LOCATE_POINT_WIDTH);
            float halfW = mPointWidth / 2.0f;
            float sqrt2 = SQRT_2 * mPointWidth;
            mLocatedPath.reset();
            mLocatedPath.moveTo(5, 0);
            mLocatedPath.lineTo(5 - halfW, 0);
            mLocatedPath.lineTo(5 + sqrt2, sqrt2);
            mLocatedPath.lineTo(5, -halfW);
            mLocatedPath.lineTo(5, 0);
        }

        @Override
        protected void onScrolled(int position) {
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int currentIndex = mCurrentPosition;
            /* if there is some cities , draw correct num black point*/
            final int size = mCityList.size();
            final int width = mItemWidth + mSkipWidth;
            final int hafWidth = mItemWidth / 2;
            for (int i = 0; i < size; i++) {
                int left = i * width;
                canvas.save();
                canvas.translate(left + hafWidth, mSkipWidth);

                mPointPaint.setColor(i == currentIndex ? 0xb2000000 : 0x33000000);

                if (mCityList.get(i).isLocateCity()) {
                    final int translate = (mPointWidth - mItemWidth) / 2;
                    canvas.translate(-translate, translate);//pointer translate
                    canvas.rotate(-90);// rotate 90 digree
                    canvas.drawPath(mLocatedPath, mPointPaint);
                } else {
                    canvas.drawCircle(0, 0, hafWidth, mPointPaint);
                }
                canvas.restore();
            }
        }

        @Override
        public void onReceived(String action, Message msg) {
            translateTo(mLeft, mOffsetY - (Float) msg.obj - msg.getData().getFloat(VerticalScrollBehavior.PROGRESS_KEY) * AndroidUtils.dip2px(mParent.getContext(), 70f / 3));
        }
    }
}
