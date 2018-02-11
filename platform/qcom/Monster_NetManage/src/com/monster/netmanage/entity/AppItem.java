package com.monster.netmanage.entity;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 应用相关信息
 * 
 * @author zhaolaichao
 *
 */
public class AppItem implements Comparable<AppItem>, Parcelable {

	/**
	 * 应用移动数据
	 */
	private long appData;
	/**
	 * 应用UID
	 */
	private int appUid;
	/**
	 * 应用信息
	 */
	private PackageInfo packageInfo;
	/**
	 * 联多状态
	 */
    private boolean policyStatus;
	public AppItem(Parcel source) {
		super();
		appData = source.readLong();
		appUid = source.readInt();
		packageInfo = source.readParcelable(PackageInfo.class.getClassLoader());
	}
   
	public AppItem() {
		super();
	}


	
	public boolean isPolicyStatus() {
		return policyStatus;
	}

	public void setPolicyStatus(boolean policyStatus) {
		this.policyStatus = policyStatus;
	}

	public PackageInfo getPackageInfo() {
		return packageInfo;
	}

	/**
	 * 应用信息
	 * @param packageInfo
	 */
	public void setPackageInfo(PackageInfo packageInfo) {
		this.packageInfo = packageInfo;
	}

	public long getAppData() {
		return appData;
	}

	/**
	 * 应用移动数据
	 * @param appData
	 */
	public void setAppData(long appData) {
		this.appData = appData;
	}

	public int getAppUid() {
		return appUid;
	}

	/**
	 * 应用UIdD
	 * @param appUid
	 */
	public void setAppUid(int appUid) {
		this.appUid = appUid;
	}

	@Override
	public int compareTo(AppItem another) {
		return Long.compare(another.appData, appData);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(appData);
		dest.writeInt(appUid);
		dest.writeParcelable(packageInfo, flags);
	}
  
	public static final Parcelable.Creator<AppItem> CREATOR = new Parcelable.Creator<AppItem>() {

		@Override
		public AppItem createFromParcel(Parcel source) {

			return new AppItem(source);
		}

		@Override
		public AppItem[] newArray(int size) {

			return new AppItem[size];
		}
	};
}
