package com.monster.interception.database;

public class BlackItem {
    //long id;
    private int mIsBlack;
    private String mLable;
    private int mUserMark;
    private String mBlackName;
    private String mNumber;
    private int mReject;

    public BlackItem() {
    }

    public BlackItem(int mIsBlack, String mLable, int mUserMark, String mBlackName, String mNumber, int mReject) {
        this.mIsBlack = mIsBlack;
        this.mLable = mLable;
        this.mUserMark = mUserMark;
        this.mBlackName = mBlackName;
        this.mNumber = mNumber;
        this.mReject = mReject;
    }

    public int getmIsBlack() {
        return mIsBlack;
    }

    public void setmIsBlack(int mIsBlack) {
        this.mIsBlack = mIsBlack;
    }

    public String getmLable() {
        return mLable;
    }

    public void setmLable(String mLable) {
        this.mLable = mLable;
    }

    public int getmUserMark() {
        return mUserMark;
    }

    public void setmUserMark(int mUserMark) {
        this.mUserMark = mUserMark;
    }

    public String getmBlackName() {
        return mBlackName;
    }

    public void setmBlackName(String mBlackName) {
        this.mBlackName = mBlackName;
    }

    public String getmNumber() {
        return mNumber;
    }

    public void setmNumber(String mNumber) {
        this.mNumber = mNumber;
    }

    public int getmReject() {
        return mReject;
    }

    public void setmReject(int mReject) {
        this.mReject = mReject;
    }

    @Override
    public boolean equals(Object o) {
        // TODO Auto-generated method stub
        if(o == null) {
            return false;
        }
        if(o == this) {
            return true;
        }
        if(o instanceof BlackItem) {
            if(this.getmNumber().equals(((BlackItem)o).getmNumber())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return this.getmNumber().hashCode();
    }
}
