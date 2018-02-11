package com.mst.t9search;

import android.net.Uri;

public class BaseContacts {
	private String mName;			
	private String mPhoneNumber;
	private int callType;
	private int simIcon;
	private String location;
	private long date;
	private String lookup;
	private long contactId;
	private Uri contactUri; 
	private int contactType;//0 常用联系人 1联系人 2通话记录 3黄页 4群组 5SIM卡
	
	//以下用于sim卡联系人
	private String quanpinyin;
	private String jianpinyin;
	private int simType;
	private String sortOrder;
	
	
	
	public String getQuanpinyin() {
		return quanpinyin;
	}

	public void setQuanpinyin(String quanpinyin) {
		this.quanpinyin = quanpinyin;
	}

	public String getJianpinyin() {
		return jianpinyin;
	}

	public void setJianpinyin(String jianpinyin) {
		this.jianpinyin = jianpinyin;
	}

	public int getSimType() {
		return simType;
	}

	public void setSimType(int simType) {
		this.simType = simType;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public BaseContacts() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getPhoneNumber() {
		return mPhoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public String getmPhoneNumber() {
		return mPhoneNumber;
	}

	public void setmPhoneNumber(String mPhoneNumber) {
		this.mPhoneNumber = mPhoneNumber;
	}

	public int getCallType() {
		return callType;
	}

	public void setCallType(int callType) {
		this.callType = callType;
	}

	public int getSimIcon() {
		return simIcon;
	}

	public void setSimIcon(int simIcon) {
		this.simIcon = simIcon;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getLookup() {
		return lookup;
	}

	public void setLookup(String lookup) {
		this.lookup = lookup;
	}

	public long getContactId() {
		return contactId;
	}

	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

	public Uri getContactUri() {
		return contactUri;
	}

	public void setContactUri(Uri contactUri) {
		this.contactUri = contactUri;
	}

	public int getContactType() {
		return contactType;
	}

	public void setContactType(int contactType) {
		this.contactType = contactType;
	}
}
