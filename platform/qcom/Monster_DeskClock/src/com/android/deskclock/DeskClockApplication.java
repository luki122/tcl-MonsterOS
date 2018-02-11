/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.deskclock;

import java.util.TimeZone;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.deskclock.events.Events;
import com.android.deskclock.events.LogEventTracker;
import com.android.deskclock.worldclock.Cities;
import com.android.deskclock.worldclock.CityObj;

public class DeskClockApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Events.addEventTracker(new LogEventTracker(getApplicationContext()));
//        if(isFirstUse()){
//            TimeZone tz = TimeZone.getDefault();
//            CityObj default_city = Utils.getCityByTZId(this, tz.getID());
//            if(default_city != null){
//                setFirstUse(default_city);
//
//                Intent i = new Intent(Cities.WORLDCLOCK_UPDATE_INTENT);
//                sendBroadcast(i);
//            }
//        }
    }
    
    private boolean isFirstUse(){
        SharedPreferences default_prefence = PreferenceManager.getDefaultSharedPreferences(this);
        return default_prefence.getBoolean("is_first_use", true);
    }
    
    private void setFirstUse(CityObj c){
        SharedPreferences default_prefence = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = default_prefence.edit();
        editor.putBoolean("is_first_use", false);
        c.saveCityToSharedPrefs(editor, 0);
        editor.putInt("number_of_cities",1);
        editor.commit();
    }
    
}
