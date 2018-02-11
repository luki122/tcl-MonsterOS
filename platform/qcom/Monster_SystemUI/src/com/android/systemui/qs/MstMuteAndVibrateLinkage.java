package com.android.systemui.qs;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.os.Handler;
import android.os.SystemVibrator;
import android.os.Vibrator;

/*
 * author: tangjun
 * */

public class MstMuteAndVibrateLinkage {
    private Context mContext = null ;
    private AudioManager mAudioManager = null;

    public MstMuteAndVibrateLinkage(Context context,AudioManager audioManager){
        this.mContext = context;
        this.mAudioManager = audioManager;
    }


    public boolean isSilent(){
        int ringMode = mAudioManager.getRingerModeInternal();
        if((AudioManager.RINGER_MODE_SILENT == ringMode) || (ringMode== AudioManager.RINGER_MODE_VIBRATE)){
            return true;
        }
        return false;
    }

    public boolean isVibrate(){
        /*int ringMode = mAudioManager.getRingerModeInternal();
        if(ringMode == AudioManager.RINGER_MODE_VIBRATE ||
            ((ringMode == AudioManager.RINGER_MODE_NORMAL) &&
                (Settings.System.getInt(mContext.getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING, 0) != 0))){
            return true;
        }*/
        return Settings.System.getInt(mContext.getContentResolver(),Settings.System.VIBRATE_WHEN_RINGING, 0) != 0;
    }


    public boolean silentChecked(boolean isChecked){
        boolean mVibrate = isVibrate();

        if(isChecked){
            //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,0);
            /*if(mVibrate){
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
            } else{
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
            }*/
            mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
        }else{
            mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
            /*if(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getLastAudibleStreamVolume(AudioManager.STREAM_MUSIC), 0);
            }*/
            /*if(mVibrate){
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1);
            } else{
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,0);
            }*/
        }
        return isVibrate();
    }

    public void vibrateChecked(boolean isChecked){

        Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,isChecked?1:0);
        /*int ringMode = mAudioManager.getRingerModeInternal();
        if(isChecked){
            if(ringMode == AudioManager.RINGER_MODE_NORMAL){
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,1);
            }else if(ringMode == AudioManager.RINGER_MODE_SILENT || ringMode == AudioManager.RINGER_MODE_VIBRATE){
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
            }
        }
        else {
            if(ringMode == AudioManager.RINGER_MODE_NORMAL){
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,0);
            }else if(ringMode == AudioManager.RINGER_MODE_VIBRATE){
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
            }
        }*/
    }
}
