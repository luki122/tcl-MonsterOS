/*Copyright (C) 2016 Tcl Corporation Limited*/
package cn.tcl.meetingassistant.services;

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
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.view.SoundRecordItemView;

public class SoundRecorderService extends Service {

    public static final int STATE_IDLE = 1;
    public static final int STATE_RECORDING = 2;
    public static final int STATE_PAUSE_RECORDING = 3;
    private static final int FRAME_COUNT = 16;

    private final String TAG = SoundRecorderService.class.getSimpleName();

    private final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private final int DEFAULT_SAMPLE_RATE = 16000;
    private final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private final int DEFAULT_CHANNEL_NUMBER = 1;
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
    private OnMobileStateChangeListener mStateListener;

    private int time = 0;//record time
    private Handler mTimeHandle = new Handler();
    private long mTempTotalTime = 0;
    private long mRecordStartTime;

    private RecordAbnormalState mRecordAbnormalState;

    private Runnable mTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mTimeHandle.postDelayed(this, 10);
            if (mOnRefreshTimeUiListener != null && mCurrentState == STATE_RECORDING) {
                long time = getCurrentRecordTime();
                mOnRefreshTimeUiListener.onRefreshTimeUi(time);
            }
            time++;
            if (time % 500 == 0) {
                double size = FileUtils.getSdAvailableSize();
                MeetingLog.d(TAG,"runtime size " + size);
                if(mRecordAbnormalState !=null ){
                    mRecordAbnormalState.setLowSize(size);
                }
            }
        }
    };

    public void setRecordAbnormalState(RecordAbnormalState recordAbnormalState){
        mRecordAbnormalState=recordAbnormalState;
    }

    public long getCurrentRecordTime(){
        long time = System.currentTimeMillis() - mRecordStartTime + mTempTotalTime;
        return time;
    }

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
                    MeetingLog.d(TAG, "click home,show notification");
                    if(mRecordAbnormalState != null){
                        mRecordAbnormalState.switchback();
                    }
                }
            }
        }
    };

    private BroadcastReceiver mPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                MeetingLog.d(TAG, "out going a call");
                pauseRecord();
            } else {
                MeetingLog.d(TAG, "phone state change");
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

        return mBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MeetingLog.d(TAG, "record service create");
        initAudioRecord();
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
        mBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT);

        if (mBufferSize % FRAME_COUNT != 0) {
            mBufferSize += (FRAME_COUNT - mBufferSize % FRAME_COUNT);
        }
        mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLE_RATE,
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

    public void startRecord() {
        MeetingLog.d(TAG, "start record");
        if (mCurrentState == STATE_RECORDING) {
            return;
        }
        if (mAudioRecord != null) {
            if (mCurrentState == STATE_IDLE) {
//                mSaveRecordFile = ;
                time = 0;
            }
            mRecordNum++;
            mIsRecording = true;
//            mOnStateChangeListener.onStateChange(STATE_RECORDING);
            mCurrentState = STATE_RECORDING;
            mRecordStartTime = System.currentTimeMillis();
            mAudioRecord.startRecording();
            new Thread(new AudioRecordThread()).start();
            mTimeHandle.post(mTimeRunnable);
        }
        MeetingLog.d(TAG,"send broadcast for start record");
        Intent intent = new Intent();
        intent.setAction(SoundRecordItemView.STOP_BROADCAST);
        sendBroadcast(intent);
    }

    public void pauseRecord() {
        if (mCurrentState == STATE_RECORDING) {
            MeetingLog.d(TAG, "pause record");
            mTempTotalTime = System.currentTimeMillis() - mRecordStartTime + mTempTotalTime;
            mRecordStartTime = -1;
            mAudioRecord.stop();
            mTimeHandle.removeCallbacks(mTimeRunnable);
            mIsRecording = false;
            mCurrentState = STATE_PAUSE_RECORDING;
            mOnStateChangeListener.onStateChange(STATE_PAUSE_RECORDING);
        }
    }

    public long stopRecord() {
        MeetingLog.d(TAG, "stop record");
        unregisterReceiver();
        if (mCurrentState == STATE_IDLE) {
            return -1;
        }
        mTimeHandle.removeCallbacks(mTimeRunnable);
        mRecordNum = 0;
        mCurrentState = STATE_IDLE;
        mIsRecording = false;
        mAudioRecord.stop();
        long time = 0;
        if (mRecordStartTime != -1) {
            time = System.currentTimeMillis() - mRecordStartTime + mTempTotalTime;
        } else {
            time = mTempTotalTime;
        }
        mTempTotalTime = 0;
        mOnStateChangeListener.onStateChange(STATE_IDLE);
        RandomAccessFile mRandomAccessFile = null;
        try {
            mRandomAccessFile = new RandomAccessFile(mSaveRecordFile, "rw");
            mRandomAccessFile.seek(0);
            encodeWaveFile(mRandomAccessFile);
        } catch (IOException e) {
            MeetingLog.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            try {
                if(null != mRandomAccessFile){
                    mRandomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return time;
    }

    public int getState() {
        return mCurrentState;
    }

    @Override
    public void onDestroy() {
        MeetingLog.d(TAG, "server onDestroy");
        if (mAudioRecord != null) {
            stopRecord();
            mAudioRecord.release();
            mAudioRecord = null;
            if (mBatteryBroadcastReceiver != null) {
                unregisterReceiver(mBatteryBroadcastReceiver);
            }
            if(mHomeKeyEventReceiver != null){
                unregisterReceiver(mHomeKeyEventReceiver);
            }
            if(mPhoneReceiver != null){
                unregisterReceiver(mPhoneReceiver);
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
            MeetingLog.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        while (mIsRecording) {
            readSize = mAudioRecord.read(byteBuffer, 0, mBufferSize);

            if (readSize > 0) {
                try {
                    if (mRandomAccessFile == null) {
                        MeetingLog.e(TAG, "mRandomAccessFile is null!");
                        return;
                    }
                    mRandomAccessFile.write(byteBuffer, 0, readSize);
                } catch (IOException e) {
                    MeetingLog.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
            } else {
                MeetingLog.d(TAG, "read size=" + readSize);
            }
        }

        try {
            if (mRandomAccessFile != null) {
                mRandomAccessFile.close();
            }
        } catch (IOException e) {
            MeetingLog.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * encode file
     *
     * @param randomAccessFile
     */
    private void encodeWaveFile(RandomAccessFile randomAccessFile) {
        long totalAudioLen;
        long totalDataLen;
        long bitsPerSample = 16;
        long longSampleRate = DEFAULT_SAMPLE_RATE;
        int channels = DEFAULT_CHANNEL_NUMBER;
        long byteRate = bitsPerSample * longSampleRate * channels / 8;
        try {
            totalDataLen = randomAccessFile.length() - 8;
            totalAudioLen = totalDataLen - 36;
            randomAccessFile.seek(0);
            WriteWaveFileHeader(randomAccessFile, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            randomAccessFile.close();
            mediaScan(mSaveRecordFile);
        } catch (IOException e) {
            MeetingLog.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * scan file for add to MediaStory
     *
     * @param file
     */
    public void mediaScan(File file) {
        // do nothing


//        MediaScannerConnection.scanFile(this,
//                new String[]{file.getAbsolutePath()}, null,
//                new MediaScannerConnection.OnScanCompletedListener() {
//                    @Override
//                    public void onScanCompleted(String path, Uri uri) {
//                        MeetingLog.d(TAG, "MediaScanWork " + "file " + path
//                                + " was scanned successfully: " + uri);
//                    }
//                });
    }

    /**
     * add wav style header
     *
     * @param out
     * @param totalAudioLen
     * @param totalDataLen
     * @param longSampleRate
     * @param channels
     * @param byteRate
     * @throws IOException
     */
    private void WriteWaveFileHeader(RandomAccessFile out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long
                                             byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';

        header[16] = 16; // 4 bytes: size of 'fmt ' chunk

        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1

        header[21] = 0;

        header[22] = (byte) channels;
        header[23] = 0;

        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);

        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        header[32] = (byte) (1 * 16 / 8); // block align    mChannels * bits per sample/8
        header[33] = 0;

        header[34] = 16; // bits per sample
        header[35] = 0;

        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';

        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    public void setOnMobileStateChangeListener(OnMobileStateChangeListener
                                                       onMobileStateChangeListener) {
        this.mStateListener = onMobileStateChangeListener;
    }

    public interface OnStateChangeListener {
        void onStateChange(int stateCode);
    }

    public interface OnRefreshTimeUiListener {
        void onRefreshTimeUi(long time);
    }

    public interface OnMobileStateChangeListener {
        void onBatteryStateChange(int level);

        boolean onAvailableSizeChange(int size);

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
}
