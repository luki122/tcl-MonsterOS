/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.Encoder;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Range;
import android.view.Surface;

import com.android.camera.debug.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by sichao.hu on 8/22/16.
 */
public class FilterVideoEncoder extends Encoder{
    private Log.Tag TAG=new Log.Tag("FilterVideoEncoder");
    private MediaCodec mVideoCodec;
    private MediaCodec mAudioCodec;
    private AudioRecord mAudioRecorder;
    private MediaMuxer mMuxer;
    private int mVideoTrackIndex =-1;
    private boolean mIsPaused; // MODIFIED by jianying.zhang, 2016-10-28,BUG-3137073
    private int mAudioTrackIndex=-1;

    private HandlerThread mVideoHandlerThread;
    private Handler mVideoHandler;
    private HandlerThread mAudioHandlerThread;
    private Handler mAudioHandler;

    private HandlerThread mAudioRecorderHandlerThread;
    private Handler mAudioRecorderHandler;

    private HandlerThread mMuxerThread;
    private Handler mMuxerHandler;

    private Surface mInputSurface;
    private String mPath;
    private File mEncodingFile;

    private class AudioFrame{
        public AudioFrame(byte[] array,long timeStamp) {
            content=array;
            this.timeStamp=timeStamp;
        }
        byte[] content;
        long timeStamp;
        boolean isEOS = false; // MODIFIED by jianying.zhang, 2016-10-28,BUG-3137073
    }

    private static final int CAPACITY=50;
    private Object mProducerConsumerLock=new Object();
    Queue<AudioFrame> mAudioProducerBuffer =new LinkedList<>();
    Queue<AudioFrame> mAudioConsumerBuffer=new LinkedList<>();

    private class VideoOutputCache{
        MediaCodec.BufferInfo info;
        ByteBuffer buffer;
    }

    private Queue<VideoOutputCache> mOutputCache=new LinkedList<>();


    public FilterVideoEncoder(){
        super();
        mVideoHandlerThread =new HandlerThread(TAG+"_Video");
        mVideoHandlerThread.start();
        mVideoHandler =new Handler(mVideoHandlerThread.getLooper());

        mAudioHandlerThread=new HandlerThread(TAG+"_Audio");
        mAudioHandlerThread.start();
        mAudioHandler=new Handler(mAudioHandlerThread.getLooper());

        mAudioRecorderHandlerThread=new HandlerThread(TAG+"_AudioRecorder");
        mAudioRecorderHandlerThread.start();
        mAudioRecorderHandler=new Handler(mAudioRecorderHandlerThread.getLooper());

        mMuxerThread=new HandlerThread(TAG+"_MuxerThread");
        mMuxerThread.start();
        mMuxerHandler=new Handler(mMuxerThread.getLooper());
    }


    private OnEncoderProgressListener mProgressListener;
    @Override
    public void setProgressListener(OnEncoderProgressListener listener) {
        mProgressListener=listener;
    }

    @Override
    public void prepare(final int width,final int height,final int bitRate,@NonNull String path,@NonNull final OnEncodeStateCallback callback) {
        mPath=path;
        mStopCallback=null;
        prepareVideoCodec(width, height, bitRate, callback);
        prepareAudioCodec();

    }

    private void prepareVideoCodec(final int width,final int height,final int bitRate,@NonNull final OnEncodeStateCallback callback){
        mVideoHandler.post(new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {

                Log.w(TAG,String.format("target resolution is  %dx%d",width,height));

                VideoInputSpec spec=queryProperInput(width,height,bitRate);

                int supportedWidth=spec.width;
                int supportedHeight=spec.height;
                int supportedFrameRate=spec.frameRate;
                int supportedBitrate=spec.bitRate;
                String codecName=spec.codecName;

                Log.w(TAG, String.format("Width is %d ,height is %d , frame rate is %d, bitRate is %d", supportedWidth, supportedHeight, supportedFrameRate, supportedBitrate));
                MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, supportedWidth, supportedHeight);
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                format.setInteger(MediaFormat.KEY_BIT_RATE, supportedBitrate);
                format.setInteger(MediaFormat.KEY_FRAME_RATE, supportedFrameRate);
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
//                format.setInteger(MediaFormat.KEY_ROTATION, VIDEO_ENCODE_ORIENTATION);

                Log.w(TAG, "video codec name is " + codecName);
                try {
                    mVideoCodec = MediaCodec.createByCodecName(codecName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mVideoCodec.setCallback(new MediaCodec.Callback() {
                    @Override
                    public void onInputBufferAvailable(MediaCodec codec, int index) {
                        Log.w(TAG, "codec inputBuffer available");
                    }

                    @Override
                    public void onOutputBufferAvailable(final MediaCodec codec, final int index, final MediaCodec.BufferInfo info) {
                        final ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                        Log.i(TAG,"onVideo outputbuffer available , timestamp is " + info.presentationTimeUs); // MODIFIED by jianying.zhang, 2016-10-28,BUG-3137073
                        outputBuffer.position(info.offset);
                        outputBuffer.limit(info.offset + info.size);
                        mMuxerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mMuxerState == STATE_READY) {
                                    if (mMuxer != null) {
                                        if (mProgressListener != null) {
                                            mProgressListener.onEncodeProgressUpdate(info.presentationTimeUs, mEncodingFile.getTotalSpace());
                                        }
                                        mMuxer.writeSampleData(mVideoTrackIndex, outputBuffer, info);
                                    }
                                } else {//Cache frames into buffer when muxer is not ready
                                    VideoOutputCache cache = new VideoOutputCache();
                                    byte[] rawData = new byte[info.size];
                                    outputBuffer.get(rawData);
                                    cache.buffer = ByteBuffer.wrap(rawData);
                                    cache.info = info;
                                    mOutputCache.add(cache);
                                }
                                codec.releaseOutputBuffer(index, false);
                                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                    Log.w(TAG, "stop video");
                                    codec.stop();
                                    codec.release();
                                    waitForMuxerStop(STATE_VIDEO_READY);
                                }
                            }
                        });

                    }

                    @Override
                    public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                        Log.w(TAG, "Codec error :" + e.getMessage(), e);
                    }

                    @Override
                    public void onOutputFormatChanged(final MediaCodec codec, MediaFormat format) {
                        Log.w(TAG, "outputFormat changed");
                        mMuxerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mMuxer != null) {
                                    mVideoTrackIndex = mMuxer.addTrack(codec.getOutputFormat());
//                                    mMuxer.setOrientationHint(VIDEO_ENCODE_ORIENTATION);
                                    waitForMuxerReady(STATE_VIDEO_READY);
                                }
                            }
                        });

                    }
                });

                mVideoCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mInputSurface = mVideoCodec.createInputSurface();

                callback.onEncoderInputSurfaceReady(mInputSurface, supportedWidth, supportedHeight);
            }
        });
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
    public void pauseRecording(final boolean isPaused) {
        mMuxerHandler.post(new Runnable() {
            @Override
            public void run() {
                mIsPaused = isPaused;
            }
        });

    }
    /* MODIFIED-END by jianying.zhang,BUG-3137073*/

    private void prepareAudioCodec(){
        mAudioRecorderHandler.post(new Runnable() {
            @SuppressLint("NewApi")
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                int min_buffer_size = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
                mAudioRecorder=new AudioRecord.Builder().setAudioSource(MediaRecorder.AudioSource.MIC).
                        setAudioFormat(
                                new AudioFormat.Builder().
                                        setEncoding(AudioFormat.ENCODING_PCM_16BIT).
                                        setSampleRate(AUDIO_SAMPLE_RATE).
                                        setChannelMask(AudioFormat.CHANNEL_IN_MONO).
                                        build()).setBufferSizeInBytes(min_buffer_size*2).
                        build();
            }
        });
        mAudioHandler.post(new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                MediaFormat audioFormat = new MediaFormat();
                audioFormat.setString(MediaFormat.KEY_MIME, AUDIO_MIME_TYPE);
                audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, AUDIO_SAMPLE_RATE);
                audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, AUDIO_CHANNEL_COUNT);
                audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BITRATE);

                String codecName=mMediaCodecList.findEncoderForFormat(audioFormat);
                Log.w(TAG,"code name is "+codecName);
                try {
                    mAudioCodec=MediaCodec.createByCodecName(codecName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mAudioCodec.setCallback(new MediaCodec.Callback() {
                    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
                    boolean mIsEOS = false;
                    @Override
                    public void onInputBufferAvailable(MediaCodec codec, int index) {
                        ByteBuffer inputBuffer = codec.getInputBuffer(index);
                        inputBuffer.clear();
                        AudioFrame frame = null;
                        if (mIsEOS) {
                            return;
                        }
                        while (frame==null) {
                            synchronized (mProducerConsumerLock){
                                frame = mAudioConsumerBuffer.poll();
                            }
                            if(frame==null){
                                try {
                                    Thread.sleep(33);//Wait for single frame duration
                                    /* MODIFIED-END by jianying.zhang,BUG-3137073*/
                                } catch (InterruptedException e) {
                                }
                            }
                        }

                        byte[] buffer = frame.content;
                        inputBuffer.put(buffer);
                        long timeStamp = frame.timeStamp;
                        /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
                        if (frame.isEOS) {
                            Log.w(TAG, "notify audio EOS");
                            mIsEOS = true;
                            codec.queueInputBuffer(index, 0, buffer.length, timeStamp, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                        /* MODIFIED-END by jianying.zhang,BUG-3137073*/
                            codec.queueInputBuffer(index, 0, buffer.length, timeStamp, 0);
                        }

                        synchronized (mProducerConsumerLock) {
                            mAudioProducerBuffer.add(frame);//return buffer to the producer
                        }
                    }
                    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
                    private long mLastRecordingTimestamp = -1;

                    private long mPausedDuration = 0;
                    private long mLastAudioTimestamp = 0;

                    @Override
                    public void onOutputBufferAvailable(final MediaCodec codec, final int index, final MediaCodec.BufferInfo info) {
                        //write sample to muxer
                        mMuxerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                while(true) {
                                    if (mIsPaused) {
                                        mPausedDuration += info.presentationTimeUs - mLastAudioTimestamp;
                                        mLastAudioTimestamp = info.presentationTimeUs;
                                        break;//simple jump to release phase
                                    }
                                    mLastAudioTimestamp = info.presentationTimeUs;
                                    final ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                                    outputBuffer.position(info.offset);
                                    outputBuffer.limit(info.offset + info.size);
                                    if (mMuxerState == STATE_READY) {
                                        if (mMuxer != null) {

                                        /* MODIFIED-BEGIN by Sichao Hu, 2016-09-23,BUG-2989818*/
                                            while (true) {
                                                long timeStamp = info.presentationTimeUs;
                                                timeStamp -= mPausedDuration;

                                                if (mLastRecordingTimestamp == -1) {
                                                    mLastRecordingTimestamp = timeStamp;
                                                }
                                                if (timeStamp < mLastRecordingTimestamp) {
                                                    break;
                                                }
                                                mLastRecordingTimestamp = timeStamp;

                                                info.presentationTimeUs = timeStamp;

                                                if (info.size > 0) {
                                                    mMuxer.writeSampleData(mAudioTrackIndex,
                                                            outputBuffer, info);
                                                }
                                                break;
                                            }
                                        /* MODIFIED-END by Sichao Hu,BUG-2989818*/
                                        /* MODIFIED-END by Sichao Hu,BUG-2989818*/
                                        }
                                    }
                                    break;
                                    /* MODIFIED-END by jianying.zhang,BUG-3137073*/
                                }
                                codec.releaseOutputBuffer(index, false);
                                if((info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0){
                                    Log.w(TAG, "stop audio");
                                    codec.stop();
                                    codec.release();
                                    waitForMuxerStop(STATE_AUDIO_READY);
                                }
                            }
                        });

                    }

                    @Override
                    public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
                        Log.e(TAG, "Audio codec error");

                    }

                    @Override
                    public void onOutputFormatChanged(final MediaCodec codec, MediaFormat mediaFormat) {
                        mMuxerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mMuxer != null) {
                                    mAudioTrackIndex = mMuxer.addTrack(codec.getOutputFormat());
                                    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
                                    mPausedDuration = 0;
                                    mIsPaused = false;
                                    /* MODIFIED-END by jianying.zhang,BUG-3137073*/
                                    waitForMuxerReady(STATE_AUDIO_READY);
                                }
                            }
                        });
                    }
                });

                mAudioCodec.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            }
        });
    }



    private static final int STATE_NONE=0;
    private static final int STATE_VIDEO_READY=1;
    private static final int STATE_AUDIO_READY=1<<1;
    private static final int STATE_READY=STATE_AUDIO_READY|STATE_VIDEO_READY;
    private int mMuxerState=STATE_NONE;
    private void waitForMuxerReady(int state){//run in MuxerHandler Thread
        mMuxerState|=state;
        if(mMuxerState==STATE_READY){
            Log.w(TAG,"muxer start");
            mMuxer.start();
            while (!mOutputCache.isEmpty()){//write all cached sample image into muxer
                VideoOutputCache cache=mOutputCache.poll();
                int position=cache.buffer.position();
                int limit=cache.buffer.limit();
                cache.info.set(position,limit-position,cache.info.presentationTimeUs,cache.info.flags);
                mMuxer.writeSampleData(mVideoTrackIndex, cache.buffer, cache.info);
            }
        }
    }

    private int mMuxerStopState=STATE_NONE;
    private void waitForMuxerStop(int state){
        mMuxerStopState|=state;
        if(mMuxerStopState==STATE_READY){
            Log.w(TAG,"try stop muxer "+mMuxer);
            if(mMuxer!=null) {
                Log.w(TAG, "muxer stop");
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
                mStopCallback.onEncoderStopped();
                mAudioRecorderHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAudioRecorder.stop();
                        mAudioRecorder.release();
                        mAudioRecorder=null;
                    }
                });
            }

        }
    }


    private Runnable mAudioRecorderSampleRunnable=new Runnable() {
        @Override
        public void run() {
            //push recorder sample to FIFO

            if(mAudioRecorder==null){
                return;
            }
            AudioFrame frame=null;
            byte[] buffer=null;
            /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
            long timestamp = EncoderUtils.systemTimeInMicroSecond() - mAudioStartTime;
            int bufferSize = AudioRecord.getMinBufferSize(mAudioRecorder.getSampleRate(),
                    mAudioRecorder.getChannelConfiguration(), mAudioRecorder.getAudioFormat());

            if(mAudioProducerBuffer.isEmpty()) {
                buffer = new byte[bufferSize];
                frame = new AudioFrame(buffer,timestamp);
            } else {
                synchronized (mProducerConsumerLock) {
                    frame = mAudioProducerBuffer.poll();
                    buffer = frame.content;
                }
            }

            int result = mAudioRecorder.read(buffer,0,bufferSize);
            if (result == AudioRecord.ERROR_BAD_VALUE
                    || result == AudioRecord.ERROR_INVALID_OPERATION)
                Log.e(TAG, "Read error");
            if (frame!=null) {//ignore this frame if producer is exhausted
                frame.timeStamp = timestamp;
                frame.isEOS = !mIsRecording;
                /* MODIFIED-END by jianying.zhang,BUG-3137073*/
                synchronized (mProducerConsumerLock) {
                    mAudioConsumerBuffer.add(frame);
                }
            }
            mAudioRecorderHandler.post(this);

        }
    };

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
    private void initializeAudioBuffer() {
        Log.w(TAG, "initialize buffer");
        mAudioProducerBuffer.clear();
        mAudioConsumerBuffer.clear();
        mOutputCache.clear();
//        for(int i=0;i<CAPACITY;i++){
//            AudioFrame frame=new AudioFrame(new byte[SAMPLE_PER_FRAME],0);
//            mAudioProducerBuffer.add(frame);
//        }
/* MODIFIED-END by jianying.zhang,BUG-3137073*/
    }

    private long mAudioStartTime;
    @Override
    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
    public void start(OnEncoderStartCallback state) {
        Log.d(TAG, "start mMuxerState : " + mMuxerState + " mIsRecording : " + mIsRecording);
        if (mMuxerState == STATE_VIDEO_READY || mMuxerState == STATE_AUDIO_READY || mIsRecording) {
            if (state != null) {
                state.onError();
            }
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3258603*/

        mEncodingFile=new File(mPath);
        if(mEncodingFile.exists()){
            mEncodingFile.delete();
        }
        try {
            mEncodingFile.createNewFile();
            mMuxer=new MediaMuxer(mEncodingFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            Log.w(TAG,"muxer initialized failed "+mPath);
            e.printStackTrace();
        }

        mMuxerState=STATE_NONE;
        mMuxerStopState=STATE_NONE;

        initializeAudioBuffer();

        Log.w(TAG,"start encoder");

        mVideoHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "Start video codec");
                mVideoCodec.start();
            }
        });

        mAudioHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG,"start audio codec");
                mAudioCodec.start();
            }
        });

        mAudioRecorderHandler.post(new Runnable() {
            @Override
            public void run() {
                mAudioRecorder.startRecording();
                mAudioStartTime = EncoderUtils.systemTimeInMicroSecond(); // MODIFIED by jianying.zhang, 2016-10-28,BUG-3137073
                mAudioRecorderSampleRunnable.run();
            }
        });
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
        mIsRecording=true;
        if (state != null) {
            state.onSuccess();
        }
        /* MODIFIED-END by jianying.zhang,BUG-3258603*/
    }


    private boolean mIsRecording=false;
    private OnEncoderStopCallback mStopCallback;
    @Override
    public void stop(@NonNull OnEncoderStopCallback callback) {
        mStopCallback=callback;
        mVideoHandler.post(new Runnable() {
            @Override
            public void run() {
                /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
                Log.d(TAG ,"stop : " + mIsRecording);
                if (!mIsRecording) {
                    return;
                }
                /* MODIFIED-END by jianying.zhang,BUG-3258603*/
                mIsRecording=false;
                Log.w(TAG,"isRecording = false");
                mVideoCodec.signalEndOfInputStream();
            }
        });
    }

    @Override
    public void release() {
        mVideoHandler.post(new Runnable() {
            @Override
            public void run() {
                mVideoHandler.removeCallbacks(null);
                mVideoHandlerThread.quitSafely();
            }
        });

        mAudioHandler.post(new Runnable() {
            @Override
            public void run() {
                mAudioHandler.removeCallbacks(null);
                mAudioHandlerThread.quitSafely();
            }
        });

        mAudioRecorderHandler.post(new Runnable() {
            @Override
            public void run() {
                mAudioRecorderHandler.removeCallbacks(null);
                mAudioRecorderHandlerThread.quitSafely();
            }
        });

        mMuxerHandler.post(new Runnable() {
            @Override
            public void run() {
                mMuxerHandler.removeCallbacks(null);
                mMuxerThread.quitSafely();
            }
        });
    }

}
