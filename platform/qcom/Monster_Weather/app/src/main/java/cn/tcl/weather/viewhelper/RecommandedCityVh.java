/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.viewhelper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.security.keystore.AndroidKeyStoreKeyFactorySpi;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leon.tools.view.AndroidUtils;
import com.leon.tools.view.DataCoverUiController;
import com.leon.tools.view.ResLayoutAdapter;
import com.leon.tools.view.UiController;

import java.util.List;

import cn.tcl.weather.ILocateActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.service.ICityManagerSupporter;
import cn.tcl.weather.service.UpdateService;
import cn.tcl.weather.utils.IManager;
import cn.tcl.weather.view.TclCustomTextView;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-5.
 * $desc
 */
public class RecommandedCityVh extends UiController implements IManager {
    private ViewGroup mRecommandedCityGroup;
    private GridView mRecommandedCityGridView;
    private ILocateActivity mActivity;
    private Resources mResources;
    private List<City> mCities;

    // Get weather update service
    private UpdateService mUpdateService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        // Service connected
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUpdateService = ((UpdateService.UpdateBinder) service).getService();
            mCities = mUpdateService.listAllCity();
            if (null != mUpdateService) {
                mUpdateService.requestHotCities(new ICityManagerSupporter.OnRequestCityListListener() {
                    @Override
                    public void onSucceed(List<City> cityList) {
                        setCities(cityList);
                    }

                    @Override
                    public void onFailed(int state) {

                    }
                });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (null != mUpdateService) {
                mUpdateService = null;
            }
        }
    };

    private void bindUpdateService() {
        Intent bindServiceIntent = new Intent((Activity) mActivity, UpdateService.class);
        ((Activity) mActivity).bindService(bindServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Constructe function
     */
    public RecommandedCityVh(ILocateActivity activity, ViewGroup recommandedCityGroup, UpdateService updateService) {
        super(new GridView(((Activity)activity)));
        mActivity = activity;
        mRecommandedCityGroup = recommandedCityGroup;
        mUpdateService = updateService;

        // Get resource
        mResources = ((Activity) mActivity).getResources();
    }

    @Override
    public void init() {
        initViews();
        mCities = mUpdateService.listAllCity();
        if (null != mUpdateService) {
            mUpdateService.requestHotCities(new ICityManagerSupporter.OnRequestCityListListener() {
                @Override
                public void onSucceed(List<City> cityList) {
                    setCities(cityList);
                }

                @Override
                public void onFailed(int state) {

                }
            });
        }
    }


    private void initViews() {
        // Set adapter to recommandedcity
        mRecommandedCityGridView = (GridView) getView();
        mRecommandedCityGridView.setNumColumns(3);

        // Set parameters to mRecommandedCityGroup
        mRecommandedCityGroup.removeAllViews();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        // Set margins
        layoutParams.setMargins(60, 123, 60, 0);
        mRecommandedCityGridView.setLayoutParams(layoutParams);

        // Set click effects
        mRecommandedCityGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        mRecommandedCityGridView.setAdapter(mAdapter);
        mRecommandedCityGridView.setOnItemClickListener(mAdapter);

        // Add GridView to location layout
        mRecommandedCityGroup.addView(mRecommandedCityGridView);

        // Add "Recommand" topic
        TclCustomTextView recommandText = new TclCustomTextView((Activity) mActivity);
        recommandText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        recommandText.setText(mResources.getText(R.string.hot));
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(60, 60, 0, 0);
        recommandText.setLayoutParams(textParams);
        recommandText.setTextColor(0x4D000000);
        mRecommandedCityGroup.addView(recommandText);
    }

    // Set cities to adapter
    public void setCities(List<City> cities) {
        mAdapter.setItemDatas(cities);
    }

    @Override
    public void recycle() {
        mRecommandedCityGroup.removeAllViews();
//        ((Activity) mActivity).unbindService(mServiceConnection);
    }

    /**
     * Px to sp
     *
     * @param
     */
    public float px2sp(int px) {
        float fontScale = mResources.getDisplayMetrics().scaledDensity;
        return (px / fontScale + 0.5f);
    }

    @Override
    public void onTrimMemory(int level) {

    }

    public void show() {
        mRecommandedCityGroup.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mRecommandedCityGroup.setVisibility(View.INVISIBLE);
    }


    private ResLayoutAdapter<City> mAdapter = new ResLayoutAdapter<City>(R.layout.other_recommanded_cities_item) {
        @Override
        protected void convertItemData(final DataCoverUiController<City> ctr, final City city, final int position, final int viewType) {

            // Set color to grey
            TextView textView = ctr.findViewById(R.id.recommand_city);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.addCity(city);
                }
            });
            ctr.setTextToTextView(R.id.recommand_city, city.getCountyName());

            // Judge whether the city was added
            if (isAddedCity(city)) {
                textView.setTextColor(0xFF19A8AE);
            }
        }

        private boolean isAddedCity(City city) {
            if (null != mCities) {
                for (int i = mCities.size() - 1; i >= 0; i--) {
                    if (city.getLocationKey().equals(mCities.get(i).getLocationKey())) {
                        return true;
                    }
                }
            }
            return false;
        }

    };

}
