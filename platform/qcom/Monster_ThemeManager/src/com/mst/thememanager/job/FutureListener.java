package com.mst.thememanager.job;


public interface FutureListener <T> {
    public void onFutureDone(Future<T> future);
}
