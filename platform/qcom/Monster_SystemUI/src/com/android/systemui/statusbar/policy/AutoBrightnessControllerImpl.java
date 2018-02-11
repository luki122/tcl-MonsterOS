/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import java.util.ArrayList;

/**
 * A controller to manage changes of AutoBrightness related states and update the views accordingly.
 */
public class AutoBrightnessControllerImpl implements AutoBrightnessController {

    private Context mContext;
    AutoBrightnessObserver mAutoBrightObserver;
    Handler mHandler = new Handler();

    private ArrayList<AutoBrightnessChangeCallback> mSettingsChangeCallbacks =
            new ArrayList<AutoBrightnessChangeCallback>();

    public AutoBrightnessControllerImpl(Context context) {
        mContext = context;
        mAutoBrightObserver = new AutoBrightnessObserver(mHandler);
        mAutoBrightObserver.startObserving();
    }

    /**
     * Add a callback to listen for changes in AutoBrightness settings.
     */
    @Override
    public void addSettingsChangedCallback(AutoBrightnessChangeCallback cb) {
        mSettingsChangeCallbacks.add(cb);
        autoBrightnessChanged(cb);
    }

    @Override
    public void removeSettingsChangedCallback(AutoBrightnessChangeCallback cb) {
        mSettingsChangeCallbacks.remove(cb);
    }

    /**
     * Enable or disable AutoBrightness in settings.
     * @return true if attempt to change setting was successful.
     */
    @Override
    public boolean setAutoBrightnessEnabled(boolean enabled) {
        final ContentResolver cr = mContext.getContentResolver();
        int mode = enabled
                ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        //modied by quan.zhai@jrdcom.com for Defect:1407392 2016-01-19 begin
        return Settings.System
                .putIntForUser(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, mode,UserHandle.USER_CURRENT);
        //modied by quan.zhai@jrdcom.com for Defect:1407392 2016-01-19 end
    }

    /**
     * Returns true if AutoBrightness isn't disabled in settings.
     */
    @Override
    public boolean isAutoBrightnessEnabled() {
        ContentResolver resolver = mContext.getContentResolver();
      //modied by quan.zhai@jrdcom.com for Defect:1407392 2016-01-19 begin
        int mode = Settings.System.getIntForUser(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,UserHandle.USER_CURRENT);
      //modied by quan.zhai@jrdcom.com for Defect:1407392 2016-01-19 end
        return mode != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
    }

    private void autoBrightnessChanged() {
        boolean isEnabled = isAutoBrightnessEnabled();
        for (AutoBrightnessChangeCallback cb : mSettingsChangeCallbacks) {
            cb.onAutoBrightnessChanged(isEnabled);
        }
    }

    private void autoBrightnessChanged(AutoBrightnessChangeCallback cb) {
        cb.onAutoBrightnessChanged(isAutoBrightnessEnabled());
    }

    /** ContentObserver to AutoBrightness **/
    private class AutoBrightnessObserver extends ContentObserver {
        public AutoBrightnessObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            autoBrightnessChanged();
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), false, this);
        }
    }
}
