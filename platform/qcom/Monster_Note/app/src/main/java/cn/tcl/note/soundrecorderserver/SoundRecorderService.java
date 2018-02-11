/*Copyright (C) 2016 Tcl Corporation Limited*/
package cn.tcl.note.soundrecorderserver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import cn.tcl.note.R;
import cn.tcl.note.util.FileUtils;
import cn.tcl.note.util.NoteLog;
import cn.tcl.note.util.WriteWav;

public class SoundRecorderService extends Service {

    public static final int STATE_IDLE = 1;
    public static final int STATE_RECORDING = 2;
    public static final int STATE_PAUSE_RECORDING = 3;
    private static final int FRAME_COUNT = 16;

    private final String TAG = SoundRecorderService.class.getSimpleName();

    private final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    private final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private short[] mPCMbuilder;

    private int mCurrentState = STATE_IDLE;
    private boolean mIsRecording = false;
    private int mBufferSize;
    private File mSaveRecordFile;
    private int mRecordNum = 0;//recording time(pause)

    private AudioRecord mAudioRecord;
    private SoundRecorderBinder mBind = new SoundRecorderBinder();

    private OnStateChangeListener mOnStateChangeListener;
    private OnRefreshTimeUiListener mOnRefreshTimeUiListener;

    private int time = 0;//record time
    private Handler mTimeHandle = new Handler();
    private long mTempTotalTime = 0;
    private long mRecordStartTime;
    private RecordAbnormalState mRecordAbnormalState;

    public void setRecordAbnormalState(RecordAbnormalState recordAbnormalState) {
        mRecordAbnormalState = recordAbnormalState;
    }

    private Runnable mTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mTimeHandle.postDelayed(this, 1000);
            if (mOnRefreshTimeUiListener != null && mCurrentState == STATE_RECORDING) {
                long time = SystemClock.elapsedRealtime() - mRecordStartTime + mTempTotalTime;
                mOnRefreshTimeUiListener.onRefreshTimeUi(time);
            }
            time++;
            if (time % 5 == 0) {
                double size = FileUtils.getSdAvailableSize();
                mRecordAbnormalState.setLowSize(size);
            }
        }
    };

    /*BroadcastReceiver for listener Battery status*/
    private BroadcastReceiver mBatteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                //current battery state
                int level = intent.getIntExtra("level", -1);
                //total battery state
                int total = intent.getIntExtra("scale", -1);
                if ((level >= 0 && total > 0)) {
                    if (mRecordAbnormalState != null) {
                        mRecordAbnormalState.setLowBattery((level * 1f / total * 100));
                    }
                }
            }
        }
    };
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (SYSTEM_HOME_KEY.equals(reason)) {
                    NoteLog.d(TAG, "click home,show notification");
                    mRecordAbnormalState.switchback();
                }
            }
        }
    };
    private BroadcastReceiver mPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                NoteLog.d(TAG, "out going a call");
                pauseRecord();
            } else {
                NoteLog.d(TAG, "phone state change");
                TelephonyManager tManager =
                        (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
                switch (tManager.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        pauseRecord();
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        break;
                }
            }
        }
    };

    public SoundRecorderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        NoteLog.d(TAG, "record service onBind");
        initReceiver();
        return mBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NoteLog.d(TAG, "record service create");
        initAudioRecord();
    }

    private void initReceiver() {
        IntentFilter mFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryBroadcastReceiver, mFilter);
        IntentFilter mHomeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeKeyEventReceiver, mHomeFilter);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mPhoneReceiver, intentFilter);
    }

    private void initAudioRecord() {
        mBufferSize = AudioRecord.getMinBufferSize(WriteWav.DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT);

        if (mBufferSize % FRAME_COUNT != 0) {
            mBufferSize += (FRAME_COUNT - mBufferSize % FRAME_COUNT);
        }
        mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE, WriteWav.DEFAULT_SAMPLE_RATE,
                DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT, mBufferSize);
        mPCMbuilder = new short[mBufferSize];
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void setSaveRecordFile(String fileName) {
        mSaveRecordFile = new File(fileName);
    }

    public int startRecord() {
        NoteLog.d(TAG, "start record");
        if (mCurrentState == STATE_RECORDING) {
            return 0;
        }
        if (mAudioRecord != null) {
            if (mCurrentState == STATE_IDLE) {
//                mSaveRecordFile = ;
                time = 0;
            }
            mAudioRecord.startRecording();
            if(mAudioRecord.getRecordingState()==1){
                Toast.makeText(this, R.string.Recording_error,Toast.LENGTH_SHORT).show();
                mAudioRecord.stop();
                return -1;
            }
            mRecordNum++;
            mIsRecording = true;
//            mOnStateChangeListener.onStateChange(STATE_RECORDING);
            mCurrentState = STATE_RECORDING;
            mRecordStartTime = SystemClock.elapsedRealtime();
            new Thread(new AudioRecordThread()).start();
            mTimeHandle.post(mTimeRunnable);
        }
        return 0;
    }

    public void pauseRecord() {
        if (mCurrentState == STATE_RECORDING) {
            NoteLog.d(TAG, "pause record");
            mTempTotalTime = SystemClock.elapsedRealtime() - mRecordStartTime + mTempTotalTime;
            mRecordStartTime = -1;
            mAudioRecord.stop();
            mIsRecording = false;
            mCurrentState = STATE_PAUSE_RECORDING;
            mOnStateChangeListener.onStateChange(STATE_PAUSE_RECORDING);
        }
    }

    public long getCurrentTime() {
        long time = 0;
        if (mRecordStartTime != -1) {
            time = SystemClock.elapsedRealtime() - mRecordStartTime + mTempTotalTime;
        } else {
            time = mTempTotalTime;
        }
        return time;
    }

    public long stopRecord() {
        NoteLog.d(TAG, "stop record");
        unregisterReceiver();
        if (mCurrentState == STATE_IDLE) {
            return -1;
        }
        mTimeHandle.removeCallbacks(mTimeRunnable);
        mRecordNum = 0;
        mCurrentState = STATE_IDLE;
        mIsRecording = false;
        mAudioRecord.stop();
        long time = getCurrentTime();
        mTempTotalTime = 0;
        mOnStateChangeListener.onStateChange(STATE_IDLE);
        WriteWav.writeWaveFile(mSaveRecordFile);
        return time;
    }

    private void unregisterReceiver() {
        if (mBatteryBroadcastReceiver != null) {
            unregisterReceiver(mBatteryBroadcastReceiver);
            mBatteryBroadcastReceiver = null;
        }
        if (mHomeKeyEventReceiver != null) {
            unregisterReceiver(mHomeKeyEventReceiver);
            mHomeKeyEventReceiver = null;
        }
        if (mPhoneReceiver != null) {
            unregisterReceiver(mPhoneReceiver);
            mPhoneReceiver = null;
        }
    }

    public int getState() {
        return mCurrentState;
    }

    @Override
    public void onDestroy() {
        NoteLog.d(TAG, "server onDestroy");
        if (mAudioRecord != null) {
            stopRecord();
            mAudioRecord.release();
            mAudioRecord = null;
            if (mTimeHandle != null) {
                mTimeHandle.removeCallbacksAndMessages(null);
            }
        }
        super.onDestroy();
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.mOnStateChangeListener = onStateChangeListener;
    }

    public void setOnRefreshTimeUiListener(OnRefreshTimeUiListener onRefreshTimeUiListener) {
        this.mOnRefreshTimeUiListener = onRefreshTimeUiListener;
    }

    private void writeDataToFile() {
        int readSize;
        byte[] byteBuffer = new byte[mBufferSize];
        RandomAccessFile mRandomAccessFile = null;
        try {
            mRandomAccessFile = new RandomAccessFile(mSaveRecordFile, "rw");
            if (mRecordNum == 1) {
                //keep back for add wav header
                mRandomAccessFile.seek(44);
            } else if (mRecordNum > 1) {
                mRandomAccessFile.seek(mRandomAccessFile.length());
            }
        } catch (IOException e) {
            NoteLog.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        while (mIsRecording) {
            readSize = mAudioRecord.read(byteBuffer, 0, mBufferSize);

            if (readSize > 0) {
                try {
                    if (mRandomAccessFile == null) {
                        NoteLog.e(TAG, "mRandomAccessFile is null!");
                        return;
                    }
                    mRandomAccessFile.write(byteBuffer, 0, readSize);
                } catch (IOException e) {
                    NoteLog.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
            } else {
                NoteLog.d(TAG, "read size=" + readSize);
            }
        }

        try {
            if (mRandomAccessFile != null) {
                mRandomAccessFile.close();
            }
        } catch (IOException e) {
            NoteLog.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public interface OnStateChangeListener {
        void onStateChange(int stateCode);
    }

    public interface OnRefreshTimeUiListener {
        void onRefreshTimeUi(long time);
    }

    public class SoundRecorderBinder extends Binder {
        public SoundRecorderService getService() {
            return SoundRecorderService.this;
        }
    }

    /**
     * will PCM data write to file
     */
    class AudioRecordThread implements Runnable {

        @Override
        public void run() {
            writeDataToFile();
        }
    }
}
