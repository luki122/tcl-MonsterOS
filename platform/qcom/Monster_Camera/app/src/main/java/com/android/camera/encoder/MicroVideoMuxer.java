package com.android.camera.encoder;

import java.io.File;
import java.io.IOException;

import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.android.camera.debug.Log;


public class MicroVideoMuxer extends Muxer{


    private Log.Tag TAG=new Log.Tag("MicroVideoMuxer");
    private static final int MSG_START_MUXER=0;
    private static final int MSG_STOP_MUXER=MSG_START_MUXER+1;
    private static final int MSG_WRITE_DATA=MSG_STOP_MUXER+1;
    private MediaMuxer mMuxer;
    private boolean mStarted;
    private MuxerHandler mHandler;

    private class MuxerHandler extends Handler{
        public MuxerHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
            case MSG_START_MUXER:
                if(mMuxer!=null){
                    mMuxer.start();
                }
                break;
            case MSG_STOP_MUXER:
                if(mMuxer!=null){
                    mMuxer.stop();
                    mMuxer.release();
                    mMuxer=null;
                }
                mStarted=false;
                break;
            case MSG_WRITE_DATA:
                MuxerBuffer buffer=(MuxerBuffer)msg.obj;
                buffer.bufferData.position(buffer.bufferInfo.offset);
                buffer.bufferData.limit(buffer.bufferInfo.offset + buffer.bufferInfo.size);
//                if(buffer.bufferInfo.flags==MediaCodec.BUFFER_FLAG_END_OF_STREAM){
//                    mMuxer.writeSampleData(buffer.track, null,)
//                }
                Log.w(TAG, "write data track for " + buffer.track + " bufferData is " + buffer.bufferData.toString());
                mMuxer.writeSampleData(buffer.track, buffer.bufferData, buffer.bufferInfo);
                break;
            }
            super.handleMessage(msg);
        }
        
        
    }
    
    public MicroVideoMuxer(){
        HandlerThread thread=new HandlerThread("VideoMuxer");
        thread.start();
        mHandler=new MuxerHandler(thread.getLooper());
    }

    @Override
    public void setOrientationHint(int degree) {
        mMuxer.setOrientationHint(degree);
    }

    private String mPath;
    @Override
    public void prepareMuxer(String path) throws IOException {
        mPath=path;
        mMuxer=new MediaMuxer(path,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    @Override
    public void startMuxer() {
        mStarted=true;
//        mHandler.sendEmptyMessage(MSG_START_MUXER);
        if(mMuxer!=null){
            mMuxer.start();
        }
    }

    @Override
    public void stopMuxer() {
//        mHandler.sendEmptyMessage(MSG_STOP_MUXER);
        if(mMuxer!=null){
            mMuxer.stop();
            mMuxer.release();
            mMuxer=null;
        }
        mStarted=false;
        File file=new File(mPath);
        Log.w(TAG, "stop muxer file size is "+file.length());
        
    }

    @Override
    public void writeData(MuxerBuffer buffer) {
//        Message.obtain(mHandler, MSG_WRITE_DATA, buffer).sendToTarget();
//        buffer.bufferData.position(buffer.bufferInfo.offset);
//        buffer.bufferData.limit(buffer.bufferInfo.offset + buffer.bufferInfo.size);
//        if(buffer.bufferInfo.flags==MediaCodec.BUFFER_FLAG_END_OF_STREAM){
//            mMuxer.writeSampleData(buffer.track, null,)
//        }
//        Log.w(TAG, "write data track for "+buffer.track+" bufferData is "+buffer.bufferData.toString());
        mMuxer.writeSampleData(buffer.track, buffer.bufferData, buffer.bufferInfo);
        File file=new File(mPath);
//        Log.w(TAG, "file size is "+file.length());
    }

    @Override
    public int addTrack(MediaFormat format) {
        return mMuxer.addTrack(format);
    }
    
}
