package com.android.deskclock;

import mst.app.MstActivity;
import mst.preference.Preference;
import mst.preference.PreferenceFragment;
import mst.preference.Preference.OnPreferenceChangeListener;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.SwitchPreference;

import com.android.deskclock.Util.SharePreferencesUtils;

import android.graphics.Color;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class TCLClockSettingActivity extends MstActivity implements OnClickListener {

    private TextView text_back;
    private SeekBar my_seekbar;
    private Switch vibrate_onoff;
    private RelativeLayout vibrate_layout;
    private static SeekBarVolumizer mVolumizer;
    
    
    private RelativeLayout interference_free_layout;
    private Switch interference_free_onoff;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Utils.setStatusBarColor(this,R.color.white);
        setMstContentView(R.layout.tcl_clock_setting_preference_layout);
        getToolbar().setTitle(R.string.str_alarm_set);

        //initView();
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
                SharePreferencesUtils.setAlarmVibrate(TCLClockSettingActivity.this, arg1);
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
                SharePreferencesUtils.setInterferenceFee(TCLClockSettingActivity.this, arg1);
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
        //intData();
        setVolumeControlStream(AudioManager.STREAM_ALARM);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVolumizer != null) {
            mVolumizer.stop();
        }
    }

    @Override
    public void onNavigationClicked(View view) {
        // 在这里处理Toolbar上的返回按钮的点击事件
        finish();
    }
    
    
    public static class SettingPrefsFragment extends PreferenceFragment  implements OnPreferenceClickListener
    ,OnPreferenceChangeListener{
        
        private  SwitchPreference  vibrate_preference;
        private  SwitchPreference  interference_free_preference;
        private TCLSettingSeekBarPreference my_seekbar_preference;

        
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
        
        private Uri getDefaultRingtoneUri() {
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            final String ringtoneUriString = sp.getString(AlarmClockFragment.PREF_KEY_DEFAULT_ALARM_RINGTONE_URI, null);

            final Uri ringtoneUri;
            if (ringtoneUriString != null) {
                ringtoneUri = Uri.parse(ringtoneUriString);
            } else {
                ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_ALARM);
            }
            return ringtoneUri;
        }

        private boolean mMuted;
        private boolean mZenMuted;
        private int mStream;
        
        private AudioManager mAudioManager;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.tcl_setting_preference);
            findPreferences();
            bindListenerToPreference();
            intData();
//            this.getListView().setBackgroundColor(R.color.white);
//            ListView mList = getListView();
//            if(mList!=null){
//                mList.setBackgroundColor(R.color.white);
//            }
//
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);

            ListView listView = (ListView)view.findViewById(android.R.id.list);
            if (null != listView) {
                listView.setBackgroundColor(Color.WHITE);
            }
            return view;
        }

        private void intData(){
            mAudioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
            mStream = AudioManager.STREAM_ALARM;
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
                mVolumizer = new SeekBarVolumizer(getContext(), mStream, sampleUri, sbvc);
            }
            mVolumizer.start();
//            mVolumizer.setSeekBar(my_seekbar);
            my_seekbar_preference.setSeekBarVolumizer(mVolumizer);
        }
        
        public void findPreferences(){
            vibrate_preference = (SwitchPreference)findPreference("vibrate_preference");
            interference_free_preference = (SwitchPreference)findPreference("interference_free_preference");
            my_seekbar_preference = (TCLSettingSeekBarPreference)findPreference("my_seekbar_preference");
            
            vibrate_preference.setChecked(SharePreferencesUtils.isAlarmVibrate(getContext()));
            if(SharePreferencesUtils.isAlarmVibrate(getContext())){
                interference_free_preference.setChecked(SharePreferencesUtils.isInterferenceFee(getContext()));
            } else {
                interference_free_preference.setChecked(false);
                interference_free_preference.setEnabled(false);
                SharePreferencesUtils.setInterferenceFee(getContext(), false);
            }

        }
        
        public void bindListenerToPreference(){
            vibrate_preference.setOnPreferenceChangeListener(this);
            interference_free_preference.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference arg0, Object arg1) {
            
            
            if(arg0 == vibrate_preference){
                
                Boolean is_check = (Boolean)arg1;
//                Log.i("zouxu", "vibrate_preference = "+is_check);
                
                SharePreferencesUtils.setAlarmVibrate(getContext(), is_check);
                if(is_check){
                    Vibrator mVib = (Vibrator)getContext().getSystemService(VIBRATOR_SERVICE);
                    mVib.vibrate(100);
                    interference_free_preference.setEnabled(true);
                } else {
                    interference_free_preference.setChecked(false);
                    interference_free_preference.setEnabled(false);
                    SharePreferencesUtils.setInterferenceFee(getContext(), false);
                }
                
            } else if(arg0 == interference_free_preference){
                Boolean is_check = (Boolean)arg1;
//                Log.i("zouxu", "interference_free_preference = "+is_check);
                SharePreferencesUtils.setInterferenceFee(getContext(), is_check);
            }

            
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference arg0) {
            
            return true;
        }
        
    }

}
