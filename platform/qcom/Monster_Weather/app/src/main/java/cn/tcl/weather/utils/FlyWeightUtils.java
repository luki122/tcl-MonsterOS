/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.utils;

import java.util.LinkedList;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created by Leon.Zhang on 16-8-2.
 */
public abstract class FlyWeightUtils<T> {

    private LinkedList<T> mElements = new LinkedList<>();

    private int mMaxSize;

    public FlyWeightUtils() {
        this(0);
    }

    public FlyWeightUtils(int maxSize) {
        mMaxSize = maxSize;
    }

    public void clearAll() {
        synchronized (mElements) {
            mElements.clear();
        }
    }

    protected abstract T newInstance();


    public T getElement() {
        T e;
        synchronized (mElements) {
            e = mElements.poll();
        }
        if (null == e) {
            e = newInstance();
        }
        return e;
    }


    public void recycleElement(T e) {
        if (0 == mMaxSize || mMaxSize > mElements.size()) {
            synchronized (mElements) {
                if (!mElements.contains(e)) {
                    mElements.add(e);
                }
            }
        }
    }


}
