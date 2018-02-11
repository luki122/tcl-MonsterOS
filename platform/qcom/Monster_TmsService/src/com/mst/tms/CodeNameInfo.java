package com.mst.tms;

import android.os.Parcel;
import android.os.Parcelable;

public class CodeNameInfo implements Parcelable {

	public String mCode;

	// Field descriptor #16 Ljava/lang/String;
	public String mName;

	
	public CodeNameInfo(String code, String name) {
		super();
		this.mCode = code;
		this.mName = name;
	}

	public String getmCode() {
		return mCode;
	}

	public void setmCode(String mCode) {
		this.mCode = mCode;
	}

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mCode);
		dest.writeString(mName);
	}
  
	public static final Parcelable.Creator<CodeNameInfo> CREATOR = new Parcelable.Creator<CodeNameInfo>() {

		@Override
		public CodeNameInfo createFromParcel(Parcel source) {

			return new CodeNameInfo(source.readString(), source.readString());
		}

		@Override
		public CodeNameInfo[] newArray(int size) {

			return new CodeNameInfo[size];
		}
	};
}
