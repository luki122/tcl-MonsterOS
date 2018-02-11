package com.monster.interception.database;

import java.util.ArrayList;
import java.util.List;

public class SmsEntity implements Comparable<SmsEntity> {

	private long lastDate;
	private int mCount;
	private String mDBPhomeNumber;
	private String body;
	private long thread_id;
	private int isMms;
	private String name;
	private List<SmsItem> smsItems;
	private int read;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIsMms() {
		return isMms;
	}

	public void setIsMms(int isMms) {
		this.isMms = isMms;
	}

	public long getThread_id() {
		return thread_id;
	}

	public void setThread_id(long thread_id) {
		this.thread_id = thread_id;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public SmsEntity() {
		smsItems = new ArrayList<SmsItem>();
	}

	public long getLastDate() {
		return lastDate;
	}

	public void setLastDate(long lastDate) {
		this.lastDate = lastDate;
	}

	public int getCount() {
		return mCount;
	}

	public List<SmsItem> getSmsItems() {
		return smsItems;
	}

	public void addSmsItem(SmsItem smsItem) {
		mCount++;
		smsItems.add(smsItem);
	}

	public String getDBPhomeNumber() {
		return mDBPhomeNumber;
	}

	public void setDBPhomeNumber(String dbPhomeNumber) {
		this.mDBPhomeNumber = dbPhomeNumber;
	}

	@Override
	public int compareTo(SmsEntity arg0) {
		long date = arg0.lastDate - lastDate;
		if (date == 0) {
			return 0;
		} else if (date > 0) {
			return 1;
		} else {
			return -1;
		}
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

}
