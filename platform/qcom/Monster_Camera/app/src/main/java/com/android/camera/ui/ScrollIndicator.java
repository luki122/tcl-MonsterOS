package com.android.camera.ui;

/**
 * Created by sichao.hu on 11/23/15.
 */
public interface ScrollIndicator {

    public void initializeWidth(int width);
    public void animateWidth(int from,int to , int duration);
    public void animateTrans(int fromWidth,int toWidth,int fromTrans , int toTrans ,int duration);
}
