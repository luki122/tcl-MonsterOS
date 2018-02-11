package com.android.mms.ui;

//tangyisen
public class BlackEntity{

    private String number;
    private String blackName;
    private int reject;
    public BlackEntity() {
    }
    public BlackEntity(String number, String blackName, int reject) {
        this.number = number;
        this.blackName = blackName;
        this.reject = reject;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getBlackName() {
        return blackName;
    }
    public void setBlackName(String blackName) {
        this.blackName = blackName;
    }
    public int getReject() {
        return reject;
    }
    public void setReject(int reject) {
        this.reject = reject;
    }
}
