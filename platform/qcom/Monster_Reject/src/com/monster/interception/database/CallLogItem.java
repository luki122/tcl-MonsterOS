package com.monster.interception.database;

public class CallLogItem {
	//每次通话记录的id
	private long mId;
	//通话记录类型-数据库获取 1呼入 ,2呼出 3未接 
	private int mType;
    //通话开始时间
	private long mCallTime;
	//通话时长
	private long mDuratation;
	
	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}
	
	public int getType() {
		return mType;
	}
	public void setmType(int type) {
		this.mType = type;
	}
	
	public void setCallTime(long callTime){
		mCallTime = callTime;
	}
	
	public long getCallTime(){
		return mCallTime;
	}

	public long getDuratation() {
		return mDuratation;
	}

	public void setDuratation(long duratation) {
		this.mDuratation = duratation;
	}
	
	
}
