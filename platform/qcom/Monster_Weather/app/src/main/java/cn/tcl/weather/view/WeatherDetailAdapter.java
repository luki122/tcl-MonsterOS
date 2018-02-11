package cn.tcl.weather.view;

import android.graphics.Canvas;

import java.util.ArrayList;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-25.
 * this class is for {@link WeatherDetailView}
 */
public class WeatherDetailAdapter {

    private ArrayList<WeatherDetailItem> mItems = new ArrayList<>(30);

    private WeatherDetailView mDetailView;
    private int mItemWidth;
    private int mItemHeight;

    private float mMaxValue = Float.MIN_VALUE;
    private float mMinValue = Float.MAX_VALUE;


    void setWeatherDetailView(WeatherDetailView view) {
        mDetailView = view;
    }

    void layout(int width, int height) {
        mItemWidth = width;
        mItemHeight = height;
        for (WeatherDetailItem item : mItems) {
            item.setParent(mDetailView);
            item.layout(mItemWidth, mItemHeight, getMaxValue(), getMinValue());
        }
    }


    public int getCount() {
        return mItems.size();
    }


    public void addItem(WeatherDetailItem item) {
        mItems.add(item);
        mMaxValue = Math.max(mMaxValue, item.getMaxValue());
        mMinValue = Math.min(mMinValue, item.getMinValue());
        notifyDataSetChanged();
    }

    public float getMaxValue() {
        return mMaxValue + 1.0f;
    }

    public float getMinValue() {
        return mMinValue - 1.0f;
    }


    public void notifyDataSetChanged() {
        if (null != mDetailView)
            mDetailView.notifyDataSetChanged();
    }


    public WeatherDetailItem getItem(int index) {
        return mItems.get(index);
    }


    /**
     * this is a item for {@link WeatherDetailAdapter}
     */
    public static abstract class WeatherDetailItem {

        protected WeatherDetailView mParent;

        void setParent(WeatherDetailView view) {
            mParent = view;
        }

        /**
         * get the value of item
         *
         * @return
         */
        public abstract float getMaxValue();

        public abstract float getMinValue();

        public abstract int getIconId();

        /**
         * when view is layout , this will be called
         *
         * @param width
         * @param height
         * @param maxValue
         */
        protected abstract void layout(int width, int height, float maxValue, float minValue);

        /**
         * when view is drawing, this will be called
         *
         * @param canvas
         */
        protected abstract void draw(Canvas canvas, int start, int current);

    }
}
