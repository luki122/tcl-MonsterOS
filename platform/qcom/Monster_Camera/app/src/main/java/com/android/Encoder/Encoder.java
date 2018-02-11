/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.Encoder;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.util.Range;
import android.view.Surface;

import com.android.camera.debug.Log;

/**
 * Created by sichao.hu on 8/22/16.
 */
public abstract class Encoder {
    private Log.Tag TAG=new Log.Tag("AbsEncoder");
    protected MediaCodecList mMediaCodecList;

    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    protected static final int AUDIO_BITRATE=128000;

    protected static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    protected static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    protected static final int AUDIO_CHANNEL_COUNT=1;
    protected static final int AUDIO_SAMPLE_RATE=44100;
    protected static final int FRAME_RATE = 30;               // 30fps
    protected static final int IFRAME_INTERVAL = 2;           // 0.55 seconds between I-frames
    /* MODIFIED-END by sichao.hu,BUG-2895116*/

    public interface OnEncodeStateCallback{
        public void onEncoderInputSurfaceReady(Surface surface,int wdith,int height);
    }

    public interface OnEncoderStopCallback{
        public void onEncoderStopped();
    }

    public interface OnEncoderProgressListener {
        /* MODIFIED-BEGIN by sichao.hu, 2016-09-12,BUG-2895116*/
        /**
         * Callback on ever unit data written to the muxer
         * @param videoDuration the duration of video under recording ,derived in microseconds
         * @param fileSize the byte-size of video under recording
         */
        public void onEncodeProgressUpdate(long videoDuration,long fileSize);
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
    public interface OnEncoderStartCallback {
        void onSuccess();
        void onError();
    }
    /* MODIFIED-END by jianying.zhang,BUG-3258603*/

    protected class VideoInputSpec{
        public int width;
        public int height;
        public int bitRate;
        public int frameRate;
        public String codecName;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Encoder(){
        mMediaCodecList=new MediaCodecList(MediaCodecList.ALL_CODECS);
    }

    public abstract void setProgressListener(OnEncoderProgressListener listener);
    public abstract void prepare(int width, int height, int bitRate,String path, OnEncodeStateCallback callback);
    public abstract void start(OnEncoderStartCallback state); // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
    public abstract void stop(OnEncoderStopCallback callback);
    public abstract void release();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected VideoInputSpec queryProperInput(int width,int height,int bitRate){
        MediaCodecInfo[] infos=mMediaCodecList.getCodecInfos();
        String codecName=null;
        int supportedWidth=width;
        int supportedHeight=height;
        int supportedFrameRate=FRAME_RATE;
        int supportedBitrate=bitRate;
        int maxArea=0;
        for(MediaCodecInfo info:infos){
            if(!info.isEncoder()){
                continue;
            }
            boolean isCodecTypeSupported=false;
            String[] supportedTypes=info.getSupportedTypes();
            for(String type:supportedTypes){
                if(type.equals(MIME_TYPE)){
                    isCodecTypeSupported=true;
                    break;
                }
            }
            if(!isCodecTypeSupported){
                continue;
            }

            Log.w(TAG, "current codec name is  " + info.getName());
            MediaCodecInfo.CodecCapabilities capabilities=info.getCapabilitiesForType(MIME_TYPE);
            boolean isSurfaceInputSupported=false;
            int[] formats=capabilities.colorFormats;
            for(int format:formats){
                if(format==MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface){
                    isSurfaceInputSupported=true;
                    break;
                }
            }
            if(!isSurfaceInputSupported){
                Log.w(TAG,"Surface input not supported");
                continue;
            }
            Range<Integer> widthRange=capabilities.getVideoCapabilities().getSupportedWidths();
            Range<Integer> heightRange=capabilities.getVideoCapabilities().getSupportedHeights();

            if((widthRange.contains(width)&&heightRange.contains(height))||
                    (widthRange.contains(height)&&heightRange.contains(width))){
                supportedWidth=width;
                supportedHeight=height;
                int upperFrameRate=capabilities.getVideoCapabilities().getSupportedFrameRates().getUpper();
                supportedFrameRate=upperFrameRate>FRAME_RATE?FRAME_RATE:upperFrameRate;
                int upperBitRate=capabilities.getVideoCapabilities().getBitrateRange().getUpper();
                supportedBitrate=upperBitRate>bitRate?bitRate:upperBitRate;
                codecName=info.getName();
                Log.w(TAG,String.format("supported width is %d ,height is %d , frameRate is %d , bitRate is %d",width,height,supportedFrameRate,supportedBitrate));
                Log.w(TAG,"expected spec supported");
                break;
            }else{//Expected width or height is larger than supported ones
                int maxWidth=widthRange.getUpper();
                int maxHeight=heightRange.getUpper();
                int maxLongerSide=Math.max(maxWidth,maxHeight);
                int maxShorterSide=Math.min(maxWidth,maxHeight);
                Log.w(TAG,String.format("looking for spec:%d*%d",maxWidth,maxHeight));

                if(width>=height){
                    maxWidth=maxLongerSide;
                    maxHeight=maxShorterSide;
                }else{
                    maxWidth=maxShorterSide;
                    maxHeight=maxLongerSide;
                }

                int maxScaledWidth=maxHeight*width/height;
                int maxScaledHeight=maxWidth*height/width;
                if(maxScaledWidth<width){
                    maxScaledWidth=maxScaledWidth-maxScaledWidth%8;//8 pixel alignment
                    int area=maxScaledWidth*maxHeight;
                    if(area>=maxArea) {
                        supportedWidth = maxScaledWidth;
                        supportedHeight = maxHeight;
                        maxArea=area;
                        int upperFrameRate=capabilities.getVideoCapabilities().getSupportedFrameRates().getUpper();
                        supportedFrameRate=upperFrameRate>FRAME_RATE?FRAME_RATE:upperFrameRate;
                        int upperBitRate=capabilities.getVideoCapabilities().getBitrateRange().getUpper();
                        supportedBitrate=upperBitRate>bitRate?bitRate:upperBitRate;
                        codecName=info.getName();
                    }
                }else{
                    maxScaledHeight=maxScaledHeight-maxScaledHeight%8;
                    int area=maxScaledHeight*maxWidth;
                    if(area>=maxArea) {
                        supportedWidth = maxWidth;
                        supportedHeight = maxScaledHeight;//8 step alignment
                        maxArea=area;
                        int upperFrameRate=capabilities.getVideoCapabilities().getSupportedFrameRates().getUpper();
                        supportedFrameRate=upperFrameRate>FRAME_RATE?FRAME_RATE:upperFrameRate;
                        int upperBitRate=capabilities.getVideoCapabilities().getBitrateRange().getUpper();
                        supportedBitrate=upperBitRate>bitRate?bitRate:upperBitRate;
                        codecName=info.getName();
                    }
                }
            }

        }

        VideoInputSpec spec=new VideoInputSpec();
        spec.width=supportedWidth;
        spec.height=supportedHeight;
        spec.bitRate=supportedBitrate;
        spec.frameRate=supportedFrameRate;
        spec.codecName=codecName;
        return spec;
    }
}
