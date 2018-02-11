package com.mst.wallpaper.object;

import java.util.jar.Pack200.Packer;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.SparseArray;

public class Wallpaper implements Parcelable {

	public static final int FLAG_SYSTEM = 1;
	
	public static final int FLAG_CUSTOM = 0;
	
	public static final int FLAG_APPLIED = 2;
	/**
	 * Type for other
	 */
	public static final int TYPE_OTHER = -1;

	/**
	 * This type wallpaper will used to Desktop wallpaper.
	 */
	public static final int TYPE_DESKTOP = 0;

	/**
	 * This type wallpaper will used to keyguard Wallpaper.
	 */
	public static final int TYPE_KEYGUARD = 1;

	public int type = TYPE_OTHER;
	/**
	 * The wallpaper's identifier.
	 */
	public int id;

	/**
	 * Current wallpaper is used or not.
	 */
	public boolean applied;

	/**
	 * Key for Search wallpaper path
	 */
	private int key = 0;

	/**
	 * Name of current Wallpaper
	 */
	public String name;

	/**
	 * Saved wallpaper path here
	 */
	private SparseArray<Object> mPaths = new SparseArray<Object>();

	public int systemFlag;
	public String themeColor;
	public int isDefaultTheme = 0;
	public int isTimeBlack = 0;
	public int isStatusBarBlack = 0;

	public int count;
	public Wallpaper() {
	}
	
	public Wallpaper(int type) {
		this(type, null);
	}

	public Wallpaper(int type, String name) {
		this.type = type;
		this.name = name;
		clearWallpaper();
	}

	public Wallpaper(Parcel in) {
		this.type = in.readInt();
		this.id = in.readInt();
		this.name = in.readString();
		this.mPaths = in.readSparseArray(ClassLoader.getSystemClassLoader());
		this.systemFlag  = in.readInt();
		this.themeColor = in.readString();
		this.isDefaultTheme = in.readInt();
		this.isTimeBlack = in.readInt();
		this.isStatusBarBlack = in.readInt();
		this.count = in.readInt();
	}

	/**
	 * Add picture disk path to this wallpaper object
	 * 
	 * @param paths
	 */
	public void addPaths(String... paths) {
		synchronized (mPaths) {
			for (String p : paths) {
				mPaths.append(key, p);
				key++;
			}
		}
	}
	
	/**
	 * Add picture disk path to this wallpaper object
	 * 
	 * @param paths
	 */
	public void addPaths(Object... paths) {
		synchronized (mPaths) {
			for (Object p : paths) {
				mPaths.append(key, p);
				key++;
			}
		}
	}

	/**
	 * Find picture disk path by key {@link Wallpaper.key}
	 * 
	 * @param key
	 * @return
	 */
	public String getPathByKey(int key) {
		synchronized (mPaths) {
			return (String) mPaths.get(key);
		}
	}
	
	public Object getObjectByKey(int key) {
		synchronized (mPaths) {
			return mPaths.get(key);
		}
	}
	

	public int getWallpaperCount() {
		if(count == 0){
			count = mPaths.size();
		}
		return count;
	}

	/**
	 * Clear all of this wallpaper object's pictures
	 */
	public void clearWallpaper() {
		mPaths.clear();
		key = 0;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(this.type);
		out.writeInt(this.id);
		out.writeString(this.name);
		out.writeSparseArray(this.mPaths);
		
		out.writeInt(systemFlag);
		out.writeString(themeColor);
		out.writeInt(isDefaultTheme);
		out.writeInt(isTimeBlack );
		out.writeInt(isStatusBarBlack );
		out.writeInt(count);
	}

	public static void writeToParcel(Wallpaper theme, Parcel out) {
		if (theme != null) {
			theme.writeToParcel(out, 0);
		} else {
			out.writeString(null);
		}
	}

	public static Wallpaper readFromParcel(Parcel in) {
		return in != null ? new Wallpaper(in) : null;
	}

	public static final Parcelable.Creator<Wallpaper> CREATOR = new Parcelable.Creator<Wallpaper>() {
		public Wallpaper createFromParcel(Parcel in) {
			return new Wallpaper(in);
		}

		public Wallpaper[] newArray(int size) {
			return new Wallpaper[size];
		}
	};
	
	@Override
	public String toString() {
		
	return "name:"+name+"  id:"+id
			+" applied:"+applied;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		Wallpaper other = (Wallpaper) obj;
		if(other == null){
			return false;
		}
		return this.id == other.id&& equalsWallpaperData(other);
	}
	
	private boolean equalsWallpaperData(Wallpaper other){
		boolean equalsCount = getWallpaperCount() == other.getWallpaperCount();
		if(!equalsCount){
			return false;
		}
		for(int i = 0;i< getWallpaperCount();i++){
			String currentWallpaper = getPathByKey(i);
			String otherWallpaper = other.getPathByKey(i);
			if(TextUtils.isEmpty(currentWallpaper)){
				return false;
			}
			if(!currentWallpaper.equals(otherWallpaper)){
				return false;
			}
		}
		return true;
	}

}
