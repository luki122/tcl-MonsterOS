package cn.tcl.weather.viewhelper;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leon.tools.view.DataCoverUiController;
import com.leon.tools.view.ResLayoutAdapter;

import java.util.List;

import cn.tcl.weather.ActivityFactory;
import cn.tcl.weather.OtherLocateActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.service.ICityManager;
import cn.tcl.weather.service.ICityManagerSupporter;
import cn.tcl.weather.service.UpdateService;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-10.
 * $desc
 */
public class OtherAddCityVh extends AbsAddCityVh implements View.OnClickListener {
    private final static int DELAY_TIME = 500;
    private final static int DELAY_TIME2 = 150;

    private OtherLocateActivity mLocateActivity;

    private String mSearchName;
    private EditText mSearchView;


    private Handler mHandler = new Handler();
    private RecommandedCityVh mRecommandedCityVh;
    private ImageView mTextClear;
    private ProgressDialogVh mProgressDialogVh;
    private UpdateService mUpdateService;


    OtherAddCityVh(OtherLocateActivity activity) {
        super(activity, R.layout.other_add_location_layout);
        mLocateActivity = activity;
    }

    public void setUpdateService(UpdateService service) {
        mUpdateService = service;
        mRecommandedCityVh = new RecommandedCityVh(mLocateActivity, (ViewGroup) findViewById(R.id.recommand_city_viewgroup), mUpdateService);
        mRecommandedCityVh.init();
    }

    public void init() {
        findViewById(R.id.img_back).setOnClickListener(this);
        mSearchView = findViewById(R.id.et_searchview);
        mSearchView.setOnClickListener(this);
        mSearchView.addTextChangedListener(mTextWatcher);

        // Clear text
        mTextClear = findViewById(R.id.text_clear);
        mTextClear.setOnClickListener(this);

        ListView lv = findViewById(R.id.search_citylist);
        lv.setAdapter(mCityListAdapter);
        lv.setOnItemClickListener(mCityListAdapter);

        mProgressDialogVh = new ProgressDialogVh(mLocateActivity);
    }


    public void addCity(City city) {
        if (null != mUpdateService) {
            mUpdateService.addCityObserver(mCityObserver);
            mUpdateService.addCity(city);
        }
    }


    public void recycle() {
        EditText searchView = findViewById(R.id.et_searchview);
        searchView.removeTextChangedListener(mTextWatcher);
        mRecommandedCityVh.recycle();
    }

    void setCitList(List<City> cityList) {
        mCityListAdapter.setItemDatas(cityList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                mLocateActivity.finish();
                break;
            case R.id.et_searchview:
                mRecommandedCityVh.hide();
                break;
            case R.id.text_clear:
                mSearchView.setText("");
                mRecommandedCityVh.show();
                //mLocateActivity.hideSoftInputFromWindow(mSearchView);
                break;
        }
    }


    private ResLayoutAdapter<City> mCityListAdapter = new ResLayoutAdapter<City>(R.layout.other_search_city_list_item_layout) {
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
            mLocateActivity.addCity(city);
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


    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //mTextClear.setVisibility(View.GONE);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!mSearchView.getText().toString().equals("")) {
                mTextClear.setVisibility(View.VISIBLE);
            } else {
                mTextClear.setVisibility(View.GONE);
            }

            // Hide recommanded cities
            mRecommandedCityVh.hide();
        }

        @Override
        public void afterTextChanged(Editable s) {
            final String cityName = s.toString();
            mHandler.removeCallbacks(mPostRequestRunnable);
            if (!TextUtils.isEmpty(cityName)) {
                mHandler.postDelayed(mPostRequestRunnable, DELAY_TIME);
            } else {
//                mLocateActivity.hideSoftInputFromWindow(mSearchView);
                setCitList(null);
                mRecommandedCityVh.show();
            }
        }

        private Runnable mPostRequestRunnable = new Runnable() {
            @Override
            public void run() {
                if (null != mUpdateService) {
                    EditText searchView = findViewById(R.id.et_searchview);
                    final String cityName = searchView.getText().toString().trim().replaceAll("[^\\u4e00-\\u9fa5a-zA-Z]", "");
                    mSearchName = cityName;
                    if (!TextUtils.isEmpty(cityName)) {
                        // Hide input method
                        mUpdateService.requestCityListByName(cityName, mRequestCityListListener);
                    }
                }
            }
        };
    };


    private ICityManager.CityObserver mCityObserver = new ICityManager.CityObserver() {
        @Override
        protected void onCityAdding(City city, int state) {
            if (ICityManager.CityObserver.ADD_STATE_ADDING == state) {
                mProgressDialogVh.showDialog(mLocateActivity.getResources().getString(R.string.adding_city));
            } else if (ICityManager.CityObserver.ADD_STATE_ADDED == state || ICityManager.CityObserver.ADD_STATE_HAS_ADDED == state) { // Addding city
                mProgressDialogVh.hide();
                mUpdateService.removeCityObserver(this);
                //Intent intent = new Intent();
                //intent.setClass(mLocateActivity, TclMainActivity.class);
                //mLocateActivity.startActivity(intent);
                ActivityFactory.jumpToActivity(ActivityFactory.MAIN_ACTIVITY, mLocateActivity, null);

                Message msg = Message.obtain();
                msg.obj = city;
                // becase the mainview's city list update more slower than this action, so we delay 300 ms.
                WeatherCNApplication.getWeatherCnApplication().sendMessage(OtherMainActivityVh.SWITCH_PAGE_ACTION, msg, DELAY_TIME2);
            } else if (ICityManager.CityObserver.ADD_STATE_FAILED == state) {
                mProgressDialogVh.hideDialog();
                mUpdateService.removeCityObserver(this);
                Toast.makeText(mLocateActivity, R.string.add_city_failed, Toast.LENGTH_LONG).show();
            }
        }
    };
}
