/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.iflytek;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.iflytek.recinbox.sdk.speech.impl.LybRecognizer;
import com.iflytek.recinbox.sdk.speech.interfaces.IRecognizeListener;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * OfflineToText
 */
public class OfflineToText implements IRecognizeListener{
    private final String TAG = OfflineToText.class.getSimpleName();
    private static final int STATUS_CONVERTING  = 0;
    private static final int STATUS_IDLE = 1;
    private int mStatus;
    private Context mContext;
    private StringBuilder mResultCache;
    private String mFilePath;
    private WavFileReader mInput;
    private LybRecognizer mSpeech;
    private OnStatusChangedListener mOnStatusChangedListener;

    private static OfflineToText mInstance;

    public synchronized static OfflineToText getInstance(Context context){
        if(null == mInstance){
            mInstance = new OfflineToText(context);
            mInstance.mStatus = STATUS_IDLE;
        }
        return mInstance;
    }

    private OfflineToText(Context context) {
        mContext = context;
        mSpeech = LybRecognizer.getInstance(mContext, getZipPath());
    }

    public void setRecognize(String path,OnStatusChangedListener onStatusChangedListener) {
        mFilePath = path;
        mOnStatusChangedListener = onStatusChangedListener;
        MeetingLog.d(TAG,"filePath is " + mFilePath);
    }

    public void stopRecognize() {
        mSpeech.stopRecognize(this);
    }

    public void destroy() {
        mSpeech.destroy();
    }

    @Override
    public void onStart(Bundle bundle) {
        if(null != mOnStatusChangedListener){
            mOnStatusChangedListener.onStart();
        }
    }

    @Override
    public void onResult(Bundle params, String result) {
        if (null != result) {
            try {
                // parase result
                JSONObject obj = new JSONObject(result);
                JSONArray jsonArray = obj.getJSONArray("result");

                //calculate the progress
                int end = obj.getInt("e") * 10;
                int mDuration = mInput.getmDuration();
                MeetingLog.d("Record", "" + end + "/ " + mDuration);
                if (mDuration > 0){
                    float value =  end * 100 /mDuration;
                    if(null != mOnStatusChangedListener){
                        mOnStatusChangedListener.onProgress(value);
                    }
                }

                // parse json to String result
                for (int i =0;i<jsonArray.length();i++) {
                    JSONObject jsonObjectSon = (JSONObject) jsonArray.opt(i);
                    String content = jsonObjectSon.optString("w");
                    mResultCache.append(content);
                    MeetingLog.d(TAG, "for content is " + content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(null != mOnStatusChangedListener){
            mOnStatusChangedListener.onResult(params,mResultCache.toString());
        }
    }

    @Override
    public void onFinish(Bundle bundle, int errorCode) {
        if(errorCode == 0){
            MeetingLog.d(TAG,"onFinish errorCode = " + errorCode);
            if(null != mOnStatusChangedListener){
                mOnStatusChangedListener.onFinish(mResultCache.toString());
            }
        }else if(errorCode == 824021){
            mOnStatusChangedListener.onStopByUser();
        }
        mStatus = STATUS_IDLE;
    }

    private String getZipPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // has sdCard
        if (!sdCardExist) {
            return null;
        }
        sdDir = Environment.getExternalStorageDirectory();
        return sdDir.toString() + "/aitalk/libaitalk5_v3.zip";

    }


    public synchronized void startConvert() {
        if(mStatus == STATUS_CONVERTING){
            Toast.makeText(mContext, R.string.audio_converting,Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mStatus = STATUS_CONVERTING;
            mInput = new WavFileReader(mFilePath);
            mResultCache = new StringBuilder();
            mSpeech.startRecognize(mInput, this, null);
            MeetingLog.d(TAG,"start convert");
        } catch (Exception e) {
            mStatus =STATUS_IDLE;
            e.printStackTrace();
        }
    }

    public boolean isIdle(){
        return mStatus == STATUS_IDLE;
    }


    public interface OnStatusChangedListener{
        void onStart();
        void onResult(Bundle params, String result);
        void onFinish(String result);
        void onProgress(float value);
        void onStopByUser();
    }

}
