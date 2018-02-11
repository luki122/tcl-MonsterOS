/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;

import java.io.Serializable;
import java.util.ArrayList;

public class FileBean implements Serializable {
    private double fileSize;
    private boolean connectState;
    private int countClass;
    private int countfiles;
    private int sendConut;
    private ArrayList<Integer> selectItem;
    private String[] SQLItem;
    private ArrayList<Integer> classNum;
    private long totalLong;
    private ArrayList<String> mergeContacts;
    private int sendFailCount;
    private long availableSpace;


    public long getAvailableSpace() {
        return availableSpace;
    }

    public void setAvailableSpace(long availableSpace) {
        this.availableSpace = availableSpace;
    }

    private ArrayList<Integer> sendFailClass;
    private boolean isAccept;
    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isAccept() {
        return isAccept;
    }

    public void setAccept(boolean isAccept) {
        this.isAccept = isAccept;
    }

    public ArrayList<Integer> getSendFailClass() {
        return sendFailClass;
    }

    public void setSendFailClass(ArrayList<Integer> sendFailClass) {
        this.sendFailClass = sendFailClass;
    }

    public ArrayList<String> getMergeContacts() {
        return mergeContacts;
    }

    public void setMergeContacts(ArrayList<String> mergeContacts) {
        this.mergeContacts = mergeContacts;
    }

    public int getSendConut() {
        return sendConut;
    }

    public void setSendConut(int sendConut) {
        this.sendConut = sendConut;
    }

    public long getTotalLong() {
        return totalLong;
    }

    public void setTotalLong(long totalLong) {
        this.totalLong = totalLong;
    }

    public ArrayList<Integer> getClassNum() {
        return classNum;
    }

    public void setClassNum(ArrayList<Integer> classNum) {
        this.classNum = classNum;
    }

    public String[] getSQLItem() {
        return SQLItem;
    }

    public void setSQLItem(String[] sQLItem) {
        SQLItem = sQLItem;
    }

    public ArrayList<Integer> getSelectItem() {
        return selectItem;
    }

    public void setSelectItem(ArrayList<Integer> selectItem) {
        this.selectItem = selectItem;
    }

    public int getCountClass() {
        return countClass;
    }

    public void setCountClass(int countClass) {
        this.countClass = countClass;
    }

    public int getCountfiles() {
        return countfiles;
    }

    public void setCountfiles(int countfiles) {
        this.countfiles = countfiles;
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isConnectState() {
        return connectState;
    }

    public void setConnectState(boolean connectState) {
        this.connectState = connectState;
    }

    public int getSendFailCount() {
        return sendFailCount;
    }

    public void setSendFailCount(int sendFailCount) {
        this.sendFailCount = sendFailCount;
    }
}
