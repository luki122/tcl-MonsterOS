package com.tcl.monster.fota.downloadengine;


import java.lang.reflect.Type;
import java.util.Set;

import com.tcl.monster.fota.model.DownloadInfo;
import com.tcl.monster.fota.misc.State;
import com.tcl.monster.fota.model.UpdatePackageInfo;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tcl.monster.fota.provider.Fota;

public class DownloadTask implements Parcelable{
	

	public static final String ID = "id";
	public static final String PAUSED_REASON = "paused_reason";
	public static final String CURRENTBYTES = "current_bytes";
	public static final String TOTALBYTES = "total_bytes";
	public static final String STATE = "state";
	public static final String URL_BEST = "url_best";
	public static final String UPDATE_INFO_JSON = "updateinfo";
	public static final String DOWNLOAD_INFO_JSON = "downloadinfo";
	public static final String DOWNLOAD_TASKS_JSON = "smalldownloadtasksinfo";
	

	private String mId;
	
	private int mPausedReason;
	
	private long mCurrentBytes;
	
	private long mTotalSize;
	
	// @Transparent no need to persist
	private long downloadSpeed;
	
	private String mState;
	
	private String mBestUrl;
	
	private String mUpdateJson ;
	
	private String mDownloadJson ;
	
	private String mSmallTasksJson;
	
    public DownloadTask() {
        mCurrentBytes = 0;
        mTotalSize = 0;
        mState = State.CHECKED.name();
    }

    public DownloadTask(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DownloadTask other = (DownloadTask) obj;

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

    public String getState() {
        return this.mState;
    }

    public void setState(String state) {
        this.mState = state;
    }

    public String getBestUrl() {
        return this.mBestUrl;
    }

    public void setBestUrl(String best) {
        this.mBestUrl = best;
    }

    public synchronized int getPausedReason() {
        return mPausedReason;
    }

    public synchronized void setPausedReason(int reason) {
		if(mPausedReason == Fota.Firmware.PAUSED_REASON_USER){
			if(reason != Fota.Firmware.PAUSED_REASON_NOT_PAUSED){
                return;
            }
        }
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

    public String getUpdateInfoJson() {
        return mUpdateJson;
    }

    public void setUpdateInfoJson(String json) {
        this.mUpdateJson = json;
    }

    public UpdatePackageInfo getUpdateInfo() {
        if (mUpdateJson == null) {

            return null;
        }
        return new Gson().fromJson(getUpdateInfoJson(), UpdatePackageInfo.class);
    }

    public String getDownloadInfoJson() {
        return mDownloadJson;
    }

    public void setDownlaodInfoJson(String json) {
        this.mDownloadJson = json;
    }

    public DownloadInfo getDownloadInfo() {
        if (mDownloadJson == null) {
            return null;
        }
        return new Gson().fromJson(getDownloadInfoJson(), DownloadInfo.class);
    }

    public String getSmallTasksJson() {
        return mSmallTasksJson;
    }

    public void setSmallTasksJson(String json) {
        this.mSmallTasksJson = json;
    }

    public Set<SmallDownloadTask> getSmallTasks() {
        if (TextUtils.isEmpty(mSmallTasksJson)) {
            return null;
        }
        Type type = new TypeToken<Set<SmallDownloadTask>>() {}.getType();

        return new Gson().fromJson(mSmallTasksJson, type);
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
        dest.writeString(mState);
        dest.writeString(mBestUrl);
        dest.writeString(mUpdateJson);
        dest.writeString(mDownloadJson);
    }

    private void readFromParcel(Parcel in) {
        mId = in.readString();
        mPausedReason = in.readInt();
        mCurrentBytes = in.readLong();
        mTotalSize = in.readLong();
        mState = in.readString();
        mBestUrl = in.readString();
        mUpdateJson = in.readString();
        mDownloadJson = in.readString();
    }

    public static final Parcelable.Creator<DownloadTask> CREATOR = new Parcelable.Creator<DownloadTask>() {
        public DownloadTask createFromParcel(Parcel in) {
            return new DownloadTask(in);
        }

        public DownloadTask[] newArray(int size) {
            return new DownloadTask[size];
        }
    };
}