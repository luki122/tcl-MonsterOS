/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.Global;

import com.android.systemui.R;
import com.android.systemui.qs.MstMuteAndVibrateLinkage;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSTile;
import android.media.AudioManager;

/**
 * Author:tangjun
 * Function:Quick settings tile: VibrationTile
 */
/** Quick settings tile: Vibration mode **/
public class VibrationTile extends QSTile<QSTile.BooleanState> {

    private static final String TAG = "VibrationTile";
    private static final boolean DEBUG = true;    
    private final SettingObserver mSetting;
    private boolean mListening;
    private AudioManager mAudioManager = null;
    private MstMuteAndVibrateLinkage mMstMuteAndVibrateLinkage;
    
    public VibrationTile(Host host) {
        super(host);
        /*mSetting = new GlobalSetting(mContext, mHandler, Global.MODE_RINGER) {
            @Override
            protected void handleValueChanged(int value) {
                refreshState();
            }
        };*/
        mSetting = new SettingObserver(mHandler);
        mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        mMstMuteAndVibrateLinkage=new MstMuteAndVibrateLinkage(mContext,mAudioManager);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();       
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_vibration_mode_label);
    }

    @Override
    public Intent getLongClickIntent() {
        Intent intent = new Intent();
        ComponentName cn = new ComponentName("com.android.settings",
                "com.android.settings.Settings$SoundSettingsActivity");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public void handleClick() {
        mMstMuteAndVibrateLinkage.vibrateChecked(!mState.value);
        refreshState();
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean vibrationMode= mMstMuteAndVibrateLinkage.isVibrate();
        state.value = vibrationMode;
        state.label = mContext.getString(R.string.quick_settings_vibration_mode_label);
        if (vibrationMode) {
            state.icon = ResourceIcon.get(R.drawable.ic_mst_vibrate_enable);
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_vibration_on);
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_mst_vibrate_disable);
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_vibration_off);
        }
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_vibration_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_vibration_changed_off);
        }
    }

    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (listening) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            mContext.registerReceiver(mReceiver, filter);
        } else {
            mContext.unregisterReceiver(mReceiver);
        }
        mSetting.setListening(listening);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ((AudioManager.RINGER_MODE_CHANGED_ACTION).equals(intent.getAction())) {
                refreshState();
            }
        }
    };

    @Override
    public int getMetricsCategory() {
        // TODO Auto-generated method stub
        return 3;
    }

    private class SettingObserver extends ContentObserver{


        public SettingObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            refreshState();
        }

        public void setListening(boolean listening){
            if (listening) {
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.VIBRATE_WHEN_RINGING), false, this);
            } else {
                mContext.getContentResolver().unregisterContentObserver(this);
            }
        }
    }
}

