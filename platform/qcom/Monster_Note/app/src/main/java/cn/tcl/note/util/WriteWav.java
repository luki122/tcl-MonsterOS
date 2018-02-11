/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.util;

import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WriteWav {
    private static final String TAG = WriteWav.class.getSimpleName();
    private static final int DEFAULT_CHANNEL_NUMBER = 1;
    public static final int DEFAULT_SAMPLE_RATE = 44100;

    public static long getAudioDura(String file) {
        MediaPlayer player = new MediaPlayer();
        try {
            player.reset();
            player.setDataSource(file);
            player.prepare();
            return player.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            player.release();
        }
        return -1;
    }

    public static void writeWaveFile(File recordFile) {
        RandomAccessFile mRandomAccessFile = null;
        try {
            mRandomAccessFile = new RandomAccessFile(recordFile, "rw");
            mRandomAccessFile.seek(0);
            encodeWaveFile(mRandomAccessFile);
        } catch (IOException e) {
            NoteLog.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            try {
                mRandomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * encode file
     *
     * @param randomAccessFile
     */
    private static void encodeWaveFile(RandomAccessFile randomAccessFile) {
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
        } catch (IOException e) {
            NoteLog.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
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
    private static void WriteWaveFileHeader(RandomAccessFile out, long totalAudioLen,
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

        header[32] = (byte) (1 * 16 / 8); // block align    channels * bits per sample/8
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
}
