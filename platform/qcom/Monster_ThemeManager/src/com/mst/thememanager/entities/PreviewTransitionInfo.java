package com.mst.thememanager.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Information for preview object.
 * It contain preview imag's position and
 * size that layout in detail page.
 *
 */
public class PreviewTransitionInfo implements Parcelable {
	
	public static final String KEY_ID = "PreviewTransitionInfo:id";
	public static final String KEY_INFO = "PreviewTransitionInfo:info";
	
	/**
	 * Index for preview image
	 */
	public int index;
	
	/**
	 * X axis for preview image
	 */
	public int x;
	
	/**
	 * Y axis for preview image
	 */
	public int y;
	
	public PreviewTransitionInfo(){}
	
	public  PreviewTransitionInfo(Parcel in) {
		// TODO Auto-generated constructor stub
		index = in.readInt();
		x = in.readInt();
		y = in.readInt();
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(index);
		dest.writeInt(x);
		dest.writeInt(y);
		
	}
	
	
	
	public static void writeToParcel(PreviewTransitionInfo info, Parcel out) {
		if (info != null) {
			info.writeToParcel(out, 0);
		} else {
			out.writeString(null);
		}
	}

	public static PreviewTransitionInfo readFromParcel(Parcel in) {
		return in != null ? new PreviewTransitionInfo(in) : null;
	}
	public static final Parcelable.Creator<PreviewTransitionInfo> CREATOR = new Parcelable.Creator<PreviewTransitionInfo>() {
		public PreviewTransitionInfo createFromParcel(Parcel in) {
			return new PreviewTransitionInfo(in);
		}

		public PreviewTransitionInfo[] newArray(int size) {
			return new PreviewTransitionInfo[size];
		}
	};

}
