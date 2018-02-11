/*
 * Copyright (C) 2013 The Android Open Source Project
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
/*
 ==========================================================================
 *HISTORY
 *
 *Tag		 Date	      Author		  Description
 *============== ============ =============== ==============================
 *CONFLICT-20021 2014/11/3   wenggangjin	Modify the package conflict
 ============================================================================
 */
package cn.tcl.filemanager.utils;

import android.os.SystemClock;
/**
 * A simple perf timer class that supports lap-time-style measurements. Once a
 * timer is started, any number of laps can be marked, but they are all relative
 * to the original start time.
 */
public class SimpleTimer {

    private static final String DEFAULT_LOG_TAG = "SimpleTimer";

    private static final boolean ENABLE_SIMPLE_TIMER = false;

    private final boolean mEnabled;
    private long mStartTime;
    private long mLastMarkTime;
    private String mSessionName;

    public SimpleTimer() {
        this(false);
    }

    public SimpleTimer(boolean enabled) {
        mEnabled = enabled;
    }

    public final boolean isEnabled() {
        //return ENABLE_SIMPLE_TIMER && LogUtils.isLoggable(getTag(), LogUtils.DEBUG)
        //        && mEnabled;
        return true;
    }

    public SimpleTimer withSessionName(String sessionName) {
        mSessionName = sessionName;
        return this;
    }

    public void start() {
        mStartTime = mLastMarkTime = SystemClock.uptimeMillis();
        LogUtils.d(getTag(), "timer START");
    }

    public void mark(String msg) {
        if (isEnabled()) {
            long now = SystemClock.uptimeMillis();
            LogUtils.i("FileManagerPerformance", String.format("%s: %sms elapsed (%sms since last mark)", msg, (now - mStartTime), (now - mLastMarkTime)));
            mLastMarkTime = now;
        }
    }

    private String getTag() {
        //return TextUtils.isEmpty(mSessionName) ? DEFAULT_LOG_TAG : mSessionName;
        return "FileManagerPerformance";
    }

}

