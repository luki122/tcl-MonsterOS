/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.iflytek;

import com.iflytek.recinbox.sdk.speech.interfaces.IAudioInputStream;
import java.io.IOException;

import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * WavFileReader
 */
public class WavFileReader extends AbsAudioFileReader implements IAudioInputStream{
    private static final int HEAD_LEN = 44;


    public WavFileReader(String fileName) {
        super(fileName);
    }

    @Override
    public void open() {
        try {
            super.open();
            byte[] fmt = new byte[44];
            read(fmt, 0, fmt.length);

            mChannels = ((0xff & fmt[23]) << 8) | ((0xff & fmt[22]));
            mSampleRate = ((0xff & fmt[27]) << 24) | ((0xff & fmt[26]) << 16)
                    | ((0xff & fmt[25]) << 8) | ((0xff & fmt[24]));

            mStep = mSampleRate * 2 * mChannels / 1000;
            mDuration = (int) ((getFileLenth() - HEAD_LEN) / mStep);

            mDataLen = (int) (getFileLenth() - HEAD_LEN);

            MeetingLog.d(TAG, "open file=" + mFileName + " mChannels=" + mChannels
                    + " mSampleRate=" + mSampleRate + " mStep=" + mStep
                    + " mDuration=" + mDuration + " mDataLen=" + mDataLen);
        } catch (Exception e) {
            MeetingLog.e(TAG, "", e);
        }

    }

    @Override
    public int readData(byte[] buffer,int offset, int count) {
        try {
            return read(buffer, offset, count);
        } catch (IOException e) {
            MeetingLog.e(TAG, "", e);
        }
        return 0;
    }
    @Override
    public void seekTo(long offset) {
        try {
            super.seekTo(offset + HEAD_LEN);
        } catch (IOException e) {
            MeetingLog.e(TAG, "", e);
        }
    }
}
