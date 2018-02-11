package com.android.camera.decoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;

import com.android.camera.debug.Log;
import com.android.camera.encoder.MicroVideoMuxer;
import com.android.camera.encoder.Muxer;
import com.android.camera.encoder.Muxer.MuxerBuffer;

public class MicroVideoRemixer extends Remixer{

    private static class Paths{
        private List<String> mPaths;
        private int mIndicator=0;
        public Paths(List<String> paths){
            mPaths=paths;
        }
        
        public String getAvailablePath(){
            if(mPaths==null||mPaths.size()==0||mIndicator>=mPaths.size()){
                return null;
            }else{
                return mPaths.get(mIndicator++);
            }
        }
        
        public void reset(){
            mIndicator=0;
        }
    }

    private Log.Tag TAG=new Log.Tag("MicroVideoRemixer");
    private MediaExtractor mExtractor;
    private Muxer mMuxer;
    private Paths mPaths;
    private int mVideoTrack=INVALID_TRACK;
    private int mAudioTrack=INVALID_TRACK;
    private String mOutputPath;
    private static final int INVALID_TRACK=-1;
    private static final String VIDEO_MIME_TYPE = "video";
    private static final String AUDIO_MIME_TYPE = "audio";
    private static final int BUF_CAP=1920*1080*2;
    
    @Override
    public void prepareForRemixer(String outputPath,List<String> paths) {
        mPaths=new Paths(paths);
        mOutputPath=outputPath;
    }
    
    private void initExtractor(Track type){
//        mExtractor=new MediaExtractor();
        updateExtractorSource(type);
        if(mMuxer==null){
            prepareMuxer();
        }
    }
    
    private boolean updateExtractorSource(Track type){
        String path=mPaths.getAvailablePath();
        if(path==null){
            return false;
        }
        if(mExtractor!=null){
            mExtractor.release();
            mExtractor=null;
        }
        try {
            mExtractor=new MediaExtractor();
            mExtractor.setDataSource(path);
            selectTrack(type);
            Log.w(TAG, "updateExtractorSource for " + type.name());
        } catch (IOException e) {
            throw new RuntimeException("Source not invalidate");
        }
        return true;
    }
    
    private long mTimeStamp=0;
    
    private enum Track{
        VIDEO,
        AUDIO,
    }
    private MediaFormat selectTrack(Track type){
        int tractCount=mExtractor.getTrackCount();
        for(int i=0;i<tractCount;i++){
            MediaFormat format=mExtractor.getTrackFormat(i);
            String mime=format.getString(MediaFormat.KEY_MIME);
            Log.w(TAG, "mime type is "+mime);
            boolean isMimeMatch=isMIMEMatch(mime, type==Track.VIDEO?VIDEO_MIME_TYPE:AUDIO_MIME_TYPE);
            if(isMimeMatch){
                mExtractor.selectTrack(i);
                Log.w(TAG, "expect track is "+i);
                return format;
            }
        }
        return null;
    }
    
    private MediaFormat getTrackFromat(Track type){
        int tractCount=mExtractor.getTrackCount();
        for(int i=0;i<tractCount;i++){
            MediaFormat format=mExtractor.getTrackFormat(i);
            String mime=format.getString(MediaFormat.KEY_MIME);
            Log.w(TAG, "mime type is "+mime);
            boolean isMimeMatch=isMIMEMatch(mime, type==Track.VIDEO?VIDEO_MIME_TYPE:AUDIO_MIME_TYPE);
            if(isMimeMatch){
                Log.w(TAG, "expect track is "+i);
                return format;
            }
        }
        return null;
    }
    
    private boolean isMIMEMatch(String mime,String pattern){
        Pattern pat=Pattern.compile(pattern);
        Matcher m=pat.matcher(mime);
        return m.find();
    }

    
    @Override
    public void startRemix() {
        StitcherTask task=new StitcherTask();
        task.execute();
    }
    
    private class StitcherTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            Log.w(TAG, "decoding video");
            doDecoding(Track.VIDEO);
            Log.w(TAG, "decoding audio");
            mPaths.reset();
            doDecoding(Track.AUDIO);
            mMuxer.stopMuxer();
            if(mListener!=null){
                mListener.onRemixDone();
            }
            return null;
        }
        
    }

    private int mOrientation=0;
    @Override
    public void setDisplayOrientation(int orientation) {
        mOrientation=orientation;
    }

    private void prepareMuxer(){
        mMuxer=new MicroVideoMuxer();
        try {
            mMuxer.prepareMuxer(mOutputPath);
        } catch (IOException e) {
            Log.w(TAG, "muxer output path invalidate");
        }
        if(mVideoTrack==INVALID_TRACK||mAudioTrack==INVALID_TRACK){
            MediaFormat videoFormat=getTrackFromat(Track.VIDEO);
            MediaFormat audioFormat=getTrackFromat(Track.AUDIO);
            mVideoTrack=mMuxer.addTrack(videoFormat);
            mAudioTrack=mMuxer.addTrack(audioFormat);
            Log.w(TAG,"muxer orientation is "+mOrientation);
            mMuxer.setOrientationHint(mOrientation);
            mMuxer.startMuxer();
        }
    }
    
    private void doDecoding(Track type){
        initExtractor(type);
        ByteBuffer inputBuffer=ByteBuffer.allocate(BUF_CAP);
        long onemillion=1000000;
        long currentBaseTimestamp=0;
        while(true){
            inputBuffer.clear();
            int size=mExtractor.readSampleData(inputBuffer, 0);
            if(size<=0||mExtractor.getSampleFlags()==MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                if(updateExtractorSource(type)){
                    currentBaseTimestamp=mTimeStamp+onemillion/30;
                    size=mExtractor.readSampleData(inputBuffer, 0);
                }else{
//                    mMuxer.stopMuxer();
                    break;
                }
            }
            updateTimestamp(currentBaseTimestamp);
            Log.w(TAG, "mTimestamp is " + mTimeStamp);
            BufferInfo bufferInfo=new BufferInfo();
            bufferInfo.set(0, size, mTimeStamp, mExtractor.getSampleFlags());
            int track=type==Track.VIDEO?mVideoTrack:mAudioTrack;
            mMuxer.writeData(new MuxerBuffer(track,inputBuffer,bufferInfo));
            mExtractor.advance();
        }
        mVideoTrack=INVALID_TRACK;
        mTimeStamp=0;
    }
    
    private void updateTimestamp(long currentBaseTimestamp){
        if(mTimeStamp==0){
            mTimeStamp=mExtractor.getSampleTime();
        }else{
            if(mTimeStamp<=mExtractor.getSampleTime()){
                mTimeStamp=mExtractor.getSampleTime();
            }else{
                mTimeStamp=currentBaseTimestamp+mExtractor.getSampleTime();//the other frame
            }
        }
    }
    
    @Override
    public void releaseRemixer() {
        if(mExtractor==null){
            return;
        }
        mExtractor.release();
        mExtractor=null;
    }
    

}
