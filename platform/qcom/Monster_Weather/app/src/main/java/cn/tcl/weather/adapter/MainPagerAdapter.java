package cn.tcl.weather.adapter;

import android.view.MotionEvent;
import android.view.View;

import com.gapp.common.utils.BitmapManager;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.weather.MainActivity;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.view.PagerView;
import cn.tcl.weather.viewhelper.AbsMainCityWeatherVh;
import cn.tcl.weather.viewhelper.OtherMainActivityVh;
import cn.tcl.weather.viewhelper.VhFactory;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-2.
 * $desc
 */
public class MainPagerAdapter extends PagerView.PagerViewAdapter {

    private List<City> mCities = new ArrayList<>(8);
    private MainActivity mActivity;
    private PagerView mPagerView;

    private BitmapManager mBitmapManager;

    private AbsMainCityWeatherVh mCurrentVh;

    private OtherMainActivityVh mParent;

    public MainPagerAdapter(MainActivity activity, OtherMainActivityVh parent, PagerView pagerView) {
        mActivity = activity;
        mParent = parent;
        mPagerView = pagerView;
        mBitmapManager = new BitmapManager(activity);
        mBitmapManager.init();
    }

    public void setCityLists(List<City> cityes) {
        mCities.clear();
        mCities.addAll(cityes);
        notifyDataSetChanged();
    }


    public int indexOfCity(City city) {
        int i = 0;
        for (City c : mCities) {
            if (c.getLocationKey().equals(city.getLocationKey())) {
                return i;
            }
            i++;
        }
        return i;
    }

    @Override
    protected boolean canScrollHorizontally(int direction, int currentPosition, MotionEvent downEvent) {
        AbsMainCityWeatherVh vh = (AbsMainCityWeatherVh) mPagerView.getChildAt(currentPosition).getTag();
        return vh.canScrollHorizontally(direction, downEvent);
    }

    @Override
    public int getCount() {
        return mCities.size();
    }

    @Override
    public View getView(View contentView, int index) {
        if (null == contentView) {
            AbsMainCityWeatherVh vh;
            if (mCities.get(index).isExternalCity()) {
                vh = VhFactory.newVhInstance(VhFactory.MAIN_EXTERNAL_CITY_WEATHER_VH, mActivity, mBitmapManager);
            } else {
                vh = VhFactory.newVhInstance(VhFactory.MAIN_INTERNAL_CITY_WEATHER_VH, mActivity, mBitmapManager);
            }
            vh.init();
            contentView = vh.getView();
            contentView.setTag(vh);
        }
        AbsMainCityWeatherVh vh = (AbsMainCityWeatherVh) contentView.getTag();
        vh.setCityInfo(mCities.get(index));
        return contentView;
    }

    @Override
    protected void onPageChanged(int oldIndex, int currentIndex) {
        View current = mPagerView.getChildView(currentIndex);
        AbsMainCityWeatherVh currentVH = null;
        if (null != current) {
            currentVH = (AbsMainCityWeatherVh) current.getTag();
        }
        if (mCurrentVh != currentVH) {
            if (null != mCurrentVh) {
                mCurrentVh.pause();
            }
            mCurrentVh = currentVH;
            if (null != mCurrentVh) {
                mCurrentVh.resume();
            }
        }
        mParent.setCurrentPosition(currentIndex);
    }


    public void pause() {
        if (null != mCurrentVh) {
            mCurrentVh.pause();
        }
    }

    public void resume() {
        if (null != mCurrentVh) {
            mCurrentVh.resume();
        }
    }

    public AbsMainCityWeatherVh getCurrentVh() {
        return mCurrentVh;
    }

    public City getCurrentCity() {
        if (null != mCurrentVh) {
            return mCurrentVh.getCityInfo();
        }
        return null;
    }

}
