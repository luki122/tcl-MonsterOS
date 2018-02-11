package com.android.deskclock;

import mst.app.MstActivity;

import com.android.deskclock.Util.SharePreferencesUtils;

import android.preference.PreferenceManager;
import android.preference.SeekBarVolumizer;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class TCLClockSettingActivityBak extends MstActivity implements OnClickListener {

    private TextView text_back;
    private SeekBar my_seekbar;
    private Switch vibrate_onoff;
    private RelativeLayout vibrate_layout;
    private SeekBarVolumizer mVolumizer;
    private boolean mMuted;
    private boolean mZenMuted;
    private int mStream;
    
    private AudioManager mAudioManager;
    
    private RelativeLayout interference_free_layout;
    private Switch interference_free_onoff;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setMstContentView(R.layout.tcl_setting_activity);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mStream = AudioManager.STREAM_ALARM;
        getToolbar().setTitle(R.string.str_alarm_set);

        initView();
    }
    
    private void intData(){
        if (my_seekbar == null) return;
        final SeekBarVolumizer.Callback sbvc = new SeekBarVolumizer.Callback() {
            @Override
            public void onSampleStarting(SeekBarVolumizer sbv) {
                mHandler.sendEmptyMessageDelayed(STOP_SAMPLE, STOP_SAMPLE_DELAY);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                boolean muted = false;
                if (progress == 0) {
                    muted = true;
                }

                if (mMuted == muted) return;
                mMuted = muted;
            }
            @Override
            public void onMuted(boolean muted, boolean zenMuted) {
                if(mStream==AudioManager.STREAM_ALARM||mStream==AudioManager.STREAM_MUSIC){
                    muted = mAudioManager.getStreamVolume(mStream) == 0;
                }
                if (mMuted == muted && mZenMuted == zenMuted) return;
                mMuted = muted;
                mZenMuted = zenMuted;
                //updateIconView();
            }
        };
        final Uri sampleUri  =  getDefaultRingtoneUri();//mStream == AudioManager.STREAM_MUSIC ? getMediaVolumeUri() : getDefaultRingtoneUri();
        if (mVolumizer == null) {
            mVolumizer = new SeekBarVolumizer(this, mStream, sampleUri, sbvc);
        }
        mVolumizer.start();
        mVolumizer.setSeekBar(my_seekbar);
    }
    
    
    private void initView(){
        text_back = (TextView)findViewById(R.id.text_back);
        my_seekbar = (SeekBar)findViewById(R.id.my_seekbar);
        vibrate_onoff = (Switch)findViewById(R.id.vibrate_onoff);
        vibrate_layout = (RelativeLayout)findViewById(R.id.vibrate_layout);
        interference_free_layout = (RelativeLayout)findViewById(R.id.interference_free_layout);
        interference_free_onoff = (Switch)findViewById(R.id.interference_free_onoff);
        
        text_back.setOnClickListener(this);
        vibrate_layout.setOnClickListener(this);
        interference_free_layout.setOnClickListener(this);
        
        vibrate_onoff.setChecked(SharePreferencesUtils.isAlarmVibrate(this));
        
        vibrate_onoff.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                SharePreferencesUtils.setAlarmVibrate(TCLClockSettingActivityBak.this, arg1);
                if(arg1){
                    Vibrator mVib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                    mVib.vibrate(100);
                }
            }
        });
        interference_free_onoff.setChecked(SharePreferencesUtils.isInterferenceFee(this));
        
        interference_free_onoff.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                SharePreferencesUtils.setInterferenceFee(TCLClockSettingActivityBak.this, arg1);
            }
        });
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        switch (id) {
        case R.id.text_back:
            finish();
            break;
        case R.id.vibrate_layout:
            vibrate_onoff.setChecked(!vibrate_onoff.isChecked());
            break;
        case R.id.interference_free_layout:
            interference_free_onoff.setChecked(!interference_free_onoff.isChecked());
            break;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        intData();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (mVolumizer != null) {
            mVolumizer.stop();
        }
    }
    
    private Uri getDefaultRingtoneUri() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String ringtoneUriString = sp.getString(AlarmClockFragment.PREF_KEY_DEFAULT_ALARM_RINGTONE_URI, null);

        final Uri ringtoneUri;
        if (ringtoneUriString != null) {
            ringtoneUri = Uri.parse(ringtoneUriString);
        } else {
            ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        }
        return ringtoneUri;
    }
    
    private final int STOP_SAMPLE = 1;
    private final int STOP_SAMPLE_DELAY = 3000;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            int what = msg.what;
            if(what == STOP_SAMPLE){
                if (mVolumizer != null) {
                    mVolumizer.stopSample();
                }
            }
        };
    };
    
    @Override
    public void onNavigationClicked(View view) {
        // 在这里处理Toolbar上的返回按钮的点击事件
        finish();
    }

}
