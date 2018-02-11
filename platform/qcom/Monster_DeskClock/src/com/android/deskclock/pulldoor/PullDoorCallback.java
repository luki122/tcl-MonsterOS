package com.android.deskclock.pulldoor;

public interface PullDoorCallback {
    public void finishIncreaseAnim();//结束变大的回调
    public void finishDecreaseAnim();//结束缩小的回调
    public void onMoveing(int height,float progress);//滑动时候的回调
    public void finishSnoozeAnim();//摇一摇睡眠10分钟的动画结束回调
}
