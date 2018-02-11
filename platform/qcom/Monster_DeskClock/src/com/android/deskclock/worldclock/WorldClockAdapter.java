/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock.worldclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.android.deskclock.AnalogClock;
import com.android.deskclock.R;
import com.android.deskclock.SettingsActivity;
import com.android.deskclock.Utils;

import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import mst.widget.MstCheckListAdapter;

public class WorldClockAdapter extends MstCheckListAdapter {
    // protected Object[] mCitiesList;
    protected List<CityObj> mCitiesList;
    private final LayoutInflater mInflater;
    private final Context mContext;
    private String mClockStyle;
    private final Collator mCollator = Collator.getInstance();
    protected HashMap<String, CityObj> mCitiesDb = new HashMap<String, CityObj>();
    protected int mClocksPerRow;
    private String mDateFormat;
    private SimpleDateFormat dateFormat;
    private ClickInterface clickListener;

    private boolean isDeleteMode = false;

    public WorldClockAdapter(Context context) {
        this(context, null);
    }

    public WorldClockAdapter(Context context, ClickInterface listener) {
        super();
        mContext = context;
        loadData(context);
        loadCitiesDb(context);
        mInflater = LayoutInflater.from(context);
        mClocksPerRow = context.getResources().getInteger(R.integer.world_clocks_per_row);
        mDateFormat = context.getString(R.string.abbrev_wday_month_day_no_year);
        Locale l = Locale.getDefault();
        dateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(l, mDateFormat), l);
        clickListener = listener;
    }

    @Override
    protected View getCheckBox(int position, View itemview) {
        ViewHolder m_holder = (ViewHolder) itemview.getTag();
        return m_holder.select_checkbox;
    }

    @Override
    protected View getMoveView(int position, View itemview) {
        ViewHolder m_holder = (ViewHolder) itemview.getTag();

        return m_holder.world_clock_slideview;
    }

    public void reloadData(Context context) {
        loadData(context);
        notifyDataSetChanged();
    }

    public List<CityObj> getCitiesList() {

        for (int i = 0; i < mCitiesList.size(); i++) {
            CityObj cityObj = mCitiesList.get(i);
            CityObj cityInDb = mCitiesDb.get(cityObj.mCityId);
            cityObj.mCityName = Utils.getCityName(cityObj, cityInDb);
        }

        return mCitiesList;
    }

    public void loadData(Context context) {
        mCitiesList = new ArrayList<CityObj>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mClockStyle = prefs.getString(SettingsActivity.KEY_CLOCK_STYLE,
                mContext.getResources().getString(R.string.default_clock_style));
        HashMap<String, CityObj> citiesMap = Cities.readCitiesFromSharedPrefs(prefs);
        for (String key : citiesMap.keySet()) {
            mCitiesList.add(citiesMap.get(key));
        }
        sortList();
        mCitiesList = addHomeCity();
    }

    public void loadCitiesDb(Context context) {
        mCitiesDb.clear();
        // Read the cities DB so that the names and timezones will be taken from
        // the DB
        // and not from the selected list so that change of locale or changes in
        // the DB will
        // be reflected.
        CityObj[] cities = Utils.loadCitiesFromXml(context);
        if (cities != null) {
            for (int i = 0; i < cities.length; i++) {
                mCitiesDb.put(cities[i].mCityId, cities[i]);
            }
        }
    }

    /***
     * Adds the home city as the first item of the adapter if the feature is on
     * and the device time zone is different from the home time zone that was
     * set by the user. return the list of cities.
     */
    private List<CityObj> addHomeCity() {
        if (needHomeCity()) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            String homeTZ = sharedPref.getString(SettingsActivity.KEY_HOME_TZ, "");
            CityObj c = new CityObj(mContext.getResources().getString(R.string.home_label), homeTZ, null, null);
            List<CityObj> temp = new ArrayList<CityObj>();
            temp.add(c);
            temp.addAll(mCitiesList);
            // Object[] temp = new Object[mCitiesList.size() + 1];
            // temp[0] = c;
            // for (int i = 0; i < mCitiesList.length; i++) {
            // temp[i + 1] = mCitiesList[i];
            // }
            return temp;
        } else {
            return mCitiesList;
        }
    }

    public void setDeleteMode(final boolean is) {

        setChecked(is);

        isDeleteMode = is;
        if (!is) {
            for (int i = 0; i < mCitiesList.size(); i++) {
                CityObj city = mCitiesList.get(i);
                city.isSelecte = false;
            }
        }
        notifyDataSetChanged();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setChecked(is);
//            }
//        },500);
    }

    public void updateHomeLabel(Context context) {
        // Update the "home" label if the home time zone clock is shown
        if (needHomeCity() && mCitiesList.size() > 0) {
            ((CityObj) mCitiesList.get(0)).mCityName = context.getResources().getString(R.string.home_label);
        }
    }

    public boolean needHomeCity() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPref.getBoolean(SettingsActivity.KEY_AUTO_HOME_CLOCK, false)) {
            String homeTZ = sharedPref.getString(SettingsActivity.KEY_HOME_TZ, TimeZone.getDefault().getID());
            final Date now = new Date();
            return TimeZone.getTimeZone(homeTZ).getOffset(now.getTime()) != TimeZone.getDefault().getOffset(
                    now.getTime());
        } else {
            return false;
        }
    }

    public boolean hasHomeCity() {
        return (mCitiesList != null) && mCitiesList.size() > 0 && ((CityObj) mCitiesList.get(0)).mCityId == null;
    }

    private void sortList() {
        final Date now = new Date();

        // Sort by the Offset from GMT taking DST into account
        // and if the same sort by City Name
        Collections.sort(mCitiesList, new Comparator<Object>() {
            private int safeCityNameCompare(CityObj city1, CityObj city2) {
                if (city1.mCityName == null && city2.mCityName == null) {
                    return 0;
                } else if (city1.mCityName == null) {
                    return -1;
                } else if (city2.mCityName == null) {
                    return 1;
                } else {
                    return mCollator.compare(city1.mCityName, city2.mCityName);
                }
            }

            @Override
            public int compare(Object object1, Object object2) {
                CityObj city1 = (CityObj) object1;
                CityObj city2 = (CityObj) object2;
                if (city1.mTimeZone == null && city2.mTimeZone == null) {
                    return safeCityNameCompare(city1, city2);
                } else if (city1.mTimeZone == null) {
                    return -1;
                } else if (city2.mTimeZone == null) {
                    return 1;
                }

                int gmOffset1 = TimeZone.getTimeZone(city1.mTimeZone).getOffset(now.getTime());
                int gmOffset2 = TimeZone.getTimeZone(city2.mTimeZone).getOffset(now.getTime());
                if (gmOffset1 == gmOffset2) {
                    return safeCityNameCompare(city1, city2);
                } else {
                    return gmOffset1 - gmOffset2;
                }
            }
        });
    }

    @Override
    public int getCount() {
        if (mClocksPerRow == 1) {
            // In the special case where we have only 1 clock per view.
            return mCitiesList.size();
        }

        // Otherwise, each item in the list holds 1 or 2 clocks
        return (mCitiesList.size() + 1) / 2;
    }

    @Override
    public Object getItem(int p) {
        return null;
    }

    @Override
    public long getItemId(int p) {
        return p;
    }

    @Override
    public boolean isEnabled(int p) {
        return false;
    }

    @Override
    protected View onCreateView(int i, ViewGroup parent) {
        // Index in cities list
//        int index = position * mClocksPerRow;
//        if (index < 0 || index >= mCitiesList.size()) {
//            return null;
//        }

//        super(view,parent);
//        if (view == null) {
        View view = mInflater.inflate(R.layout.world_clock_list_item, parent, false);
        ViewHolder m_holder = new ViewHolder();
        m_holder.name = (TextView) (view.findViewById(R.id.city_name));
        m_holder.world_clock_slideview = (RelativeLayout) (view.findViewById(R.id.world_clock_slideview));
        m_holder.text_is_late_or_early = (TextView) (view.findViewById(R.id.text_is_late_or_early));
        m_holder.dayOfWeek = (TextView) (view.findViewById(R.id.city_day));//原生的 现在不显示
        m_holder.city_date = (TextView) (view.findViewById(R.id.city_date));
        m_holder.dclock = (TextClock) (view.findViewById(R.id.digital_clock));
        m_holder.aclock = (AnalogClock) (view.findViewById(R.id.analog_clock));
        m_holder.img_day_night = (ImageView) (view.findViewById(R.id.img_day_night));
        m_holder.select_checkbox = (CheckBox) view.findViewById(R.id.select_checkbox);
        view.setTag(m_holder);
//        }
        //updateView(view.findViewById(R.id.city_left), (CityObj) mCitiesList.get(index), index);
        return view;
    }

    @Override
    protected void onBindView(final int position, View itemView) {
        updateView(itemView, (CityObj) mCitiesList.get(position), position);
    }

    private class ViewHolder {
        TextView name;
        TextView text_is_late_or_early;
        TextView dayOfWeek;
        TextView city_date;
        TextClock dclock;
        AnalogClock aclock;
        ImageView img_day_night;
        CheckBox select_checkbox;
        RelativeLayout world_clock_slideview;
    }

    private void updateView(View clock, final CityObj cityObj, final int pos) {

        final ViewHolder m_holder = (ViewHolder) clock.getTag();

        TextView name = m_holder.name;
        TextView text_is_late_or_early = m_holder.text_is_late_or_early;
        TextView dayOfWeek = m_holder.dayOfWeek;//原生的 现在不显示
        TextView city_date = m_holder.city_date;
        TextClock dclock = m_holder.dclock;
        AnalogClock aclock = m_holder.aclock;
        ImageView img_day_night = m_holder.img_day_night;
        CheckBox select_checkbox = m_holder.select_checkbox;
//        select_checkbox.setButtonDrawable( mContext.getResources().getDrawable(R.drawable.btn_check_anim) );


        aclock.enableSeconds(true);
        aclock.setTimeZone(cityObj.mTimeZone);
        dclock.setTimeZone(cityObj.mTimeZone);
        Utils.setTimeFormat(mContext, dclock, mContext.getResources().getDimensionPixelSize(R.dimen.label_font_size));

        // if (mClockStyle.equals("analog")) {
        // dclock.setVisibility(View.GONE);
        // aclock.setVisibility(View.VISIBLE);
        // aclock.setTimeZone(cityObj.mTimeZone);
        // aclock.enableSeconds(true);
        // } else {
        // dclock.setVisibility(View.VISIBLE);
        // aclock.setVisibility(View.GONE);
        // dclock.setTimeZone(cityObj.mTimeZone);
        // Utils.setTimeFormat(mContext, dclock,
        // mContext.getResources().getDimensionPixelSize(R.dimen.label_font_size));
        // }
        CityObj cityInDb = mCitiesDb.get(cityObj.mCityId);
        // Home city or city not in DB , use data from the save selected cities
        // list

        final Calendar now = Calendar.getInstance();
        TimeZone local_zone = TimeZone.getDefault();
        now.setTimeZone(local_zone);
        long now_time = now.getTimeInMillis();
        int now_hour = now.get(Calendar.HOUR_OF_DAY);
        int now_min = now.get(Calendar.MINUTE);
        // int myDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        // Get timezone from cities DB if available
        String cityTZ = (cityInDb != null) ? cityInDb.mTimeZone : cityObj.mTimeZone;
        now.setTimeZone(TimeZone.getTimeZone(cityTZ));
        // int cityDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        dayOfWeek.setText(now.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
        dateFormat.setTimeZone(TimeZone.getTimeZone(cityTZ));


        int this_hour = now.get(Calendar.HOUR_OF_DAY);

        if (this_hour > 6 && this_hour < 18) {
            img_day_night.setImageResource(R.drawable.day);
        } else {
            img_day_night.setImageResource(R.drawable.night);
        }


        if (isDeleteMode) {
            //select_checkbox.setVisibility(View.VISIBLE);
            //text_is_late_or_early.setVisibility(View.GONE);
        } else {
            //select_checkbox.setVisibility(View.GONE);
            //text_is_late_or_early.setVisibility(View.VISIBLE);
        }


        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df1.setTimeZone(TimeZone.getTimeZone(cityTZ));
        String this_date_str = df1.format(now.getTime());
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//用这个城市的时区的时间字符串转换到当前时区
        df2.setTimeZone(TimeZone.getDefault());
        Date this_date = new Date();
        try {
            this_date = df2.parse(this_date_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long this_time = this_date.getTime();

        String text_late_early_add = mContext.getString(R.string.local_time);
        String text_late_early = "";

//        int offset_time = TimeZone.getTimeZone(cityTZ).getRawOffset();

        if (now_time > this_time) {
            text_late_early = mContext.getString(R.string.late_local_time);
        } else if (now_time < this_time) {
            text_late_early = mContext.getString(R.string.early_local_time);
        }

        if (!TextUtils.isEmpty(text_late_early)) {
            int offset_sec = (int) Math.abs(this_time - now_time) / 1000;//换算成秒
            int offset_hour = offset_sec / 3600;
            int offset_min = (offset_sec % 3600) / 60;
            if (offset_hour > 0) {
                text_late_early_add = text_late_early + offset_hour + mContext.getString(R.string.timer_hour);
            }
            if (offset_min > 0) {
                if (offset_min % 5 != 0) {//有时候相减少一分钟 为29 59 的情况
                    offset_min = offset_min + 1;
                }
                if (offset_min != 60) {//有可能是5９分钟+1变成60分钟
                    text_late_early_add = text_late_early_add + offset_min + mContext.getString(R.string.timer_min);
                } else {
                    offset_min = 0;
                    offset_hour = offset_hour + 1;
                    text_late_early_add = text_late_early + offset_hour + mContext.getString(R.string.timer_hour);
                }
            }

            if (offset_hour == 0 && offset_min == 0) {
                text_late_early_add = mContext.getString(R.string.local_time);
            }
        }

        int this_date_hour = this_date.getHours();
        int this_date_min = this_date.getMinutes();

        text_is_late_or_early.setText(text_late_early_add);

        String day_lab = "";
        if (now_time > this_time && this_date_hour * 60 + this_date_min > now_hour * 60 + now_min) {
            day_lab = mContext.getString(R.string.yestoday_lab);
        } else if (now_time < this_time && this_date_hour * 60 + this_date_min < now_hour * 60 + now_min) {
            day_lab = mContext.getString(R.string.tomorrow_lab);
        } else {
            day_lab = mContext.getString(R.string.today_lab);
        }


//        city_date.setText(dateFormat.format(now.getTime()));
        city_date.setText(day_lab);//改为不显示日期 显示今天昨天明天


        String city_name = Utils.getCityName(cityObj, cityInDb);

        name.setText(city_name);




        clock.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                if (clickListener != null) {
                    clickListener.onItemLongClick(pos);
                    if (!select_checkbox.isChecked()) {
                        select_checkbox.setChecked(true);
                    }
                }
                return true;
            }
        });

        clock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (isDeleteMode) {
                    select_checkbox.setChecked(!select_checkbox.isChecked());
                }
            }
        });

        select_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                cityObj.isSelecte = arg1;
                if (clickListener != null) {
                    clickListener.upateCount();
                }
            }
        });

        select_checkbox.setChecked(cityObj.isSelecte);
        Log.i("zouxu","pos = "+cityObj.isSelecte);

//        TimeZone.setDefault(local_zone);//还原

    }

    public List<CityObj> getCityList() {
        return mCitiesList;
    }

    public void setSelectAll(boolean is) {
        for (int i = 0; i < mCitiesList.size(); i++) {
            mCitiesList.get(i).isSelecte = is;
        }
        notifyDataSetChanged();
    }

    public boolean isDeleteMode() {
        return isDeleteMode;
    }
}
