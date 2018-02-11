package com.monster.interception.database;

import java.util.ArrayList;
import java.util.List;

public class CallLogEntity implements Comparable<CallLogEntity> {
	// 最后一次通话时间
	private long mLastCallDate;
	// 通话次数
	private int mCount;
	// 电话号码
	private String mPhoneNumber;
	// 数据库中存放的电话号码
	private String mDBPhomeNumber;
	// 每次通话记录的详细信息
	private List<CallLogItem> mCallLogItems;
	// 手机号码归属地
	private String area;
	// 拦截类型
	private int reject;
	private String name;
	private String lable;
	private int simId;
	private int type;

	public int getSimId() {
		return simId;
	}

	public void setSimId(int simId) {
		this.simId = simId;
	}

	public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLable() {
		return lable;
	}

	public void setLabel(String lable) {
		this.lable = lable;
	}

	public int getReject() {
		return reject;
	}

	public void setReject(int reject) {
		this.reject = reject;
	}

	public CallLogEntity() {
		mCallLogItems = new ArrayList<CallLogItem>();
	}

	/**
	 * 获取最后一次通话时间
	 * 
	 * @return
	 */
	public long getLastCallDate() {
		return mLastCallDate;
	}

	public void setLastCallDate(long lastCallDate) {
		this.mLastCallDate = lastCallDate;
	}

	/**
	 * 获取手机号码
	 * 
	 * @return
	 */
	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.mPhoneNumber = phoneNumber;
	}

	/**
	 * 获取通话次数
	 * 
	 * @return
	 */
	public int getCount() {
		return mCount;
	}

	/**
	 * 获取通话记录详情列表
	 * 
	 * @return
	 */
	public List<CallLogItem> getCallLogItems() {
		return mCallLogItems;
	}

	public void addCallLogItem(CallLogItem callLogItem) {
		mCount++;
		mCallLogItems.add(callLogItem);
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getDBPhomeNumber() {
		return mDBPhomeNumber;
	}

	public void setDBPhomeNumber(String dbPhomeNumber) {
		this.mDBPhomeNumber = dbPhomeNumber;
	}

	@Override
	public int compareTo(CallLogEntity arg0) {
		long date = arg0.mLastCallDate - mLastCallDate;
		if (date == 0) {
			return 0;
		} else if (date > 0) {
			return 1;
		} else {
			return -1;
		}
	}
}
