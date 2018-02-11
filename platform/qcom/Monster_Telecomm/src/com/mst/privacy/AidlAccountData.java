package com.monster.privacymanage.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 账户信息
 * @author 
 */
public class AidlAccountData implements Parcelable{
    private long accountId;//账户id(用非负整数表示，0表示非隐私账号，其他表示隐私帐号)
    private String homePath;//当前账户的存储路径
    
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeLong(accountId);
		dest.writeString(homePath);		
	}
	
	public static final Parcelable.Creator<AidlAccountData> CREATOR = 
			new Parcelable.Creator<AidlAccountData>(){
        public AidlAccountData createFromParcel(Parcel in){
            return new AidlAccountData(in);
        }

        public AidlAccountData[] newArray(int size){
            return new AidlAccountData[size];
        }
    };
    
    public AidlAccountData(Parcel in){
    	readFromParcel(in);
    }
    
    private void readFromParcel(Parcel in){
    	accountId = in.readLong();
    	homePath = in.readString();
    }
    
    public AidlAccountData(){ }
    
    public void setAccountId(long accountId){
    	this.accountId = accountId;
    }
    
    public void setHomePath(String homePath){
    	this.homePath = homePath;
    }
    
    public long getAccountId(){
    	return this.accountId;
    }
    
    public String getHomePath(){
    	return this.homePath;
    }
}
