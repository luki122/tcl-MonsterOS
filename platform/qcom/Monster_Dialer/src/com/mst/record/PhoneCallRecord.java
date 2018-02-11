package com.mst.record;

public class PhoneCallRecord {
        private String mPath;
        private long mDruation;
        private long mEndTime;
        private String mType;
        
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
        public void setEndTime(long endTime) {
            mEndTime = endTime;
        }
        public long getEndTime() {
            return mEndTime;
        }
        
        @Override
        public String toString() {
            return "Path = " + mPath + "\n Druation = " + mDruation + "   EndTime = " + mEndTime;
        }
        public void setMimeType(String type) {
            mType = type;
        }
        public String getMimeType() {
            return mType;
        }
    }