package com.mst.tms;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class NetInfoEntity implements Parcelable {
	// Field descriptor #55 J
	public long mTotalForMonth;

	// Field descriptor #55 J
	public long mUsedForMonth;

	// Field descriptor #55 J
	public long mUsedTranslateForMonth;

	// Field descriptor #55 J
	public long mUsedReceiveForMonth;

	// Field descriptor #55 J
	public long mRetialForMonth;

	// Field descriptor #55 J
	public long mUsedForDay;

	// Field descriptor #55 J
	public long mUsedTranslateForDay;

	// Field descriptor #55 J
	public long mUsedReceiveForDay;
	//free time
	public long mFreeUsedForMonth;
	// Field descriptor #58 Ljava/util/Date;
	public java.util.Date mStartDate;

	
	public NetInfoEntity() {
		super();
	}

	public NetInfoEntity(long mTotalForMonth, long mUsedForMonth, long mUsedTranslateForMonth,
			long mUsedReceiveForMonth, long mRetialForMonth, long mUsedForDay, long mUsedTranslateForDay,
			long mUsedReceiveForDay, Date mStartDate) {
		super();
		this.mTotalForMonth = mTotalForMonth;
		this.mUsedForMonth = mUsedForMonth;
		this.mUsedTranslateForMonth = mUsedTranslateForMonth;
		this.mUsedReceiveForMonth = mUsedReceiveForMonth;
		this.mRetialForMonth = mRetialForMonth;
		this.mUsedForDay = mUsedForDay;
		this.mUsedTranslateForDay = mUsedTranslateForDay;
		this.mUsedReceiveForDay = mUsedReceiveForDay;
		this.mStartDate = mStartDate;
	}

	public long getmTotalForMonth() {
		return mTotalForMonth;
	}

	public void setmTotalForMonth(long mTotalForMonth) {
		this.mTotalForMonth = mTotalForMonth;
	}

	public long getmUsedForMonth() {
		return mUsedForMonth;
	}

	public void setmUsedForMonth(long mUsedForMonth) {
		this.mUsedForMonth = mUsedForMonth;
	}

	public long getmUsedTranslateForMonth() {
		return mUsedTranslateForMonth;
	}

	public void setmUsedTranslateForMonth(long mUsedTranslateForMonth) {
		this.mUsedTranslateForMonth = mUsedTranslateForMonth;
	}

	public long getmUsedReceiveForMonth() {
		return mUsedReceiveForMonth;
	}

	public void setmUsedReceiveForMonth(long mUsedReceiveForMonth) {
		this.mUsedReceiveForMonth = mUsedReceiveForMonth;
	}

	public long getmRetialForMonth() {
		return mRetialForMonth;
	}

	public void setmRetialForMonth(long mRetialForMonth) {
		this.mRetialForMonth = mRetialForMonth;
	}

	public long getmUsedForDay() {
		return mUsedForDay;
	}

	public void setmUsedForDay(long mUsedForDay) {
		this.mUsedForDay = mUsedForDay;
	}

	public long getmUsedTranslateForDay() {
		return mUsedTranslateForDay;
	}

	public void setmUsedTranslateForDay(long mUsedTranslateForDay) {
		this.mUsedTranslateForDay = mUsedTranslateForDay;
	}

	public long getmUsedReceiveForDay() {
		return mUsedReceiveForDay;
	}

	public void setmUsedReceiveForDay(long mUsedReceiveForDay) {
		this.mUsedReceiveForDay = mUsedReceiveForDay;
	}
	
	public long getmFreeUsedForMonth() {
		return mFreeUsedForMonth;
	}

	public void setmFreeUsedForMonth(long mFreeUsedForMonth) {
		this.mFreeUsedForMonth = mFreeUsedForMonth;
	}

	public java.util.Date getmStartDate() {
		return mStartDate;
	}

	public void setmStartDate(java.util.Date mStartDate) {
		this.mStartDate = mStartDate;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mTotalForMonth);
		dest.writeLong(mUsedForMonth);
		dest.writeLong(mUsedTranslateForMonth);
		dest.writeLong(mUsedReceiveForMonth);
		dest.writeLong(mRetialForMonth);
		dest.writeLong(mUsedForDay);
		dest.writeLong(mUsedTranslateForDay);
		dest.writeLong(mUsedReceiveForDay);
		dest.writeSerializable(mStartDate);

	}

	public static final Parcelable.Creator<NetInfoEntity> CREATOR = new Parcelable.Creator<NetInfoEntity>() {

		@Override
		public NetInfoEntity createFromParcel(Parcel source) {

			return new NetInfoEntity(source.readLong(), source.readLong(), source.readLong(), source.readLong(),
					source.readLong(), source.readLong(), source.readLong(), source.readLong(),
					(Date) source.readSerializable());
		}

		@Override
		public NetInfoEntity[] newArray(int size) {

			return new NetInfoEntity[size];
		}
	};

	/**
	 * 读
	 * 
	 * @param in
	 */
	public void readFromParcel(Parcel in) {
		mTotalForMonth = in.readLong();
		mUsedForMonth = in.readLong();
		mUsedTranslateForMonth = in.readLong();
		mUsedReceiveForMonth = in.readLong();
		mRetialForMonth = in.readLong();
		mUsedForDay = in.readLong();
		mUsedTranslateForDay = in.readLong();
		mUsedReceiveForDay = in.readLong();
		mStartDate = (Date) in.readSerializable();
	}

	@Override
	public String toString() {
		return "NetInfoEntity [mTotalForMonth=" + mTotalForMonth + ", mUsedForMonth=" + mUsedForMonth
				+ ", mUsedTranslateForMonth=" + mUsedTranslateForMonth + ", mUsedReceiveForMonth="
				+ mUsedReceiveForMonth + ", mRetialForMonth=" + mRetialForMonth + ", mUsedForDay=" + mUsedForDay
				+ ", mUsedTranslateForDay=" + mUsedTranslateForDay + ", mUsedReceiveForDay=" + mUsedReceiveForDay
				+ ", mFreeUsedForMonth=" + mFreeUsedForMonth + "]";
	}
	
}
