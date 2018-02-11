 package com.mst.recordsettings;
 
public class CallRecord {
		private String mPath;
		private long mDruation;
		private long mEndTime;
		private String mType;
		private String mName;     // contact name
		private String mFileName; // file name
		
		public void setPath(String path) {
			mPath = path;
		}
		public String getPath() {
			return mPath;
		}
		
		public void setDruation(long druation) {
			mDruation = druation;
		}
		public long getDruation() {
			return mDruation;
		}
		
		public void setEndTime(long time) {
			mEndTime = time;
		}
		public long getEndTime() {
			return mEndTime;
		}
		
		public void setName(String name) {
			mName = name;
		}
		public String getName() {
			return mName;
		}
		
		public void setFileName(String name) {
			mFileName = name;
		}
		public String getFileName() {
			return mFileName;
		}
		
		public void setMimeType(String type) {
			mType = type;
		}
		public String getMimeType() {
			return mType;
		}
		
		@Override
		public String toString() {
			return "Path = " + mPath + "\n Druation = " + mDruation + "   mEndTime = " + mEndTime + " mName = " + mName;
		}
	}