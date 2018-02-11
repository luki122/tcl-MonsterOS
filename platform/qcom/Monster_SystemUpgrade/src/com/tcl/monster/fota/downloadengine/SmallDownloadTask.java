package com.tcl.monster.fota.downloadengine;

import android.os.Parcel;
import android.os.Parcelable;

public class SmallDownloadTask implements Parcelable{

    public static final String ID = "id";
    public static final String PAUSED_REASON = "paused_reason";
    public static final String CURRENTBYTES = "current_bytes";
    public static final String TOTALBYTES = "total_bytes";
    public static final String STATUS = "status";

    public static final int STATUS_PENDDING = 1 << 0;// 1

    public static final int STATUS_RUNNING = 1 << 1;// 2

    public static final int STATUS_PAUSED = 1 << 2;// 4

    public static final int STATUS_CANCELED = 1 << 3;// 8

    public static final int STATUS_FINISHED = 1 << 4;// 16

    public static final int STATUS_KILLED = 1 << 5;// 16

    public static final int STATUS_ERROR = 1 << 6;// 32

    private String mId;

    private int mPausedReason;

    private long mCurrentBytes;

    private long mTotalSize;

    // @Transparent no need to persist
    private long downloadSpeed;

    private int status;

    private long mFrom;

    private String mUrl;

    public int mRetryTimes;

    public int sTaskId;
    private static int a;

    public SmallDownloadTask(String url, long from, long total) {
        mCurrentBytes = 0;
        mTotalSize = 0;
        this.mFrom = from;
        this.mUrl = url;
        this.mTotalSize = total;
        status = STATUS_PENDDING;
        sTaskId = a++;
        this.mId = String.valueOf(sTaskId);
    }

    public SmallDownloadTask(Parcel in) {
        readFromParcel(in);
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SmallDownloadTask other = (SmallDownloadTask) obj;

        if (mId == null) {
            if (other.mId != null)
                return false;
        } else if (!mId.equals(other.mId))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((mId == null) ? 0 : mId.hashCode());

        return result;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public long getFrom() {
        return mFrom;
    }

    public int getPausedReason() {
        return mPausedReason;
    }

    public void setPausedReason(int reason) {
        this.mPausedReason = reason;
    }

    public long getCurrentBytes() {
        return mCurrentBytes;
    }

    public void setCurrentBytes(long currentbytes) {
        this.mCurrentBytes = currentbytes;
    }

    public long getTotalBytes() {
        return mTotalSize;
    }

    public void setTotalBytes(long totalBytes) {
        this.mTotalSize = totalBytes;
    }

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(long downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeInt(mPausedReason);
        dest.writeLong(mCurrentBytes);
        dest.writeLong(mTotalSize);
        dest.writeInt(status);
    }

    private void readFromParcel(Parcel in) {
        mId = in.readString();
        mPausedReason = in.readInt();
        mCurrentBytes = in.readLong();
        mTotalSize = in.readLong();
        status = in.readInt();
    }

    public static final Parcelable.Creator<SmallDownloadTask> CREATOR = new Parcelable.Creator<SmallDownloadTask>() {
        public SmallDownloadTask createFromParcel(Parcel in) {
            return new SmallDownloadTask(in);
        }

        public SmallDownloadTask[] newArray(int size) {
            return new SmallDownloadTask[size];
        }
    };

}
