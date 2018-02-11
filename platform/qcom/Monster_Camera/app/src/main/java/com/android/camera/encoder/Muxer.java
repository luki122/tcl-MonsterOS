package com.android.camera.encoder;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

public abstract class Muxer {
    public static class MuxerBuffer{
        public MuxerBuffer(int track,ByteBuffer buffer ,BufferInfo info){
            this.track=track;
            bufferData=buffer;
            bufferInfo=info;
        }
        
        public int track;
        public ByteBuffer  bufferData;
        public BufferInfo  bufferInfo;
        
    }
    public abstract void prepareMuxer(String path) throws IOException;
    
    public abstract void startMuxer();

    public abstract void setOrientationHint(int degree);
    
    public abstract void stopMuxer();
    
    public abstract void writeData(MuxerBuffer buffer);
    
    public abstract int addTrack(MediaFormat format);
}
