package com.tcl.monster.fota.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

/**
 * This class is a java bean ,which represent information about 
 * the update package . It will be filled out when first step 
 * of checking is successfully done .And will be saved to 
 * DownloadTask when second step of checking is successfully done.
 * @author haijun.chen
 *
 */
public class UpdatePackageInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The description of the update package .Bug fix ,UX improvements etc.
	 */
	public String mUpdateDesc ;
	public String mEncodingError ;
	/**
	 * The CU of this update package .
	 */
	public String mCuref;
    
	public String mType ;
	public String mFv;
	public String mTv;
	public String mSvn;
	public String mReleaseYear ;
	public String mReleaseMonth ;
	public String mReleaseDay ;
	public String mReleaseHour ;
	public String mReleaseMinute ;
	public String mReleaseSecond ;
	public String mReleaseTimezone;
	public String mReleasePublisher;
    
	public String mFirmwareId ;
	public int mFileCount ;
	public List<UpdateFile> mFiles = new ArrayList<UpdateFile>();
	

	public String mDescription ;
    

	public static class UpdateFile implements Serializable{
		public String mFileName;
		public String mFileId ;
		public long mFileSize ;
		public String mCheckSum ;
		public String mFileVersion;
		public String mFileIndex ;
		
		@Override
		public String toString() {
			return "UpdateFile [mFileName=" + mFileName + ", mFileId="
					+ mFileId + ", mFileSize=" + mFileSize + ", mCheckSum="
					+ mCheckSum + ", mFileVersion=" + mFileVersion
					+ ", mFileIndex=" + mFileIndex + "]";
		}
	}
	
	@Override
	public String toString() {
		return "UpdatePackageInfo [mUpdateDesc=" + mUpdateDesc
				+ ", mEncodingError=" + mEncodingError + ", mCuref=" + mCuref
				+ ", mType=" + mType + ", mFv=" + mFv + ", mTv=" + mTv
				+ ", mSvn=" + mSvn + ", mReleaseYear=" + mReleaseYear
				+ ", mReleaseMonth=" + mReleaseMonth + ", mReleaseDay="
				+ mReleaseDay + ", mReleaseHour=" + mReleaseHour
				+ ", mReleaseMinute=" + mReleaseMinute + ", mReleaseSecond="
				+ mReleaseSecond + ", mReleaseTimezone=" + mReleaseTimezone
				+ ", mReleasePublisher=" + mReleasePublisher + ", mFirmwareId="
				+ mFirmwareId + ", mFileCount=" + mFileCount + ", mFiles="
				+ mFiles + ", mDescription=" + mDescription + "]";
	}
	
    public String toJson() {
        return new Gson().toJson(this);
    }
    
}
