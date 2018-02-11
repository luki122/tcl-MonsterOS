/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.iflytek;

import java.io.IOException;
import java.io.RandomAccessFile;

import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * AbsAudioFileReader
 */
public abstract class AbsAudioFileReader {
    protected static final String TAG = AbsAudioFileReader.class.getSimpleName();
    protected RandomAccessFile mFile;
    protected int mSampleRate;
    protected int mChannels;
    protected int mDuration;
    protected int mStep;// frame period
    protected int mDataLen;
    protected String mFileName;

    public AbsAudioFileReader(String fileName){
        this.mFileName = fileName;
    }
    public int getSampleRate() {
        return mSampleRate;
    }

    public int getChannels() {
        return mChannels;
    }

    public int getmDuration() {
        return mDuration;
    }

    public float getStep() {
        return mStep;
    }

    public String getFileName() {
        return mFileName;
    }

    public int getDataLength() {
        return mDataLen;
    }

    public void open() throws IOException {
        mFile = new RandomAccessFile(mFileName, "r");
    }

    public void close() {
        try {
            if (null != mFile) {
                mFile.close();
                mFile = null;
            }
        } catch (IOException e) {
            MeetingLog.e(TAG, "", e);
        }
    }

    public void seekTo(long offset) throws IOException {
        if (null == mFile) {
            throw new IOException();
        }
        mFile.seek(offset);
    }

    public long getFileLenth() throws IOException {
        if (null == mFile) {
            throw new IOException();
        }
        return mFile.length();
    }

    protected int read(byte[] buffer, int offset, int count) throws IOException {
        if (null == mFile) {
            throw new IOException();
        }
        return mFile.read(buffer, offset, count);
    }
}


