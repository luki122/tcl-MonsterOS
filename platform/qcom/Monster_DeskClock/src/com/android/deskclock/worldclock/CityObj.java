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

import java.util.ArrayList;

import android.content.SharedPreferences;

public class CityObj {

    private static final String CITY_NAME = "city_name_";
    private static final String CITY_TIME_ZONE = "city_tz_";
    private static final String CITY_ID = "city_id_";
    private static final String CITY_INDEX = "city_index_";

    public String mCityName;
    public String mTimeZone;
    public String mCityId;
    public String mCityIndex;
    public boolean isHeader;
    
    private String str_time;//时间字符串 世界时钟View专用
    private String am_or_pm = "";//AM / PM 世界时钟View专用
    private String tomo_or_yest = "";//显示昨天还是明天 世界时钟View专用
    
    public String pinyin;//拼音 20160920
    public ArrayList<String> firstLetter = new ArrayList<String>();//首字母 20160920

    
    public void setTimeString(String str){
        str_time = str;
    }
    
    public void setAmOrPm(String str){
        am_or_pm = str;
    }
    
    public String getTimeString(){
        return str_time;
    }
    
    public String getAmOrPm(){
        return am_or_pm;
    }
    
    public void setTomOrYes(String str){
        tomo_or_yest = str;
    }
    
    public String getTomOrYes(){
        return tomo_or_yest;
    }
    
    
    public boolean isSelecte = false;

    public CityObj(String name, String timezone, String id, String index) {
        mCityName = name;
        mTimeZone = timezone;
        mCityId = id;
        mCityIndex = index;
    }

    @Override
    public String toString() {
        return "CityObj{" +
                "name=" + mCityName +
                ", timezone=" + mTimeZone +
                ", id=" + mCityId +
                ", index=" + mCityIndex +
                '}';
    }

    public CityObj(SharedPreferences prefs, int index) {
        mCityName = prefs.getString(CITY_NAME + index, null);
        mTimeZone = prefs.getString(CITY_TIME_ZONE + index, null);
        mCityId = prefs.getString(CITY_ID + index, null);
        mCityIndex = prefs.getString(CITY_INDEX + index, null);
    }

    public void saveCityToSharedPrefs(SharedPreferences.Editor editor, int index) {
        editor.putString(CITY_NAME + index, mCityName);
        editor.putString(CITY_TIME_ZONE + index, mTimeZone);
        editor.putString(CITY_ID + index, mCityId);
        editor.putString(CITY_INDEX + index, mCityIndex);
    }
    public String getSortLetters(){
        return mCityIndex.substring(0,1);
    }

}
