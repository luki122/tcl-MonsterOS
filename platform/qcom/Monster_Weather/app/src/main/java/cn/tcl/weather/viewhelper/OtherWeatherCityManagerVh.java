package cn.tcl.weather.viewhelper;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gapp.common.utils.BitmapManager;
import com.leon.tools.view.DataCoverUiController;
import com.leon.tools.view.ResLayoutAdapter;

import java.util.List;

import cn.tcl.weather.ActivityFactory;
import cn.tcl.weather.OtherWeatherManagerActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.TclWeatherManagerActivity;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.bean.CityWeatherInfo;
import cn.tcl.weather.bean.DayWeather;
import cn.tcl.weather.internet.StatusWeather;
import cn.tcl.weather.service.ICityManager;
import cn.tcl.weather.service.UpdateService;
import cn.tcl.weather.utils.CommonUtils;
import cn.tcl.weather.utils.ToastUtils;
import cn.tcl.weather.utils.bitmap.AbsBmpLoadItem;
import cn.tcl.weather.utils.bitmap.ActivityBmpLoadManager;
import cn.tcl.weather.view.SwipeLayout;
import cn.tcl.weather.view.list.DragSortListView;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-10-10.
 * $desc
 */
public class OtherWeatherCityManagerVh extends AbsWeatherCityManagerVh implements View.OnClickListener {

    private OtherWeatherManagerActivity mActivity;

    private UpdateService mUpdateService;
    private DragSortListView mCityListView;
    private ImageView mBackButton;
    private ImageView mCityLocateButton; // Start city locate activity
//    private BitmapManager mBitmapManager;

    private ActivityBmpLoadManager mActivityBmpLoadManager;

    private SwipeLayout mShowLayout;

    OtherWeatherCityManagerVh(OtherWeatherManagerActivity activity) {
        super(activity, R.layout.other_weather_manager);
        mActivity = activity;
    }


    public void setUpdateService(UpdateService updateService) {
        mUpdateService = updateService;
        if (null != mUpdateService) {
            mUpdateService.addCityObserver(mCityObserver);
        }
    }


    @Override
    public void init() {
        mActivityBmpLoadManager = WeatherCNApplication.getWeatherCnApplication().getActivityBmpLoadManager();
        mCityLocateButton = findViewById(R.id.add);
        mCityLocateButton.setOnClickListener(this);
//        mBitmapManager = new BitmapManager(mActivity);
//        mBitmapManager.init();

        mBackButton = findViewById(R.id.back_button);
        mCityListView = findViewById(R.id.city_manager_listview);

//        mCityListView.setDragEnabled(false);
        mCityListView.setAdapter(mCityAdapter);
        mCityListView.setDragListener(new DragSortListView.DragListener() {
            @Override
            public void drag(int from, int to) {
            }

            @Override
            public boolean canDraging(int position) {
                if (position >= 0 && position < mCityAdapter.getCount()) {
                    return !mCityAdapter.getItem(position).isLocateCity();
                }
                return true;
            }
        });
        mCityListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                while (mCityAdapter.getItem(to).isLocateCity()) {
                    to++;
                }
                if (from != to) {
                    mUpdateService.changeCityPosition(mCityAdapter.getItem(from), to);
                    mCityAdapter.changePosition(from, to);
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
    }

    @Override
    public void recycle() {
        if (null != mUpdateService) {
            mUpdateService.removeCityObserver(mCityObserver);
            mUpdateService = null;
        }
//        mBitmapManager.recycle();
    }

    @Override
    public void onDonwTouch() {
        if (null != mShowLayout) {
            mShowLayout.hide();
        }
    }


    /**
     * Set item's background
     */
    private void setBackground(CityWeatherInfo weatherInfo, View view) {
        int backgroundId = StatusWeather.getWeatherBackgroundNo(weatherInfo.weatherNo);
//        Bitmap bitmap = mBitmapManager.generateBitmap(backgroundId);
//        Drawable drawable = new BitmapDrawable(bitmap);
//        view.setBackground(drawable);
        mActivityBmpLoadManager.loadBmp(mActivity, new AbsBmpLoadItem.ResFileViewBgLoadItem(view).setResId(backgroundId));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.add:
                // If city number is full, informate users
                if (mUpdateService.isCityFull()) {
                    ToastUtils.show(mActivity, R.string.city_is_too_much, Toast.LENGTH_LONG);
                } else {
                    //Intent cityLocateIntent = new Intent(mActivity, LocateActivity.class);
                    //mActivity.startActivity(cityLocateIntent);
                    ActivityFactory.jumpToActivity(ActivityFactory.LOCATE_ACTIVITY, mActivity, null);
                }
                break;
            default:
                break;
        }
    }

    private ICityManager.CityObserver mCityObserver = new ICityManager.CityObserver() {
        @Override
        protected void onCityListChanged() {
            mCityAdapter.setItemDatas(mUpdateService.listAllCity());
        }
    };


    // Initialize adapter
    private ResLayoutAdapter<City> mCityAdapter = new ResLayoutAdapter<City>(R.layout.other_weather_manager_item) {

        @Override
        protected void convertItemData(final DataCoverUiController<City> ctr, final City city,
                                       final int position, int viewType) {
            CityWeatherInfo weatherInfo = city.getCityWeatherInfo();
            TextView cityName = ctr.findViewById(R.id.city_name);
            TextView weatherState = ctr.findViewById(R.id.weather_state);
            TextView temperature = ctr.findViewById(R.id.temperature);

            List<DayWeather> dayWeathers = city.getCityWeatherInfo().getDayWeathers();
            DayWeather dayWeather = dayWeathers.get(0);

            // Set item's background
            setBackground(weatherInfo, ctr.findViewById(R.id.city_item));

            cityName.setText(city.getCountyName());
            weatherState.setText(StatusWeather.getWeatherStatus(weatherInfo.weatherNo));
            temperature.setText(CommonUtils.getTempString(dayWeather.nightTemp, dayWeather.dayTemp));

            // Set location icon
            ctr.findViewById(R.id.location_icon).setVisibility(city.isLocateCity() ? View.VISIBLE : View.INVISIBLE);

            ctr.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUpdateService.removeCity(city);
                }
            });

            SwipeLayout layout = (SwipeLayout) ctr.getView();
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null == mShowLayout) {
                        ActivityFactory.jumpToActivity(ActivityFactory.MAIN_ACTIVITY, mActivity, null);
                        Message msg = Message.obtain();
                        msg.obj = city;
                        WeatherCNApplication.getWeatherCnApplication().sendMessage(OtherMainActivityVh.SWITCH_PAGE_ACTION, msg);
                    }
                }
            });

            layout.setEnabled(!city.isLocateCity());
            layout.setOnSwipeLayoutListener(new SwipeLayout.OnSwipeLayoutListener() {
                @Override
                public void onShow(SwipeLayout layout) {
                    if (null != mShowLayout && mShowLayout != layout) {
                        mShowLayout.hide();
                    }
                    mShowLayout = layout;
                    ctr.findViewById(R.id.delete).setEnabled(true);
                }

                @Override
                public void onHide(SwipeLayout layout) {
                    if (mShowLayout == layout)
                        mShowLayout = null;
                    ctr.findViewById(R.id.delete).setEnabled(false);
                }
            });
        }
    };
}
