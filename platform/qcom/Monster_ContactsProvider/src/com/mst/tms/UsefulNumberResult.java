package com.mst.tms;


import android.os.Parcel;
import android.os.Parcelable;

public class UsefulNumberResult implements Parcelable {	

	// Field descriptor #52 Ljava/lang/String;
	public String number;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	// Field descriptor #52 Ljava/lang/String;
	public String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	

	public UsefulNumberResult(String number, String name) {
		this.number = number;
		this.name = name;
	}

	@Override
	public int describeContents() {
		return 1;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(number);
		dest.writeString(name);
	}

	public static final Parcelable.Creator<UsefulNumberResult> CREATOR = new Parcelable.Creator<UsefulNumberResult>() {

		@Override
		public UsefulNumberResult createFromParcel(Parcel source) {

			return new UsefulNumberResult(source.readString(), source.readString());
		}

		@Override
		public UsefulNumberResult[] newArray(int size) {

			return new UsefulNumberResult[size];
		}
	};

	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("number:");
		strBuilder.append(number);
		strBuilder.append(" name:");
		strBuilder.append(name);    	
		return strBuilder.toString();
	}
}