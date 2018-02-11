package cn.tcl.weather.viewhelper;

import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.MstSearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.leon.tools.view.DataCoverUiController;
import com.leon.tools.view.ResLayoutAdapter;

import java.util.List;

import cn.tcl.weather.ActivityFactory;
import cn.tcl.weather.R;
import cn.tcl.weather.TclLocateActivity;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.service.ICityManager;
import cn.tcl.weather.service.ICityManagerSupporter;
import cn.tcl.weather.service.UpdateService;
import mst.widget.toolbar.Toolbar;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-10.
 * TCL rom use
 */
public class TclAddCityVh extends AbsAddCityVh implements View.OnClickListener {
    private final static int DELAY_TIME = 500;
    private final static int DELAY_TIME2 = 150;
    private TclLocateActivity mTclLocateActivity;

    private String mSearchName;
    private MstSearchView mMstSearchView;
    private Toolbar mToolbar;
    private Handler mHandler = new Handler();
    private RecommandedCityVh mRecommandedCityVh;
    private ProgressDialogVh mProgressDialogVh;
    private UpdateService mUpdateService;

    TclAddCityVh(TclLocateActivity activity) {
        super(activity, R.layout.tcl_add_location_layout);
        mTclLocateActivity = activity;
    }

    public void setUpdateService(UpdateService service) {
        mUpdateService = service;

        // Create recommanded cities
        mRecommandedCityVh = new RecommandedCityVh(mTclLocateActivity, (ViewGroup) findViewById(R.id.recommand_city_viewgroup), mUpdateService);
        mRecommandedCityVh.init();
    }

    public void init() {
        // Initialize searchview
        initSearchView();

        ListView lv = findViewById(R.id.search_citylist);
        lv.setAdapter(mCityListAdapter);
        lv.setOnItemClickListener(mCityListAdapter);

        mProgressDialogVh = new ProgressDialogVh(mTclLocateActivity);
    }

    /**
     * Initialize search bar
     */
    public void initSearchView() {
        mToolbar = findViewById(R.id.toolbar);
//        mToolbar.inflateMenu(R.menu.search_actionbar);
//        Menu menu = mToolbar.getMenu();
        mToolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);

        // Set navigation icon listener to back
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityFactory.jumpToActivity(ActivityFactory.WEAHTER_MANAGER_ACTIVITY, mTclLocateActivity, null);
            }
        });

        // Get search view
        // mMstSearchView = (MstSearchView) menu.findItem(R.id.search).getActionView();
        mMstSearchView = findViewById(R.id.search);
        mMstSearchView.setQueryHint(mTclLocateActivity.getResources().getString(R.string.search_location));

        // Hind default search icon
        mMstSearchView.needHintIcon(false);
        // Set the search view show up directly
        mMstSearchView.setIconifiedByDefault(false);

        // Set the texting listener
        mMstSearchView.setOnQueryTextListener(new MstSearchView.OnQueryTextListener() {
            private String cityName;

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                cityName = s.trim().replaceAll("[^\\u4e00-\\u9fa5a-zA-Z]", "");
                mRecommandedCityVh.hide();

                // Search city by input string
                if (!TextUtils.isEmpty(cityName)) {
                    mRecommandedCityVh.hide();
                    mHandler.postDelayed(mPostRequestRunnable, DELAY_TIME);
                } else {
                    //mHandler.postDelayed(delayHideSoftInput, 100);
                    //Hide soft input
                    //mMstSearchView.clearFocus();
                    mRecommandedCityVh.show();
                    setCitList(null);
                }

                return true;
            }

            // Because onCloseListener will call requestFocus() to get focus again,
            // so the softinput will hide then show immediately.
            // According to this situation, we delay calling hideSoftInputFromWindow()
            // after calling requestFocus, the delay time is 100ms.
            private Runnable delayHideSoftInput = new Runnable() {
                @Override
                public void run() {
                    mTclLocateActivity.hideSoftInputFromWindow(mMstSearchView);
                }
            };

            // To search city from service
            private Runnable mPostRequestRunnable = new Runnable() {
                @Override
                public void run() {
                    mSearchName = cityName;
                    if (null != mUpdateService) {
                        if (!TextUtils.isEmpty(cityName)) {
                            // Hide input method
                            mUpdateService.requestCityListByName(cityName, mRequestCityListListener);
                        }
                    }
                }
            };
        });
    }


    public void addCity(City city) {
        if (null != mUpdateService) {
            mUpdateService.addCityObserver(mCityObserver);
            mUpdateService.addCity(city);
        }
    }


    public void recycle() {
        mRecommandedCityVh.recycle();
    }

    void setCitList(List<City> cityList) {
        mCityListAdapter.setItemDatas(cityList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                mTclLocateActivity.finish();
                break;
            case R.id.et_searchview:
                mRecommandedCityVh.hide();
                break;
        }
    }


    private ResLayoutAdapter<City> mCityListAdapter = new ResLayoutAdapter<City>(R.layout.tcl_search_city_list_item_layout) {
        @Override
        protected void convertItemData(DataCoverUiController<City> ctr, City city, int position, int viewType) {
            ctr.setTextToTextView(R.id.cityListitem, city.getFullName(", "));
            TextView textView = ctr.findViewById(R.id.cityListitem);
            String cityName = textView.getText().toString();

            // Set the first word's color to 0xFF4BB6AE
            SpannableStringBuilder builder = new SpannableStringBuilder(cityName);
            int index = -1;
            int mSearchLength = mSearchName.length();
            index = cityName.toLowerCase().indexOf(mSearchName.toLowerCase());
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(0xFF4BB6AE);

            if (index != -1) {
                builder.setSpan(colorSpan, index, mSearchLength + index, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            textView.setText(builder);
        }

        @Override
        protected void onItemClick(DataCoverUiController<City> ctr, City city, int position) {
            mTclLocateActivity.addCity(city);
        }
    };


    private ICityManagerSupporter.OnRequestCityListListener mRequestCityListListener = new ICityManagerSupporter.OnRequestCityListListener() {
        @Override
        public void onSucceed(List<City> cityList) {
            setCitList(cityList);
        }

        @Override
        public void onFailed(int state) {

        }
    };

    private ICityManager.CityObserver mCityObserver = new ICityManager.CityObserver() {
        @Override
        protected void onCityAdding(City city, int state) {
            if (ICityManager.CityObserver.ADD_STATE_ADDING == state) {
                mProgressDialogVh.showDialog(mTclLocateActivity.getResources().getString(R.string.adding_city));
            } else if (ICityManager.CityObserver.ADD_STATE_ADDED == state || ICityManager.CityObserver.ADD_STATE_HAS_ADDED == state) { // Addding city
                mProgressDialogVh.hide();
                mUpdateService.removeCityObserver(this);

                // Jump to main activity
                ActivityFactory.jumpToActivity(ActivityFactory.MAIN_ACTIVITY, mTclLocateActivity, null);

                Message msg = Message.obtain();
                msg.obj = city;
                // becase the mainview's city list update more slower than this action, so we delay 300 ms.
                WeatherCNApplication.getWeatherCnApplication().sendMessage(OtherMainActivityVh.SWITCH_PAGE_ACTION, msg, DELAY_TIME2);
            } else if (ICityManager.CityObserver.ADD_STATE_FAILED == state) {
                mProgressDialogVh.hideDialog();
                mUpdateService.removeCityObserver(this);
                Toast.makeText(mTclLocateActivity, R.string.add_city_failed, Toast.LENGTH_LONG).show();
            }
        }
    };
}
