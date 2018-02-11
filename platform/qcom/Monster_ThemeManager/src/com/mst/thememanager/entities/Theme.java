package com.mst.thememanager.entities;

import java.util.ArrayList;
import java.util.List;

import com.mst.thememanager.job.LocalThemeJob;
import com.mst.thememanager.job.ThreadPool.Job;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * 
 * Base class for Theme.app may to extends this 
 * to declare sub Theme(such as,Wallpaper,RingTong).
 *
 */
public class Theme implements Parcelable {
	
	public static final int THEME_NULL = 0x00;
	public static final int THEME_PKG = 0x01;
	public static final int RINGTONG = 0x02;
	public static final int WALLPAPER = 0x03;
	public static final int FONTS = 0x04;
	
	public static final int APPLIED = 1;
	public static final int UN_APPLIED = 0;
	
	
	public int type = THEME_PKG;
	public int id;
	public String name;
	public String designer;
	public String version;
	public String size;
	public int sizeCount;
	public String description;
	public String themeFilePath;
	public ThemeZip themeZipFile;
	public String downloadUrl;
	public String loadedPath;
	public boolean applyStatus;
	public boolean loaded;
	public int downloadStatus;
	public long lastModifiedTime;
	public long totalBytes;
	public int systemWallpaperNumber = -1;
	public String systemKeyguardWallpaperName = null;
	
	public ArrayList<String> previewArrays = new ArrayList<String>();
	
	public ArrayList<String> wallpaperArrays = new ArrayList<String>();

	public Theme() {
		// TODO Auto-generated constructor stub
	}
	
	 public  Job<Theme> requestThemeInfo(String path){
		 
		 return new LocalThemeJob(path);
	 }
	
	@SuppressWarnings("unchecked")
	public Theme(Parcel in) {
		id = in.readInt();
		sizeCount = in.readInt();
		type = in.readInt();
		name = in.readString();
		description = in.readString();
		designer = in.readString();
		version = in.readString();
		size = in.readString();
		themeFilePath = in.readString();
		loadedPath = in.readString();
		totalBytes = in.readLong();
		systemWallpaperNumber = in.readInt();
		systemKeyguardWallpaperName = in.readString();
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(id);
		dest.writeInt(sizeCount);
		dest.writeInt(type);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(designer);
		dest.writeString(version);
		
		dest.writeString(size);
		dest.writeString(themeFilePath);
		dest.writeString(loadedPath);
		dest.writeLong(totalBytes);
		dest.writeInt(systemWallpaperNumber);
		dest.writeString(systemKeyguardWallpaperName);

	}
	public static void writeToParcel(Theme theme, Parcel out) {
		if (theme != null) {
			theme.writeToParcel(out, 0);
		} else {
			out.writeString(null);
		}
	}

	public static Theme readFromParcel(Parcel in) {
		return in != null ? new Theme(in) : null;
	}
	public static final Parcelable.Creator<Theme> CREATOR = new Parcelable.Creator<Theme>() {
		public Theme createFromParcel(Parcel in) {
			return new Theme(in);
		}

		public Theme[] newArray(int size) {
			return new Theme[size];
		}
	};

}
